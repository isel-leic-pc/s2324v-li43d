
//#define LOG_ENABLED

#ifdef LOG_ENABLED

#define log(msg) printf("%s\n", msg)
#define logv(msg, v) printf("%s [%ld]\n", msg, (uint64_t) v)

#else

#define log(msg)  
#define logv(msg, v)  

#endif
