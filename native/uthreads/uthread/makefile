all: libuthread.so 

libuthread.so: uthread.o ctxswitch.o
	gcc -o libuthread.so -shared uthread.o  ctxswitch.o



#
# uthread core
#
uthread.o: uthread.c
	gcc -c -g -fPIC -Wall -I ../include uthread.c
	
ctxswitch.o: ctxswitch.s
	as -o ctxswitch.o -c ctxswitch.s

install:
	sudo cp libuthread.so /usr/local/lib
		
clean:
	rm -rf *.o *.so *.a

	
