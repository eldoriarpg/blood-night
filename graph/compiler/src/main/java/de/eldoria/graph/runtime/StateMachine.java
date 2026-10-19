/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/**
 * The states of one compiled graph instance. States are numbered depth first, so a state and everything inside it form
 * one range of numbers; the machine always rests in a leaf.
 * <p>
 * Moving runs the exits from the current leaf up to the deepest state the old and the new place share, then the enters
 * from there down to the new leaf, entering initial children below the target. A move is refused when a state that
 * would be entered says no. Going to the current state or to a state containing it does nothing. A move made from
 * inside an enter or exit takes over: the moves before it stop where they are.
 */
public final class StateMachine {
    private final String[] names;
    private final int[] parents;
    private final int[] lasts;
    private final int[] initialChildren;
    private final int initial;
    private final IntConsumer enter;
    private final IntConsumer exit;
    private final IntPredicate allowed;
    private int current = -1;
    private long since = System.currentTimeMillis();
    private int moves;

    /**
     * @param names           the states' names by number
     * @param parents         each state's parent, -1 for a top-level state
     * @param lasts           the highest number inside each state, itself for a leaf
     * @param initialChildren each state's initial child, -1 for a leaf
     * @param initial         the initial top-level state
     * @param enter           runs a state's enter
     * @param exit            runs a state's exit
     * @param allowed         a state's enter condition
     */
    public StateMachine(
            String[] names,
            int[] parents,
            int[] lasts,
            int[] initialChildren,
            int initial,
            IntConsumer enter,
            IntConsumer exit,
            IntPredicate allowed) {
        this.names = names.clone();
        this.parents = parents.clone();
        this.lasts = lasts.clone();
        this.initialChildren = initialChildren.clone();
        this.initial = initial;
        this.enter = enter;
        this.exit = exit;
        this.allowed = allowed;
    }

    /**
     * Enters the initial states, once; does nothing later.
     */
    public void start() {
        if (current >= 0 || moves > 0) return;
        moves++;
        current = leaf(initial);
        since = System.currentTimeMillis();
        int move = moves;
        for (int state : path(-1, current)) {
            enter.accept(state);
            if (moves != move) return;
        }
    }

    /**
     * @param state a state's number
     * @return whether the machine is in it or in a state inside it
     */
    public boolean in(int state) {
        return current >= state && current <= lasts[state];
    }

    /**
     * Moves to a state.
     *
     * @param target the state's number
     * @return whether the machine moved
     */
    public boolean goTo(int target) {
        start();
        if (in(target)) return false;
        int shared = shared(target);
        int leaf = leaf(target);
        List<Integer> entering = path(shared, leaf);
        for (int state : entering) {
            if (!allowed.test(state)) return false;
        }
        int move = ++moves;
        for (int state = current; state != shared && state >= 0; state = parents[state]) {
            exit.accept(state);
            if (moves != move) return true;
        }
        current = leaf;
        since = System.currentTimeMillis();
        for (int state : entering) {
            enter.accept(state);
            if (moves != move) return true;
        }
        return true;
    }

    /**
     * Moves to one of several states, picked by weight among those it can enter.
     *
     * @param targets the states' numbers
     * @param weights their weights; 0 or less leaves a state out
     * @return the number of the state moved to, or -1 when none was taken
     */
    public int goToOneOf(int[] targets, double[] weights) {
        start();
        List<Integer> open = new ArrayList<>();
        for (int index = 0; index < targets.length; index++) {
            if (weights[index] > 0 && !in(targets[index])) open.add(index);
        }
        while (!open.isEmpty()) {
            double total = open.stream().mapToDouble(index -> weights[index]).sum();
            double roll = ThreadLocalRandom.current().nextDouble(total);
            int picked = open.getLast();
            for (int index : open) {
                roll -= weights[index];
                if (roll < 0) {
                    picked = index;
                    break;
                }
            }
            if (goTo(targets[picked])) return targets[picked];
            open.remove(Integer.valueOf(picked));
        }
        return -1;
    }

    /**
     * @return the name of the innermost current state, empty before the first state is entered
     */
    public String current() {
        return current < 0 ? "" : names[current];
    }

    /**
     * @param state a state's number, or -1
     * @return its name, empty for -1
     */
    public String name(int state) {
        return state < 0 ? "" : names[state];
    }

    /**
     * @return seconds since the machine last moved
     */
    public double seconds() {
        return (System.currentTimeMillis() - since) / 1000.0;
    }

    private int leaf(int state) {
        int leaf = state;
        while (initialChildren[leaf] >= 0) leaf = initialChildren[leaf];
        return leaf;
    }

    /**
     * @return the deepest state containing both the current leaf and the target, or -1
     */
    private int shared(int target) {
        for (int state = parents[target]; state >= 0; state = parents[state]) {
            if (current >= 0 && in(state)) return state;
        }
        return -1;
    }

    /**
     * @return the states below {@code top} down to {@code leaf}, outermost first
     */
    private List<Integer> path(int top, int leaf) {
        List<Integer> path = new ArrayList<>();
        for (int state = leaf; state != top && state >= 0; state = parents[state]) path.addFirst(state);
        return path;
    }
}
