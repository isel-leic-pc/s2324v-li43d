
dynamic: simpletest.c event_test.c echo_srv.c echo_cli.c
	gcc -o simpletest -Wall -g -I ../include simpletest.c -luthread
	gcc -o event_test -Wall -g -I ../include event_test.c -luthread
	gcc -o echo_srv -Wall -g -I ../include echo_srv.c  chrono.c -luthread
	gcc -o echo_cli -Wall -g   echo_cli.c  chrono.c
	gcc -o echo_ut_cli -Wall -g -I ../include  echo_ut_cli.c  chrono.c -luthread
	
static : simpletest.c event_test.c
	gcc -o simpletest -Wall -g -I ../include simpletest.c ../uthread/libuthread.a
	gcc -o event_test -Wall -g -I ../include event_test.c ../uthread/libuthread.a
	
clean:
	rm -rf simpletest event_test echo_cli echo_srv massive echo_ut_cli
