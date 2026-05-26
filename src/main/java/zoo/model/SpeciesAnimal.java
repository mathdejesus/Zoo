package zoo.model;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generic zoo animal bound to a user-defined species and diet.
 * <p>
 * Threading concept — {@code non-blocking resource access}:
 * {@link #eat} uses {@link FoodSupply#tryConsume} which returns a
 * boolean immediately instead of blocking with {@code wait()}.
 * This is the key design decision that prevents the feeding cycle
 * deadlock: if food is insufficient, the animal skips the meal
 * and the cycle can complete. Compare this with {@link Caterpillar}
 * and {@link Butterfly}, which use the blocking {@code consumeFood()}
 * method as part of their demo-oriented design.
 */
public class SpeciesAnimal extends Animal {

    private final String speciesName;
    private final Diet diet;

    public SpeciesAnimal(String individualName, String speciesName, Diet diet) {
        super(individualName);
        this.speciesName = speciesName;
        this.diet = diet;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public Diet getDiet() {
        return diet;
    }

    @Override
    public void eat(FoodSupply food) {
        FoodType foodType = diet.getFoodType();
        int needed = diet.getFoodPerMeal();
        // tryConsume returns immediately — never blocks on wait()
        boolean ate = food.tryConsume(foodType, needed);
        if (ate) {
            energy += 5;
            System.out.printf(
                    "[%s] %s ate (%s, %d units of %s). Energy: %d%n",
                    Thread.currentThread().getName(),
                    name,
                    diet.getLabel(),
                    needed,
                    foodType.getLabel(),
                    energy
            );
        } else {
            System.out.printf(
                    "[%s] %s found no %s and went hungry!%n",
                    Thread.currentThread().getName(),
                    name,
                    foodType.getLabel()
            );
        }
    }

    @Override
    public void rest() {
        try {
            int sleepMs = ThreadLocalRandom.current().nextInt(300, 801);
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            alive = false;
        }
    }

    @Override
    public Animal transform() {
        return this;
    }

    @Override
    public String toString() {
        return name + " [" + speciesName + ", " + diet.getLabel() + ", energy=" + energy + "]";
    }
}
