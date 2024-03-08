#include <stdio.h>
#include <unistd.h>
#include <time.h>
#include "chrono.h"

chrono_t chrono_start() {
	struct timespec ts;
	clock_gettime(CLOCK_MONOTONIC, &ts);
	return ts;
}

long chrono_micros(chrono_t c) {
	struct timespec ts;
	clock_gettime(CLOCK_MONOTONIC, &ts);
	
	if (ts.tv_sec == c.tv_sec)
		return (ts.tv_nsec - c.tv_nsec) /1000;
	long micros = ((1000000000 - c.tv_nsec) +
				((long) ts.tv_sec - c.tv_sec - 1) * 1000000000 +
				ts.tv_nsec)/1000;
				
	return micros;	
}


void show_times() {
	clockid_t clocks[] = {
		CLOCK_REALTIME,
		CLOCK_MONOTONIC,
		CLOCK_PROCESS_CPUTIME_ID,
		CLOCK_THREAD_CPUTIME_ID,
		(clockid_t) -1
	};
	
	for(int i=0; clocks[i] != (clockid_t) -1; ++i) {
		struct timespec ts;
		int ret;
		
		ret = clock_gettime(clocks[i], &ts);
		if (ret != 0)
			perror("clock_gettime");
		else
			printf("clock=%d, sec=%ld, nsec = %ld\n",
				clocks[i], ts.tv_sec, ts.tv_nsec);
	}
}

 
