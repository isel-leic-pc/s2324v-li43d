#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <strings.h>
#include "usynch.h"
#include "waitblock.h"
#include "aio-sockets.h"
#include <fcntl.h>
#include <errno.h>

// globals
static struct epoll_event rdy_set[EPOOL_MAX_FDS]; 	// for ready set notifications
static int nclients;								// total current clients (async handles)

static int epoll_fd = -1;							// epoll file descriptor
static int pending_opers;							// on going operations count
static long spurious_count;							// epoll_wait calls without progress


// async handler generic creator
static async_handle_t ah_create(int fd) {
	if (fcntl(fd, F_SETFL, fcntl(fd, F_GETFL, 0) | O_NONBLOCK) ==-1) return NULL;
	async_handle_t ah = (async_handle_t) malloc(sizeof(struct  async_handle_impl));
	ah->fd = fd;
	init_list_head(&ah->readers);
	init_list_head(&ah->writers);
 	return ah;
}

// creates an async handle and associates the wrapped file descriptor 
// to the epoll interest set
static async_handle_t aio_add_fd(int fd, int events) {
	struct epoll_event evd;  
	
	async_handle_t ah  = ah_create(fd);
 
 	evd.data.ptr = ah;
	evd.events = events | EPOLLET;
	if (epoll_ctl(epoll_fd, EPOLL_CTL_ADD, fd, &evd) == -1) {
		free(ah);
		return NULL;
	}
	nclients++;
	return ah;
}


// remove a file descriptor from the epoll set
static  int aio_rm_fd(int fd) {
	struct epoll_event evd;  
	nclients--;
	fcntl(0, F_SETFL, fcntl(0, F_GETFL, 0) & ~O_NONBLOCK);
	 
	return epoll_ctl(epoll_fd, EPOLL_CTL_DEL, fd, &evd);
}



/**
 * waiting queues management auxiliary functions
 */

static uthread_t* wakeup_aio_waiter(list_entry_t * waiters) {
 	//printf("dispatch pending waiter!\n");
	pending_opers--;
	uthread_t* thread = 
		container_of(remove_list_first(waiters), waitblock_t, entry)->thread;
	if (ut_self() != thread) { /* printf("async completion!\n"); */ ut_activate(thread); }
	//else printf("thread %p synchronously completed!\n", thread);
	return  thread;
}

// wakeup all waiters in queue (used on aio_close)
static void wakeup_aio_waiters(list_entry_t * waiters) {
	while (!is_list_empty(waiters)) {
		wakeup_aio_waiter(waiters);
	}
}

// block uthread on I/O queue
static void aio_block(list_entry_t * waiters, waitblock_t *wb) {
	insert_list_last(waiters, &wb->entry);
	pending_opers++;
	ut_deactivate();
}


/** 
 * auxiliary functions to the dispatch process
 */

static aio_wait_state_t process_complete(list_entry_t *waiters) {
	if (!is_list_empty(waiters)) {
		uthread_t *thread = wakeup_aio_waiter(waiters);
	  
	    return thread == ut_self() ?  AIO_SYNCH_OPER : AIO_ASYNC_WAIT;	 
	}
 
	//spurious_count++;
	return AIO_NONE;
}

static aio_wait_state_t process_complete_writer(async_handle_t ah, int events) {
	return process_complete(&ah->writers);
}

static aio_wait_state_t process_complete_reader(async_handle_t ah, int events) {
	return process_complete(&ah->readers);
}

aio_wait_state_t process_handle(async_handle_t ah, int events) {
	int result = AIO_NONE;
	if (events & (EPOLLIN | EPOLLERR | EPOLLHUP)) {
		//printf("process pending read or accept!\n");
		result =  process_complete_reader(ah, events);
	}
	if (events & (EPOLLOUT | EPOLLERR )) {	
		//printf("process pending write!\n");
		int res =  process_complete_writer(ah, events);
		if (result == AIO_NONE) result =  res;
	}
	return result;
}


/**
 *  process (dispatch) the ready handles
 *  return 0 if current thread has a readiness file descriptor,
 *  and 1 otherwise
 */
static aio_wait_state_t process(int nready) {
	//printf("%d descriptors are ready!\n", nready);
	if (nready == 0) return AIO_NONE;
	
	aio_wait_state_t result = AIO_ASYNC_WAIT; 
	int some_dispatched = 0;

	for(int i= 0;  i < nready ; ++i) {
		struct epoll_event *evd = rdy_set+i;  
	    aio_wait_state_t res = process_handle((async_handle_t) evd->data.ptr, evd->events);
	    if (res  == AIO_SYNCH_OPER) {
			result = AIO_SYNCH_OPER;
		}
		if (res != AIO_NONE && res != AIO_ERROR) {
			//printf("some dispatched!\n");
			some_dispatched ++;
		}
	}	
	if (!some_dispatched) {
		spurious_count++;
		result = AIO_NONE;
	}
	//printf("nready=%d, some_dispatched= %d\n", nready, some_dispatched);
	return result;	
}


/**
 *  public interface
 */

// get the  associated fd
int aio_getfd(async_handle_t ah) { return ah->fd; }


bool aio_in_use() { return epoll_fd != -1 && nclients > 0; }


