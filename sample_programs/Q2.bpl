/*
string x[10];


void fun(string x[]) {
  write(x[5]);
}
*/

void main() {
  int *y;
  int x;
  x = 5;
  y = &x;
  fun(x);
}
