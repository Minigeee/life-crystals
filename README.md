# Life Crystals

A small mod that adds life crystals that can be used to increase max health.

Life crystals can be crafted using life crystal shards, which can be found by mining life crystal ore using an iron pickaxe or better.
This ore has a similar generation behavior and rarity as diamond, and can be found at diamond levels.
Each life crystal requires 6 shards to craft.

Life crystal shards, and occasionally entire life crystals, can be found in dungeon type chests, including
ancient cities, buried treasure, desert pyramids, abandoned mineshafts, and more. This feature can be turned off using the config file.

Settings for this mod can be configured in the **life_crystals.json** config file:

```json5
{
  // The amount of health each life crystal should give (2 health is equal to 1 heart)
  "healthIncrement": 2,
  // The amount of health each player should start with
  "baseHealth": 20,
  // The maximum amount of health allowed
  "maxHealth": 60,
  // The chance of finding life crystals in each dungeon chest
  "lootChance": 0.5,
  // Determines if life crystals and shards should be found in dungeon chests
  "addChestLoot": true
}
```