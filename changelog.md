# 0.8.0

* Added a upload screen mostly as a fallback for when drag and drop doesn't work
* Free-playing now supports the mouse

# 0.7.1

* Maybe fixed conflicts with GUI mods

# 0.7.0

* Fixed non-ascii file name conflicts
* Added `autoSynchronize` config to disable auto synchronization of nearby melodies
* Fixed crashes on Forge
* Channels are now treated as different tracks
* Added better free playing UI
* Offhand now supports selecting melodies
* You can now play with two instruments at once (why tho?)
* Instruments now trigger Skulk sensors
* MIDI input now supports sustain pedal
* Fixed syncing issues

# 0.6.4

* Fixed entities replacing NBT configured instruments
* Added `rightClickToDropEntityInstrumentPermissionLevel` config

# 0.6.3

* Fixed first-person sound and bound sound instances to entities
* Fix keyboard mapping in free playing (Thanks PhyX-Meow!)
* Synced translations
* Added note humanization

# 0.6.2

* Fixed recipes, advancements, and tags not loading correctly
* Fixed a crash

# 0.6.1

* Fixed a crash

# 0.6.0

* Fixed direct playback not working correctly in Multiplayer
* Breaking API changes to prepare for 1.21
    * Switched to Mojmap (Might break addons!)
    * Retired cobalt networking and registration

# 0.5.1

* Melodies are now grouped by namespaces as well
* Melody names can now be translated via key `immersive_melodies.melodies.<file_name>`

# 0.5.0

* Added Keyboard and MIDI device support
* Added perceived loudness adjustment for pitch
    * This makes lower frequencies louder, and higher frequencies quieter, making instruments sound more balanced
    * Configurable via `perceivedLoudnessAdjustmentFactor`, 0.5 by default
* Added Bad Apple and a bit of Sus
* Added Handpan
* Added the Ender Bass

# 0.4.0

* Added a vielle
* Fixed instrument sustain math, instruments sound a bit smoother now
* Added a few more songs
* Fixed playing melodies while riding
* Added config flags
    * `uploadPermissionLevel` (2 for example, only allows OPs to upload)
    * `loadInbuiltMidis` To allow disabling the inbuilt midis
    * `mobInstrumentDropFactor` To adjust the chance of mobs dropping their instrument
    * `stopGameMusicForPlayers` To pause the game music when a player is playing a melody
    * `stopGameMusicForMobs` To also pause when a mob is playing (disabled by default)
    * `instrumentVolumeFactor` To adjust the volume of the instruments
* Silence is now trimmed from the beginning of the midi
* Removed automatic track selection to avoid confusion, tracks are now all enabled by default
* Finally fixed de-syncs between tracks

# 0.3.0

* Fixed the recipe of tiny drum
* Left-handed players now hold the instruments correctly
* Technical improvements related to animations
    * E.g., Pillagers can now hold instruments

# 0.2.0

* Added midi track selection
* Added a tiny drum
* Sounds are now played at twice the distance (32 blocks)
* Fixed issues with updating/reuploading tracks

# 0.1.2

* Fixed dedicated server crash

# 0.1.1

* Zombiefied Piglins now also move their arms
* Added compatibility with Entity Culling
* Fixed wrong animations when in side hand
* Allay now also like music
* Husks can now also play (on fresh configs)
* Fixed incompatibility with Sound Physics Remastered

# 0.1.0

* Added plugin API (thanks NerjalNosk!)
* Fixed midis upload not working on Windows
* Added error message when the file cannot be parsed

# 0.0.5

* Fixed some crashes
* Fixed null textures

# 0.0.4

* Fixed broken backport
* Fixed fabric incompatibility with model loading

# 0.0.3

* Only relevant entities pick up instruments
* Right-click on an entity will cause them to drop their instrument
* Fixed some midis
* Added support for tempo changes mid-play
* Streamed melody transfer for extra large midis
* Changed data format
    * Midi now stores tracks (no GUI to actually select specific tracks, but the technical capacities are here now)
    * One .dat per melody to scale better
* Lazy loading of datapacks to speed up starting time

# 0.0.2

* Slightly enhanced, which track is used until a proper selection is added
* MIDIs without note-offs are now supported
* Hat layer is now animated as well
* No more crashes with Immersive Paintings
* Added example datapack

# 0.0.1

* Released
