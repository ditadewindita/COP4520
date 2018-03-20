# Lock-Free Stack w/ RDCSS

A Lock-Free Stack implementation using Java's Atomics library. This stack uses
AtomicInteger and AtomicReference in place of mutexes, as well as RDCSS (Restricted Double
Compare Single Swap)

## Structure
Use the following to run the program:
```
$ javac LockFreeStack.java LockFreeStackRunner.java
$ java LockFreeStackRunner
```

## RDCSS
The RDCSS here only swaps values if both the original and expected value of address
1 equal and the original and expected of address 2. In that case, the memory location
in address 2 is updated to the new value wanted for address 2.
Using Descriptor objects, the RDCSS is performed in two parts: a compare and swap
to see if a descriptor object is still in play, and if it is, complete the remainder
of the CAS operation. Continue to do so until CAS() stops returning descriptors.

## RDCSSRead
RDCSSRead looks at a memory location and sees if it is an RDCSSDescriptor. If it is,
then complete its pending CAS operation and continue to check if its a descriptor.

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
manner, which satisfies linearizability's non-blocking property. By not having unexpected delays, some thread `x` does not block another thread `y` from making progress.

## Performance

<center>
  ![Performance Graph](https://raw.githubusercontent.com/ditadewindita/COP4520/rdcss/RDCSS/graph.png?token=AMmCer7ZSAbw3ebbN7weWuMxA7XvWOgtks5aubuTwA%3D%3D)
  _Graph of the operations of this lock-free stack accross 1, 2, 4, 8, 16, and 32 threads_
</center>
