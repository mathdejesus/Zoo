package zoo.demo;

/**
 * Demonstrates the classic lock-ordering deadlock.
 * <p>
 * Deadlock concept — {@code circular wait}:
 * Thread A holds lock1 and waits for lock2.
 * Thread B holds lock2 and waits for lock1.
 * Neither can proceed → the program hangs forever.
 * <p>
 * This is one of the four Coffman conditions for deadlock:
 * <ol>
 *   <li>Mutual exclusion — locks are not shareable</li>
 *   <li>Hold and wait — each thread holds one lock while waiting for another</li>
 *   <li>No preemption — locks cannot be forcibly taken</li>
 *   <li>Circular wait — Thread A → lock2 → Thread B → lock1 → Thread A</li>
 * </ol>
 * <p>
 * The fix (shown commented below) is to acquire locks in a consistent
 * global order. If both threads acquire lock1 before lock2, the circular
 * wait is broken and deadlock cannot occur.
 */
public class DeadlockDemo {

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(String[] args) {
        System.out.println("=== Deadlock demonstration (program will freeze) ===");

        // Thread A: lock1 → lock2
        Thread threadA = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread A acquired lock1");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized (lock2) {
                    System.out.println("Thread A acquired lock2");
                }
            }
        }, "Thread-A");

        // Thread B: lock2 → lock1  ← opposite order causes circular wait
        Thread threadB = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread B acquired lock2");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized (lock1) {
                    System.out.println("Thread B acquired lock1");
                }
            }
        }, "Thread-B");

        threadA.start();
        threadB.start();
    }

    /*
     * FIXED VERSION (commented out):
     * Both threads acquire locks in the SAME order (lock1 → lock2).
     * Circular wait is eliminated → no deadlock.
     *
     * Thread threadA = new Thread(() -> {
     *     synchronized (lock1) {
     *         synchronized (lock2) { ... }
     *     }
     * });
     *
     * Thread threadB = new Thread(() -> {
     *     synchronized (lock1) {   // same order!
     *         synchronized (lock2) { ... }
     *     }
     * });
     */
}
