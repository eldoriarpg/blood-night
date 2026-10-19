---
title: Particle effects
sidebar_position: 9
---

# Particle effects

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

Particle effects make mobs look special. A mob can glow in a red aura, leave green haze behind, or have wings of dark
dust. An effect can also aim at a player: a beam from a wizard to a player, or a spear that flies through them.

You make an effect once, in the **Particles** tab of the editor. Then mobs wear it, or graphs play it.

## Built-in effects

BloodNight comes with ready effects. Their ids start with `bloodnight:`, for example `bloodnight:blood_aura`. You
cannot change them, but they get better with plugin updates. To change one, open it and click **Copy to edit**. You get
your own copy with a new id.

| Id | What it looks like |
|---|---|
| `bloodnight:blood_aura` | A dark red ring at the feet, with red dust rising |
| `bloodnight:toxic_haze` | A low green cloud |
| `bloodnight:void_shimmer` | A purple helix and portal sparks |
| `bloodnight:ember_crown` | A ring of small flames above the head |
| `bloodnight:storm_charge` | Sparks flickering around the body |
| `bloodnight:soul_mist` | Souls and smoke in a spiral |
| `bloodnight:tide_drip` | Water dripping in a ring |
| `bloodnight:spore_cloud` | Crimson spores and blossom dust |
| `bloodnight:fuse_glow` | An orange ring and small flames |
| `bloodnight:teleport_puff` | A burst of portal particles |
| `bloodnight:heal_burst` | Hearts and green sparks |
| `bloodnight:shock_wave` | A white ring that grows outwards |
| `bloodnight:night_end_smoke` | A column of smoke |
| `bloodnight:spawn_rise` | Souls rising in a ring |
| `bloodnight:death_burst` | Red crumbs and dust |
| `bloodnight:speed_trail` | Small clouds and dust at the feet |
| `bloodnight:dread_shadow` | A dark pool at the feet and witch sparks |
| `bloodnight:arcane_glow` | Enchanting letters and a purple ring above the head |
| `bloodnight:shadow_wings` | Dark wings on the target's back that flap |
| `bloodnight:arcane_lines` | Two magic arcs from the source to the target |
| `bloodnight:drain_beam` | A red beam of particles from the source to the target, with a ring where it hits |
| `bloodnight:lightning_bolt` | A crackling bolt from the sky to the ground |
| `bloodnight:blood_spear` | A trident that flies through the target |
| `bloodnight:rune_circle` | A turning rune on the ground |

## Your own effects

Your effects live in `particle_effects.yml`. The editor writes this file for you.

```yaml
effects:
  blood_aura:
    name: Blood aura
    duration: 40
    emitters:
      - particle: minecraft:dust
        data: {color: "#8a0303", size: 1.2}
        anchor: {at: FEET, on: SOURCE}
        offset: {x: 0, y: 0.1, z: 0}
        shape: {type: RING, radius: 0.8, points: 14, spin: 9}
        every: 3
```

- `name`: the name the editor shows.
- `duration`: how long one play lasts, in ticks. 20 ticks are one second. 0 plays one single tick.
- `emitters`: what draws the particles. An effect has up to 8.
- `pieces`: display pieces, see below. An effect has up to 4.
- `self`: whether a player the effect plays on sees it. Turn it off for wings on a player's back, so they do not block
  the view. Everyone else still sees them.

## Emitters

An emitter draws one particle in a shape, again and again while the effect plays.

| Key | What it does |
|---|---|
| `particle` | The particle, for example `minecraft:flame` |
| `data` | What some particles need: a colour, a size, a block or an item. The editor shows only what the particle uses. |
| `anchor` | Where it sits: `on` the `SOURCE`, the `TARGET` or a `PIECE`, and `at` the `FEET`, `CENTER`, `EYES` or `ABOVE` the head; `aim` is the point it aims at on the other end |
| `offset` | A shift from the anchor, in blocks: `x` to the right, `y` up, `z` forward |
| `shape` | Where the particles appear, see below |
| `motion` | Lets it travel to the other end, see below |
| `count` | Particles at each point of the shape |
| `spread` | A random box around each point, in blocks |
| `speed` | How fast the particles fly off. What it means depends on the particle. |
| `every` | Ticks between two emissions |
| `delay` | Ticks into the play before the first emission |
| `chance` | How likely each emission is, from 0 to 1. Use it for flicker. |

## Shapes

| Type | What it draws |
|---|---|
| `POINT` | One point at the anchor |
| `RING` | Points on a circle, `radius` and `points` |
| `SPHERE` | Points on a ball, `radius` and `points` |
| `LINE` | Points from the anchor to `to` |
| `SPIRAL` | A rising coil: `radius`, `height`, `turns`, `points`. Two `strands` make a helix. |
| `BEAM` | A straight line from the source to the target. `jitter` makes it crackle. |
| `ARC` | A curved line from the source to the target. `bend` is how high it curves, `twist` turns the curve. |
| `PATTERN` | A small picture, see below |

Every shape can also use:

