/* A program to input, sort, and output. */
int x[10];

void switch (int A[], int i, int j) {
    int temp;
    if ( i != j) {
        temp = A[i];
        A[i] = A[j];
        A[j] = temp;
    }
}

void sort( int A[], int first, int last) {
    int i;
    int j;
    int small;

    i = first;
    while (i < last-1) {
        /* get smallest remaining value and put it at position i */
        j = i;
        small = j;
        while (j < last) {
            if (A[j] < A[small])
                small = j;
            j = j + 1;
        }
        switch(A, i, small);
        i = i + 1;
    }

}

void main(void) {
    int i;
    /*
    i = 0;
    while (i < 10) {
        x[i] = read();
        i = i+1;
    }
    */
    x[0] = 60;
    x[1] = -5;
    x[2] = 9;
    x[3] = 17;
    x[4] = 23;
    x[5] = -1;
    x[6] = 0;
    x[7] = 35;
    x[8] = -75;
    x[9] = 6;

    sort(x,0,10);
    i = 0;
    while (i<10) {
        write(x[i]);
        i = i + 1;
    }
    writeln();
}
