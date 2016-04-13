void main(void) {
  int x;
  int *p;
  int y[10];
  x = y[x];
  p = &x;
  *p = x;
  *p = &x;
  x = *p+1;
}
