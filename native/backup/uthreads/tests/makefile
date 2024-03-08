 
CFLAGS=-Wall -g -I ../include 
 

all: simpletest join_test

simpletest: simpletest.c
	gcc -o simpletest $(CFLAGS) simpletest.c -luthread

join_test: join_test.c
	gcc -o join_test $(CFLAGS) join_test.c  -luthread
	
clean:
	rm -rf simpletest  join_test
