#include <stdio.h>
#include <stdlib.h>
#include <math.h>

int main() {
  int l;
  double n;
  long unsigned int k = 2; 
  long unsigned int seed = k;
  k = k << 16;
  k = k+ 13070;
  double lambda = 0.01;
  //printf("%lu\n", k);

  
  long unsigned int a = 25214903917;
  int c = 11;
  long unsigned int mod = pow(2,48);
  double result = 0;
  srand48(seed);
  
  for (l = 1; l <=1419;l++){ 
    k = (long unsigned int)((a*k)+c)%mod;
    result = ((k+0.0)/mod);
    
    
    n = drand48();
    double randValue = ceil(-1*log(n)/lambda);
    if(randValue<=200)
      printf("Accepted: %f\n",n);
    else
      printf("Rejected: %f\n",n);


  }
  
  return 0;}
