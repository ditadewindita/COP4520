// Haerunnisa Dewindita
// COP4520, Spring 2018
// HA990936

import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.lang.*;
import java.io.*;

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

    // While the head pointer of the stack is something we do not expect, keep
    // trying to push
    while(true) {
      currHead = this.head.get();
      newNode.next = currHead;
      numOps.getAndIncrement();

      // If the stack's current head is something we expect, then swap and
      // complete the push operation
      if(head.compareAndSet(currHead, newNode))
        break;
    }

    return true;
  }

  public T pop() {
    Node<T> currHead;
    Node<T> newHead;

    // While we're accessing a head that's not expected, keep trying!
    while(true) {
      currHead = this.head.get();

      // If stack is empty, leave it be!
      if(currHead == null)
        return null;

      newHead = currHead.next;
      numOps.getAndIncrement();

      // If the head is something we expected, then swap the nodes and complete
      // the operation
      if(head.compareAndSet(currHead, newHead))
        break;
    }

    return currHead.getValue();
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
