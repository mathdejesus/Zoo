package zoo.demo;

import zoo.model.FoodSupply;

/**
 * Demonstrates thread starvation and a timeout-based mitigation.
 * <p>
 * Starvation concept:
 * One "greedy" thread runs a tight loop (no {@code Thread.sleep()})
 * and continuously re-acquires the intrinsic lock on {@link FoodSupply}.
 * Other threads that call {@code synchronized} methods must wait for
 * the lock to become available. Since the greedy thread almost never
 * releases the lock for long, the other threads get little to no CPU
 * time and effectively starve.
 * <p>
 * Two scenarios:
 * <ol>
 *   <li><b>Without timeout</b> — workers wait indefinitely via {@code wait()}.
 *       The greedy thread monopolizes the food, and workers get nothing.</li>
 *   <li><b>With timeout</b> — workers use {@code wait(timeout)}. After 3 seconds
 *       without food, they give up and print "is starving!".</li>
 * </ol>
 * <p>
 * The timeout approach does not solve starvation completely (the greedy
 * thread still gets most of the food), but it prevents threads from
 * blocking forever — a practical compromise.
 */
public class StarvationDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Starvation demonstration (others wait indefinitely) ===");
        runScenario(false);

        Thread.sleep(1000);

        System.out.println();
        System.out.println("=== Starvation with wait(timeout) fix ===");
        runScenario(true);
    }

    private static void runScenario(boolean useTimeout) throws InterruptedException {
        FoodSupply food = new FoodSupply(2);
        Thread[] workers = new Thread[5];

        for (int i = 0; i < workers.length; i++) {
            String name = "Caterpillar-" + (i + 1);
            workers[i] = new Thread(() -> eatLoop(name, food, useTimeout, false), name);
            workers[i].start();
        }

        // Greedy thread runs a tight loop with no sleep — tends to starve others.
        Thread greedy = new Thread(() -> eatLoop("Greedy", food, useTimeout, true), "Greedy");
        greedy.start();

        Thread.sleep(useTimeout ? 8000 : 5000);

        for (Thread worker : workers) {
            worker.interrupt();
        }
        greedy.interrupt();

        for (Thread worker : workers) {
            worker.join(1000);
        }
        greedy.join(1000);
    }

    private static void eatLoop(String name, FoodSupply food, boolean useTimeout, boolean greedy) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                boolean ate;
                if (useTimeout) {
                    // Bounded wait: give up after 3 seconds
                    ate = food.consumeFood(1, 3000, name);
                } else {
                    // Unbounded wait: block until food is available (may never happen)
                    food.consumeFood(1);
                    ate = true;
                }

                if (!ate) {
                    break;
                }

                System.out.println(name + " ate food. Remaining: " + food.getAmount());

                if (!greedy) {
                    // Worker threads yield the lock by sleeping — gives others a chance
                    Thread.sleep(500);
                }
                // Greedy thread: no sleep, tight loop — continuously re-acquires the lock
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
