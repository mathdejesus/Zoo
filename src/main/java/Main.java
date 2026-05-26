import zoo.console.ZooConsole;
import zoo.model.FoodType;
import zoo.model.SpeciesSetup;
import zoo.thread.ZooManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interactive zoo simulation: species setup, feeding cycles, and population changes.
 * <p>
 * Threading concept — {@code main thread coordination}:
 * The main thread handles console I/O and orchestrates the simulation loop.
 * Animal concurrency is managed inside {@link ZooManager#runFeedingCycle}
 * via {@link java.util.concurrent.CompletableFuture}. The main thread
 * never blocks on {@code wait()} — it only blocks on {@code join()} which
 * waits for all animal tasks to finish the current cycle.
 * <p>
 * The loop structure:
 * <ol>
 *   <li>Run feeding cycle (concurrent, via CompletableFuture)</li>
 *   <li>Print zoo status</li>
 *   <li>Ask for changes (food stock or animal population)</li>
 *   <li>Check exit condition</li>
 * </ol>
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        ZooConsole console = new ZooConsole(scanner);

        System.out.println("=== Zoo Simulator ===");
        System.out.println();

        List<SpeciesSetup> setups = console.readSpeciesSetup();
        Map<FoodType, Integer> initialStock = console.readInitialFoodStock();

        ZooManager zoo = new ZooManager(initialStock);
        zoo.initialize(setups);

        console.printZooStatus(zoo);

        int cycle = 0;
        while (true) {
            cycle++;
            console.printCycleHeader(cycle);
            zoo.runFeedingCycle();
            console.printZooStatus(zoo);

            if (console.askAnimalChanges()) {
                ZooConsole.ChangeTarget target = console.askChangeTarget();
                switch (target) {
                    case FOOD -> {
                        FoodType foodType = console.readFoodTypeForChange();
                        boolean increase = console.askFoodIncrease();
                        int amount = console.readFoodQuantityAdjustment();
                        zoo.adjustFoodStock(increase, foodType, amount);
                        console.printZooStatus(zoo);
                    }
                    case ANIMALS -> {
                        String species = console.readSpeciesNameForChange(zoo);
                        boolean increase = console.askIncrease();
                        int amount = console.readQuantityAdjustment();
                        zoo.adjustSpecies(species, increase, amount);
                        console.printZooStatus(zoo);
                    }
                }
            }

            if (console.shouldExitProgram()) {
                break;
            }
        }

        zoo.stopZoo();
        System.out.println("Zoo simulation ended.");
        scanner.close();
    }
}
