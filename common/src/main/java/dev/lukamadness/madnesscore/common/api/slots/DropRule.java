package dev.lukamadness.madnesscore.common.api.slots;

/**
 * Regla de que pasa con el item de un slot cuando la entidad muere / dropea su inventario.
 * Portado de dev.emi.trinkets.api.TrinketEnums.DropRule
 */
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