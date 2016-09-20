# Correlated Potentialistics 2.x
This is it. CoPo2. The future of <s>IndustrialCraft</s> Correlated Potentialistics.

*cough*

Welcome to the unfinished 2.x branch of CoPo! Targets for the release of 2.0.0:

* [ ] New Cool Dungeon™
  * [ ] Automatons
    * [x] Taming
    * [x] Wandering
    * [x] Storage
    * [ ] Exec mode
  * [x] Floor plan generator
  * [ ] Keycards
  * [ ] The awesome room at the end with the processor
  * [x] Unstable Pearl
* [ ] Rework storage math
  * [x] Memory
    * [x] Allocate memory based on complexity of types (example: a backpack allocates as much memory as all the items within it would, as does a Data Core or Drive)
    * [x] Remove the concept of types from drives
    * [x] Add memory bay
      * [x] Soft reboot the network when memory is changed
    * [x] Increase drive size to MiBs so memory can be KiBs
    * [x] Memory crafting recipe
    * [x] Memory bay crafting recipe
  * [x] Migration system
    * [x] Data Cores
    * [x] Detect old storage systems
    * [x] Demolish old storage systems
      * [x] Put chest in place of Controller (made a special scrollable chest instead, for overly large networks to import properly)
      * [x] Convert old drives to Data Cores
      * [x] Refund resources
      * [ ] Write apology message for inclusion in chest
  * [x] `free` command
  * [ ] `part` command
  * [ ] Fix VT log entries to reflect type memory usage and partitioning
* [ ] Ingame documentation
* [ ] Cables
* [ ] Fluid storage