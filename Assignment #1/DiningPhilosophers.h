#ifndef DININGPHILOSOPHERS_H
#define DININGPHILOSOPHERS_H

#include <thread>
#include <mutex>

// Philosopher states
#define THINKING 0
#define EATING 1
#define WAITING 2

using namespace std;

class Chopstick {
  public:
    int id;
    mutex *chopstick;

    Chopstick(int id, mutex *chopstick);

    void putDown();

    bool pickUp();
};

class Philosopher {
  public:
    int id;
    int state;
    Chopstick *left_chopstick;
    Chopstick *right_chopstick;

    Philosopher(int id, int state, Chopstick *left_chopstick, Chopstick *right_chopstick);

    void think();

    void eat();

    void wait();

    void think_and_eat();
};

#endif
