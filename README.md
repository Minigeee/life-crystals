# Life Crystals

A small mod that adds life crystals that can be used to increase max health.

Life crystals can be crafted using life crystal shards, which can be found by mining life crystal ore using an iron pickaxe or better.
This ore has the same generation behavior as diamond, but with slightly smaller veins.
Each life crystal requires 6 shards to craft.

Settings for this mod can be configured in the **life_crystals.json** config file:

```json5
{
  // The amount of health each life crystal should give (2 health is equal to 1 heart)
  "healthIncrement": 2,
  // The amount of health each player should start with
  "baseHealth": 20,
  // The maximum amount of health allowed
  "maxHealth": 60
}
```