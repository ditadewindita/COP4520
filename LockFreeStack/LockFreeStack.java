// Haerunnisa Dewindita
// COP4520, Spring 2018
// HA990936

import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.lang.*;
import java.io.*;
import java.util.*;

class Node<T> {
  T val;
  Node<T> next;

  public Node(T val) {
    this.val = val;
  }

  public T getValue() {
    return this.val;
  }

  public void setValue(T x) {
    this.val = x;
  }
}

public class LockFreeStack<T> {
  AtomicReference<Node<T>> head;
  AtomicInteger numOps;

  // Node bank related data structures
  AtomicInteger newNodeIndex;
  ArrayList<Node<T>> nodeBank;

  public LockFreeStack() {
    head = new AtomicReference<Node<T>>();
    numOps = new AtomicInteger(0);
    newNodeIndex = new AtomicInteger(0);
    nodeBank = new ArrayList<>(LockFreeStackRunner.MAX_NUM_NODES);

    // Fill up our array with a bunch of nodes!
    prepareNodeBank();
  }

  public Node<T> getNewNode() {
    return nodeBank.get(newNodeIndex.getAndIncrement() % LockFreeStackRunner.MAX_NUM_NODES);
  }

  // Method to prepare a bunch of new nodes with NULL values to avoid 'dealing'
  // with memory protection
  public void prepareNodeBank() {
    for(int i = 0; i < LockFreeStackRunner.MAX_NUM_NODES; i++)
      nodeBank.add(new Node<>(null));
  }

  public boolean push(T x) {
    // Get a fresh new node and assign it with the value passed
    Node<T> newNode = getNewNode();
    newNode.setValue(x);

    Node<T> currHead;

    // While the head pointer of the stack is something we do not expect, keep
    // trying to push

    do {
      currHead = this.head.get();
      newNode.next = currHead;
    } while(!head.compareAndSet(currHead, newNode));

    return true;
  }

  public T pop() {
    Node<T> currHead;
    Node<T> newHead;

    // While we're accessing a head that's not expected, keep trying!
    do {
      currHead = this.head.get();

      // If stack is empty, leave it be!
      if(currHead == null)
        return null;

      newHead = currHead.next;
    } while(!head.compareAndSet(currHead, newHead));

    return currHead.getValue();
  }

  public int getNumOps() {
    return this.numOps.get();
  }

  public Node<T> getHead() {
    return this.head.get();
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
