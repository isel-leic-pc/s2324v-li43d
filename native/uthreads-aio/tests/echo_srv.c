/*
 * implements a concurrent echo server using a stream IP socket on uthreads
 */

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include "chrono.h"

#include "echo_service.h"
#include "aio.h"

static async_handle_t sfd;
static bool terminated = false;
static char welcome_msg[] = "welcome, new client!\n";
static char client_bye_msg[] = "bye\n";
static char server_bye_msg[] = "bye, till next visit!\n";

// uthread for attend termination
void watchdog(void *arg) {
	char buf[2];
	async_handle_t input = aio_get_input_handle();
	aio_read(input, buf, 1);
	printf("stop server!\n");
	shutdown(aio_getfd(sfd), SHUT_RDWR);
	aio_rm_input_handle();
}


void process_connection(async_handle_t cfd) {
	echo_msg_t msg;
	int nread;
 
	aio_write(cfd, welcome_msg, strlen(welcome_msg));
	while ((nread = aio_read(cfd, msg, sizeof(echo_msg_t))) > 0 ) {
		msg[nread] = 0;
		
		if (strcmp(client_bye_msg, msg) != 0) {
			aio_write(cfd, msg, nread);
		}
		else {
			aio_write(cfd, server_bye_msg, strlen(server_bye_msg));
		    break;
		}
	}
	//printf("end session!\n");
	aio_close(cfd);		 
}

// uthread for process client session
void dispatch_connection(void *arg) {
	process_connection((async_handle_t) arg);
}


// uthread for accepting loop
void run(void *arg) {
	
	// create server socket
	if ((sfd = aio_server_socket(ECHO_SERVER_PORT)) == NULL) {
		fprintf(stderr, "error creating socket\n");
		return;
	}
	
	printf("server ready!\n");
	for (;;) {  
		async_handle_t cfd; // connection socket
		struct sockaddr_in cli_addr;
		socklen_t addrlen = sizeof(struct sockaddr_in);
		cfd = aio_accept(sfd, (struct sockaddr *)  &cli_addr, &addrlen);
		if (cfd == NULL) {	
			perror("error accepting socket");
			break;
		}
		// dispatch the processing of the new connection to other uthread 
		ut_create(dispatch_connection,  cfd); 
	}
	aio_close(sfd);
	terminated = true;
}



int main(int argc, char *argv[]) {
	ut_init();
	
	ut_create(run, NULL);
	
	ut_create(watchdog, NULL);
	 
	ut_run();
	printf("server end!\n");
	
	ut_end();
	return 0;
}


 
