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

// NOTES:
// Method to make program wait - for eating/thinking 'a while'
// ith philosophers have left chopstick (i) and right chopstick (i + 1)
// Lock mutex when chopstick is 'picked' up
// MAKE MUTEXES BEFORE PHILOSOPHERS
// Deadlock when philosophers try to take same chopstick
// Starvation when some number of philosophers keep eating and not letting
//  others eat
// Each philosopher eats, pickup chopstick, thinks, put down chopsticks

// think -> eat -> wait

// CHOPSTICK DEFINITIONS
Chopstick::Chopstick(int temp_id, mutex *temp_chopstick) : id(temp_id), chopstick(temp_chopstick) {}

void Chopstick::putDown() {
  chopstick->unlock();
}

void Chopstick::pickUp() {
  chopstick->lock();
}

// PHILOSOPHER DEFINITIONS
Philosopher::Philosopher(int temp_id, int temp_state, Chopstick *temp_left_chopstick, Chopstick *temp_right_chopstick) :
  id(temp_id), state(temp_state), left_chopstick(temp_left_chopstick), right_chopstick(temp_right_chopstick) {}

void Philosopher::think() {
  cout << "Philosopher " << id << " is thinking.\n";
  state = THINKING;
  left_chopstick->putDown();
  right_chopstick->putDown();
  wait();
}

void Philosopher::eat() {
  left_chopstick->pickUp();
  right_chopstick->pickUp();
  state = EATING;
  cout << "Philosopher " << id << " is eating.\n";
}

void Philosopher::wait() {
  state = WAITING;
  this_thread::sleep_for(chrono::milliseconds(WAIT_TIME));
}

int main(int argc, const char *argv[]) {
  string stop = "";
  vector<Philosopher*> philosophers;
  vector<Chopstick*> chopsticks;

  // Initialize chopsticks
  for(int i = 0; i < NUM_CHOPSTICKS; i++) {
    chopsticks[i] = new Chopstick(i, new mutex());
  }

  // Initialize philosophers and assign them their neighboring chopsticks
  for(int i = 0; i < NUM_PHILOSOPHERS; i++) {
    philosophers[i] = new Philosopher(i, THINKING, chopsticks[i], chopsticks[(i + 1) % NUM_PHILOSOPHERS]);
  }

  while(stop.compare("n") != 0) {

    cin >> stop;
  }
}
