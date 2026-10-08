# Immersive Melodies

[![Crowdin](https://badges.crowdin.net/immersive-collection/localized.svg)](https://crowdin.com/project/immersive-melodies)

Hosted on CurseForge: https://www.curseforge.com/minecraft/mc-mods/immersive-melodies

Config docu here: https://github.com/Luke100000/ImmersiveMelodies/wiki/Config

Modpack/Datapack Creator help: https://github.com/Luke100000/ImmersiveMelodies/wiki/Custom-Melodies

Maven: https://maven.conczin.net/#/Artifacts/net/conczin/immersive_melodies

## API and Custom Instruments

1) Add sounds for every octave (1-8) into `resources/assets/{namespace}/sounds/instruments/{instrument_name}`, or use
   `scripts/convert.sh` to convert a C4 base audio automatically.
2) Register the sounds into the `sounds.json`
3) Register the item via
   `immersive_melodies.Items.register(java.lang.String, java.lang.String, long, org.joml.Vector3f)`
4) Register the animation handler via `immersive_melodies.client.animation.ItemAnimators.register`
5) Add `instrument.json` and `instrument_hand.json` item models and textures
6) And of course recipes, lang files, tags, and whatever else might be needed

## Playback API

Add Immersive Melodies as a compile-time dependency and load your compatibility code only when the mod is installed.
Run playback changes and server melody selection on the server thread after initialization.

- `Items.getRandomInstrument(random)` returns a new instrument stack in an `Optional<ItemStack>`, including addon
  instruments.
  Call it after item registration. Use `Items.getSortedItems()` to list all stacks.
- `ServerMelodyManager.getRandomMelody(random, filter)` picks a datapack or uploaded melody, counting each ID once.
  Use `id -> id.getNamespace().equals(namespace)` to filter by namespace, or `id -> true` to allow all melodies.
  It returns `Optional.empty()` if nothing matches.
- `InstrumentItem.getPlayback(stack)` returns an `Optional<InstrumentItem.Playback>` containing the
  playing instrument's melody ID and start tick. It returns empty for non-instruments, paused stacks, or incomplete
  playback data.
- `instrument.play(stack, melodyId, level, performer)` starts a melody now.
  To join another performer in sync, call `instrument.play(stack, playback.melody(), playback.startTime(), performer)`.
  Start times use world game time in ticks.
- `instrument.pause(stack, level)` pauses playback. Mobs holding an instrument start playing again on their next
  instrument tick, so restore their previous held item when a temporary performance ends.