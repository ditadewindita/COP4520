#include <iostream>
#include <thread>
#include <vector>
#include <math.h>

using namespace std;

#define NUM_THREADS 8
#define MAX_PRIME 100000000
#define NUM_TOP_PRIMES 10

// 10^8
// 1 - 10^4 = found
// 10 ^ 4 - 10 ^ 5
// 10 ^ 5 - 10 ^ 6
// 10 ^ 6 - 10 ^ 7
// 10 ^ 7 - 10 ^ 8

// 8 threads, sieve only 10 times each

// O(n log*n)
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
  // seq: time - 1,907,027, 1,854,579; primes - 5761455
  //             9,645,240
  // par: time - 9,701,474
  //            10,023,587
  //             9,644,667
  //      avg -  9,348,849
  // sum: 279209790387276
  //      279209790387276

  int min = 1;
  int max = min * 10;

  bool *primes = new bool[MAX_PRIME + 1];
  vector<thread> threads_list;

  memset(primes, true, MAX_PRIME + 1);
  primes[0] = false;
  primes[1] = false;

  long prime_sum = 0;
  int num_primes = 0;
  long *topPrimes = new long[NUM_TOP_PRIMES];

  std::cout << "Launched from main.\n";

  int start = clock();

  for(int i = 0; i < NUM_THREADS && max <= MAX_PRIME; i++){
    threads_list.push_back(thread(segmented_sieve, primes, min, max));
    min = max + 1;
    max *= 10;
  }

  for_each(threads_list.begin(), threads_list.end(), mem_fn(&thread::join));

  int stop = clock();

  std::cout << "Joined all threads.\n";

  int numMaxPrimes = 0;

  for(int i = MAX_PRIME; i >= 0; i--)
    if(primes[i]){
      prime_sum += i;
      num_primes++;

      if(numMaxPrimes < NUM_TOP_PRIMES)
        topPrimes[numMaxPrimes++] = i;
    }

  std::cout << (double)(stop - start) / CLOCKS_PER_SEC << "s, num_primes: " << num_primes << ", prime_sum: " << prime_sum << "\n";
  std::cout << "Top " << NUM_TOP_PRIMES << " primes: ";

  for(int i = 0; i < NUM_TOP_PRIMES; i++) {
    std::cout << topPrimes[i];

    if(i == NUM_TOP_PRIMES - 1)
      std::cout << "\n";
    else
      std::cout << ", ";
  }

  return 0;
}
