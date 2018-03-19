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
  private static final int SIZE_OP = 2;
  private final int id;
  private int currOp;
  public AtomicReference<LockFreeStack<Integer>> stack;

  public LockFreeRunnable(LockFreeStack<Integer> stack, int id, int currOp) {
    this.stack = new AtomicReference<>(stack);
    this.id = id;
    this.currOp = currOp;
  }

  public void run() {
    int operation = this.currOp; // (int)(Math.random() * 3);

    if(operation == PUSH_OP) {
      int randomNum = LockFreeStackRunner.getRandomNumber();
      boolean push = stack.get().push(randomNum);
      // long time = System.nanoTime();
      //
      // if(push)
      //   System.out.println("-> Thread " + this.id + " pushed " + randomNum + " at " + time + "ms.");
      // else
      //   System.out.println("-> Thread " + this.id + " failed call to push() " + " at " + time + "ms.");
    }
    else if(operation == POP_OP){
      Integer pop = stack.get().pop();
      // long time = System.nanoTime();
      //
      // if(pop != null)
      //   System.out.println("-> Thread " + this.id + " popped " + pop + " at " + time + "ms.");
      // else
      //   System.out.println("-> Thread " + this.id + " failed call to pop()" + " at " + time + "ms.");
    }
    else {
      int size = stack.get().size();
    }
  }
}

public class LockFreeStackRunner<T> {
  public static final int MIN_NUM = 0;
  public static final int MAX_NUM = 300;
  public static final int NUM_THREADS = 32;
  public static final int MAX_NUM_NODES = 1000;

  public static int getRandomNumber() {
    return ThreadLocalRandom.current().nextInt(MIN_NUM, MAX_NUM);
  }

  public static void main(String[] args) throws Exception {
    Thread threads[] = new Thread[NUM_THREADS];
    LockFreeStack<Integer> lockFreeStack = new LockFreeStack<>();
    int currNumThreads = 1;

    // Filenames
    String files[] = {"push.csv", "pop.csv", "size.csv"};

    for(int k = 0; k < files.length; k++) {
      // Create a new file if needed
      File newFile = new File(files[k]);
      newFile.createNewFile();

      // Set headers
      PrintWriter pw = new PrintWriter(newFile);
      StringBuilder sb = new StringBuilder();
      sb.append("numThreads");
      sb.append(',');
      sb.append("executionTime");
      sb.append('\n');

      // If it is a pop or size operation, preload the stack with nodes
      if(k == 1 || k == 2) {
        for(int j = 0; j < MAX_NUM_NODES; j++) {
          lockFreeStack.push(getRandomNumber());
        }
      }

      for(int j = currNumThreads; j <= NUM_THREADS; j *= 2) {
        long start = System.nanoTime();

        for(int i = 0; i < j; i++) {
          threads[i] = new Thread(new LockFreeRunnable(lockFreeStack, i, k));
          threads[i].start();
        }

        for(int i = 0; i < j; i++) {
          threads[i].join();
        }

        long stop = System.nanoTime();

        sb.append(j);
        sb.append(',');
        sb.append((double)(stop - start) / 1000000.0);
        sb.append('\n');
      }

      pw.write(sb.toString());
      pw.close();
      System.out.println("Finished writing " + files[k]);
    }

    // System.out.println();
    // lockFreeStack.printStack();
    //
    // System.out.println("\nRuntime: " + (stop - start) + "ms.");
  }
}
