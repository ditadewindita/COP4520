// Haerunnisa Dewindita
// COP4520, Spring 2018
// HA990936

import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.lang.*;
import java.io.*;
import java.util.*;

// Current Runnable implements the stack to hold INTEGERS and randomizes the data
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
    int operation = (int)(Math.random() * 2);

    if(operation == PUSH_OP) {
      // Since this code is running with a preloaded bank of nodes,
      // it doesn't matter what number is pushed onto the stack in this
      // call because getNewNode() is the method dictating the value
      // of our nodes!
      int randomNum = LockFreeStackRunner.getRandomNumber();
      boolean push = stack.get().push(randomNum);
      long time = System.currentTimeMillis();

      if(push)
        System.out.println("-> Thread " + this.id + " pushed " + randomNum + " at " + time + "ms.");
      else
        System.out.println("-> Thread " + this.id + " failed call to push() " + " at " + time + "ms.");
    }
    else {
      Integer pop = stack.get().pop();
      long time = System.currentTimeMillis();

      if(pop != null)
        System.out.println("-> Thread " + this.id + " popped " + pop + " at " + time + "ms.");
      else
        System.out.println("-> Thread " + this.id + " failed call to pop()" + " at " + time + "ms.");
    }
  }
}

public class LockFreeStackRunner<T> {
  public static final int MIN_NUM = 0;
  public static final int MAX_NUM = 300;
  public static final int NUM_THREADS = 10;
  public static final int MAX_NUM_NODES = 1000;

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
