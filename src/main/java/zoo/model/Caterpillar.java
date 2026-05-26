package zoo.model;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A caterpillar eats 2 food units per meal and increments a growth
 * counter. When growth reaches the threshold (10), it metamorphoses
 * into a {@link Butterfly}.
 * <p>
 * Threading concept — {@code Thread.sleep()}:
 * The {@link #rest} method simulates the passage of time. In a real
 * system this might represent digestion or activity time. The method
 * catches {@link InterruptedException} and gracefully terminates
 * the animal, demonstrating the standard interrupt handling pattern:
 * restore the interrupt flag and mark the animal as not alive.
 * <p>
 * Casting concept — {@code upcast}:
 * {@link #transform} returns a {@code Butterfly} assigned to the
 * {@code Animal} return type. This is a widening conversion (subtype
 * → supertype) and happens implicitly — no explicit cast is needed.
 * The old caterpillar sets {@code alive = false} so its run loop ends.
 */
public class Caterpillar extends Animal {

    private static final int FOOD_PER_MEAL = 2;

    private int growthLevel;

    public Caterpillar(String name) {
        super(name);
        this.growthLevel = 0;
    }

    @Override
    public void eat(FoodSupply food) {
        try {
            food.consumeFood(FOOD_PER_MEAL);
            energy += 5;
            growthLevel++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            alive = false;
        }
    }

    @Override
    public void rest() {
        try {
            int sleepMs = ThreadLocalRandom.current().nextInt(1000, 3001);
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            alive = false;
        }
    }

    @Override
    public Animal transform() {
        if (growthLevel >= 10) {
            // Upcast: Butterfly is a subtype of Animal, returned as Animal.
            // The caller (AnimalThread) detects the reference change via !=.
            Butterfly butterfly = new Butterfly(name, energy);
            alive = false;
            return butterfly;
        }
        return this;
    }

    public int getGrowthLevel() {
        return growthLevel;
    }

    @Override
    public String toString() {
        return name + " (Caterpillar, growth=" + growthLevel + ")";
    }
}
