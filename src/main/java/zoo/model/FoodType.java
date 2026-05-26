package zoo.model;

/**
 * Types of food tracked in the shared {@link FoodSupply}.
 * Each {@link Diet} maps to exactly one FoodType, creating logical
 * resource isolation: carnivores compete only for MEAT, herbivores
 * only for LEAVES_VEGETABLES, etc.
 * <p>
 * The numeric {@code code} is used for console input selection.
 * {@link #ordinal()} is used as the array index inside FoodSupply
 * for O(1) per-type access.
 */
public enum FoodType {
    MEAT(1, "Meat"),
    MIX(2, "Mix"),
    LEAVES_VEGETABLES(3, "Leaves/Vegetables");

    private final int code;
    private final String label;

    FoodType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static FoodType fromCode(int code) {
        return switch (code) {
            case 1 -> MEAT;
            case 2 -> MIX;
            case 3 -> LEAVES_VEGETABLES;
            default -> throw new IllegalArgumentException("Invalid food type code: " + code);
        };
    }
}
