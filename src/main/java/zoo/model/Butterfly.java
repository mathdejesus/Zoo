package zoo.model;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A butterfly drinks nectar (1 food unit per meal) and rests briefly.
 * It is the final metamorphosis stage — {@link #transform} returns
 * {@code this} (identity), meaning no further transformation occurs.
 * <p>
 * Casting concept — {@code polymorphism through constructors}:
 * The constructor {@link #Butterfly(String, int)} receives energy
 * from the previous {@link Caterpillar} stage, preserving state
 * across metamorphosis. This demonstrates how object state can
 * be transferred when one object replaces another at runtime.
 */
public class Butterfly extends Animal {

    private static final int FOOD_PER_MEAL = 1;

    public Butterfly(String name) {
        super(name);
    }

    /**
     * Constructor that preserves energy from the caterpillar stage.
     */
    public Butterfly(String name, int energy) {
        super(name);
        this.energy = energy;
    }

    @Override
    public void eat(FoodSupply food) {
        try {
            food.consumeFood(FOOD_PER_MEAL);
            energy += 3;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            alive = false;
        }
    }

    @Override
    public void rest() {
        try {
            int sleepMs = ThreadLocalRandom.current().nextInt(500, 1501);
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            alive = false;
        }
    }

    /**
     * Identity transform: butterflies do not metamorphose further.
     * Returning {@code this} keeps the same object reference.
     */
    @Override
    public Animal transform() {
        return this;
    }

    @Override
    public String toString() {
        return name + " (Butterfly, energy=" + energy + ")";
    }
}
