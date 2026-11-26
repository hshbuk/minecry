# Towers & Puzzles / מגדלים ופאזלים

## 1. Biome Themes (6 ערכות נושא)
Pillager Signal Towers generate naturally across the Overworld (`farsight:pillager_tower`) and adapt their blocks to the local biome:
- **Plains / Forest**: Cobblestone, Stone Bricks, Dark Oak, Oak Planks
- **Desert**: Sandstone, Chiseled Sandstone, Terracotta
- **Snowy / Taiga**: Deepslate Bricks, Spruce, Soul Lanterns
- **Jungle**: Mossy Cobblestone, Jungle Planks & Logs
- **Badlands / Savanna**: Red Sandstone, Blackstone, Orange Terracotta
- **Swamp / Mangrove**: Mud Bricks, Cut Copper, Mangrove

## 2. Parkour & Tethered Guards
- **Floor 2**: Stepping-stone jumps up to **Signal Relay #1**.
- **Floor 3**: **Exterior Ledge Parkour** — exit through the side doorway, jump across the outer wooden platforms, and re-enter on the opposite side to reach **Signal Relay #2**.
- **Floor 4**: Elevated rafter beams leading to the tiered Loot Chest and rooftop ladder.
- **Anti-Flee Guards**: All tower Pillagers, Vindicators, and Evokers are tethered to their posts (`farsight_tower_guard`) with full rooftop guardrails so they never flee or fall off.

## 3. Dual Puzzle Mechanics
1. **Signal Relays (`farsight:signal_relay`)**: Right-click each relay to cycle its frequency (`0..3`) until it chimes and locks (`TUNED = true`). Tuning both relays removes the **Tower Security Barrier** (`farsight:tower_barrier`) blocking the rooftop hatch.
2. **Tower Signal Terminal (`farsight:tower_terminal`)**: Once all tower guards are defeated and both relays are tuned, right-click the rooftop terminal to open the **3×3 Circuit Alignment Puzzle**. Click conduits to rotate them and connect `IN` (left) to `OUT` (right).
