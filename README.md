# FarSight: World Discovery (Fabric 1.21.1)

[English](#english) | [עברית](#עברית)

---

<a id="english"></a>
## English

A **Minecraft 1.21.1 (Fabric)** mod that brings **Far Cry-style world discovery** into Minecraft: climb biome-themed Pillager towers, defeat guards, solve dual puzzles, trigger a cinematic region flyover, liberate road checkpoints, fast travel, and explore a Fog-of-War World Map, HUD Compass, and 3D light pillars.

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
   - **Physical Tower Puzzle**: Floors 2 and 3 hold **Signal Relay Nodes** (`farsight:signal_relay`). Right-click each relay to tune its frequency until locked; tuning all relays removes the **Tower Security Barrier** (`farsight:tower_barrier`) blocking the rooftop hatch.
   - **Terminal Circuit Puzzle**: After clearing the guards and tuning the relays, right-click the rooftop **Tower Signal Terminal** (`farsight:tower_terminal`) to open a 3×3 circuit-rotation puzzle connecting `IN` to `OUT`.

3. **Cinematic Region Flyover & Automatic POI Scanning**:
   - Activating the terminal unlocks a **512-block radius region** (`1024×1024`), persisted per world in `DiscoveryState`.
   - **Far Cry Region Flyover**: Immediately flies the camera around up to 4 real-coordinate highlights inside the newly unlocked region (Villages, Road Checkpoints, Temples/Trial Chambers, and Special Caves), then returns you safely to the tower roof (press `SPACE` anytime to skip).
   - Automatically scans and marks special locations inside the region:
     - **Structures**: Villages, Pillager Outposts, Desert/Jungle Temples, Witch Huts, Igloos, Trial Chambers, Ancient Cities, Ruined Portals, Ocean Monuments, Woodland Mansions, Strongholds, Mineshafts.
     - **Special Biomes & Caves**: Lush Caves, Deep Dark, Dripstone Caves, Cherry Grove, Mushroom Island.
     - **Next Frontier Tower**: Marks nearby unclaimed Pillager Towers on the horizon.

4. **Roads, Road Checkpoints & Fast Travel**:
   - **Terrain-Following Roads**: Unlocking a region generates paved roads connecting the Tower to **Road Checkpoints** and nearby **Villages**, rendered as road lines on the World Map.
   - **Road Checkpoints**: Fortified Pillager checkpoints with loot chests and a **Checkpoint Flag** (`farsight:checkpoint_flag`). Defeat the guards and interact with the flag to liberate the checkpoint.
   - **Fast Travel**: Select any claimed **Tower** or liberated **Checkpoint** on the World Map and click **Fast Travel** to teleport directly there.

5. **Map, Compass, 3D Markers & Scout Binoculars**:
   - **Interactive World Map (`M` key)**: Crisp 2×2 topographic terrain with elevation shading, Fog of War outside unlocked regions, a local 80-block radius around the player, zoom & pan, a **Towers & Checkpoints** sidebar, POI tooltips, and click-to-track **Waypoints**.
   - **Top HUD Compass**: Displays cardinal directions, nearby POI icons, active Waypoint distance, and a `"REGION UNLOCKED"` banner.
   - **3D World Markers**: Translucent glowing light pillars over discovered POIs and your active Waypoint.
   - **Scout Binoculars (`farsight:scout_binoculars`)**: Craftable item to spot and mark distant Pillager Towers on your map.

### Commands & Build
- **In-game commands** (OP level 2+):
  - `/farsight spawn_tower` — Spawns a biome-themed Pillager Tower nearby.
  - `/farsight spawn_checkpoint` — Spawns a fortified Road Checkpoint nearby.
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

מוד עבור **Minecraft 1.21.1 (Fabric)** המכניס למשחק את מכניקת גילוי העולם של **Far Cry**: טיפוס על מגדלי פילג'רים מותאמי ביומים, חיסול שומרים, פתרון פאזלים, חזיון מעוף קולנועי מעל האזור, שחרור צ'קפוינטים וכבישים, טלפורט מהיר, מפת עולם עם ערפל מלחמה, מצפן עליון ועמודי אור בתלת-ממד.

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
   - **פאזל פיזי בתוך המגדל**: בקומות 2 ו-3 מוצבים **ממסרי תדר (`Signal Relay`)**. לחיצה ימנית מסנכרנת את התדר עד לנעילה; סנכרון כל הממסרים פותח ומסיר את **מחסום האבטחה (`Tower Barrier`)** שחוסם את העלייה לגג.
   - **פאזל המסוף בגג**: לאחר חיסול השומרים וכוונון הממסרים, לחיצה על **מסוף השידור (`Tower Terminal`)** פותחת פאזל סיבוב מעגלים 3×3 שבו מחברים את הזרם מ-`IN` ל-`OUT`.

3. **חזיון מעוף קולנועי וגילוי אוטומטי של אתרים מיוחדים**:
   - הפעלת המגדל חושפת אזור ברדיוס **512 בלוקים** (`1024×1024`) שנשמר בעולם.
   - **חזיון מעוף (Far Cry Flyover)**: מטיס את המצלמה במעוף מהיר מעל עד 4 מאפיינים אמיתיים באזור (כפרים, צ'קפוינטים, מקדשים/Trial Chambers ומבט למערות מיוחדות) ובסיום מחזיר אותך למקומך על המגדל (ניתן לדלג בלחיצה על **`SPACE`**).
   - סריקה אוטומטית של כל הדברים המיוחדים באזור:
     - **מבנים**: כפרים (Villages), Pillager Outposts, מקדשי מדבר/ג'ונגל, Witch Huts, Igloos, Trial Chambers, Ancient Cities, Ruined Portals, Ocean Monuments, Woodland Mansions, Strongholds ו-Mineshafts.
     - **ביומים ומערות מיוחדים**: Lush Caves, Deep Dark, Dripstone Caves, Cherry Grove ו-Mushroom Island.
     - **המגדל הבא באופק**: מסמן את מגדלי הפילג'רים הלא-כבושים הקרובים.

4. **כבישים, צ'קפוינטים וטלפורט מהיר (Fast Travel)**:
   - **כבישים ודרכים**: פתיחת אזור יוצרת דרכים סלולות בשטח המחברות את המגדל לצ'קפוינטים ולכפרים באזור (ומופיעות על המפה).
   - **צ'קפוינטים (Road Checkpoints)**: מחסומים מבוצרים של פילג'רים עם תיבת שלל ו**דגל צ'קפוינט (`Checkpoint Flag`)**. חסל את השומרים ולחץ על הדגל לשחרור הצ'קפוינט.
   - **טלפורט (Fast Travel)**: בחר מגדל כבוש או צ'קפוינט משוחרר במפה ולחץ על **`Fast Travel`** כדי להשתגר לשם מיד.

5. **מפה, מצפן, עמודי אור ומשקפת סיור**:
   - **מפת עולם אינטראקטיבית (מקש `M`)**: תצוגת שטח חדה (2×2) עם הצללת גבהים, ערפל מלחמה באזורים נעולים, רדיוס מקומי סביב השחקן, זום וגרירה, פירוט ומרחק על כל אייקון, רשימת מגדלים וצ'קפוינטים בצד, וסימון **Waypoint**.
   - **מצפן HUD עליון**: מציג כיוונים (`N/E/S/W`), אייקונים קרובים, מרחק ל-Waypoint הפעיל ובאנר `"REGION UNLOCKED"` עם פתיחת אזור.
   - **עמודי אור בתלת-ממד**: אלומות אור זוהרות בעולם מעל אתרים שהתגלו ומעל ה-Waypoint.
   - **משקפת סיור (`Scout Binoculars`)**: פריט ניתן ליצירה לסריקה וסימון של מגדלי פילג'רים מרוחקים על המפה.

### פקודות ובנייה
- **פקודות במשחק** (הרשאות אופרטור):
  - `/farsight spawn_tower` — מזמן מגדל פילג'רים מותאם לביום הנוכחי לידך.
  - `/farsight spawn_checkpoint` — מזמן צ'קפוינט מבוצר לידך.
  - `/farsight info` — מציג כמה אזורים ואתרים נחשפו.
  - `/farsight sync` — מסנכרן מחדש את נתוני הגילוי לכל השחקנים.
- **בנייה מקוד המקור** (נדרש **JDK 21**):
  ```bash
  ./gradlew build
  ```
  קובץ ה-`.jar` ייווצר בתיקייה `build/libs/farsight-1.0.0.jar`.
