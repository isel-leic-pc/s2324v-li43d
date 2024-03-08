#ifndef _CHRON_H
#define _CHRONO_H



typedef struct timespec chrono_t ;

chrono_t chrono_start();
	 
long chrono_micros(chrono_t c);
	 

void show_times();
	 



#endif
