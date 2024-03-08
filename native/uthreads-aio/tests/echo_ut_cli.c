/*
 * implements an echo client using a stream IP socket 
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
 
#include "aio.h"
#include "uthread.h"
#include "chrono.h"
#include "echo_service.h"
#include "log.h"


#define NITERS 10
#define MIN_CLIENT_START 20
#define MAX_CLIENT_START 50

int terminated = 0;
int remaining;
int nclients=1;
int started;

void start_wait_rand() {
	usleep( rand() % (MAX_CLIENT_START - MIN_CLIENT_START) + MIN_CLIENT_START);
}

void echo_client(void *arg) {
	async_handle_t cfd;
	int client_id =  (int) ((size_t) arg);
	struct sockaddr_in srv_addr;
	 
	if ((cfd = aio_client_socket()) == NULL) {
		fprintf(stderr, "error creating socket\n");
		return;
	}
	
	/* Construct server address, and make the connection */
	bzero(&srv_addr, sizeof(struct sockaddr_in));
	srv_addr.sin_family = AF_INET;
	srv_addr.sin_addr.s_addr = inet_addr(ECHO_SERVER_ADDR);
	srv_addr.sin_port = htons(ECHO_SERVER_PORT);
	
		
	if (aio_connect(cfd, (struct sockaddr *) &srv_addr)== -1) {
		fprintf(stderr, "Error connecting socket on client %d\n", client_id);
		aio_close(cfd);
		return;
	}
	start_wait_rand();
	if (++started == nclients) {
		log("\nall started!\n");
	}
 
	echo_msg_t buf;
 
	
	for(int try=1; try <= NITERS; try++) {
		sprintf(buf, "Hello_%d_%d", client_id, try);
		int res;
		if ((res=aio_write(cfd, buf, strlen(buf))) != strlen(buf)) {
			perror("error writing msg");
			break;
		}
		// get response
		
		if ((res=aio_read(cfd, &buf, sizeof(echo_msg_t))) == -1 ) {
			perror("error reading msg");
			break;
		}
		 
	}
	
	//printf("client %d done: %d tries in %ld micros!\n", client_id, NITERS, chrono_micros(chron));
	remaining--;
	if (remaining == 0) terminated = 1;
	 
	aio_close(cfd);
}

int main(int argc, char *argv[]) {
	 
	
	if (argc == 2) {
		nclients = atoi(argv[1]);
	}
	
	ut_init();
	
	chrono_t chron = chrono_start();
	
	remaining = nclients;
	for(int i=0; i < nclients; ++i) {
		ut_create(echo_client, (void *) ((size_t) i));
	}

	ut_run();
	
	printf("%d clients done in %ld micros!\n", nclients, chrono_micros(chron));
	
	ut_end();
  	
 
	return 0;
}
