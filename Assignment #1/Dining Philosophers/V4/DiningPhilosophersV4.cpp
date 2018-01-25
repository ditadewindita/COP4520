// Haerunnisa Dewindita
// COP4520
// HA990936

#include <iostream>
#include <thread>
#include <mutex>
#include <chrono>
#include <string>
#include <vector>
#include <queue>
#include "DiningPhilosophers.h"

#define DEFAULT_NUM_PHILOSOPHERS 5
#define DEFAULT_NUM_CHOPSTICKS 5
#define THINK_TIME 200
#define EAT_TIME 150
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
  state = THINKING;
  string p_out = "Philosopher " + to_string(id) + " is thinking.\n";
  cout << p_out;

  wait(THINK_TIME);
}

void Philosopher::eat() {

  // Pickup chopstick in an order based on the philosopher ID to prevent deadlock,
  // and while they cannot pickup chopsticks, have them wait
  if(id % 2 != 0) {
    while(!left_chopstick->pickUp())
      wait(WAIT_TIME);

    while(!right_chopstick->pickUp())
      wait(WAIT_TIME);
  }
  else {
    while(!right_chopstick->pickUp())
      wait(WAIT_TIME);

    while(!left_chopstick->pickUp())
      wait(WAIT_TIME);
  }

  state = EATING;
  foodWaitTime = 0;

  string p_out;
  p_out = "Philosopher " + to_string(id) + " is eating.\n";
  cout << p_out;

  wait(EAT_TIME);
}

void Philosopher::hungry() {
  state = HUNGRY;

  string p_out = "Philosopher " + to_string(id) + " is hungry.\n";
  cout << p_out;
}

// Generic wait call for a philosopher
void Philosopher::wait(int wait_time) {
  this_thread::sleep_for(chrono::milliseconds(wait_time));
}

bool can_eat(Philosopher *p, Philosopher *left_p, Philosopher *right_p) {
  // If either neighbor is eating, current philosopher cannot eat
  if(left_p->state == EATING || right_p->state == EATING)
    return 0;

  // If left philosopher is hungry and has been waiting wayyy more than the current
  // philosopher, he cannot eat yet
  if(left_p->state == HUNGRY && left_p->foodWaitTime > (p->foodWaitTime * 5))
    return 0;

  // Same as above, but in comparison to the right philosopher
  if(right_p->state == HUNGRY && right_p->foodWaitTime > (p->foodWaitTime * 5))
    return 0;

  return 1;
}

void think_and_eat(atomic<bool> &stop, Philosopher *p, Philosopher *left_p, Philosopher *right_p) {
  while(!stop) {

    while(!can_eat(p, left_p, right_p)) {
      p->wait(WAIT_TIME);
      p->foodWaitTime += WAIT_TIME;
    }

    p->eat();

    p->left_chopstick->putDown();
    p->right_chopstick->putDown();

    p->think();

    p->hungry();
  }
}

int main(int argc, char **argv) {
  int num_philosophers = (argc < 2) ? DEFAULT_NUM_PHILOSOPHERS : atoi(argv[1]);
  int num_chopsticks = (argc < 2) ? DEFAULT_NUM_CHOPSTICKS : num_philosophers;

  vector<Philosopher*> philosophers;
  vector<Chopstick*> chopsticks;
  vector<thread*> threads;
  atomic<bool> stop(false);
  char c;

  // Initialize chopsticks
  for(int i = 0; i < num_chopsticks; i++)
    chopsticks.push_back(new Chopstick(i, new mutex()));

  // Initialize philosophers and  assign them their neighboring chopsticks
  for(int i = 0; i < num_philosophers; i++)
    philosophers.push_back(new Philosopher(i, chopsticks[i], chopsticks[(i + 1) % num_philosophers]));

  Philosopher *last_p = philosophers[num_philosophers - 1];

  for(int i = 0; i < num_philosophers; i++) {
    threads.push_back(new thread(think_and_eat, ref(stop), philosophers[i], last_p, philosophers[(i + 1) % num_philosophers]));
    last_p = philosophers[i];
  }

  while((c = getchar()) != 'n');
  stop = true;

  for_each(threads.begin(), threads.end(), mem_fn(&thread::join));
}
