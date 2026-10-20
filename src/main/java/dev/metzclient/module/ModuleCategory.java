package dev.metzclient.module;

public enum ModuleCategory {
    COMBAT("COMBAT"),
    MOVEMENT("MOVE"),
    PLAYER("PLAYER"),
    WORLD("WORLD"),
    RENDER("RENDER"),
    UTILITY("UTILITY"),
    CLIENT("CLIENT");

    private final String label;
    ModuleCategory(String label) { this.label = label; }
    public String label() { return label; }
}
