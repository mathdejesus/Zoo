package zoo.model;

import java.util.Map;

/**
 * Shared food resource accessed concurrently by multiple animal threads.
 * Tracks a separate stock for each {@link FoodType}, so different diets
 * draw from independent resources without interfering with each other.
 * <p>
 * Threading concept — {@code synchronized} (intrinsic lock):
 * All public methods are {@code synchronized} on {@code this}. This means
 * only one thread at a time can execute any synchronized method on the
 * same FoodSupply instance. This guarantees that:
 * <ul>
 *   <li>{@code stock} reads and writes are atomic (no partial updates)</li>
 *   <li>The invariant {@code stock[i] >= 0} is preserved under concurrent access</li>
 * </ul>
 * <p>
 * Threading concept — {@code wait() / notifyAll()} (condition queue):
 * The legacy {@link #consumeFood(int)} method uses {@code wait()} when
 * there is not enough food. {@code wait()} releases the intrinsic lock
 * and puts the current thread to sleep on the object's condition queue.
 * This is essential: without releasing the lock, no other thread could
 * call {@link #addFood} and the system would deadlock immediately.
 * {@link #addFood} calls {@code notifyAll()} to wake all waiting threads
 * so they can re-check the condition.
 * <p>
 * Threading concept — {@code non-blocking alternative}:
 * The main simulation uses {@link #tryConsume(FoodType, int)} which
 * returns a boolean instead of blocking. This prevents the deadlock
 * that would occur if every animal blocked inside a feeding cycle
 * that is synchronized with {@code CompletableFuture.allOf().join()}.
 * <p>
 * Legacy methods ({@link #consumeFood(int)}) exist for the demo classes
 * ({@link zoo.demo.StarvationDemo}) which demonstrate blocking/wait
 * semantics and timeout-based starvation prevention.
 */
public class FoodSupply {

    private final int[] stock;

    public FoodSupply() {
        this.stock = new int[FoodType.values().length];
    }

    /**
     * Initializes food supply with specific amounts for each type.
     */
    public FoodSupply(Map<FoodType, Integer> initial) {
        this();
        for (Map.Entry<FoodType, Integer> entry : initial.entrySet()) {
            if (entry.getValue() > 0) {
                stock[entry.getKey().ordinal()] = entry.getValue();
            }
        }
    }

    /**
     * Legacy constructor: puts all initial food in the MEAT stock.
     * Used by demo classes that work with a single undifferentiated pool.
     */
    public FoodSupply(int initialAmount) {
        this();
        stock[FoodType.MEAT.ordinal()] = initialAmount;
    }

    /**
     * Adds food of a specific type and wakes all waiting consumers.
     * {@code notifyAll()} is used because multiple threads may be
     * waiting on different food types; we do not know which one
     * can now proceed. Waking all is correct but may cause
     * "thundering herd" — a design trade-off explained in many
     * concurrency textbooks.
     */
    public synchronized void addFood(FoodType type, int qty) {
        stock[type.ordinal()] += qty;
        notifyAll();
    }

    /**
     * Administrative removal: reduces a food type's stock without
     * blocking. The stock is clamped to zero, so it never goes
     * negative. No {@code notifyAll()} call here because reducing
     * stock cannot help threads waiting for more food.
     */
    public synchronized void removeFood(FoodType type, int qty) {
        if (qty <= 0) {
            return;
        }
        stock[type.ordinal()] = Math.max(0, stock[type.ordinal()] - qty);
    }

    /**
     * Non-blocking consume: if the requested type has enough stock,
     * deducts it and returns {@code true}. Otherwise returns {@code false}.
     * <p>
     * This is the primary method used by the main simulation
     * ({@link SpeciesAnimal#eat}). Because it never blocks, a
     * feeding cycle using {@code CompletableFuture.allOf().join()}
     * cannot deadlock even when food is insufficient — animals
     * simply skip the meal.
     */
    public synchronized boolean tryConsume(FoodType type, int qty) {
        int idx = type.ordinal();
        if (stock[idx] >= qty) {
            stock[idx] -= qty;
            return true;
        }
        return false;
    }

    /**
     * Legacy blocking consume (for demo classes only).
     * <p>
     * Threading concept — {@code guarded block}:
     * The {@code while (condition)} loop pattern is essential:
     * after {@code wait()} returns, the condition may still be false
     * (spurious wakeup or another thread consumed the food first).
     * Always re-check in a loop, never use {@code if}.
     * <p>
     * Deduction is from total stock across all types via
     * {@link #deductFromAny}, since this legacy method does not
     * distinguish food types.
     */
    public synchronized void consumeFood(int qty) throws InterruptedException {
        while (getTotalAmount() < qty) {
            wait();
        }
        deductFromAny(qty);
    }

    /**
     * Legacy blocking consume with timeout (for demo classes only).
     * <p>
     * Threading concept — {@code bounded wait}:
     * {@code wait(timeout)} limits how long a thread remains blocked.
     * If the deadline expires, the thread gives up and reports
     * starvation. This is one strategy for handling the starvation
     * problem — see {@link zoo.demo.StarvationDemo}.
     *
     * @return true if food was consumed, false if the wait timed out
     */
    public synchronized boolean consumeFood(int qty, long timeoutMs, String consumerName)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (getTotalAmount() < qty) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                System.out.println(consumerName + " is starving!");
                return false;
            }
            wait(remaining);
        }
        deductFromAny(qty);
        return true;
    }

    /**
     * Deducts {@code qty} units from available food types, taking
     * from each type in enum order until the quantity is satisfied.
     * Used only by legacy methods that operate on total stock.
     */
    private void deductFromAny(int qty) {
        int remaining = qty;
        for (FoodType type : FoodType.values()) {
            int idx = type.ordinal();
            int take = Math.min(stock[idx], remaining);
            stock[idx] -= take;
            remaining -= take;
            if (remaining == 0) {
                break;
            }
        }
    }

    /**
     * Returns the amount available for a specific food type.
     */
    public synchronized int getAmount(FoodType type) {
        return stock[type.ordinal()];
    }

    /**
     * Returns the total amount across all food types.
     */
    public synchronized int getTotalAmount() {
        int total = 0;
        for (int v : stock) {
            total += v;
        }
        return total;
    }

    /**
     * Legacy getAmount — returns total across all types.
     */
    public synchronized int getAmount() {
        return getTotalAmount();
    }
}
