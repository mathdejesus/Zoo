package zoo.console;

import zoo.model.Diet;
import zoo.model.FoodType;
import zoo.model.SpeciesSetup;
import zoo.thread.ZooManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Interactive console prompts for zoo setup and cycle management.
 * <p>
 * Input validation concept:
 * All read methods use while-loops that retry on invalid input, ensuring
 * the program never proceeds with malformed data. This defensive pattern
 * is essential for console applications where users may type anything.
 * <p>
 * The system accepts input in English (primary) and Portuguese (fallback)
 * for yes/no and selection prompts, making it accessible to a wider audience.
 */
public class ZooConsole {

    private final Scanner scanner;

    public ZooConsole(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Target of a change after a cycle: food stock or animals. */
    public enum ChangeTarget {
        FOOD,
        ANIMALS
    }

    public List<SpeciesSetup> readSpeciesSetup() {
        List<SpeciesSetup> setups = new ArrayList<>();
        int speciesCount = readPositiveInt("How many species do you want to register? ");

        for (int i = 1; i <= speciesCount; i++) {
            System.out.println();
            System.out.println("--- Species " + i + " of " + speciesCount + " ---");
            String name = readNonBlank("Name: ");
            int quantity = readPositiveInt("Quantity: ");
            Diet diet = readDiet();
            setups.add(new SpeciesSetup(name, quantity, diet));
            System.out.printf(
                    "Registered: %s x%d (%s)%n",
                    name,
                    quantity,
                    diet.getLabel()
            );
        }
        return setups;
    }

    /**
     * Asks whether there is initial food and, if so, the quantity for each type.
     */
    public Map<FoodType, Integer> readInitialFoodStock() {
        Map<FoodType, Integer> stock = new EnumMap<>(FoodType.class);
        for (FoodType type : FoodType.values()) {
            stock.put(type, 0);
        }

        while (true) {
            String hasFood = readNonBlank("Is there initial food available? (y/n): ")
                    .toLowerCase(Locale.ROOT);
            if (isNo(hasFood)) {
                System.out.println("Initial stock set to 0 for all food types.");
                return stock;
            }
            if (isYes(hasFood)) {
                System.out.println("Enter the initial quantity for each food type:");
                for (FoodType type : FoodType.values()) {
                    int amount = readNonNegativeInt("  " + type.getLabel() + ": ");
                    stock.put(type, amount);
                }
                System.out.println("Initial stock configured:");
                for (FoodType type : FoodType.values()) {
                    System.out.println("  " + type.getLabel() + ": " + stock.get(type));
                }
                return stock;
            }
            System.out.println("Please answer 'y' for yes or 'n' for no.");
        }
    }

    /**
     * Asks whether any changes occurred after the cycle (food stock or animals).
     */
    public boolean askAnimalChanges() {
        while (true) {
            String answer = readNonBlank(
                    "After this feeding cycle, did any change occur in the food stock or animal population? (y/n): "
            ).toLowerCase(Locale.ROOT);
            if (isYes(answer)) {
                return true;
            }
            if (isNo(answer)) {
                return false;
            }
            System.out.println("Please answer 'y' for yes or 'n' for no.");
        }
    }

    /**
     * Asks whether the change was in food stock or in animals.
     */
    public ChangeTarget askChangeTarget() {
        while (true) {
            String answer = readNonBlank(
                    "Did the change affect the food stock or the animals? (f/a): "
            ).toLowerCase(Locale.ROOT);
            if (isFood(answer)) {
                return ChangeTarget.FOOD;
            }
            if (isAnimals(answer)) {
                return ChangeTarget.ANIMALS;
            }
            System.out.println("Enter 'f' for food or 'a' for animals.");
        }
    }

    public String readSpeciesNameForChange(ZooManager zoo) {
        while (true) {
            System.out.println("Select the species to change using its numeric code:");
            int index = 1;
            for (String name : zoo.listSpeciesNames()) {
                System.out.printf("  %d - %s%n", index++, name);
            }
            int choice = readIntInRange("Enter the species code: ", 1, index - 1);
            String canonical = zoo.getSpeciesNameById(choice);
            if (canonical != null) {
                return canonical;
            }
            System.out.println("Invalid species code. Try again.");
        }
    }

    public int readQuantityAdjustment() {
        return readPositiveInt("Enter the number of animals to add or remove (positive integer): ");
    }

    public boolean askIncrease() {
        while (true) {
            String answer = readNonBlank("Should the number of animals increase or decrease? (i/d): ")
                    .toLowerCase(Locale.ROOT);
            if (isIncrease(answer)) {
                return true;
            }
            if (isDecrease(answer)) {
                return false;
            }
            System.out.println("Enter 'i' for increase or 'd' for decrease.");
        }
    }

    public FoodType readFoodTypeForChange() {
        System.out.println("Select the food type whose stock was changed, using its numeric code:");
        int index = 1;
        for (FoodType type : FoodType.values()) {
            System.out.printf("  %d - %s%n", index++, type.getLabel());
        }
        int code = readIntInRange("Enter the food type code (1-3): ", 1, FoodType.values().length);
        return FoodType.fromCode(code);
    }

    public boolean askFoodIncrease() {
        while (true) {
            String answer = readNonBlank("Should the food stock increase or decrease? (i/d): ")
                    .toLowerCase(Locale.ROOT);
            if (isIncrease(answer)) {
                return true;
            }
            if (isDecrease(answer)) {
                return false;
            }
            System.out.println("Enter 'i' for increase or 'd' for decrease.");
        }
    }

    public int readFoodQuantityAdjustment() {
        return readPositiveInt("Enter the number of food units to add or remove (positive integer): ");
    }

    /**
     * Asks whether to end the program. Accepts integer 0 or exit words in multiple languages.
     * Unrecognized text triggers a language hint and a second attempt.
     */
    public boolean shouldExitProgram() {
        System.out.println();
        System.out.println("Press Enter to continue.");
        System.out.println("To exit, type 0 or an exit word (e.g., exit, quit, sair, bye).");
        System.out.print("> ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return false;
        }
        if (ExitCommand.isZero(input) || ExitCommand.isExit(input)) {
            return true;
        }

        return confirmExitLanguageAndRetry(input);
    }

    private boolean confirmExitLanguageAndRetry(String firstInput) {
        System.out.println();
        System.out.println("You typed: \"" + firstInput + "\".");
        System.out.println("To exit, type 0 or an exit word (e.g., exit, quit, sair, bye, stop).");
        System.out.println("Supported words (sample): " + ExitCommand.supportedExitHint());
        System.out.println();
        System.out.println("Type 0 or an exit word to quit, or press Enter to continue: ");
        String second = scanner.nextLine().trim();
        return ExitCommand.isZero(second) || ExitCommand.isExit(second);
    }

    public void printCycleHeader(int cycle) {
        System.out.println();
        System.out.println("========== Feeding Cycle " + cycle + " ==========");
    }

    public void printZooStatus(ZooManager zoo) {
        System.out.println();
        System.out.println("--- Zoo Status ---");
        System.out.println("Food stock:");
        for (FoodType type : FoodType.values()) {
            System.out.printf("  %s: %d%n", type.getLabel(), zoo.getFoodSupply().getAmount(type));
        }
        for (String species : zoo.listSpeciesNames()) {
            System.out.printf(
                    "  %s: %d alive / %d total registered%n",
                    species,
                    zoo.countAliveBySpecies(species),
                    zoo.getRegisteredQuantity(species)
            );
        }
        System.out.println("Total alive: " + zoo.countAliveAnimals());
    }

    private Diet readDiet() {
        while (true) {
            System.out.println("Diet type:");
            System.out.println("  1 - Carnivore");
            System.out.println("  2 - Omnivore");
            System.out.println("  3 - Herbivore");
            int choice = readIntInRange("Choice (1-3): ", 1, 3);
            Diet diet = Diet.fromChoice(choice);
            if (diet != null) {
                return diet;
            }
        }
    }

    // --- Input helpers ---

    /**
     * Returns true if the input means "yes" in English or Portuguese.
     */
    private boolean isYes(String input) {
        return input.equals("y") || input.equals("yes") || input.equals("s") || input.equals("sim");
    }

    /**
     * Returns true if the input means "no" in English or Portuguese.
     */
    private boolean isNo(String input) {
        return input.equals("n") || input.equals("no") || input.equals("nao") || input.equals("não");
    }

    /**
     * Returns true if the input refers to food stock.
     */
    private boolean isFood(String input) {
        return input.equals("f") || input.equals("food") || input.equals("comida") || input.equals("estoque");
    }

    /**
     * Returns true if the input refers to animals.
     */
    private boolean isAnimals(String input) {
        return input.equals("a") || input.equals("animals") || input.equals("animais") || input.equals("animal");
    }

    /**
     * Returns true if the input means "increase".
     */
    private boolean isIncrease(String input) {
        return input.equals("i") || input.equals("increase")
                || input.equals("a") || input.equals("acrescentar") || input.equals("+") || input.equals("aumentou");
    }

    /**
     * Returns true if the input means "decrease".
     */
    private boolean isDecrease(String input) {
        return input.equals("d") || input.equals("decrease")
                || input.equals("diminuir") || input.equals("-") || input.equals("diminuiu");
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // retry
            }
            System.out.println("Enter a positive integer greater than zero.");
        }
    }

    private int readNonNegativeInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // retry
            }
            System.out.println("Enter a non-negative integer (zero or greater).");
        }
    }

    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // retry
            }
            System.out.printf("Enter a number between %d and %d.%n", min, max);
        }
    }

    private String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Value cannot be empty.");
        }
    }
}
