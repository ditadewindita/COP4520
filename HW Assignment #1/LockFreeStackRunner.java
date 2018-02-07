// Haerunnisa Dewindita
// COP4520, Spring 2018
// HA990936

import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.lang.*;
import java.io.*;

// PUSH
// - acquires lock
// - allocate new node n w/ given x value
// - n->next = head
// - head = n->next
// - numOps++

// POP
// - acquires lock
// - if empty stack, release lock and return null
// - retireve head
// - head = n->next
// - numOps++

// compare and swap instruction needed so can atomically read and write
// atomic reference cause thread-safe
// volatile = makes sure all threads refer to the master copy of a variable

// Volatile variables share the visibility features of synchronized, but none
// of the atomicity features. This means that threads will automatically see the
// most up-to-date value for volatile variables. They can be used to provide
// thread safety, but only in a very restricted set of cases: those that do not
// impose constraints between multiple variables or between a variable's current
// value and its future values.

class Node<T> {
  T val;
  Node<T> next;

  public Node(T val) {
    this.val = val;
  }

  public T getValue() {
    return this.val;
  }
}

class LockFreeStack<T> {
  AtomicReference<Node<T>> head;
  AtomicInteger numOps;

  public LockFreeStack() {
    head = new AtomicReference<Node<T>>();
    numOps = new AtomicInteger(0);
  }

  public boolean push(T x) {
    Node<T> newNode = new Node<>(x);
    Node<T> currHead;

    do {
      currHead = this.head.get();
      newNode.next = currHead;

      numOps.getAndIncrement();
    } while(!head.compareAndSet(currHead, newNode));

    return true;
  }

  public T pop() {
    Node<T> currHead;
    Node<T> newHead;

    do {
      currHead = this.head.get();

      if(currHead == null)
        return null;

      newHead = currHead.next;
      numOps.getAndIncrement();

    } while(!head.compareAndSet(currHead, newHead));

    return currHead.val;
  }

  public int getNumOps() {
    return this.numOps.get();
  }

  public void printStack() {
    Node<T> temp = this.head.get();

    System.out.println("-> Stack contents:");

    if(temp == null)
      System.out.println("EMPTY");

    while(temp != null) {
      System.out.println(temp.getValue());
      temp = temp.next;
    }
  }
}

// Current Runnable implements the stack to hold INTEGERS, and prints out
// data to keep track of the stack's activities
class LockFreeRunnable implements Runnable {
  private static final int PUSH_OP = 0;
  private static final int POP_OP = 1;
  private final int id;
  public AtomicReference<LockFreeStack<Integer>> stack;

  public LockFreeRunnable(LockFreeStack<Integer> stack, int id) {
    this.stack = new AtomicReference<>(stack);
    this.id = id;
  }

  public void run() {
    int randomNum = LockFreeStackRunner.getRandomNumber();
    int operation = (int)(Math.random() * 2);

    if(operation == PUSH_OP) {
      boolean push = stack.get().push(randomNum);

      if(push)
        System.out.println("-> Thread " + this.id + " pushed " + randomNum + ".");
      else
        System.out.println("-> Thread " + this.id + " failed call to push().");
    }
    else {
      Integer pop = stack.get().pop();

      if(pop != null)
        System.out.println("-> Thread " + this.id + " popped " + pop + ".");
      else
        System.out.println("-> Thread " + this.id + " failed call to pop().");
    }
  }
}
// LOCKS
// PUSH
// 10 threads
// avg 4 ms
// avg 3 items successfully pushed

// POP
// 10 threads
// avg 3.3333ms
// avg 7 items successfully popped

// LOCK-FREE
// PUSH
// 10 threads
// avg 5.4 ms
// avg 10 items successfully pushed

// POP
// 10 threads
// avg 6.4 ms
// avg 10 items successfully popped

public class LockFreeStackRunner {
  public static final int MIN_NUM = 0;
  public static final int MAX_NUM = 300;
  public static final int NUM_THREADS = 10;

  public static int getRandomNumber() {
    return ThreadLocalRandom.current().nextInt(MIN_NUM, MAX_NUM);
  }

  public static void main(String[] args) throws Exception {
    Thread threads[] = new Thread[NUM_THREADS];
    LockFreeStack<Integer> lockFreeStack = new LockFreeStack<>();

    long start = System.currentTimeMillis();

    for(int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new Thread(new LockFreeRunnable(lockFreeStack, i));
      threads[i].start();
    }

    for(int i = 0; i < NUM_THREADS; i++) {
      threads[i].join();
    }

    long stop = System.currentTimeMillis();

    lockFreeStack.printStack();

    System.out.println("Runtime: " + (stop - start) + "ms.");
  }
}
