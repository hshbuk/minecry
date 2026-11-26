package com.farsight.data;

public enum PoiType {
    TOWER_CLAIMED("Tower (Claimed)", 0xFF00E5FF, "T"),
    TOWER_UNCLAIMED("Pillager Tower", 0xFFFF3B30, "!"),
    CHECKPOINT_LOCKED("Road Checkpoint", 0xFFFF6B00, "K"),
    CHECKPOINT_CLAIMED("Liberated Checkpoint", 0xFF00FFAA, "K"),
    VILLAGE("Village", 0xFF34C759, "V"),
    PILLAGER_OUTPOST("Pillager Outpost", 0xFFFF9500, "X"),
    TEMPLE("Ancient Temple", 0xFFFFCC00, "A"),
    ANCIENT_CITY("Ancient City", 0xFF0A84FF, "C"),
    TRIAL_CHAMBERS("Trial Chambers", 0xFFE07A2F, "R"),
    RUINED_PORTAL("Ruined Portal", 0xFFAF52DE, "P"),
    OCEAN_MONUMENT("Ocean Monument", 0xFF5AC8FA, "M"),
    WOODLAND_MANSION("Woodland Mansion", 0xFF8E5A2B, "W"),
    STRONGHOLD("Stronghold", 0xFF64D2FF, "S"),
    MINESHAFT("Mineshaft", 0xFFA2845E, "H"),
    SPECIAL_CAVE("Special Cave / Geode", 0xFFBF5AF2, "*"),
    WAYPOINT("Waypoint", 0xFFFFFFFF, "+");

    private final String displayName;
    private final int color;
    private final String symbol;

    PoiType(String displayName, int color, String symbol) {
        this.displayName = displayName;
        this.color = color;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public String getSymbol() {
        return symbol;
    }
}
