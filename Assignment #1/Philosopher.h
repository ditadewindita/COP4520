#define PHILOSOPHERS_H
#ifndef PHILOSOPHERS_H

#include <thread>

class Philosophers {
  public:
    int id;
    thread *thread;

    void eat();

    void think();
};

#endif
