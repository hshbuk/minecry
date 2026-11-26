# FarSight: World Discovery (Fabric 1.21.1)

[English](#english) | [עברית](#עברית)

---

<a id="english"></a>
## English

A **Minecraft 1.21.1 (Fabric)** mod that brings **Far Cry-style world discovery** into Minecraft: climb biome-themed Pillager towers, defeat guards, solve dual puzzles, unlock regions, and reveal every special point of interest on a Fog-of-War World Map, HUD Compass, and 3D light pillars.

### Features
1. **Biome-Themed Pillager Towers (6 Themes)**:
   - Naturally generated in the Overworld ([`TowerStructure`](src/main/java/com/farsight/world/TowerStructure.java), [`TowerTheme`](src/main/java/com/farsight/world/TowerTheme.java)):
     - **Plains / Forest** (Cobblestone, Stone Bricks, Dark Oak)
     - **Desert** (Sandstone, Terracotta)
     - **Snowy / Taiga** (Deepslate Bricks, Spruce, Soul Lanterns)
     - **Jungle** (Mossy Cobblestone, Jungle Planks)
     - **Badlands / Savanna** (Red Sandstone, Blackstone, Orange Terracotta)
     - **Swamp / Mangrove** (Mud Bricks, Cut Copper, Mangrove)
   - Each tower has 4 interior floors + a rooftop radio spire, alternating ladders, **Pillager** & **Vindicator** guards, and an outpost loot chest.

2. **Dual Puzzle Mechanics**:
   - **Physical Tower Puzzle**: Floors 2 and 3 hold **Signal Relay Nodes** (`farsight:signal_relay`). Right-click each relay to tune its frequency until locked; tuning all relays opens the **Tower Security Barrier** (`farsight:tower_barrier`) blocking the rooftop hatch.
   - **Terminal Circuit Puzzle**: After clearing the guards and tuning the relays, right-click the rooftop **Tower Signal Terminal** (`farsight:tower_terminal`) to open a 3×3 circuit-rotation puzzle connecting `IN` to `OUT`.

3. **Region Unlocking & Automatic POI Scanning**:
   - Activating the terminal unlocks a **512-block radius region** (`1024×1024`), persisted per world in `DiscoveryState`.
   - Automatically scans and marks special locations inside the region:
     - **Structures**: Villages, Pillager Outposts, Desert/Jungle Temples, Witch Huts, Igloos, Trial Chambers, Ancient Cities, Ruined Portals, Ocean Monuments, Woodland Mansions, Strongholds, Mineshafts.
     - **Special Biomes & Caves**: Lush Caves, Deep Dark, Dripstone Caves, Cherry Grove, Mushroom Island.
     - **Next Frontier Tower**: Marks the nearest unclaimed Pillager Tower on the horizon.

4. **Map, Compass, 3D Markers & Scout Binoculars**:
   - **Interactive World Map (`M` key)**: Fog of War outside unlocked regions, live terrain colors inside unlocked regions, zoom & pan, POI tooltips, and click-to-track **Waypoints**.
   - **Top HUD Compass**: Displays cardinal directions, nearby POI icons, active Waypoint distance, and a `"REGION UNLOCKED"` banner.
   - **3D World Markers**: Translucent glowing light pillars over discovered POIs and your active Waypoint.
   - **Scout Binoculars (`farsight:scout_binoculars`)**: Craftable item to spot and mark distant Pillager Towers on your map.

### Commands & Build
- **In-game commands** (OP level 2+):
  - `/farsight spawn_tower` — Spawns a biome-themed Pillager Tower nearby.
  - `/farsight info` — Shows unlocked regions and discovered POIs count.
  - `/farsight sync` — Re-syncs discovery data to all players.
- **Build from source** (requires **JDK 21**):
  ```bash
  ./gradlew build
  ```
  The compiled mod JAR is generated in `build/libs/farsight-1.0.0.jar`.

---

<a id="עברית"></a>
## עברית

מוד עבור **Minecraft 1.21.1 (Fabric)** המכניס למשחק את מכניקת גילוי העולם של **Far Cry**: טיפוס על מגדלי פילג'רים מותאמי ביומים, חיסול שומרים, פתרון פאזלים, פתיחת אזורים וגילוי כל האתרים המיוחדים על גבי מפת עולם עם ערפל מלחמה, מצפן עליון ועמודי אור בתלת-ממד.

### פיצ'רים עיקריים
1. **מגדלי פילג'רים מותאמי ביומים (6 ערכות נושא)**:
   - נוצרים באופן טבעי בעולם ומתאימים את חומרי הבנייה לביום:
     - **Plains / Forest** (Cobblestone, Stone Bricks, Dark Oak)
     - **Desert** (Sandstone, Terracotta)
     - **Snowy / Taiga** (Deepslate Bricks, Spruce, Soul Lanterns)
     - **Jungle** (Mossy Cobblestone, Jungle Planks)
     - **Badlands / Savanna** (Red Sandstone, Blackstone, Orange Terracotta)
     - **Swamp / Mangrove** (Mud Bricks, Cut Copper, Mangrove)
   - כל מגדל כולל 4 קומות + גג עם אנטנת רדיו, סולמות מתחלפים, שומרי **Pillager** ו-**Vindicator** ותיבת שלל.

2. **מערכת פאזלים כפולה בכל מגדל**:
   - **פאזל פיזי בתוך המגדל**: בקומות 2 ו-3 מוצבים **ממסרי תדר (`Signal Relay`)**. לחיצה ימנית מסנכרנת את התדר עד לנעילה; סנכרון כל הממסרים פותח את **מחסום האבטחה (`Tower Barrier`)** שחוסם את העלייה לגג.
   - **פאזל המסוף בגג**: לאחר חיסול השומרים וכוונון הממסרים, לחיצה על **מסוף השידור (`Tower Terminal`)** פותחת פאזל סיבוב מעגלים 3×3 שבו מחברים את הזרם מ-`IN` ל-`OUT`.

3. **פתיחת אזור וגילוי אוטומטי של אתרים מיוחדים**:
   - הפעלת המגדל חושפת אזור ברדיוס **512 בלוקים** (`1024×1024`) שנשמר בעולם.
   - סריקה אוטומטית של כל הדברים המיוחדים באזור:
     - **מבנים**: כפרים (Villages), Pillager Outposts, מקדשי מדבר/ג'ונגל, Witch Huts, Igloos, Trial Chambers, Ancient Cities, Ruined Portals, Ocean Monuments, Woodland Mansions, Strongholds ו-Mineshafts.
     - **ביומים ומערות מיוחדים**: Lush Caves, Deep Dark, Dripstone Caves, Cherry Grove ו-Mushroom Island.
     - **המגדל הבא באופק**: מסמן את מגדל הפילג'רים הלא-כבוש הקרוב ביותר.

4. **מפה, מצפן, עמודי אור ומשקפת סיור**:
   - **מפת עולם אינטראקטיבית (מקש `M`)**: ערפל מלחמה (Fog of War) מחוץ לאזורים פתוחים, צבעי שטח באזורים פתוחים, זום וגרירה, פירוט ומרחק על כל אייקון, ולחיצה לסימון **Waypoint**.
   - **מצפן HUD עליון**: מציג כיוונים (`N/E/S/W`), אייקונים קרובים, מרחק ל-Waypoint הפעיל ובאנר `"REGION UNLOCKED"` עם פתיחת אזור.
   - **עמודי אור בתלת-ממד**: אלומות אור זוהרות בעולם מעל אתרים שהתגלו ומעל ה-Waypoint.
   - **משקפת סיור (`Scout Binoculars`)**: פריט ניתן ליצירה לסריקה וסימון של מגדלי פילג'רים מרוחקים על המפה.

### פקודות ובנייה
- **פקודות במשחק** (הרשאות אופרטור):
  - `/farsight spawn_tower` — מזמן מגדל פילג'רים מותאם לביום הנוכחי לידך.
  - `/farsight info` — מציג כמה אזורים ואתרים נחשפו.
  - `/farsight sync` — מסנכרן מחדש את נתוני הגילוי לכל השחקנים.
- **בנייה מקוד המקור** (נדרש **JDK 21**):
  ```bash
  ./gradlew build
  ```
  קובץ ה-`.jar` ייווצר בתיקייה `build/libs/farsight-1.0.0.jar`.
