int fun1(int x, int *y, int A[]) {

}

void main(void) {
  int x;
  int *p;
  int y[10];
  /*x = fun1(x,y);*/
  x = y[x];
  y[5] = 23;
  p = &x;
  *p = x;
  /* *p = &x;*/
  x = *p+1;
}
