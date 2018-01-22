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
// mutex.try_lock

// think -> eat -> wait

// CHOPSTICK DEFINITIONS
Chopstick::Chopstick(int temp_id, mutex *temp_chopstick) : id(temp_id), chopstick(temp_chopstick) {}

void Chopstick::putDown() {
  chopstick->unlock();
}

bool Chopstick::pickUp() {
  return chopstick->try_lock();
}

// PHILOSOPHER DEFINITIONS
Philosopher::Philosopher(int temp_id, int temp_state, Chopstick *temp_left_chopstick, Chopstick *temp_right_chopstick) :
  id(temp_id), state(temp_state), left_chopstick(temp_left_chopstick), right_chopstick(temp_right_chopstick) {}

void Philosopher::think() {
  string p_out = "Philosopher " + to_string(id) + " is thinking.\n";
  cout << p_out << flush;

  state = THINKING;
  left_chopstick->putDown();
  right_chopstick->putDown();
  this_thread::sleep_for(chrono::milliseconds(WAIT_TIME));
}

void Philosopher::eat() {
  // Only eat if left chopstick can be picked up. If the left one cannot be
  // picked up, then the right one cannot either since both need to be active
  // to reach an EATING state
  if(left_chopstick->pickUp())
    if(right_chopstick->pickUp()) {
      state = EATING;

      string p_out = "Philosopher " + to_string(id) + " is eating.\n";
      cout << p_out << flush;
    }
}

void Philosopher::wait() {
  state = WAITING;
  this_thread::sleep_for(chrono::milliseconds(WAIT_TIME));
}

void Philosopher::think_and_eat() {
  think();
  eat();
  wait();
}

int main(void) {
  char stop;
  vector<Philosopher*> philosophers;
  vector<Chopstick*> chopsticks;
  vector<thread*> threads;

  // Uncomment if philosophers will have probabilistic hunger/thinking
  //srand(std::time(NULL));
  // Initialize chopsticks
  for(int i = 0; i < NUM_CHOPSTICKS; i++)
    chopsticks.push_back(new Chopstick(i, new mutex()));

  // Initialize philosophers and assign them their neighboring chopsticks
  for(int i = 0; i < NUM_PHILOSOPHERS; i++)
    philosophers.push_back(new Philosopher(i, THINKING, chopsticks[i], chopsticks[(i + 1) % NUM_PHILOSOPHERS]));

  do {

    for(int i = 0; i < NUM_PHILOSOPHERS; i++)
      threads.push_back(new thread(&Philosopher::think_and_eat, philosophers[i]));

    if((stop = getchar()) != 'n')
      break;
      
  } while(1);

  for_each(threads.begin(), threads.end(), mem_fn(&thread::join));
}
