# Lock-Free Stack

A Lock-Free Stack implementation using Java's Atomics library. This stack uses
AtomicInteger and AtomicReference in place of mutexes.

## Atomic vs. Volatile
The use of atomic, volatile, or both types were considered to make this stack lock free.
Atomic variables are able to do operations atomically, while volatile types ensure that
all threads are reading the most up-to-date copy of a variable. However, volatile variables are not by default atomic.

This implementation uses atomic types over volatiles because Java's Atomics library offers
an atomic compare and set. By comparing the nodes in the stack, wether it be during a push
or a pop, and being able to swap them at the correct time in one atomic instruction, the
operation will only proceed if it sees a head pointer it is expecting. The `compareAndSet()`
checks the current head of the stack and compares it with the expected head node, and if it
is indeed the same (meaning no other thread had modified the stack), it will 'swap' the nodes respective to the type of instruction (push or pop).

If the stack did not use atomics or volatiles, different threads could read different values
from the stack and execute their code based on a false-positive. It could also do node swaps while another thread is swapping, and have the stack corrupted.

## Linearizability
Briefly discuss the correctness (i.e. linearizability) and progress (i.e. lock-freedom) properties of your modified lock-free stack.

The linearizability of this lock-free stack depends on the atomic variables within it.
Since the stack uses an atomic `compareAndSet()` (an \*instantaneous operation) before switching nodes, if another thread modifies the same memory location the current thread is
handling, the method call is guaranteed to fail. Therefore, its execution is not interrupted
halfway since the instruction is atomic. The shared objects in the stack are accessed \*instantaneously, so its linearizability is proven by having the operations complete in a reasonable and expected manner.

\*seemingly instantaneous *enough*, since things are not ~actually~ done in instantaneously
in the computer ;)

## Structure

This stack has two separate files: `LockFreeStack.java` and `LockFreeStackRunner.java`.

`LockFreeStack.java` includes a basic `Node` class that takes in a generic type `T` and
the `LockFreeStack` class, which also holds generic types.

The `LockFreeStackRunner.java` bears lots of goodies! It includes a `LockFreeRunnable` that implements `Runnable` with a custom `run()` method that randomizes between push and pop operations on the `LockFreeStack` that holds random integers. With each operation, a print
statement is outputted with failure/success notices and the time the operation took place.
The stack's contents is also printed at the end of all the thread executions, along with
the runtime in milliseconds.

Use the following to run the program:
```
$ javac LockFreeStack.java LockFreeStackRunner.java
$ java LockFreeStackRunner
```
## Comparisons
The transition from lock based to lock free proved to show heavily improved success rates.
Although the execution time bumped up with the lock free implementation, it was only by less than a handful of milliseconds.

In an run with 10 threads, a push/pop operation on a lock based stack had the following stats:
```
push() -> ~4ms execution, 30% success rate
pop() -> ~3ms execution, 70% success rate
```

In the same run with the lock free stack, the statistics came out as follows:
```
push() -> ~5.4ms execution, 100% success rate
pop() -> ~6.4ms execution, 100% success rate
```

*The above pop() operations were made consecutively on a stack pre-loaded with
enough nodes to pop off.
