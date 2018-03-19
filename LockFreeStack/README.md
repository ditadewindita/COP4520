# Lock-Free Stack

A Lock-Free Stack implementation using Java's Atomics library. This stack uses
AtomicInteger and AtomicReference in place of mutexes, as well as descriptor
objects.

<!-- ## Atomic vs. Volatile
The use of atomic, volatile, or both types were considered to make this stack lock free. Atomic variables are able to do operations instantaneously, while volatile types ensure that all threads are reading the most up-to-date copy of a variable. However, volatile variables are not by default atomic.

This implementation uses atomic types over volatiles because Java's Atomics library offers an atomic compare and set. By comparing the nodes in the stack, whether it be during a push or a pop, and being able to swap them at the correct time in one atomic instruction, the operation will only proceed if it sees a head pointer it is expecting. The `compareAndSet()`checks the current head of the stack and compares it with the expected head node, and if it is indeed the same (meaning no other thread had modified the stack), it will 'swap' the nodes respective to the type of instruction (push or pop).

If the stack did not use atomics or volatiles, different threads could read different values from the stack and execute their code based on a false-positive. It could also do node swaps while another thread is swapping, and have the stack corrupted.

## Linearizability
The linearizability of this lock-free stack depends on the atomic variables within it. Since the stack uses an atomic `compareAndSet()` (an instantaneous\* operation) before switching nodes, if another thread modifies the same memory location the current thread is handling, the method call is guaranteed to fail. Therefore, its execution is not interrupted halfway and it does not produce ambiguous results. The shared objects in the stack are accessed \*instantaneously, so its linearizability is proven by having the operations complete in a reasonable and expected manner. Furthermore, since the stack completes its operations in a predictable manner, it satisfies linearizability's non-blocking property. By not having unexpected delays (thanks to atomics!), some thread `x` does not block another thread `y` from making progress.

\*seemingly instantaneous *enough*, since things are not *actually* done instantaneously in the computer ;) -->

#### Correctness
Linearizability is a correctness condition in which concurrent operations appear
to execute instantaneously, and therefore defines that concurrent histories have some legal sequential history. This lock-free stack uses CAS operations that
feature changes atomically. Those atomic variables within the stack only change
if another thread does not modify the same memory location it's handling, therefore
the method call is guaranteed to fail and not produce ambiguous results.

#### Progress
This stack's use of CAS means that its operations are executed in a predictable
manner, which satisfies linearizability's non-blocking property. By not having unexpected delays, some thread `x` does not block another thread `y` from making progress.

#### Efficiency

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
-> Thread 5 failed call to pop() at 1521253087445ms.
-> Thread 2 failed call to pop() at 1521253087445ms.
-> Thread 4 failed call to pop() at 1521253087445ms.
-> Thread 0 pushed 120 at 1521253087447ms.
-> Thread 3 pushed 203 at 1521253087447ms.
-> Thread 7 pushed 105 at 1521253087447ms.
-> Thread 8 pushed 68 at 1521253087447ms.
-> Thread 6 pushed 286 at 1521253087447ms.
-> Thread 9 popped 253 at 1521253087447ms.
-> Thread 1 pushed 253 at 1521253087447ms.

-> Stack contents:
203
105
68
286
120

Runtime: 3ms.
```
<!-- ## Comparisons
The transition from lock based to lock free proved to show heavily improved success rates and stable execution times.

In a run with 10 threads, a push/pop operation on a _lock_ based stack had the following stats:
```
push() -> ~4ms execution, 30% success rate
pop() -> ~3ms execution, 70% success rate
```

In the same run with the _lock free_ stack, the statistics came out as follows:
```
push() -> ~3ms execution, 100% success rate
pop() -> ~3ms execution, 100% success rate
```

\*The above `pop()` operations were made consecutively on a stack pre-loaded with
enough nodes to successfully pop off. -->

## Performance

<center>
  ![Performance Graph](https://raw.githubusercontent.com/ditadewindita/COP4520/lock-free-stack/LockFreeStack/graph.png?token=AMmCeubrlR-o_yMvSvKqGe49dMytzfMhks5atdjUwA%3D%3D)
  _Graph of the operations of this lock-free stack accross 1, 2, 4, 8, 16, and 32 threads_
</center>

### Notes
A bank of pre-made nodes was used for this implementation due to chances of memory
protection occurrences. An ArrayList is made with `MAX_NUM_NODES` (initially at 1000) nodes with `null` values. The stack's `push()` operation will grab an allocated node from the ArrayList and set it's value to the value passed into
the call to push.
