package zoo.thread;

import zoo.model.Animal;
import zoo.model.Butterfly;
import zoo.model.FoodSupply;

/**
 * Wraps an {@link Animal} in a dedicated thread. After each life-cycle
 * iteration, checks metamorphosis and swaps the local animal reference
 * when a caterpillar transforms into a butterfly.
 * <p>
 * Threading concept — {@code extending Thread}:
 * This is the classic (pre-Java-5) way to create a thread. Each animal
 * gets its own OS thread. Compare with {@link ZooManager#runFeedingCycle}
 * which uses {@link java.util.concurrent.CompletableFuture} with a
 * shared thread pool — a more scalable approach for many animals.
 * <p>
 * Casting concept — {@code downcast with instanceof pattern matching}:
 * When {@link zoo.model.Caterpillar#transform} returns a new
 * {@link Butterfly} (upcast to {@code Animal}), we narrow it back
 * via {@code instanceof Butterfly butterfly}. This is the safe
 * downcast pattern in Java 17+: the {@code instanceof} check and
 * variable declaration happen in one expression, and the variable
 * is in scope only inside the {@code if} block. No explicit
 * {@code (Butterfly)} cast is needed.
 * <p>
 * The mutable {@code animal} field lets the thread continue operating
 * on the transformed instance without restarting.
 */
public class AnimalThread extends Thread {

    private Animal animal;
    private final FoodSupply foodSupply;
    private final ZooManager zooManager;

    public AnimalThread(Animal animal, FoodSupply foodSupply, ZooManager zooManager) {
        super(animal.getName());
        this.animal = animal;
        this.foodSupply = foodSupply;
        this.zooManager = zooManager;
    }

    public Animal getAnimal() {
        return animal;
    }

    @Override
    public void run() {
        while (animal.isAlive()) {
            animal.eat(foodSupply);
            if (!animal.isAlive()) {
                break;
            }
            animal.rest();
            if (!animal.isAlive()) {
                break;
            }

            Animal transformed = animal.transform();
            if (transformed != animal) {
                System.out.println(animal.getName() + " has transformed into a Butterfly!");

                // Downcast: narrow Animal to Butterfly after instanceof check.
                // Pattern matching in Java 17: no explicit cast needed.
                if (transformed instanceof Butterfly butterfly) {
                    animal = butterfly;
                    zooManager.recordTransformation();
                } else {
                    animal = transformed;
                }
            }
        }
    }
}
