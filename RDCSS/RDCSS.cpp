#include <iostream>
#include <thread>
#include <vector>
#include <atomic>
#include "RDCSS.h"

// RDCSSDescriptor
RDCSSDescriptor::RDCSSDescriptor(int *temp_a1, int temp_o1, int *temp_a2, int temp_o2, int temp_n2) :
  a1(temp_a1), o1(temp_o1), a2(temp_a2), o2(temp_o2), n2(temp_n2) {}

int RDCSS(int *a1, int o1, int *a2, int o2, int n2) {
  int r = *a1;

  // Change value at address #2 iff the memory at address #1 and #2 hasn't changed
  if(r == o2 && *a1 == o1) {
    *a2 = n2;

    // Return value from a2
    return r;
  }
}
