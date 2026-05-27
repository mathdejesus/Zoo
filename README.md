# Zoo Simulator

An interactive Java zoo simulation focused on **concurrent programming** concepts.
This academic project demonstrates threads, synchronization, deadlock, starvation,
metamorphosis (upcast/downcast), and asynchronous computation — all through the
metaphor of a zoo with species, diets, and feeding cycles.

## Features

- **Interactive species registration** — each species has a name, population count, and diet
- **Three diet types** with separate food stocks:
  - **Carnivore** → Meat (3 units/meal)
  - **Omnivore** → Mix (2 units/meal)
  - **Herbivore** → Leaves/Vegetables (1 unit/meal)
- **Concurrent feeding cycles** — all animals eat in parallel via `CompletableFuture`
- **Population and stock adjustments** between cycles via console prompts
- **Metamorphosis** — a caterpillar transforms into a butterfly (upcast/downcast demo)
- **Deadlock and starvation demos** — classic concurrency problems with documented fixes

## Concurrent Programming Concepts

| Concept | Where it's demonstrated |
|---------|------------------------|
| `synchronized` (intrinsic lock) | `FoodSupply.java` — every public method is synchronized on `this` |
| `wait()` / `notifyAll()` | `FoodSupply.consumeFood()` / `addFood()` — condition queue pattern |
| `volatile` (visibility) | `Animal.alive` — ensures cross-thread shutdown visibility |
| `CompletableFuture` + `allOf().join()` | `ZooManager.runFeedingCycle()` — async tasks + barrier sync |
| `AtomicInteger` | `ZooManager.idSequence` — lock-free thread-safe ID generation |
| Deadlock (circular wait) | `DeadlockDemo.java` — lock ordering creates a deadlock |
| Starvation (greedy thread) | `StarvationDemo.java` — tight loop without sleep starves others |
| `wait(timeout)` | `StarvationDemo.java` — bounded wait limits starvation |
| Upcast (subtype → supertype) | `Caterpillar.transform()` — returns `Butterfly` as `Animal` |
| Downcast with `instanceof` | `AnimalThread.run()` — pattern matching narrows `Animal` to `Butterfly` |
| Non-blocking resource access | `FoodSupply.tryConsume()` — prevents deadlock in feeding cycles |
| Template Method pattern | `Animal.run()` — skeleton algorithm, subclasses implement details |

## Requirements

- Java 17 or later
- Maven 3.6+

## How to Run

```bash
# Compile and run interactively
mvn compile exec:java

# Or build a JAR
mvn package
java -jar target/zoo-simulation-1.0-SNAPSHOT.jar
```

To run the concurrency demos:

```bash
# Deadlock demo (program will freeze — this is intentional)
java -cp target/classes zoo.demo.DeadlockDemo

# Starvation demo (runs two scenarios: without and with timeout)
java -cp target/classes zoo.demo.StarvationDemo
```

## Workflow Example

Below is a complete interactive session. User input is shown inline.

