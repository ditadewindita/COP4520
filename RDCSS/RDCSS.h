#ifndef RDCSS_H
#define RDCSS_H

#include <thread>
#include <atomic>

using namespace std;

class RDCSSDescriptor {
  public:
    int *a1;
    int o1;
    int *a2;
    int o2;
    int n2;

    RDCSSDescriptor(int *a1, int o1, int *a2, int o2, int n2);
};

#endif
