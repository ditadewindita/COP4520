# Lock-Free Stack

A Lock-Free Stack implementation using Java's Atomics library. This stack uses
AtomicInteger and AtomicReference in place of mutexes, as well as descriptor
objects.

#### Correctness
Linearizability is a correctness condition in which concurrent operations appear
to execute instantaneously, and therefore defines that concurrent histories have some legal sequential history. This lock-free stack uses CAS operations that
feature changes atomically. Those atomic variables within the stack only change
if another thread does not modify the same memory location it's handling, therefore
the method call is guaranteed to fail and not produce ambiguous results.

#### Linearization Points
Linearization points can be identified by statements whose results can be visible
by other threads after its execution. Since the stack utilizes descriptors to modify its
components, the linearization points are whenever a CAS operation succeeds on a
descriptor object in `push()`, `pop()`, and `size()`.

#### Progress
This stack's use of CAS means that its operations are executed in a predictable
manner, which satisfies linearizability's non-blocking property. By not having unexpected delays, some thread `x` does not block another thread `y` from making progress. The
stack operations also rely on a descriptor object, specifically a `WriteDescriptor`, to
change the stack's contents. Since the `WriteDescriptor` only performs its CAS operation
if it is in a _pending_ state, any number of threads can access this descriptor at one
time and one is guaranteed to succeed. The success of at least one thread shows that
the system as a whole can make progress.

## Structure
This stack has two separate files: `LockFreeStack.java` and `LockFreeStackRunner.java`.

`LockFreeStack.java` includes a basic `Node` class that takes in a generic type `T` and
the `LockFreeStack` class, which also holds generic types.

The `LockFreeStackRunner.java` bears lots of goodies! It includes a `LockFreeRunnable` which implements `Runnable` with a custom `run()` method that randomizes between push, pop, and size operations on the `LockFreeStack` that holds random integers. With each operation, a print statement is outputted with failure/success notices and the time the operation took place.
The stack's contents is also printed at the end of all the thread executions, along with the runtime in milliseconds.

Use the following to run the program:
```
$ javac LockFreeStack.java LockFreeStackRunner.java
$ java LockFreeStackRunner
```
And expect an output similar to this example:
```
-> Thread 9 checked size as 0 at 1521487158326ms.
-> Thread 11 failed call to pop() at 1521487158326ms.
-> Thread 7 failed call to pop() at 1521487158326ms.
-> Thread 8 failed call to pop() at 1521487158326ms.
-> Thread 1 checked size as 0 at 1521487158327ms.
-> Thread 10 failed call to pop() at 1521487158326ms.
-> Thread 0 failed call to pop() at 1521487158326ms.
-> Thread 13 failed call to pop() at 1521487158327ms.
-> Thread 2 failed call to pop() at 1521487158327ms.
-> Thread 12 checked size as 0 at 1521487158327ms.
-> Thread 3 checked size as 0 at 1521487158326ms.
-> Thread 15 checked size as 0 at 1521487158328ms.
-> Thread 16 checked size as 0 at 1521487158328ms.
-> Thread 17 checked size as 0 at 1521487158329ms.
-> Thread 18 failed call to pop() at 1521487158329ms.
-> Thread 6 pushed 292 at 1521487158329ms.
-> Thread 5 pushed 186 at 1521487158329ms.
-> Thread 4 pushed 251 at 1521487158329ms.
-> Thread 19 pushed 267 at 1521487158329ms.
-> Thread 14 pushed 16 at 1521487158329ms.
-> Thread 20 popped 267 at 1521487158329ms.
-> Thread 21 checked size as 4 at 1521487158330ms.
-> Thread 22 checked size as 4 at 1521487158330ms.
-> Thread 23 popped 16 at 1521487158330ms.
-> Thread 24 checked size as 3 at 1521487158330ms.
-> Thread 25 popped 251 at 1521487158330ms.
-> Thread 26 pushed 44 at 1521487158331ms.
-> Thread 27 popped 44 at 1521487158331ms.
-> Thread 28 popped 186 at 1521487158331ms.
-> Thread 29 pushed 96 at 1521487158331ms.
-> Thread 30 checked size as 2 at 1521487158331ms.
-> Thread 31 checked size as 2 at 1521487158331ms.

-> Stack contents:
96
292

Runtime: 7ms.
```

## Performance

![Pop Performance](https://raw.githubusercontent.com/ditadewindita/COP4520/master/LockFreeStack/graph.png)
_Graph of the operations of this lock-free stack across 1, 2, 4, 8, 16, and 32 threads_

### Notes
A bank of pre-made nodes was used for this implementation due to chances of memory
protection occurrences. An ArrayList is made with `MAX_NUM_NODES` (initially at 1000) nodes with `null` values. The stack's `push()` operation will grab an allocated node from the ArrayList and set it's value to the value passed into
the call to push.
