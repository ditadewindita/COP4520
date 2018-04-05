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
  public static final int PUSH_OP = 0;
  public static final int POP_OP = 1;
  public static final int SIZE_OP = 2;
  private final int id;
  // private int currOp;
  public AtomicReference<LockFreeStack<Integer>> stack;

  public LockFreeRunnable(LockFreeStack<Integer> stack, int id) {
    this.stack = new AtomicReference<>(stack);
    this.id = id;
    // this.currOp = currOp;
  }

  public void run() {
    int operation = (int)(Math.random() * 3);

    if(operation == PUSH_OP) {
      int randomNum = EliminationBackoffStackRunner.getRandomNumber();
      boolean push;
      long time;

      try {
        push = stack.get().push(randomNum);
        time = System.currentTimeMillis();

        if(push)
          System.out.println("-> Thread " + this.id + " pushed " + randomNum + " at " + time + "ms.");
        else
          System.out.println("-> Thread " + this.id + " failed call to push() " + " at " + time + "ms.");

      } catch(Exception ex) {
        time = System.currentTimeMillis();

        System.out.println("-> Thread " + this.id + " failed call to push() " + " at " + time + "ms.");
      }
    }
    else if (operation == POP_OP){
      Integer pop;
      long time;

      try {
        pop = stack.get().pop();
        time = System.currentTimeMillis();

        if(pop != null)
          System.out.println("-> Thread " + this.id + " popped " + pop + " at " + time + "ms.");
        else
          System.out.println("-> Thread " + this.id + " failed call to pop()" + " at " + time + "ms.");

      } catch(Exception ex) {
        time = System.currentTimeMillis();

        System.out.println("-> Thread " + this.id + " failed call to pop()" + " at " + time + "ms.");
      }

    }
    else {
      int size = stack.get().size();
      long time = System.currentTimeMillis();

      System.out.println("-> Thread " + this.id + " checked size as " + size + " at " + time + "ms.");
    }
  }
}

public class EliminationBackoffStackRunner<T> {
  public static final int MIN_NUM = 0;
  public static final int MAX_NUM = 300;
  public static final int NUM_THREADS = 32;
  public static final int MAX_NUM_NODES = 1000;
  public static final int LOCK_FREE_STACK = 0;
  public static final int ELIM_BACKOFF_STACK = 1;
  public static final int NUM_RUNS = 50;

  public static int getRandomNumber() {
    return ThreadLocalRandom.current().nextInt(MIN_NUM, MAX_NUM);
  }

  // Used for when creating CSV files to build performance graphs
  // public static StringBuilder runLockFreeStack(Thread[] threads, LockFreeStack<Integer> stack, StringBuilder sb, int currOp, int stackType) {
  //   // If it is a pop or size operation, preload the stack with nodes
  //   if(currOp == LockFreeRunnable.POP_OP || currOp == LockFreeRunnable.SIZE_OP) {
  //     for(int j = 0; j < MAX_NUM_NODES; j++) {
  //       stack.push(getRandomNumber());
  //     }
  //   }
  //
  //   for(int j = 1; j <= NUM_THREADS; j *= 2) {
  //     double totalTime = 0;
  //
  //     for(int l = 0; l < NUM_RUNS; l++) {
  //       long start = System.nanoTime();
  //
  //       for(int i = 0; i < j; i++) {
  //         threads[i] = new Thread(new LockFreeRunnable(stack, i, currOp));
  //         threads[i].start();
  //       }
  //
  //       for(int i = 0; i < j; i++) {
  //         try {
  //           threads[i].join();
  //         } catch(Exception ex) {
  //
  //         }
  //       }
  //
  //       long stop = System.nanoTime();
  //
  //       totalTime += (double)(stop - start) / 1000000.0;
  //     }
  //
  //     sb.append(j);
  //     sb.append(',');
  //     sb.append(totalTime / NUM_RUNS);
  //     sb.append(',');
  //     sb.append(stackType);
  //     sb.append('\n');
  //   }
  //
  //   return sb;
  // }

  public static void main(String[] args) throws Exception {
    Thread threads[] = new Thread[NUM_THREADS];
    // Thread threadsLockFree[] = new Thread[NUM_THREADS];
    LockFreeStack<Integer> eliminationBackoffStack = new EliminationBackoffStack<>();
    // LockFreeStack<Integer> lockFreeStack = new LockFreeStack<>();

    // Filenames
    // String files[] = {"push.csv", "pop.csv", "size.csv"};
    //
    // for(int k = 0; k < files.length; k++) {
    //   // Create a new file if needed
    //   File newFile = new File(files[k]);
    //   newFile.createNewFile();
    //
    //   // Set headers
    //   PrintWriter pw = new PrintWriter(newFile);
    //   StringBuilder sb = new StringBuilder();
    //   sb.append("numThreads");
    //   sb.append(',');
    //   sb.append("executionTime");
    //   sb.append(',');
    //   sb.append("stackType");
    //   sb.append('\n');

      // sb = runLockFreeStack(threadsLockFree, lockFreeStack, sb, k, LOCK_FREE_STACK);
      // sb = runLockFreeStack(threadsElim, eliminationBackoffStack, sb, k, ELIM_BACKOFF_STACK);

    //   pw.write(sb.toString());
    //   pw.close();
    //   System.out.println("Finished writing " + files[k]);
    // }

    long start = System.currentTimeMillis();

    for(int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new Thread(new LockFreeRunnable(eliminationBackoffStack, i));
      threads[i].start();
    }

    for(int i = 0; i < NUM_THREADS; i++) {
      try {
        threads[i].join();
      } catch(Exception ex) {

      }
    }

    long stop = System.currentTimeMillis();


    System.out.println();
    eliminationBackoffStack.printStack();

    System.out.println("\nRuntime: " + (stop - start) + "ms.");
  }
}
