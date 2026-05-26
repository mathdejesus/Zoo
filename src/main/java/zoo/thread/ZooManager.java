package zoo.thread;

import zoo.model.Animal;
import zoo.model.Diet;
import zoo.model.FoodSupply;
import zoo.model.FoodType;
import zoo.model.SpeciesAnimal;
import zoo.model.SpeciesSetup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coordinates species registration, feeding cycles with concurrent animal threads,
 * and population adjustments from the console.
 * <p>
 * Threading concept — {@code CompletableFuture + ForkJoinPool}:
 * Each feeding cycle launches a {@link CompletableFuture} task per alive
 * animal via {@link #runFeedingCycle}. These tasks run on the common
 * ForkJoinPool, so we get parallel execution without managing threads
 * manually. {@link CompletableFuture#allOf} combines all tasks into one,
 * and {@code join()} blocks the main thread until every animal has
 * finished eating AND resting — a barrier synchronization point.
 * <p>
 * This approach contrasts with {@link AnimalThread}, which assigns each
 * animal its own dedicated OS thread (one-thread-per-animal). The
 * CompletableFuture approach is more resource-efficient for many animals.
 * <p>
 * Deadlock prevention:
 * {@link SpeciesAnimal#eat} uses the non-blocking {@link FoodSupply#tryConsume}.
 * If any animal blocked on {@code wait()} inside the feeding cycle,
 * {@code allOf().join()} would hang forever because the main thread
 * cannot reach the console prompt to add food. The non-blocking design
 * is essential to avoid this deadlock.
 */
public class ZooManager {

    private final FoodSupply foodSupply;
    private final Map<String, SpeciesSetup> speciesRegistry = new LinkedHashMap<>();
    private final List<ManagedAnimal> animals = new ArrayList<>();
    private final AtomicInteger idSequence = new AtomicInteger(1);
    private int cycleCount;
    private int transformationCount;

    public ZooManager(Map<FoodType, Integer> initialStock) {
        this.foodSupply = new FoodSupply(initialStock);
    }

    public void initialize(List<SpeciesSetup> setups) {
        speciesRegistry.clear();
        animals.clear();
        idSequence.set(1);
        cycleCount = 0;

        for (SpeciesSetup setup : setups) {
            speciesRegistry.put(setup.getSpeciesName().toLowerCase(), setup);
            addAnimals(setup.getSpeciesName(), setup.getDiet(), setup.getQuantity());
        }
    }

    public FoodSupply getFoodSupply() {
        return foodSupply;
    }

    public List<String> listSpeciesNames() {
        return speciesRegistry.values().stream()
                .map(SpeciesSetup::getSpeciesName)
                .toList();
    }

    /**
     * Returns species entries with stable numeric IDs (insertion order).
     */
    public List<SpeciesEntry> listSpeciesWithIds() {
        List<SpeciesEntry> entries = new ArrayList<>();
        int index = 1;
        for (SpeciesSetup setup : speciesRegistry.values()) {
            entries.add(new SpeciesEntry(index++, setup.getSpeciesName()));
        }
        return entries;
    }

    /**
     * Returns the canonical species name for a given numeric ID.
     * Returns {@code null} if the ID is out of range.
     */
    public String getSpeciesNameById(int id) {
        if (id <= 0 || id > speciesRegistry.size()) {
            return null;
        }
        int index = 1;
        for (SpeciesSetup setup : speciesRegistry.values()) {
            if (index == id) {
                return setup.getSpeciesName();
            }
            index++;
        }
        return null;
    }

    public boolean hasSpecies(String speciesName) {
        return speciesRegistry.containsKey(speciesName.toLowerCase());
    }

    public String resolveSpeciesName(String input) {
        SpeciesSetup setup = speciesRegistry.get(input.toLowerCase());
        return setup != null ? setup.getSpeciesName() : null;
    }

    public int getRegisteredQuantity(String speciesName) {
        SpeciesSetup setup = speciesRegistry.get(speciesName.toLowerCase());
        return setup != null ? setup.getQuantity() : 0;
    }

    public int countAliveBySpecies(String speciesName) {
        String key = speciesName.toLowerCase();
        int count = 0;
        for (ManagedAnimal managed : animals) {
            if (managed.speciesKey().equals(key) && managed.animal().isAlive()) {
                count++;
            }
        }
        return count;
    }

    public int countAliveAnimals() {
        int alive = 0;
        for (ManagedAnimal managed : animals) {
            if (managed.animal().isAlive()) {
                alive++;
            }
        }
        return alive;
    }

    /**
     * Runs one feeding cycle: each alive animal eats and rests
     * concurrently via CompletableFuture on the common ForkJoinPool.
     * <p>
     * Threading concept — {@code barrier synchronization}:
     * {@code CompletableFuture.allOf().join()} blocks the main thread
     * until ALL animal tasks finish. This ensures no animal starts
     * the next cycle before every animal has finished the current one.
     * Without this barrier, fast animals would get more feeding
     * opportunities than slow ones.
     * <p>
     * Because {@link SpeciesAnimal#eat} uses {@link FoodSupply#tryConsume}
     * (non-blocking), this method never hangs even when food is scarce.
     */
    public void runFeedingCycle() throws InterruptedException {
        cycleCount++;
        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        for (ManagedAnimal managed : animals) {
            Animal animal = managed.animal();
            if (!animal.isAlive()) {
                continue;
            }
            // Each animal runs in its own ForkJoinPool task
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                animal.eat(foodSupply);
                if (animal.isAlive()) {
                    animal.rest();
                }
            });
            tasks.add(task);
        }

        if (!tasks.isEmpty()) {
            // Barrier: wait for ALL animals to finish eating and resting
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        }
    }

    public int getCycleCount() {
        return cycleCount;
    }

    /** Used by {@link AnimalThread} when caterpillars metamorphose (legacy demo flow). */
    public synchronized void recordTransformation() {
        transformationCount++;
    }

    public synchronized int getTransformationCount() {
        return transformationCount;
    }

    /**
     * Adjusts the food stock for a specific type (increase or decrease)
     * and prints the new total. Called from the main thread only,
     * between feeding cycles.
     */
    public void adjustFoodStock(boolean increase, FoodType foodType, int amount) {
        if (amount <= 0) {
            System.out.println("No adjustment applied to food stock (amount must be > 0).");
            return;
        }
        if (increase) {
            foodSupply.addFood(foodType, amount);
            System.out.printf(
                    "Stock of %s increased by %d unit(s). New total: %d%n",
                    foodType.getLabel(),
                    amount,
                    foodSupply.getAmount(foodType)
            );
        } else {
            foodSupply.removeFood(foodType, amount);
            System.out.printf(
                    "Stock of %s decreased by %d unit(s). New total: %d%n",
                    foodType.getLabel(),
                    amount,
                    foodSupply.getAmount(foodType)
            );
        }
    }

    public void adjustSpecies(String speciesName, boolean increase, int amount) {
        String key = speciesName.toLowerCase();
        SpeciesSetup setup = speciesRegistry.get(key);
        if (setup == null) {
            System.out.println("Species not found: " + speciesName);
            return;
        }

        if (increase) {
            addAnimals(setup.getSpeciesName(), setup.getDiet(), amount);
            updateRegisteredQuantity(key, setup.getQuantity() + amount);
            System.out.printf("Added %d animal(s) of species %s.%n", amount, setup.getSpeciesName());
        } else {
            int removed = removeAnimals(key, amount);
            updateRegisteredQuantity(key, Math.max(0, setup.getQuantity() - removed));
            System.out.printf("Removed %d animal(s) of species %s.%n", removed, setup.getSpeciesName());
        }
    }

    /**
     * Shuts down the zoo. Sets all animals as not alive and adds a
     * large amount of food ("poison pill") to each type to unblock
     * any threads that may be waiting in legacy {@code consumeFood()}
     * calls (used by the demo classes).
     */
    public void stopZoo() {
        for (ManagedAnimal managed : animals) {
            managed.animal().setAlive(false);
        }
        // Poison pill: unblocks any legacy waiting threads
        for (FoodType type : FoodType.values()) {
            foodSupply.addFood(type, 1000);
        }
    }

    private void addAnimals(String speciesName, Diet diet, int count) {
        String key = speciesName.toLowerCase();
        for (int i = 0; i < count; i++) {
            String individualName = speciesName + "-" + idSequence.getAndIncrement();
            animals.add(new ManagedAnimal(key, new SpeciesAnimal(individualName, speciesName, diet)));
        }
    }

    private int removeAnimals(String speciesKey, int requested) {
        int removed = 0;
        for (int i = animals.size() - 1; i >= 0 && removed < requested; i--) {
            ManagedAnimal managed = animals.get(i);
            if (managed.speciesKey().equals(speciesKey) && managed.animal().isAlive()) {
                managed.animal().setAlive(false);
                removed++;
            }
        }
        return removed;
    }

    private void updateRegisteredQuantity(String key, int newQuantity) {
        SpeciesSetup old = speciesRegistry.get(key);
        speciesRegistry.put(key, new SpeciesSetup(old.getSpeciesName(), newQuantity, old.getDiet()));
    }

    public record SpeciesEntry(int id, String name) {
    }

    private record ManagedAnimal(String speciesKey, Animal animal) {
    }
}
