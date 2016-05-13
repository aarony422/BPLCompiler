/*
string x[10];


void fun(string x[]) {
  write(x[5]);
}
*/

int fuckRight(int lol[]) {
  write("FUCK TO THE RIGHT");
  write(lol[5]);
}

int fuckLeft(int lol[]) {
  write("FUCK TO THE LEFT");
  fuckRight(lol);
}

int *x;

void main() {
  int y[10];
  y[5] = 69;
  fuckLeft(y);
}
