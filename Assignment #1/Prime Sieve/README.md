# Prime Sieve

This implementation of finding primes up to `10^8` uses a segmented sieve, along with 8 concurrent threads.
Use the following to run the program:
```
$ g++ -std=c++11 -pthread PrimeSieve.cpp
$ ./a.out
```

The output of the program will show the time it took, the number of primes found, the sum of the primes, as well as the top 10 primes
in the format below:
```
<time>s num_primes: <number of primes found> prime_sum: <sum of found primes>
Top 10 primes: <top 10 primes>
```

Each thread takes a segment of the number range and performs a segmented sieve on its given slice. Since each thread's sieve only runs
on its given segment, the threads will not touch each other's given 'slice' and try to access the same spot in memory and have a data race.

A segmented sieve has runtime of `O(n log log n)`, and with the numbers represented as bits in an array, it only stores a boolean for each number.

Using just the main thread, the segmented sieve runs for `9.645240s` average (over ~10 runs). Over 8 threads, however, the segmented
sieve runs for `9.348849s`.

The program consistently outputs the following, with the runtime slightly changing over each exection:
```
<variable_amount_of_time>s num_primes: 5761455, prime_sum: 279209790387276
Top 10 primes: 99999989, 99999971, 99999959, 99999941, 99999931, 99999847, 99999839, 99999827, 99999821, 99999787
```
