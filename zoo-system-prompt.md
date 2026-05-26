# Zoo System Simulation — Java Project Prompt

> **How to use this file:**
> Copy the content inside the `---` blocks below and send it to Qwen3.5:9B via OpenCode CLI.
> Read through each section before starting so you understand what to ask at each step.
> Interact one phase at a time — do not send everything at once.

---

## INITIAL SYSTEM PROMPT
*Send this first, before anything else.*

```
You are a Java expert helping me build a Zoo Simulation system as a university project.
The project focuses on applying the following Java concepts:

- Threads (each animal is a Thread)
- Shared variables with synchronization (synchronized, wait, notifyAll)
- CompletableFuture for asynchronous operations
- Deadlock and Starvation (demonstrate the problems and their solutions)
- Data casting (a Caterpillar transforms into a Butterfly over time)

Rules you must follow throughout this project:
1. Always write complete, compilable Java code — no pseudocode.
2. Explain every synchronized block, every wait() and notifyAll() you write.
3. Use Java 17+ features where appropriate.
4. Organize the code in packages: zoo.model, zoo.thread, zoo.demo, and a root Main class.
5. Add short comments in English explaining the threading logic.
6. When I ask for a file, deliver the full file content inside a code block.

Acknowledge these rules and tell me you are ready to start.
```

---

## PHASE 1 — Project Structure
*Send after the model acknowledges the rules.*

```
Let's start with the project structure.

Create the following:
1. The abstract class Animal in the package zoo.model
   - Fields: String name, int energy, volatile boolean alive
   - Abstract methods: eat(FoodSupply food), rest(), transform()
   - A concrete run() method that loops: eat → rest → check transform → repeat
   - The loop must stop when alive is false

2. The class FoodSupply in the package zoo.model
   - Field: private int amount
   - Method addFood(int qty): adds food and calls notifyAll()
   - Method consumeFood(int qty): uses synchronized + wait() until food is available
   - Explain in a comment why wait() is needed here and how Starvation can happen

Deliver both files fully. After delivering, wait for my next instruction.
```

---

## PHASE 2 — Caterpillar and Butterfly
*Send after Phase 1 is complete and you reviewed the code.*

```
Now create the two concrete animal classes.

1. Class Caterpillar in zoo.model, extends Animal
   - Field: private int growthLevel (starts at 0)
   - eat(): synchronized access to FoodSupply, gains energy and increases growthLevel
   - rest(): Thread.sleep() for a random time between 1000ms and 3000ms
   - transform(): if growthLevel >= 10, return a new Butterfly with the same name; otherwise return this
   - Override toString() to show name and current growthLevel

2. Class Butterfly in zoo.model, extends Animal
   - eat(): synchronized access to FoodSupply, but consumes LESS food than Caterpillar (it drinks nectar)
   - rest(): Thread.sleep() for a shorter random time (500ms to 1500ms)
   - transform(): butterflies do not transform — always return this
   - Override toString() to show name and energy

Important: the transform() return type is Animal (the parent class).
Show in a comment where the upcast and downcast happen.

Deliver both files fully.
```

---

## PHASE 3 — Animal Thread and Zoo Manager
*Send after reviewing Phase 2.*

```
Now build the threading layer.

1. Class AnimalThread in zoo.thread, extends Thread
   - Constructor receives an Animal and a FoodSupply
   - The run() method delegates to animal.run()
   - After each cycle, check if animal.transform() returns a different object
     If it does, replace the local reference and print:
     "[name] has transformed into a Butterfly!"
   - Use instanceof and casting to confirm the new type

2. Class ZooManager in zoo.thread
   - Field: List<AnimalThread> threads
   - Field: FoodSupply foodSupply
   - Method startZoo(int numberOfAnimals):
       - Creates numberOfAnimals Caterpillar instances
       - Wraps each in an AnimalThread and starts it
       - Uses CompletableFuture.runAsync() to start an automatic food dispenser
         that calls foodSupply.addFood(3) every 2 seconds in a loop
   - Method stopZoo(): sets alive = false on all animals and joins all threads

Deliver both files fully.
```

