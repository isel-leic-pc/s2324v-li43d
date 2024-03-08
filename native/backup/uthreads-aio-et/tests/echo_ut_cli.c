/*
 * implements an echo client using a stream IP socket 
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
 

#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include "aio-sockets.h"
#include "uthread.h"

#include "chrono.h"
#include "echo_service.h"
 
#define MAX_SOCK_NAME 256
#define CLIENT_SOCK_PREFIX "sock_client_"

#define NITERS 1000
#define MIN_CLIENT_START 20
#define MAX_CLIENT_START 100

int terminated = 0;
int remaining;
int nclients=1;
int started;

void start_wait_rand() {
	usleep( rand() % (MAX_CLIENT_START - MIN_CLIENT_START) + MIN_CLIENT_START);
}

void stats(void *arg) {
	chrono_t start = chrono_start();
	while (!terminated) {
		while (chrono_micros(start) < 1000000) ut_yield();
		printf("pending count = %d\n", aio_pending_count());
		printf("spurious count = %ld\n", aio_spurious_count());
		start = chrono_start();
	}
		
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
		printf("Error connecting socket on client %d\n", client_id);
		aio_close(cfd);
		return;
	}
	//start_wait_rand();
	if (++started == nclients)
		printf("\nall started!\n\n");
 
	echo_msg_t msg, resp;
 
	chrono_t chron = chrono_start();
	for(int try=1; try <= NITERS; try++) {
		sprintf(msg, "Hello_%d_%d", client_id, try);
		int res;
		if ((res=aio_write(cfd, &resp, strlen(msg))) != strlen(msg)) {
			char error_msg[128];
			sprintf(error_msg, "error %d writing cmd %d", res, try);
			perror(error_msg);
			break;
		}
		// get response
		
		if ((res=aio_read(cfd, &resp, sizeof(echo_msg_t), try)) == -1 ) {
			printf("error %d reading response %d!\n", res, try);
			break;
		}
		 
	}
	
	printf("client %d done: %d tries in %ld micros!\n", client_id, NITERS, chrono_micros(chron));
	remaining--;
	if (remaining == 0) terminated = 1;
	//usleep(500);
	//shutdown(aio_getfd(cfd), SHUT_RDWR);
	aio_close(cfd);
}

int main(int argc, char *argv[]) {
	 
	
	if (argc == 2) {
		nclients = atoi(argv[1]);
	}
	
	ut_init();
	
	//ut_create(stats, NULL);
	remaining = nclients;
	for(int i=0; i < nclients; ++i) {
		ut_create(echo_client, (void *) ((size_t) i));
	}

	ut_run();
	
	ut_end();
  	
 
	return 0;
}
