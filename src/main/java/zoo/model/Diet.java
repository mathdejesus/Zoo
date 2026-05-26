package zoo.model;

/**
 * Diet type for a species.
 * Each diet binds to a specific {@link FoodType}, so different diets
 * draw from separate shared resources. This avoids unnecessary lock
 * contention between species that do not compete for the same food.
 * <p>
 * {@code foodPerMeal} drives how many units an animal consumes each cycle.
 * Carnivores eat the most (3/meal), herbivores the least (1/meal).
 */
public enum Diet {
    CARNIVORE(3, "Carnivore", FoodType.MEAT),
    OMNIVORE(2, "Omnivore", FoodType.MIX),
    HERBIVORE(1, "Herbivore", FoodType.LEAVES_VEGETABLES);

    private final int foodPerMeal;
    private final String label;
    private final FoodType foodType;

    Diet(int foodPerMeal, String label, FoodType foodType) {
        this.foodPerMeal = foodPerMeal;
        this.label = label;
        this.foodType = foodType;
    }

    public int getFoodPerMeal() {
        return foodPerMeal;
    }

    public String getLabel() {
        return label;
    }

    public FoodType getFoodType() {
        return foodType;
    }

    public static Diet fromChoice(int choice) {
        return switch (choice) {
            case 1 -> CARNIVORE;
            case 2 -> OMNIVORE;
            case 3 -> HERBIVORE;
            default -> null;
        };
    }
}
