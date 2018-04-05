# Elimination Backoff Lock-Free Stack

A Lock-Free Stack implementation using Java's Atomics library. This stack uses
AtomicInteger and AtomicReference in place of locks, as well as the Elimination
Backoff implementation.

#### Correctness
Linearizability is a correctness condition in which concurrent operations appear
to execute instantaneously, and therefore defines that concurrent histories have some legal sequential history. This lock-free stack uses CAS operations that
feature changes atomically. Those atomic variables within the stack only change
if another thread does not modify the same memory location it's handling, therefore
the method call is guaranteed to fail and not produce ambiguous results. The ABA problem is also not present in this implementation because the CAS operations that change the state of the stack doesn't inspect the item.

#### Linearization Points
Linearization points can be identified by statements whose results can be visible
by other threads after its execution. The linearization point for a successful exchange in the `LockFreeExchanger` is when the second thread to arrive changes from `WAITING` to `BUSY`, and for a failed exchange, the linearization point is when a `TimeoutException` is thrown. More generically, a successful `pop()` and `push()` can be linearized when the `LockFreeStack` is accessed, a `push()` eliminated by
a `pop()` is linearized when they collide, and any unsuccessful calls are linearized when an `Exception` is thrown.

## Structure
This stack has two separate files: `EliminationBackoffStack.java` and `EliminationBackoffStackRunner.java`.

`EliminationBackoffStack.java` includes a basic `Node` class that takes in a generic type `T` and
the `EliminationBackoffStack` class, which also holds generic types.

The `EliminationBackoffStackRunner.java` bears lots of goodies! It includes a `LockFreeRunnable` which implements `Runnable` with a custom `run()` method that randomizes between push, pop, and size operations on the `EliminationBackoffStack` that holds random integers. With each operation, a print statement is outputted with failure/success notices and the time the operation took place.
The stack's contents is also printed at the end of all the thread executions, along with the runtime in milliseconds.

Use the following to run the program:
```
$ javac EliminationBackoffStack.java EliminationBackoffStackRunner.java
$ java EliminationBackoffStackRunner
```
And expect an output similar to this example:
```
-> Thread 0 checked size as 0 at 1522781110235ms.
-> Thread 1 failed call to pop() at 1522781110236ms.
-> Thread 4 checked size as 0 at 1522781110235ms.
-> Thread 2 failed call to pop() at 1522781110237ms.
-> Thread 5 pushed 80 at 1522781110237ms.
-> Thread 6 popped 80 at 1522781110237ms.
-> Thread 3 checked size as 0 at 1522781110235ms.
-> Thread 7 failed call to pop() at 1522781110238ms.
-> Thread 8 failed call to pop() at 1522781110239ms.
-> Thread 9 pushed 177 at 1522781110239ms.
-> Thread 10 popped 177 at 1522781110239ms.
-> Thread 11 failed call to pop() at 1522781110240ms.
-> Thread 12 checked size as 0 at 1522781110240ms.
-> Thread 13 failed call to pop() at 1522781110240ms.
-> Thread 14 checked size as 0 at 1522781110240ms.
-> Thread 15 pushed 2 at 1522781110241ms.
-> Thread 16 popped 2 at 1522781110241ms.
-> Thread 17 failed call to pop() at 1522781110241ms.
-> Thread 18 pushed 59 at 1522781110241ms.
-> Thread 19 pushed 106 at 1522781110241ms.
-> Thread 20 pushed 115 at 1522781110241ms.
-> Thread 21 pushed 123 at 1522781110242ms.
-> Thread 22 checked size as 4 at 1522781110242ms.
-> Thread 23 checked size as 4 at 1522781110242ms.
-> Thread 25 popped 123 at 1522781110242ms.
-> Thread 24 pushed 248 at 1522781110242ms.
-> Thread 26 checked size as 4 at 1522781110242ms.
-> Thread 27 popped 248 at 1522781110242ms.
-> Thread 28 pushed 122 at 1522781110243ms.
-> Thread 29 checked size as 3 at 1522781110243ms.
-> Thread 30 pushed 159 at 1522781110243ms.
-> Thread 31 checked size as 5 at 1522781110243ms.

-> Stack contents:
159
122
115
106
59

Runtime: 10ms.
```

## Performance Against Regular LockFreeStack
The performance of this stack against the lock-free implementation with descriptor objects are outlined in the graphs below for each operation. The Elimination Backoff method shows better results when scaled because it
reduces contention at the top of the stack since access to it is restricted through the elimination array.

**Pop**
![Pop Performance](https://raw.githubusercontent.com/ditadewindita/COP4520/master/Elimination%20Back-Off%20Stack/pop.png)

**Push**
![Push Performance](https://raw.githubusercontent.com/ditadewindita/COP4520/master/Elimination%20Back-Off%20Stack/push.png)

**Size**
![Size Performance](https://raw.githubusercontent.com/ditadewindita/COP4520/master/Elimination%20Back-Off%20Stack/size.png)

_*Graph of the operations of this lock-free stack are across 1, 2, 4, 8, 16, and 32 threads_

### Notes
A bank of pre-made nodes was used for this implementation due to chances of memory
protection occurrences. An ArrayList is made with `MAX_NUM_NODES` (initially at 1000) nodes with `null` values. The stack's `push()` operation will grab an allocated node from the ArrayList and set it's value to the value passed into
the call to push.
