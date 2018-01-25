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

// SAME TWO PHILOSOPHERS ALWAYS EATING

// NOTES:
// Method to make program wait - for eating/thinking 'a while'
// ith philosophers have left chopstick (i) and right chopstick (i + 1)
// Lock mutex when chopstick is 'picked' up
// MAKE MUTEXES BEFORE PHILOSOPHERS
// Deadlock when philosophers try to take same chopstick
// Starvation when some number of philosophers keep eating and not letting
//  others eat
// Each philosopher eats, pickup chopstick, thinks, put down chopsticks
// mutex.try_lock

// think -> eat -> wait

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
  string p_out = "Philosopher " + to_string(id) + " is thinking.\n";
  cout << p_out;

  wait();
}

void Philosopher::eat() {
  // Only eat if left chopstick can be picked up. If the left one cannot be
  // picked up, then the right one cannot either since both need to be active
  // to reach an EATING state

  // Pickup chopstick in an order based on the philosopher ID to prevent deadlock
  if(id % 2 != 0) {
    while(!left_chopstick->pickUp())
      wait();

    while(!right_chopstick->pickUp())
      wait();
  }
  else {
    while(!right_chopstick->pickUp())
      wait();

    while(!left_chopstick->pickUp())
      wait();
  }

  string p_out;
  p_out = "Philosopher " + to_string(id) + " is eating.\n";
  cout << p_out;

  wait();
}

void Philosopher::wait() {
  this_thread::sleep_for(chrono::milliseconds(WAIT_TIME));
}

void think_and_eat(atomic<bool> &stop, Philosopher *p) {
  while(!stop) {
    p->eat();

    p->left_chopstick->putDown();
    p->right_chopstick->putDown();

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

  // Uncomment if philosophers will have probabilistic hunger/thinking
  //srand(std::time(NULL));

  // Initialize chopsticks
  for(int i = 0; i < NUM_CHOPSTICKS; i++)
    chopsticks.push_back(new Chopstick(i, new mutex()));

  // Initialize philosophers and  assign them their neighboring chopsticks
  for(int i = 0; i < NUM_PHILOSOPHERS; i++)
    philosophers.push_back(new Philosopher(i, chopsticks[i], chopsticks[(i + 1) % NUM_PHILOSOPHERS]));

  for(int i = 0; i < NUM_PHILOSOPHERS; i++)
    threads.push_back(new thread(think_and_eat, ref(stop), philosophers[i]));

  while((c = getchar()) != 'n');

  stop = true;

  for(int i = 0; i < NUM_PHILOSOPHERS; i++)
    threads[i]->join();

}