---

## PHASE 4 — Deadlock and Starvation Demos
*Send after reviewing Phase 3.*

```
Now create two demonstration classes in the package zoo.demo.

1. Class DeadlockDemo
   - Simulate a deadlock with two threads and two locks (Object lock1, Object lock2)
   - Thread A acquires lock1 first, then tries lock2
   - Thread B acquires lock2 first, then tries lock1
   - Add a Thread.sleep(100) between the two lock acquisitions to guarantee the deadlock
   - Print the moment each thread acquires its first lock so I can see the freeze happen
   - After showing the deadlock, add a COMMENTED-OUT fixed version where both threads
     always acquire the locks in the same order (lock1 → lock2)

2. Class StarvationDemo
   - Simulate starvation: create one FoodSupply with very little food
   - Create 5 Caterpillar threads competing for it
   - Make one "greedy" thread that runs in a tight loop without resting,
     so it always grabs the food before others can
   - Show the other threads waiting indefinitely
   - Then fix it: add a wait(timeout) of 3000ms in consumeFood() so that
     a thread gives up after 3 seconds and prints "[name] is starving!"

Deliver both files fully with explanatory comments.
```

---

## PHASE 5 — Main Class and Final Wiring
*Send after reviewing Phase 4.*

```
Now create the Main class at the root of the zoo package.

The main() method must:
1. Create a ZooManager and call startZoo(5) — five Caterpillars
2. Use a ScheduledExecutorService to print a status report every 3 seconds:
   - How many animals are currently alive
   - Current food amount in FoodSupply
   - How many transformations have happened so far (add a counter to ZooManager)
3. After 30 seconds, call zoo.stopZoo() and shutdown the scheduler
4. Print "Zoo simulation ended." when everything is stopped

Also create a pom.xml for Maven with Java 17 as the compiler target.
No external dependencies are needed beyond the JDK.

Deliver Main.java and pom.xml fully.
```

---

## PHASE 6 — Review and Concepts Check
*Send this after the full project is complete. Use it to practice English and consolidate your understanding.*

```
The project is complete. Now help me understand it deeply by answering these questions:

1. In our FoodSupply class, why did we use notifyAll() instead of notify()?
   What would go wrong with notify() in this scenario?

2. Explain exactly what happens step by step when a Caterpillar transforms into a Butterfly.
   Which line in the code performs the upcast? Which line performs the downcast?
   Why is the downcast safe here?

3. In the DeadlockDemo, at what exact moment does the deadlock occur?
   What is the minimal change needed to fix it without removing the locks?

4. What is the difference between using synchronized(this) and a dedicated lock object
   like new Object()? Which did we use for FoodSupply and why?

5. How does CompletableFuture.runAsync() differ from manually creating a new Thread?
   In what part of our project did we use it, and why was it a good choice there?

Answer each question separately and clearly. Use examples from our actual code.
```

---

## USEFUL FOLLOW-UP PROMPTS
*Keep these ready to use during development if something goes wrong.*

**If the code does not compile:**
```
The code has a compilation error. Here is the error message: [paste error here].
Show me the exact lines that need to change and explain why.
```

**If you want to add more animals:**
```
Add a new animal called Lion in zoo.model.
Lions eat a lot (consume 3 food per cycle), rest very little (200ms to 500ms),
and never transform. Make sure it fits the existing Animal class contract.
```

**If you want to test thread safety:**
```
Add a thread-safety test to FoodSupply:
start 10 threads that all call consumeFood(1) at the same time with only 5 food available.
Print the food amount before and after each consumption.
Show that without synchronization, the amount can go negative.
Then show the fixed version with synchronization working correctly.
```

---

> **Tip for practicing Portuguese:**
> After each phase, ask Qwen to explain the code as if you were a junior developer.
> Then ask follow-up questions like: "Why not use X instead of Y?" or "What happens if I remove this synchronized?"
> This forces real technical conversation in Portuguese.
