#include <stdio.h>
#include <unistd.h>
#include "uthread.h"


void func1(void * arg) {
	for(int i=0; i < 10; ++i) {
		printf("t1 alive\n");
		usleep(100000);
		ut_yield();
	}
	printf("t1 terminating...\n");
}


void func2(void * arg) {
	printf("t2 go waiting for joined_thread\n");
	uthread_t* joined = (uthread_t *) arg;
	ut_join(joined);
	printf("t2: joined thread end!\n");
}

void func3(void * arg) {
	printf("t3 go waiting for joined_thread\n");
	uthread_t* joined = (uthread_t *) arg;
	ut_join(joined);
	printf("t3: joined thread end!\n");
}



void test_join() {
	printf("\n :: Test 2 - BEGIN :: \n\n");
	
	uthread_t *joined_thread = ut_create(func1, NULL);
	
	ut_create(func2, joined_thread);
	ut_create(func3, joined_thread);
	
	ut_run();

	printf("\n\n :: Test 2 - END :: \n");
}



int main () {
	ut_init();
 
	test_join();	
	 
	ut_end();
	return 0;
}

