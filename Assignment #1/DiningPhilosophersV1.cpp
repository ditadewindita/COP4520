#include <iostream>
#include <thread>
#include <mutex>
#include <string>

#define NUM_PHILOSOPHERS 5
#define NUM_CHOPSTICKS 5

using namespace std;

// NOTES:
// Method to make program wait - for eating/thinking 'a while'
// ith philosophers have left chopstick (i) and right chopstick (i + 1)
// Lock mutex when chopstick is 'picked' up
// MAKE MUTEXES BEFORE PHILOSOPHERS
// Deadlock when philosophers try to take same chopstick
// Starvation when some number of philosophers keep eating and not letting
//  others eat
// Each philosopher eats, pickup chopstick, thinks, put down chopsticks


int main(int argc, const char *argv[]) {
  string stop = "";
  thread *philosophers = new thread[NUM_PHILOSOPHERS];
  mutex *chopsticks = new mutex[NUM_CHOPSTICKS];

  while(stop.compare("n") != 0) {

  }
}
