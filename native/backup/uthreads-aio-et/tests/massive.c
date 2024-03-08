#include "uthread.h"
#include <sys/types.h>
#include <stdio.h>

int result;

#define NTHREADS (1024*1024)
#define NINCRS 10

void inc_thread(void *arg) {
	int id = (int)(size_t) arg;
	
	printf("start thread %d\n", id);
	for(int i=0; i < NINCRS; ++i) {
		result++;
		ut_yield();
	}
	printf("end thread %d\n", id);
}
	
int main() {
	ut_init();
	
	for(int i=0; i < NTHREADS; ++i) {
		ut_create(inc_thread, (void*) (size_t) i);
	}
	printf("start!n");
	ut_run();
	
	ut_end();
	printf("result=%d\n", result);
	return 0;
}
