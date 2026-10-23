# Aegis Torch (Forge 1.7.10)

A torch that lights the area like a normal torch (light level 15) and blocks **natural**
spawning of hostile mobs around it, can push hostile mobs out of its zone and show the zone
in the world. Idea inspired by the Magnum Torch from Extra Utilities.

## Download

Grab the latest `AegisTorch-1.7.10-x.y.z.jar` from
[Releases](../../releases) and drop it into your `mods` folder (Forge 10.13.4.1614 for 1.7.10).

**Right click** the torch to open its settings:

```
            Aegis Torch
        Position: x, y, z
  [-10] [-1]  Radius: 64  [+1] [+10]
   Zone: circle 129 wide, 129 high
  [ Push hostile mobs out: ON / OFF ]   <- force field, per torch, saved
  [      Show zone: ON / OFF        ]   <- toggles the zone outline in the world
  [             Done                ]
```

- Every torch has its own radius (1..maxRadius), saved in the world and synced to all players.
- "Push hostile mobs out": every few ticks hostile mobs inside the zone get an outward push
  (away from the torch, plus a small hop to clear steps). Mobs that walk in are pushed back.
  Bosses (Wither, Ender Dragon) and passive mobs are never pushed.
- "Show zone" draws the protected cylinder (cyan lines, faintly visible through walls).
  It is client-side only and is cleared when you change dimension.
- Translations: English, Ukrainian, Russian.

## How it works

```
 placed / chunk loaded             broken / chunk unloaded
        |                                   |
  TileAegisTorch.validate()          TileAegisTorch.invalidate() / onChunkUnload()
        |                                   |
        v                                   v
  TorchRegistry.add(world, x, y, z)   TorchRegistry.remove(...)
        \                                  /
         +---- Map<World, Set<pos>> ------+
                        |
 natural spawn attempt  |
 (SpawnerAnimals) --> LivingSpawnEvent.CheckSpawn --> SpawnBlocker
                        |
          IMob? and inside cylinder (radius R, height +-R) of any torch?
                        |
                 yes -> Result.DENY (spawn skipped)
```

- Zone: cylinder, radius R horizontally and R blocks up and down (R set per torch in the GUI).
- GUI changes go to the server as a packet; the server checks the player is within 12 blocks,
  clamps the value, saves it and resyncs it to clients.
- Only hostile mobs (`IMob`) by default; passive mobs can be blocked via config.
- Monster spawners (cages) are NOT affected - in Forge 1.7.10 they do not fire
  `CheckSpawn`, so mob farms keep working.
- The tile entity does not tick, so the torch costs nothing per tick.
- A torch works only while its chunk is loaded.

## Config - `config/aegistorch.cfg`

Mod ID: `aegistorch`, block: `aegistorch:aegis_torch`.


| key | default | meaning |
|---|---|---|
| `defaultRadius` | 64 | radius of a newly placed torch |
| `maxRadius` | 128 | largest radius allowed in the GUI |
| `blockPassiveMobs` | false | also block animals, bats, squids |
| `enableRecipe` | true | register the default recipe |
| `pushing.allowPushing` | true | master switch for pushing (hides the GUI button if false) |
| `pushing.pushByDefault` | true | new torches start with pushing on |
| `pushing.pushStrength` | 0.6 | outward speed per push, blocks/tick |
| `pushing.pushInterval` | 2 | push every N ticks |

## Recipe

```
T T T      T - torch
D G D      D - diamond, G - gold block
  L        L - any log
```

## Building

Requires JDK 21 (Gradle runs on it; the mod is compiled for Java 8 automatically).

```
git clone https://github.com/zakharfsk/aegis-torch && cd aegis-torch
./gradlew build          # jar -> build/libs/ (version is taken from the latest git tag)
./gradlew runClient      # test in a dev client
```

Built on the [GTNewHorizons ExampleMod1.7.10](https://github.com/GTNewHorizons/ExampleMod1.7.10)
template (RetroFuturaGradle, MIT).

## License

MIT - see [LICENSE](LICENSE).
