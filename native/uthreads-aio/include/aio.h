
#include <sys/types.h>
#include <sys/epoll.h>
#include <stdbool.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "uthread.h"
#include "list.h"

// NO TIMEOUT
#define	 INFINITE -1

// ZERO TIMEOUT
#define  POLLING   0

// MAXIMUM READY HANDLES
#define EPOOL_MAX_FDS 2048


// async handles


#define BACKLOG 5

/**
 * an async handle maintains the associated files descriptor
 * and wait queues for pending reads and writes
 */
typedef struct  async_handle_impl {
	int fd; // associated file descriptor for read/write
	list_entry_t readers; // pending readers
	list_entry_t writers; // pending writers		
} async_handle_impl_t, *async_handle_t;

 
// async operations publlic interface

int aio_pending_count();

long aio_spurious_count();
 
bool aio_in_use();

int aio_init();

void aio_end();

// for assynchronous access to standard input	 
async_handle_t aio_get_input_handle();

// dissaciate input standard
void aio_rm_input_handle();

int aio_getfd(async_handle_t ah); 

// creates a tcp server socket for uthreads blocking (system async) accepts
async_handle_t aio_server_socket(int port);


// create a client socket for epoll use
async_handle_t aio_client_socket( );

int aio_connect(async_handle_t cli_sock, struct sockaddr * srv_addr);

// uthread blocking accept
async_handle_t  aio_accept(async_handle_t serv_sock, struct sockaddr * cliaddr, socklen_t *clilen);

// uthread blocking read
int aio_read(async_handle_t ah, void *buf, int size);

// uthread blocking write
int aio_write(async_handle_t ah, void *buf, int size);

// async opers dispatcher result
typedef enum aio_wait_state { AIO_SYNCH_OPER, AIO_ASYNC_WAIT, AIO_CLOSED, AIO_INACTIVE, AIO_ERROR, AIO_NONE } aio_wait_state_t;

// async dispatcher
aio_wait_state_t aio_wait(int timeout);

void aio_close(async_handle_t ah);
