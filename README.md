# FarSight: World Discovery (Fabric 1.21.1)

[English](#english) | [עברית](#עברית) | [Wiki (Home)](wiki/Home.md)

---

<a id="english"></a>
## English

A **Minecraft 1.21.1 (Fabric)** mod that brings **Far Cry-style world discovery, progression, vehicles, and exploration** into Minecraft.

### Key Features
1. **Biome-Themed Pillager Towers (6 Themes + Parkour + Tiers `★1–★5`)**:
   - Naturally generated in the Overworld ([`TowerStructure`](src/main/java/com/farsight/world/TowerStructure.java), [`TowerTheme`](src/main/java/com/farsight/world/TowerTheme.java)): **Plains/Forest**, **Desert**, **Snowy/Taiga**, **Jungle**, **Badlands/Savanna**, and **Swamp/Mangrove**.
   - **Parkour & Tethered Guards**: Interior stepping stones, exterior scaffold ledge jumps, rafter beams, full rooftop guardrails, and tethered Pillager/Vindicator/Evoker guards that never flee or jump off.
   - **Dual Puzzles**: Tune both **Signal Relay Nodes** (`farsight:signal_relay`) to remove the **Tower Security Barrier** (`farsight:tower_barrier`), clear the guards, and solve the 3×3 **Tower Signal Terminal** circuit puzzle.

2. **Region Tiers (`★1–★5`) & Difficulty Scaling**:
   - Towers, Checkpoints, and Regions scale from **Tier I (`★1`)** near spawn up to **Tier V (`★5`)** at `3200m+` ([`TierCalculator`](src/main/java/com/farsight/world/TierCalculator.java)).
   - Higher-tier regions increase hostile mob Health & Attack Damage, spawn extra guards (including Evokers at Tier 4+), and award higher Score & better Chest Loot.

3. **Cinematic Region Flyover & Automatic POI Scanning**:
   - Activating a tower unlocks a **512-block radius region** (`1024×1024`) and triggers a **Far Cry-style camera flyover** around up to 4 real-coordinate highlights (Villages, Checkpoints, Temples, Caves) before returning you to the tower (`SPACE` to skip).
   - Automatically reveals Villages, Pillager Outposts, Temples, Trial Chambers, Ancient Cities, Ruined Portals, Ocean Monuments, Woodland Mansions, Strongholds, Mineshafts, Special Caves/Biomes, and nearby unclaimed Towers.

4. **Roads, Checkpoints & Fast Travel**:
   - **Smoothed Roads**: Terrain-following 3-wide roads with slope clamping, stone-brick bridges over water, embankments, and roadside lanterns connecting Towers, Checkpoints, and Villages in a loop.
   - **Road Checkpoints**: Fortified enemy checkpoints with a **Checkpoint Flag** (`farsight:checkpoint_flag`). Clear the guards and interact with the flag to liberate it and spawn a **FarSight Caravan Merchant**.
   - **Fast Travel**: Select any claimed Tower or liberated Checkpoint on the World Map (`M`) and click **Fast Travel** to teleport safely.

5. **Interactive World Map (`M`), HUD Compass & 3D Pillars**:
   - Crisp 2×2 topographic terrain with elevation shading, Fog of War outside unlocked regions, clickable **Legend Filters** (toggle any POI type on/off across Map, Compass, and 3D Pillars), **Towers & Checkpoints** sidebar, and Waypoint tracking.

6. **Score, Levels, Skills (`U`), Weapons, Mounts, Aircraft & Merchants**:
   - **Skills (`U` key)**: Earn Score & Skill Points from kills, checkpoints, and towers. Upgrade **Vitality** (`+2 HP`), **Swiftness** (`+8% Speed`), **Lethality** (`+1.5 Damage`), and **Scavenger** (`+25% Score`).
   - **Armory Weapons**: **Tactical Machete** (`200 PTS`), **Hunter Bow** (`300 PTS`), **Flare Launcher** (`450 PTS`).
   - **Mounts & Aircraft**: **Scout Horse Whistle** (`150 PTS` — summons/recalls an armored Scout Horse), **Scout Plane Kit** (`600 PTS`), and **Recon Helicopter Kit** (`900 PTS` — VTOL hover + `W/A/S/D` + `Space`/`Ctrl`).
   - **Caravan Merchants**: Spawn at liberated Checkpoints (or via `/farsight spawn_merchant`) selling tactical gear, weapons, horse whistles, and aircraft kits for Emeralds.

### Wiki & Commands
- Full guides in the **[Wiki](wiki/Home.md)**:
  - [Towers & Puzzles](wiki/Towers-and-Puzzles.md)
  - [Regions, Roads & Map](wiki/Regions-Roads-and-Map.md)
  - [Progression, Weapons & Vehicles](wiki/Progression-Weapons-and-Vehicles.md)
- **Commands (OP level 2+)**: `/farsight spawn_tower`, `/farsight spawn_checkpoint`, `/farsight spawn_merchant`, `/farsight info`, `/farsight sync`.
- **Build (JDK 21)**: `./gradlew build` -> `build/libs/farsight-1.0.0.jar`.

---

<a id="עברית"></a>
## עברית

מוד עבור **Minecraft 1.21.1 (Fabric)** המכניס למשחק את חוויית גילוי העולם, ההתקדמות, כלי הרכב והחקירה של **Far Cry**.

### פיצ'רים עיקריים
1. **מגדלי פילג'רים מותאמי ביומים (6 ערכות נושא + פארקור + דירוג `★1–★5`)**:
   - נוצרים באופן טבעי בעולם ב-6 סגנונות (Plains, Desert, Snowy, Jungle, Badlands, Swamp).
   - **פארקור ושומרים נעולים לעמדה**: קפיצות פנימיות, פיגומים חיצוניים על דופן המגדל, קורות תקרה, מעקה מלא בגג ושומרי Pillager/Vindicator/Evoker שנשארים בעמדתם ולא בורחים או נופלים.
   - **פאזלים כפולים**: כוונון 2 **ממסרי תדר (`Signal Relay`)** לפתיחת **מחסום האבטחה**, חיסול השומרים, ופתרון פאזל המעגלים 3×3 ב**מסוף השידור**.

2. **דירוגי קושי לאזורים ולמגדלים (`★1–★5`)**:
   - ככל שמתרחקים ממרכז העולם הדירוג עולה מ-`★1` עד `★5`: מובים עוינים חזקים יותר, יותר שומרים במגדלים ובצ'קפוינטים (כולל Evokers), שלל עשיר יותר וניקוד גבוה יותר.

3. **חזיון מעוף קולנועי וגילוי אוטומטי של אתרים**:
   - הפעלת המגדל חושפת אזור ברדיוס **512 בלוקים** ומפעילה **חזיון מעוף (Flyover)** מעל עד 4 אתרים אמיתיים באזור (כפרים, צ'קפוינטים, מקדשים ומערות) לפני חזרה אוטומטית לגג המגדל (דילוג עם **`SPACE`**).
   - סורק ומסמן כפרים, Outposts, מקדשים, Trial Chambers, Ancient Cities, פורטלים, Monuments, Mansions, Strongholds, Mineshafts, מערות מיוחדות ומגדלים נוספים באופק.

4. **כבישים, צ'קפוינטים וטלפורט (Fast Travel)**:
   - **כבישים סלולים**: דרכים ברוחב 3 בלוקים עם החלקת שיפועים, גשרים עם מעקות מעל מים, סוללות תמיכה ופנסי רחוב המחברים את המגדל לצ'קפוינטים ולכפרים ברשת ללא דרכים ללא מוצא.
   - **צ'קפוינטים**: מחסומים מבוצרים עם **דגל צ'קפוינט (`Checkpoint Flag`)**. חיסול השומרים ולחיצה על הדגל משחררים את הצ'קפוינט ומזמנים **סוחר נווד**.
   - **Fast Travel**: בחירת מגדל כבוש או צ'קפוינט משוחרר במפה (`M`) ולחיצה על **`Fast Travel`** משגרת לשם מיד ובבטחה.

5. **מפת עולם (`M`), מצפן עליון ועמודי אור בתלת-ממד**:
   - מפה טופוגרפית חדה (2×2) עם ערפל מלחמה, **פילטרים לחיצים במקרא** להסתרה/הצגה של סוגי סימונים במפה, במצפן ובעמודי האור, וסימון **Waypoints**.

6. **ניקוד, רמות, שדרוגים (`U`), נשקים, סוסים, מטוסים, מסוקים וסוחרים**:
   - **שדרוגי דמות (`U`)**: **Vitality** (`+2 HP`), **Swiftness** (`+8%` מהירות), **Lethality** (`+1.5` נזק), **Scavenger** (`+25%` ניקוד).
   - **נשקי המוד**: **מצ'טה טקטית** (`200 PTS`), **קשת ציידים** (`300 PTS`), **משגר נורים** (`450 PTS`).
   - **סוסים וכלי טיס**: **משרוקית סוס סיור** (`150 PTS`), **מטוס סיור** (`600 PTS`), ו**מסוק סיור** (`900 PTS` — ריחוף מלא עם `W/A/S/D` + `Space`/`Ctrl`).
   - **סוחרים נוודים**: מופיעים בצ'קפוינטים משוחררים (או עם `/farsight spawn_merchant`) ומוכרים ציוד, נשקים, משרוקית סוס וכלי טיס תמורת אמרלדים.

### ויקי ופקודות
- מדריכים מפורטים ב-**[Wiki](wiki/Home.md)**:
  - [מגדלים ופאזלים](wiki/Towers-and-Puzzles.md)
  - [אזורים, דרכים ומפה](wiki/Regions-Roads-and-Map.md)
  - [התקדמות, נשקים וכלי טיס](wiki/Progression-Weapons-and-Vehicles.md)
- **פקודות**: `/farsight spawn_tower`, `/farsight spawn_checkpoint`, `/farsight spawn_merchant`, `/farsight info`, `/farsight sync`.
- **בנייה (JDK 21)**: `./gradlew build` -> `build/libs/farsight-1.0.0.jar`.
