void main(void) {
  int x;
  int *p;
  int y[10];
  x = y[x];
  y[5] = 23;
  p = &x;
  *p = x;
  /**p = &x;  not allowed */
  x = *p+1;
}
