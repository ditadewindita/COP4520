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
    return val;
  }

  public void setValue(T x) {
    val = x;
  }
}

class Descriptor<T> {
  AtomicInteger size;
  WriteDescriptor<T> pending;

  public Descriptor(int size, WriteDescriptor<T> pending) {
    this.size = new AtomicInteger(size);
    this.pending = pending;
  }

  public int getSize() {
    return size.get();
  }
}

class WriteDescriptor<T> {
  Node<T> oldVal;
  Node<T> newVal;
  AtomicReference<Node<T>> currHead;
  AtomicBoolean completed;

  public WriteDescriptor(Node<T> oldVal, Node<T> newVal, Node<T> currHead) {
    this.oldVal = oldVal;
    this.newVal = newVal;
    this.currHead = new AtomicReference<>(currHead);
    this.completed = new AtomicBoolean(false);
  }

  public Node<T> getCurrHead() {
    return currHead.get();
  }

  public boolean getCompleted() {
    return completed.get();
  }

  public void setCompleted(boolean bool) {
    completed.set(bool);
  }
}

public class LockFreeStack<T> {
  AtomicReference<Node<T>> head;
  AtomicInteger numOps;
  AtomicReference<Descriptor<T>> desc;

  // Node bank related data structures
  AtomicInteger newNodeIndex;
  ArrayList<Node<T>> nodeBank;

  public LockFreeStack() {
    head = new AtomicReference<>();
    numOps = new AtomicInteger(0);
    desc = new AtomicReference<>(new Descriptor<>(0, null));

    // Node bank related initialization
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
    Descriptor<T> currDesc;
    Descriptor<T> nextDesc;
    WriteDescriptor<T> writeOp;

    // While the head pointer of the stack is something we do not expect, keep
    // trying to push
    do {
      currDesc = desc.get();
      completeWrite(currDesc.pending);

      currHead = head.get();
      newNode.next = currHead;

      writeOp = new WriteDescriptor<>(currHead, newNode, head.get());
      nextDesc = new Descriptor<>(currDesc.getSize() + 1, writeOp);

      numOps.getAndIncrement();

    } while(!desc.compareAndSet(currDesc, nextDesc));

    completeWrite(nextDesc.pending);

    return true;
  }

  public T pop() {
    Node<T> currHead;
    Node<T> newHead;
    Descriptor<T> currDesc;
    Descriptor<T> nextDesc;
    WriteDescriptor<T> writeOp;

    // While we're accessing a head that's not expected, keep trying!
    do {
      currDesc = desc.get();
      completeWrite(currDesc.pending);

      currHead = head.get();

      // If stack is empty, leave it be!
      if(currHead == null)
        return null;

      newHead = currHead.next;
      writeOp = new WriteDescriptor<>(currHead, newHead, head.get());
      nextDesc = new Descriptor<>(currDesc.getSize() - 1, writeOp);

      numOps.getAndIncrement();

    } while(!desc.compareAndSet(currDesc, nextDesc));

    completeWrite(nextDesc.pending);

    return currHead.getValue();
  }

  public void completeWrite(WriteDescriptor<T> writeOp) {
    if(writeOp != null && !writeOp.getCompleted()) {
      // ?
      head.compareAndSet(writeOp.oldVal, writeOp.newVal);
      writeOp.setCompleted(true);
    }
  }

  public int getNumOps() {
    return numOps.get();
  }

  public void printStack() {
    Node<T> temp = head.get();

    System.out.println("-> Stack contents:");

    if(temp == null)
      System.out.println("EMPTY");

    while(temp != null) {
      System.out.println(temp.getValue());
      temp = temp.next;
    }
  }
}
