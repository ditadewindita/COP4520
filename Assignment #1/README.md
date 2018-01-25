# Dining Philosophers
 
This implementation of Dijkstra's Dining Philosophers problem has each philosopher represented as a thread, and each chopstick as a mutex.

This project structure includes a directory for each different version, and you can run each C++ source file by traversing to the respective
version folder and running the following:

```
$ g++ -std=c++11 -pthread <filename>
$ ./a.out
```

For version 4, the program accepts a single command line argument for the number of philosophers. Compile it the same way above, but run it like so:
```
$ ./a.out <number_of_philosophers>
```

## Version 1
Version 1 implements a basic foundation of the dining philosophers problem, where no philosopher holds the same chopstick as another philosopher at the same time.
This is achieved in the code by checking if the wanted chopstick is locked yet or not. Only when one of the chopsticks is acquired that the philosopher continues 
to try and pick up his next one.

Here is some data from an execution of version 1:
```
Total program ran for 4258ms.
Philosopher 0 has total of 1943ms hunger time.
Philosopher 1 has total of 1581ms hunger time.
Philosopher 2 has total of 753ms hunger time.
Philosopher 3 has total of 2791ms hunger time.
Philosopher 4 has total of 2225ms hunger time.
```
As the time shows, Philosopher 2 dominates the chopsticks while Philosopher 3 has the longest time waiting, almost close to the execution time.

## Version 2
Version 2 utilizes Dijkstra's solution of applying a partial ordering to the shared resources (chopsticks). The idea behind this solution is each philosopher picks up 
their lower ordered chopstick first - this way, the last philosopher cannot pickup the fifth chopstick, since it has been acquired by the first philosopher already. Therefore,
no deadlock!
```
Total program ran for 9315ms
Philosopher 0 has a total of 5880ms hunger time.
Philosopher 1 has a total of 29ms hunger time.
Philosopher 2 has a total of 3315ms hunger time.
Philosopher 3 has a total of 12ms hunger time.
Philosopher 4 has a total of 4144ms hunger time.
```
The times above show no deadlock, since every philosopher has a hunger time siginificantly less than the exection time, but it does show chopstick dominance. This shows that
this solution displays starvation!

## Version 3
Version 3 utilizes queueing system like those found at DMVs and delis. Each philosopher takes a number when he is hungry, and he will only eat if he has the lowest number and
has available chopsticks. This way, each hungry philosopher will have a chance at eating.
```
Total program ran for 10273ms.
Philosopher 0 has total of 1650ms hunger time.
Philosopher 1 has total of 2731ms hunger time.
Philosopher 2 has total of 2315ms hunger time.
Philosopher 3 has total of 2625ms hunger time.
Philosopher 4 has total of 2657ms hunger time.
```
The hunger times here for each philosopher is more evened out, since everyone has a chance to eat more frequently.

## Version 4
Version 4 utilizes a similar ordering system like Version 3, but it uses hunger times as reference. If neighboring philosophers have waited longer than a multiplier of their
own time, then the current philosopher cannot eat.
```
Total program ran for 11293ms.
Philosopher 0 has total of 2373ms hunger time.
Philosopher 1 has total of 1208ms hunger time.
Philosopher 2 has total of 2541ms hunger time.
Philosopher 3 has total of 2418ms hunger time.
Philosopher 4 has total of 2596ms hunger time.
Philosopher 5 has total of 2304ms hunger time.
Philosopher 6 has total of 2420ms hunger time.
Philosopher 7 has total of 2000ms hunger time.
Philosopher 8 has total of 2481ms hunger time.
Philosopher 9 has total of 2263ms hunger time.
```

The data above shows that each philosopher has an almost equal chance of eating than the rest of the philosophers. Although Philosopher 1 seems to have the least
amount of time hungry, the rest of the philosophers have about equal wait times, and not one was left to 'starve' by themselves.

*Note: Since version 4 uses a variable amount of philosophers, this data was extracted from a run of 10 philosophers.

