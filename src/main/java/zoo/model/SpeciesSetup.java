package zoo.model;

/**
 * Immutable data holder for a user-defined species configuration at zoo startup.
 * Once created, the species name, quantity, and diet cannot change.
 * To adjust quantity at runtime, {@link zoo.thread.ZooManager} replaces
 * the entry in its registry with a new {@code SpeciesSetup}.
 */
public class SpeciesSetup {

    private final String speciesName;
    private final int quantity;
    private final Diet diet;

    public SpeciesSetup(String speciesName, int quantity, Diet diet) {
        this.speciesName = speciesName;
        this.quantity = quantity;
        this.diet = diet;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public int getQuantity() {
        return quantity;
    }

    public Diet getDiet() {
        return diet;
    }
}