- `facing`: `UP` lies flat on the ground. `BODY` stands up and turns with the body. `LOOK` turns where the mob looks.
  `PATH` faces the other end, so a ring becomes a disc the beam goes through.
- `spin`: degrees the shape turns at each emission.
- `grow`: the shape grows from nothing to its size during the play. Beams and arcs grow in length.

A shape has at most 64 points. One emission draws at most 256 particles, points times `count`.

### Pictures

A `PATTERN` is a picture of up to 16 by 16 cells. In the editor you click the cells. In the file, `x` is a point and `.`
is none.

```yaml
shape:
  type: PATTERN
  facing: BODY
  scale: 0.12
  mirror: true
  flap: {degrees: 25, ticks: 16}
  rows:
    - "......xx"
    - "....xxxx"
    - "..xxxxx."
    - "xxxxxx.."
```

- `scale`: blocks between two cells.
- `mirror`: copies the picture to the other side. The left column is the middle. This is how wings are made.
- `flap`: both halves swing forward and back, like wings.

## Source and target

An effect plays at a source: a mob, a player or a place. It can also have a target, for example the player a wizard
attacks. Emitters and pieces can sit on either end. Beams and arcs run from one end to the other and follow both while
they move.

### Things that travel

With `motion`, an emitter or a piece flies from its own end to the other one:

```yaml
motion: {ticks: 8, through: 1.5, ease: IN}
```

- `ticks`: how long it takes to reach the other end.
- `through`: how far it goes on past the other end, in blocks. This makes a spear go through a player.
- `ease`: `LINEAR` keeps the same speed, `IN` speeds up, `OUT` slows down.

On the other end, an emitter or piece aims at the same point of the body as its anchor, unless the anchor sets
`aim`: `anchor: {at: EYES, aim: CENTER}` starts at the mob's eyes and hits the player's chest. Aim at `CENTER` for
anything aimed at a player: at their `EYES`, it ends right in front of their face and looks cut off and wide. An Ender
Dragon's `EYES` are its head.

## Display pieces

A piece shows something solid that particles cannot draw: an item, a block, or a bar.

| Key | What it does |
|---|---|
| `kind` | `ITEM`, `BLOCK`, or `SPAN`, a block stretched from the source to the target |
| `item`, `block` | What it shows. An item from your items keeps its resource pack model. |
| `scale`, `rotation` | Its size, and a fixed turn in degrees (`pitch`, `yaw`, `roll`) |
| `anchor`, `offset`, `facing` | Where it sits and how it is turned, like emitters |
| `motion` | Lets it fly to the other end |
| `stick` | Ticks it stays where it landed |
| `glow`, `color` | A glowing outline and its colour. The outline shows through walls. |
| `width`, `grow` | For a span: how thick it is, and the ticks it takes to reach the target |

An emitter can sit on a piece with `anchor: {on: PIECE, piece: 0}`. Then the particles follow it, like a trail behind
a spear.

## Effects a mob wears

Every mob has a **Particles** part. It needs no graph:

```yaml
particles:
  ambient: bloodnight:blood_aura
  spawn: bloodnight:spawn_rise
  death: bloodnight:death_burst
```

- `ambient` plays again and again while the mob lives. It pauses while no player is near.
- `spawn` plays once when the mob appears.
- `death` plays once where the mob dies.

## Effects from a graph

The **Play Effect** node plays an effect once. Give it a source, and a target for effects that aim. Its **Arrived**
output runs when the things that fly in the effect reach the target. Use it to deal the damage of a spear exactly when
it hits. See the [node reference](../reference/nodes/world.md#play-effect).

## Try it in game

The preview in the editor shows the shapes exactly. How particles move and fade is only close to the game, because each
player's game draws them. To see the real thing, stand in game and run:

```
/bloodnight editor preview <id>
```

It plays the effect at you as it is in the open editor session, without applying anything.

## Limits

Effects can send many particles. These settings in `config.yml` keep the server fast:

```yaml
particles:
  enabled: true
  ambient: true
  viewDistance: 32
  budgetPerTick: 2000
  perMob: 4
```

- `enabled`: turns all particle effects off. Spawn Particle nodes still work.
- `ambient`: turns only the effects mobs wear off.
- `viewDistance`: players further away than this see nothing.
- `budgetPerTick`: how many particles all effects together send per tick. Each player who sees a particle counts. When
  it is used up, the effects mobs wear wait first. Effects from graphs go on up to twice as much.
- `perMob`: how many effects one mob plays at once. The effect it wears counts too.

The editor shows about how many particles an effect sends per second, and warns when it is a lot.

## Players can switch effects off

Every player can turn mob particle effects off for themselves:

```
/bloodnight particles off
/bloodnight particles on
```

The choice is kept, also after they log out.

## Mobs from the store

The [store](store.md) gives its mobs effects. When the store updates a mob, the effects you picked for it stay. To take
the store's effects as well, use `/bloodnight store update --particles`, or turn on **Also take the store's particle
effects** in the editor's store. The editor marks mobs whose effects differ from the store's.