```
=== Zoo Simulator ===

How many species do you want to register? 2

--- Species 1 of 2 ---
Name: Lion
Quantity: 3
Diet type:
  1 - Carnivore
  2 - Omnivore
  3 - Herbivore
Choice (1-3): 1
Registered: Lion x3 (Carnivore)

--- Species 2 of 2 ---
Name: Zebra
Quantity: 5
Diet type:
  1 - Carnivore
  2 - Omnivore
  3 - Herbivore
Choice (1-3): 3
Registered: Zebra x5 (Herbivore)

Is there initial food available? (y/n): y
Enter the initial quantity for each food type:
  Meat: 20
  Mix: 0
  Leaves/Vegetables: 30
Initial stock configured:
  Meat: 20
  Mix: 0
  Leaves/Vegetables: 30

--- Zoo Status ---
Food stock:
  Meat: 20
  Mix: 0
  Leaves/Vegetables: 30
  Lion: 3 alive / 3 total registered
  Zebra: 5 alive / 5 total registered
Total alive: 8

========== Feeding Cycle 1 ==========
[ForkJoinPool.commonPool-worker-1] Lion-2 ate (Carnivore, 3 units of Meat). Energy: 15
[ForkJoinPool.commonPool-worker-2] Lion-3 ate (Carnivore, 3 units of Meat). Energy: 15
[ForkJoinPool.commonPool-worker-3] Lion-1 ate (Carnivore, 3 units of Meat). Energy: 15
[ForkJoinPool.commonPool-worker-4] Zebra-1 ate (Herbivore, 1 units of Leaves/Vegetables). Energy: 15
[ForkJoinPool.commonPool-worker-1] Zebra-2 ate (Herbivore, 1 units of Leaves/Vegetables). Energy: 15
[ForkJoinPool.commonPool-worker-2] Zebra-3 ate (Herbivore, 1 units of Leaves/Vegetables). Energy: 15
[ForkJoinPool.commonPool-worker-3] Zebra-4 ate (Herbivore, 1 units of Leaves/Vegetables). Energy: 15
[ForkJoinPool.commonPool-worker-4] Zebra-5 ate (Herbivore, 1 units of Leaves/Vegetables). Energy: 15

--- Zoo Status ---
Food stock:
  Meat: 11          # 3 lions × 3 = 9 consumed
  Mix: 0
  Leaves/Vegetables: 25  # 5 zebras × 1 = 5 consumed
  Lion: 3 alive / 3 total registered
  Zebra: 5 alive / 5 total registered
Total alive: 8

After this feeding cycle, did any change occur in the food stock or animal population? (y/n): y
Did the change affect the food stock or the animals? (f/a): f
Select the food type whose stock was changed, using its numeric code:
  1 - Meat
  2 - Mix
  3 - Leaves/Vegetables
Enter the food type code (1-3): 1
Should the food stock increase or decrease? (i/d): i
Enter the number of food units to add or remove (positive integer): 10
Stock of Meat increased by 10 unit(s). New total: 21

--- Zoo Status ---
Food stock:
  Meat: 21
  Mix: 0
  Leaves/Vegetables: 25
  Lion: 3 alive / 3 total registered
  Zebra: 5 alive / 5 total registered
Total alive: 8

Press Enter to continue.
To exit, type 0 or an exit word (e.g., exit, quit, sair, bye).
> [Enter]

========== Feeding Cycle 2 ==========
... (continues until user types 0 or an exit word)

> 0
Zoo simulation ended.
```

### What happened in the example

1. **Setup** — The user registered 3 Lions (Carnivore) and 5 Zebras (Herbivore),
   then stocked 20 Meat and 30 Leaves/Vegetables.
2. **Cycle 1** — All 8 animals ate concurrently in the ForkJoinPool.
   3 Lions consumed 9 Meat total, 5 Zebras consumed 5 Leaves/Vegetables total.
3. **Adjustment** — The user noticed Meat was running low and added 10 more units.
4. **Continuation** — The simulation continues cycle by cycle until the user exits.

## Project Structure

```
src/main/java/
├── Main.java                         # Interactive loop (main thread coordinator)
└── zoo/
    ├── console/
    │   ├── ZooConsole.java           # Console prompts, input validation (EN + PT fallback)
    │   └── ExitCommand.java          # Multi-language exit word recognition
    ├── model/
    │   ├── Animal.java               # Abstract base with volatile alive + Template Method
    │   ├── Butterfly.java            # Final metamorphosis stage (identity transform)
    │   ├── Caterpillar.java          # Grows and transforms into Butterfly (upcast)
    │   ├── Diet.java                 # Enum: maps diet → FoodType + food-per-meal
    │   ├── FoodSupply.java           # Synchronized shared resource (wait/notifyAll)
    │   ├── FoodType.java             # Enum: food type labels and ordinal-based indexing
    │   ├── SpeciesAnimal.java        # Diet-bound animal (non-blocking tryConsume)
    │   └── SpeciesSetup.java         # Immutable species configuration holder
    ├── thread/
    │   ├── AnimalThread.java         # Legacy per-animal Thread with metamorphosis
    │   └── ZooManager.java           # Feeding cycles via CompletableFuture + adjustments
    └── demo/
        ├── DeadlockDemo.java         # Classic circular-wait deadlock + fix
        └── StarvationDemo.java       # Greedy-thread starvation + timeout fix
```

