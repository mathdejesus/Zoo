package zoo.model;

/**
 * Base class for zoo animals. Each animal follows a life cycle of
 * eat, rest, and optional transform (metamorphosis).
 * <p>
 * Threading concept — {@code volatile visibility}:
 * The {@code alive} field is {@code volatile} so that when one thread
 * (e.g. {@link zoo.thread.ZooManager#stopZoo}) writes {@code false},
 * other threads reading {@code alive} (e.g. the animal's run loop)
 * see the updated value immediately. Without {@code volatile}, the
 * JIT compiler could cache the old value and the animal might never
 * stop, causing a thread leak or preventing clean shutdown.
 * <p>
 * Design pattern — {@code Template Method}:
 * {@link #run} defines the skeleton algorithm (eat → rest → transform)
 * and subclasses provide concrete implementations via the abstract
 * methods {@link #eat}, {@link #rest}, and {@link #transform}.
 * This is the Template Method behavioral pattern.
 */
public abstract class Animal {

    protected final String name;
    protected int energy;
    protected volatile boolean alive = true;

    protected Animal(String name) {
        this.name = name;
        this.energy = 10;
    }

    public abstract void eat(FoodSupply food);

    public abstract void rest();

    /**
     * Metamorphosis hook. Returns {@code this} if no transformation
     * occurs, or a new {@link Animal} instance (upcast to Animal)
     * if the animal changes form.
     * <p>
     * Upcast: a {@link Butterfly} is returned as {@code Animal}
     * (subtype → supertype). The caller may downcast back after
     * an {@code instanceof} check.
     */
    public abstract Animal transform();

    /**
     * One full life-cycle iteration: eat, rest, then check transform.
     * Each phase checks {@code alive} so a concurrent call to
     * {@link #setAlive(boolean)} from another thread terminates
     * the loop promptly.
     */
    public void run(FoodSupply food) {
        while (alive) {
            eat(food);
            if (!alive) {
                break;
            }
            rest();
            if (!alive) {
                break;
            }
            transform();
        }
    }

    public String getName() {
        return name;
    }

    public int getEnergy() {
        return energy;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
