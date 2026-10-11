package de.eldoria.graph.toy;

import java.io.IOException;
import java.util.List;

/**
 * Reflection target of the test consumer. Its members cover every rule: plain, boolean and generic getters, an array,
 * a deprecated and a throwing getter, a static method, and overloaded setters.
 */
public class ToyPlayer {
    private final String name;
    private int score;
    private boolean online = true;
    private String nickname = "";

    public ToyPlayer(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public static ToyPlayer getDefault() {
        return new ToyPlayer("default", 0);
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public List<String> getFriends() {
        return List.of("Lilly");
    }

    public String[] getTags() {
        return new String[0];
    }

    @Deprecated
    public int getLevel() {
        return 1;
    }

    public String getHome() throws IOException {
        throw new IOException("unreachable");
    }

    public String getURLString() {
        return "https://example.com";
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setNickname(CharSequence nickname) {
        this.nickname = "chars:" + nickname;
    }
}
