import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.lang.*;
import java.io.*;

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

public class LockFreeStack<T> {
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
