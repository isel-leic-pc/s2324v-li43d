all: libuthread.so 

libuthread.so: uthread.o event.o semaphore.o ctxswitch.o aio.o
	gcc -o libuthread.so -shared uthread.o event.o semaphore.o aio.o ctxswitch.o

 

#
# uthread core
#

uthread.o: uthread.c
	gcc -c -g -fPIC -Wall -I ../include uthread.c
	
	
ctxswitch.o: ctxswitch.s
	as -o ctxswitch.o -c ctxswitch.s

#
# uthread syncronizers
#

event.o: event.c
	gcc -c -g -fPIC -Wall -I ../include event.c 
	
semaphore.o: semaphore.c
	gcc -c -g -fPIC -Wall -I ../include semaphore.c 

aio.o: aio.c
	gcc -c -g -fPIC -Wall -I ../include aio.c 

install:
	cp libuthread.so /usr/local/lib
	
clean:
	rm -rf *.o *.so *.a

	