int aio_init()  {
	if ((epoll_fd = epoll_create1(0)) == -1) {
		perror("error creating epoll object");
		return -1;
	}
	nclients = 0;
	return 0;
}

int aio_pending_count() {
	return pending_opers;
}

long aio_spurious_count() {
	return spurious_count;
}

void aio_end() {
	close(epoll_fd);
}

async_handle_t aio_get_input_handle() {
	return aio_add_fd(0, EPOLLIN | EPOLLERR );
}

void aio_rm_input_handle() {
	aio_rm_fd(0);
	
}

/**
 * waits and process the ready epoll set
 * return AIO_SYNCH_OPER if one of them belongs to the current thread.
 * (wich means the operation can immediatelly complete without blocking)
 */ 
aio_wait_state_t aio_wait(int timeout) {
	//printf("start aio_wait with timeout %d\n", timeout);
	if (!aio_in_use()) return AIO_CLOSED;
	int nready = epoll_wait(epoll_fd, rdy_set, EPOOL_MAX_FDS, timeout);
	if (nready == -1) return AIO_ERROR;
	//printf("end aio_wait\n");
	return process(nready);	 
}


// close handle
void aio_close(async_handle_t ah) {
	int fd = aio_getfd(ah);
	aio_rm_fd(fd);
	
	close(fd);
	
	// wakeup all current waiters
	wakeup_aio_waiters(&ah->writers);
	wakeup_aio_waiters(&ah->readers);
	free(ah);
}

/**
 * create a server TCP socket for epoll use
 */
async_handle_t aio_server_socket(int port) {
	int sfd;
	struct sockaddr_in srv_addr;
	
	sfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sfd == -1) return NULL;
	

	// bind socket  
	bzero(&srv_addr, sizeof(struct sockaddr_in));
	srv_addr.sin_family = AF_INET;
	srv_addr.sin_addr.s_addr = htonl(INADDR_ANY); // inet_addr(ip_addr);
	srv_addr.sin_port = htons(port);
	
	if (bind(sfd, (struct sockaddr *) &srv_addr, sizeof(struct sockaddr_in)) == -1) {
		close(sfd);
		return NULL;
	}
	
	// set listen queue size
	if (listen(sfd, BACKLOG) == -1) {
		close(sfd);
		return NULL;
	}
	
	// regist on epoll
	async_handle_t ah;
	if ((ah=aio_add_fd(sfd,  EPOLLIN | EPOLLERR)) == NULL) {
		close(sfd);
		return NULL;
	}
	return ah;
}


/**
 * create a TCP client socket for epoll use
 */
async_handle_t aio_client_socket() {
	
	int cfd;
	
	if ((cfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		perror("error creating socket");
		return NULL;
	}
	 		
	// regist on epoll
	async_handle_t ah;
	if ((ah=aio_add_fd(cfd, EPOLLIN |  EPOLLOUT | EPOLLERR)) == NULL) {
		close(cfd);
		return NULL;
	}
	return ah;
}


int aio_connect(async_handle_t cli_sock, struct sockaddr * srv_addr) {
	waitblock_t wb;
	int cfd = aio_getfd(cli_sock);
	
	while(1) {
		int res = connect(cfd, (struct sockaddr *) srv_addr, sizeof(struct sockaddr_in));
		if (res != -1) break;
		if(errno != EINPROGRESS) return -1;
	
		init_waitblock(&wb);
		aio_block(&cli_sock->writers, &wb);	 
		
		return 0;
	}
	
	return 0;
}


async_handle_t aio_accept(async_handle_t serv_sock, struct sockaddr * cliaddr, socklen_t *clilen) {
	waitblock_t wb;
	int fd = aio_getfd(serv_sock);
	int cli_sock;
	while(1) {
		cli_sock = accept(fd, cliaddr, clilen);
		if (cli_sock != -1) break;
	 
		if(errno != EAGAIN ) return NULL;
		init_waitblock(&wb);
		aio_block(&serv_sock->readers, &wb);	 
			 
	}
	async_handle_t ah;
	if ((ah=aio_add_fd(cli_sock, EPOLLIN | EPOLLOUT | EPOLLERR)) == NULL) {
		printf("error adding conection socket to epoll\n");
		close(cli_sock);
		return NULL;
	}
	return ah;
	
}

/**
 * (system) asynchronous read
 */
int aio_read(async_handle_t ah, void *buf, int size) {
	waitblock_t wb;
	int fd = aio_getfd(ah);
	while(1) {
		int res = read(fd, buf, size);
		if (res != -1) return res;
	 
		if(errno != EAGAIN ) return -1;
		
		init_waitblock(&wb);
		aio_block(&ah->readers, &wb);	 
			 
	}
}

/**
 * (system) asynchronous write 
 */
int aio_write(async_handle_t ah, void *buf, int size) {
	waitblock_t wb;
	int fd = aio_getfd(ah);
	while(1) {
		int res = write(fd, buf, size);
		if (res != -1) return res;
	 
		//printf("ERRNO=%d\n", errno);
		if(errno != EAGAIN ) return -1;
		//printf("\nwait for write readiness!\n\n");
		init_waitblock(&wb);
		aio_block(&ah->writers, &wb);	 	 
	}
 
}

