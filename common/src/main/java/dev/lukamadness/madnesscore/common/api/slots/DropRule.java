package dev.lukamadness.madnesscore.common.api.slots;

public enum DropRule {
    KEEP, DROP, DESTROY, DEFAULT;

    public static boolean has(String name) {
        for (DropRule rule : values()) {
            if (rule.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
