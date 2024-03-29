#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#define STACK_SIZE (4096)

typedef struct context {
	uint64_t r15;
	uint64_t r14;
	uint64_t r13;
	uint64_t r12;
	uint64_t rbx;
	uint64_t rbp;
	void (*ret_address)();
} context_t;
 

typedef struct  uthread {
	context_t *saved_ctx;
	uint8_t stack[STACK_SIZE];
} uthread_t;



void context_switch(uthread_t *tin, uthread_t *tout);



uthread_t t1, t2, t3;
uthread_t tmain;


void f1() {
	puts("t1: step 1");
	context_switch(&t1, &t2);
	puts("t1: step2");
	context_switch(&t1, &t2);
	puts("t1 : not expected here!");
}


void f2() {
	puts("t2: step 1");
	context_switch(&t2, &t3);
	puts("t2: step 2");
	context_switch(&t2, &tmain);
}


void f3() {
	puts("t3: step 1");
	context_switch(&t3, &t1);
	puts("t3 : not expected here!");
}

void thread_init(uthread_t *thread, void (*fun)(void)) {
	 context_t *ctx = ((context_t*) (thread->stack + STACK_SIZE)) -1;
	 ctx->rbp = 0; // to enjoy debuggers
	 ctx->ret_address = fun;
	 thread->saved_ctx = ctx;
}

int main() {
	thread_init(&t1, f1);
	thread_init(&t2, f2);
	thread_init(&t3, f3);
	
	context_switch(&tmain, &t1);
	puts("back to main!");
	return 0;
}
