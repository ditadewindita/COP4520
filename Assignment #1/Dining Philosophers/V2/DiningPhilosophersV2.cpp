// Haerunnisa Dewindita
// COP4520
// HA990936

#include <iostream>
#include <thread>
#include <mutex>
#include <chrono>
#include <string>
#include <vector>
#include "DiningPhilosophers.h"

#define NUM_PHILOSOPHERS 5
#define NUM_CHOPSTICKS 5
#define WAIT_TIME 100

// CHOPSTICK DEFINITIONS
Chopstick::Chopstick(int temp_id, mutex *temp_chopstick) : id(temp_id), chopstick(temp_chopstick) {}

Chopstick::~Chopstick() {
  delete chopstick;
}

void Chopstick::putDown() {
  chopstick->unlock();
}

bool Chopstick::pickUp() {
  return chopstick->try_lock();
}

// PHILOSOPHER DEFINITIONS
Philosopher::Philosopher(int temp_id, Chopstick *temp_left_chopstick, Chopstick *temp_right_chopstick) :
  id(temp_id), left_chopstick(temp_left_chopstick), right_chopstick(temp_right_chopstick) {}

Philosopher::~Philosopher() {
  delete left_chopstick;
  delete right_chopstick;
}

void Philosopher::think() {
  wait();
}

void Philosopher::eat() {

  // Pick up the lower numbered chopstick first as to not deadlock
  if(left_chopstick->id < right_chopstick->id) {

    while(!left_chopstick->pickUp());
    while(!right_chopstick->pickUp());

    string p_out;
    p_out = "Philosopher " + to_string(id) + " is eating.\n";
    cout << p_out;

    right_chopstick->putDown();
    left_chopstick->putDown();

    p_out = "Philosopher " + to_string(id) + " is thinking.\n";
    cout << p_out;
  }

  if(right_chopstick->id < left_chopstick->id) {

    while(!right_chopstick->pickUp());
    while(!left_chopstick->pickUp());

    string p_out;
    p_out = "Philosopher " + to_string(id) + " is eating.\n";
    cout << p_out;

    left_chopstick->putDown();
    right_chopstick->putDown();

    p_out = "Philosopher " + to_string(id) + " is thinking.\n";
    cout << p_out;
  }

  wait();
}

void Philosopher::wait() {
  this_thread::sleep_for(chrono::milliseconds(WAIT_TIME));
}

void think_and_eat(atomic<bool> &stop, Philosopher *p) {
  while(!stop) {
    p->eat();

    p->think();

    string p_out = "Philosopher " + to_string(p->id) + " is hungry.\n";
    cout << p_out;
  }
}

int main(void) {
  vector<Philosopher*> philosophers;
  vector<Chopstick*> chopsticks;
  vector<thread*> threads;
  atomic<bool> stop(false);
  char c;

  // Initialize chopsticks
  for(int i = 0; i < NUM_CHOPSTICKS; i++)
    chopsticks.push_back(new Chopstick(i, new mutex()));

  // Initialize philosophers and  assign them their neighboring chopsticks
  for(int i = 0; i < NUM_PHILOSOPHERS; i++)
    philosophers.push_back(new Philosopher(i, chopsticks[i], chopsticks[(i + 1) % NUM_PHILOSOPHERS]));

  for(int i = 0; i < NUM_PHILOSOPHERS; i++)
    threads.push_back(new thread(think_and_eat, ref(stop), philosophers[i]));

  // Listen on input
  while((c = getchar()) != 'n');
  stop = true;

  // Join threads to end them
  for_each(threads.begin(), threads.end(), mem_fn(&thread::join));

}
