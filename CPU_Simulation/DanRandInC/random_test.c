#include <stdio.h>
#include <stdlib.h>
#include <math.h>

int main() {
  int l;
  //double n;
  long unsigned int k = 70; 
  long unsigned int seed = k;
  k = k << 16;
  k = k+ 13070;
  printf("%lu\n", k);

  
  long unsigned int a = 25214903917;
  int c = 11;
  long unsigned int mod = pow(2,48);
  double result = 0;
  srand48(seed);
  
  for (l = 1; l <=15;l++){ 
    k = (long unsigned int)((a*k)+c)%mod;
    result = ((k+0.0)/mod);
    printf("NUM   = %f\n", result);
    
    //n = drand48();
    //printf("CHECK = %f\n", n);

  }
  
  return 0;
}
