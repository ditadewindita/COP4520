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

class LockFreeStack<T> {
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
    nodeBank = new ArrayList<>(EliminationBackoffStackRunner.MAX_NUM_NODES);

    // Fill up our array with a bunch of nodes!
    prepareNodeBank();
  }

  public Node<T> getNewNode() {
    return nodeBank.get(newNodeIndex.getAndIncrement() % EliminationBackoffStackRunner.MAX_NUM_NODES);
  }

  // Method to prepare a bunch of new nodes with NULL values to avoid 'dealing'
  // with memory protection
  public void prepareNodeBank() {
    for(int i = 0; i < LockFreeStackRunner.MAX_NUM_NODES; i++)
      nodeBank.add(new Node<>(null));
  }

  public boolean push(T x) {
    Node<T> currHead;
    Descriptor<T> currDesc;
    Descriptor<T> nextDesc;
    WriteDescriptor<T> writeOp;

    // Get a fresh new node and assign it with the value passed
    Node<T> newNode = getNewNode();
    newNode.setValue(x);

    // While the head pointer of the stack is something we do not expect, keep
    // trying to push
    do {
      // Complete any pending writes
      currDesc = desc.get();
      completeWrite(currDesc.pending);

      // Signify new head for push()
      currHead = head.get();
      newNode.next = currHead;

      // Create new WriteDescriptor to switch stack's head
      writeOp = new WriteDescriptor<>(currHead, newNode, head.get());

      // Increment size with write descriptor
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
      // Complete any pending writes
      currDesc = desc.get();
      completeWrite(currDesc.pending);

      currHead = head.get();

      // If stack is empty, leave it be!
      if(currHead == null)
        return null;

      // Signify new head after pop()
      newHead = currHead.next;

      // Create new WriteDescriptor to switch stack's head
      writeOp = new WriteDescriptor<>(currHead, newHead, head.get());

      // Decrement size with write descriptor
      nextDesc = new Descriptor<>(currDesc.getSize() - 1, writeOp);

      numOps.getAndIncrement();

    } while(!desc.compareAndSet(currDesc, nextDesc));

    // Compleye pending writes for newly made descriptor
    completeWrite(nextDesc.pending);

    return currHead.getValue();
  }

  public void completeWrite(WriteDescriptor<T> writeOp) {
    if(writeOp == null)
      return;

    // Perform CAS operation only if the write descriptor is pending
    if(!writeOp.getCompleted()) {
      head.compareAndSet(writeOp.oldVal, writeOp.newVal);

      // Reset completed boolean
      writeOp.setCompleted(true);
    }
  }

  public int getNumOps() {
    return numOps.get();
  }

  public int size() {
    Descriptor<T> currDesc = desc.get();

    if(currDesc.pending == null)
      return 0;

    // Take into account if there is a pending write operation
    if(!currDesc.pending.getCompleted())
      return currDesc.size.decrementAndGet();

    return currDesc.size.get();
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

class RangePolicy {
  int maxRange;
  int currentRange;

  public RangePolicy(int maxRange) {
    this.currentRange = 1;
    this.maxRange = maxRange;
  }

  public void recordEliminationSuccess() {
    if(currentRange < maxRange)
      currentRange++;
  }

  public void recordEliminationTimeout() {
    if(currentRange > 1)
      currentRange--;
  }

  public int getRange() {
    return currentRange;
  }
}

class LockFreeExchanger<T> {
  static final int EMPTY = 0;
  static final int WAITING = 1;
  static final int BUSY = 2;

  AtomicStampedReference<T> slot;

  public LockFreeExchanger() {
    this.slot = new AtomicStampedReference<T>(null, 0);
  }

  public T exchange(T myItem, long timeout, TimeUnit unit) throws TimeoutException {
    long nanos = unit.toNanos(timeout);
    long timeBound = System.nanoTime() + nanos;

    int[] stampHolder = {EMPTY};

    while(true) {
      if(System.nanoTime() > timeBound)
        throw new TimeoutException();

      T yrItem = slot.get(stampHolder);
      int stamp = stampHolder[0];

      switch(stamp) {
        case EMPTY:
          if(slot.compareAndSet(yrItem, myItem, EMPTY, WAITING)) {
            while(System.nanoTime() < timeBound) {
              yrItem = slot.get(stampHolder);

              if(stampHolder[0] == BUSY) {
                slot.set(null, EMPTY);

                return yrItem;
              }
            }

            if(slot.compareAndSet(myItem, null, WAITING, EMPTY)) {
              throw new TimeoutException();
            }
            else {
              yrItem = slot.get(stampHolder);
              slot.set(null, EMPTY);

              return yrItem;
            }
          }
          break;

        case WAITING:
          if(slot.compareAndSet(yrItem, myItem, WAITING, BUSY))
            return yrItem;
          break;

        case BUSY:
          break;

        default:
          break;
      }
    }
  }
}

class EliminationArray<T> {
  private static final int duration = 2;
  ArrayList<LockFreeExchanger<T>> exchanger;
  Random random;

  public EliminationArray(int capacity) {
    this.exchanger = new ArrayList<LockFreeExchanger<T>>(capacity);

    for(int i = 0; i < capacity; i++)
      exchanger.add(new LockFreeExchanger<T>());

    this.random = new Random();
  }

  public T visit(T value, int range) throws TimeoutException {
    int slot = random.nextInt(range);

    return exchanger.get(slot).exchange(value, duration, TimeUnit.MILLISECONDS);
  }
}

public class EliminationBackoffStack<T> extends LockFreeStack<T> {
  static final int capacity = EliminationBackoffStackRunner.MAX_NUM_NODES;

  EliminationArray<T> eliminationArray;
  AtomicInteger size;
  static ThreadLocal<RangePolicy> policy;

  public EliminationBackoffStack() {
    this.eliminationArray = new EliminationArray<T>(capacity);
    this.size = new AtomicInteger(0);
    this.policy = new ThreadLocal<RangePolicy>() {
      protected synchronized RangePolicy initialValue() {
        return new RangePolicy(capacity);
      }
    };
  }

  protected boolean tryPush(Node<T> node) {
    Node<T> currHead = head.get();
    node.next = currHead;

    return head.compareAndSet(currHead, node);
  }

  public boolean push(T x) {
    RangePolicy rangePolicy = policy.get();

    Node<T> node = getNewNode();
    node.setValue(x);

    while(true) {
      if(tryPush(node)) {
        size.getAndIncrement();
        return true;
      }
      else try {
        T otherValue = eliminationArray.visit(x, rangePolicy.getRange());

        if(otherValue == null) {
          rangePolicy.recordEliminationSuccess();
          size.getAndIncrement();

          return true;
        }
      } catch (TimeoutException ex) {
        rangePolicy.recordEliminationTimeout();
      }
    }
  }

  protected Node<T> tryPop() throws EmptyStackException {
    Node<T> currHead = head.get();

    if(currHead == null)
      throw new EmptyStackException();

    Node<T> newHead = currHead.next;

    if(head.compareAndSet(currHead, newHead))
      return currHead;

    return null;
  }

  public T pop() throws EmptyStackException {
    RangePolicy rangePolicy = policy.get();

    while(true) {
      Node<T> returnNode = tryPop();

      if(returnNode != null) {
        size.getAndDecrement();

        return returnNode.val;
      } else try {
        T otherValue = eliminationArray.visit(null, rangePolicy.getRange());

        if(otherValue != null) {
          rangePolicy.recordEliminationSuccess();
          size.getAndDecrement();

          return otherValue;
        }
      } catch(TimeoutException ex) {
        rangePolicy.recordEliminationTimeout();
      }
    }
  }

  public int size() {
    return size.get();
  }
}
