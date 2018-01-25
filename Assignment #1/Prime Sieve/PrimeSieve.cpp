// Haerunnisa Dewindita
// COP4520
// HA990936

#include <iostream>
#include <fstream>
#include <thread>
#include <vector>
#include <math.h>

using namespace std;

#define NUM_THREADS 8
#define MAX_PRIME 100000000
#define NUM_TOP_PRIMES 10

void normal_sieve(bool *primes, int n) {

  for(int i = 2; i * i <= n; i++)
    if(primes[i])
      for(int j = i * 2; j * j <= n; j += i)
        primes[j] = false;
}

void segmented_sieve(bool *primes, int min, int max) {
  if(min == 1)
    min++;

  for(int i = 2; i < MAX_PRIME && i * i <= max; i++) {
    int temp = i * i;

    if(temp < min)
      temp = ((min + i - 1) / i) * i;

    for(int j = temp; j <= max; j += i)
      primes[j] = false;
  }
}

int main() {
  int min = 1;
  int max = min * 10;

  bool *primes = new bool[MAX_PRIME + 1];
  vector<thread> threads_list;
  ofstream f_out;

  // Set the initial values of the primes array
  memset(primes, true, MAX_PRIME + 1);
  primes[0] = false;
  primes[1] = false;

  f_out.open("primes.txt");

  long prime_sum = 0;
  int num_primes = 0;
  long *topPrimes = new long[NUM_TOP_PRIMES];

  int start = clock();

  // Every time a new thread is spawned, shift the range of primes to find
  for(int i = 0; i < NUM_THREADS && max <= MAX_PRIME; i++){
    threads_list.push_back(thread(segmented_sieve, primes, min, max));
    min = max + 1;
    max *= 10;
  }

  for_each(threads_list.begin(), threads_list.end(), mem_fn(&thread::join));

  int stop = clock();

  int currNumMaxPrimes = NUM_TOP_PRIMES - 1;

  for(int i = MAX_PRIME; i >= 0; i--)
    if(primes[i]){
      prime_sum += i;
      num_primes++;

      if(currNumMaxPrimes >= 0 && currNumMaxPrimes < NUM_TOP_PRIMES)
        topPrimes[currNumMaxPrimes--] = i;
    }

  f_out << (double)(stop - start) / CLOCKS_PER_SEC << "s " << num_primes << " " << prime_sum << "\n";

  for(int i = 0; i < NUM_TOP_PRIMES; i++) {
    f_out << topPrimes[i];

    if(i == NUM_TOP_PRIMES - 1)
      f_out << "\n";
    else
      f_out << ", ";
  }

  f_out.close();

  return 0;
}
