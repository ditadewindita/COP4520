#ifndef DININGPHILOSOPHERS_H
#define DININGPHILOSOPHERS_H

#include <thread>
#include <mutex>

// Philosopher states
#define THINKING 0
#define EATING 1
#define HUNGRY 2

using namespace std;

class Chopstick {
  public:
    int id;
    mutex *chopstick;

    Chopstick(int id, mutex *chopstick);

    ~Chopstick();

    void putDown();

    bool pickUp();
};

class Philosopher {
  public:
    int id;
    int state;
    int currNum;
    long foodWaitTime;
    Chopstick *left_chopstick;
    Chopstick *right_chopstick;

    Philosopher(int id, Chopstick *left_chopstick, Chopstick *right_chopstick);

    ~Philosopher();

    void think();

    void eat();

    void wait(int wait_time);

    void hungry();

};

class Counter {
  public:
    long count;
    mutex *lock;

    Counter(mutex *lock);

    long getAndIncrement();
};

#endif
