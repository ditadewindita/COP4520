// Haerunnisa Dewindita
// COP4520, Spring 2018
// HA990936

// Wish the stack was done in C++ in the beginning :(. Got stuck on updating
// memory locations since Java is pass-by-value.

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

class RDCSSDescriptor<T> {
  AtomicInteger a1;
  Integer o1;
  AtomicReference<Node<T>> a2;
  Node<T> o2;
  Node<T> n2;
  boolean complete;

  public RDCSSDescriptor(AtomicInteger a1, Integer o1, AtomicReference<Node<T>> a2, Node<T> o2, Node<T> n2) {
    this.a1 = a1;
    this.o1 = o1;
    this.a2 = a2;
    this.o2 = o2;
    this.n2 = n2;
    this.complete = false;
  }
}

public class LockFreeStack<T> {
  AtomicReference<Node<T>> head;
  AtomicInteger size;
  AtomicInteger numOps;
  // AtomicReference<Descriptor<T>> desc;

  // Node bank related data structures
  AtomicInteger newNodeIndex;
  ArrayList<Node<T>> nodeBank;

  public LockFreeStack() {
    head = new AtomicReference<>();
    numOps = new AtomicInteger(0);
    size = new AtomicInteger(0);
    // desc = new AtomicReference<>(new Descriptor<>(0, null));

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

  public RDCSSDescriptor<T> RDCSS(RDCSSDescriptor<T> currDesc) {
    RDCSSDescriptor<T> r;

    do {
      r = CAS(currDesc, currDesc.a2, currDesc.o2, currDesc.n2);
      if(r.complete)
        completeDesc(r);
    } while(r.complete);

    if(r.o2 == currDesc.o2)
      completeDesc(currDesc);

    return r;
  }

  public RDCSSDescriptor<T> RDCSSRead(RDCSSDescriptor<T> currDesc) {

    do {
      if(currDesc.complete)
        completeDesc(currDesc);
    } while(currDesc.complete);

    return currDesc;
  }

  public void completeDesc(RDCSSDescriptor<T> currDesc) {
    if(currDesc.a1.compareAndSet(currDesc.o1, currDesc.a1.get())){
      CAS(currDesc, currDesc.a2, currDesc.o2, currDesc.n2);
      // currDesc.a1.getAndIncrement();
      currDesc.complete = false;
    }
    else
      CAS(currDesc, currDesc.a2, currDesc.o2, currDesc.o2);
  }

  public RDCSSDescriptor<T> CAS(RDCSSDescriptor<T> desc, AtomicReference<Node<T>> addr, Node<T> oldVal, Node<T> newVal) {
    if(addr.compareAndSet(oldVal, newVal)) {
      desc.complete = true;
    }

  return desc;
  }

  public boolean push(T x) {
    Node<T> currHead;
    Integer currSize;

    RDCSSDescriptor<T> newDesc;

    // Get a fresh new node and assign it with the value passed
    Node<T> newNode = getNewNode();
    newNode.setValue(x);

    // While the head pointer of the stack is something we do not expect, keep
    // trying to push
    do {
      currSize = size.get();

      // Signify new head for push()
      currHead = head.get();
      newNode.next = currHead;

      numOps.getAndIncrement();

      newDesc = new RDCSSDescriptor<T>(size, currSize, this.head, currHead, newNode);

    } while((RDCSS(newDesc)).a2.get() != this.head.get());

    return true;
  }

  public T pop() {
    Node<T> currHead;
    Node<T> newHead;
    Integer currSize;

    RDCSSDescriptor<T> newDesc;

    // While we're accessing a head that's not expected, keep trying!
    do {
      currSize = size.get();
      currHead = head.get();

      // If stack is empty, leave it be!
      if(currHead == null)
        return null;

      // Signify new head after pop()
      newHead = currHead.next;

      numOps.getAndIncrement();

      newDesc = new RDCSSDescriptor<T>(size, currSize, this.head, currHead, newHead);

    } while((RDCSS(newDesc)).a2.get() != this.head.get());

    return currHead.getValue();
  }

  public int getNumOps() {
    return numOps.get();
  }

  public int size() {
    return size.get();
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
