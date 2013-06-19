/*------------------------------------------------------------------------------
 * green.c
 *
 * Client for the GreenServer running in Java.
 * Jaco Geldenhuys <jaco@cs.sun.ac.za>
 * 6 June 2013
 *----------------------------------------------------------------------------*/

#include <stdio.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "green.h"

/**
 * The size of the query result buffer.
 */

#define GREEN_BUFSIZE (32)

/**
 * The socket connection to the server.
 */

int green_socket;

/**
 * Error reporting routine.
 */

void report_and_die(char* message) {
	fprintf(stderr, "ERROR: %s\n", message);
	exit(-1);
}

/**
 * Initialize the Green client.  This amounts to connecting to the server.  If
 * the parameter is the server port.  For now we assume that the server is
 * running on the local machine, but this might change in the future.
 */

void green_initialize(int port) {
	struct sockaddr_in server;       /* Green server address */

	/* Create a reliable, stream socket using TCP */
	if ((green_socket = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) {
		report_and_die("socket() failed");
	}

	/* Construct the server address structure */
	memset(&server, 0, sizeof(server));              /* Zero out structure */
	server.sin_family      = AF_INET;                /* Internet address family */
	server.sin_addr.s_addr = inet_addr("127.0.0.1"); /* Server IP address */
	server.sin_port        = htons(port);            /* Server port */

	/* Establish the connection to the echo server */
	if (connect(green_socket, (struct sockaddr *) &server, sizeof(server)) < 0) {
		report_and_die("connect() failed");
	}
}

void green_shutdown() {
	if (send(green_socket, "CLOSE\n", 5, 0) != 5) {
		// do nothing
	}
	close(green_socket);
	exit(0);
}

int green_issat(char* query) {
	int query_len;
	int bytes_rcvd;
	char buf[GREEN_BUFSIZE];

	query_len = strlen(query);

	if (send(green_socket, query, query_len, 0) != query_len) {
		report_and_die("send() sent a different number of bytes than expected");
	}

	if ((bytes_rcvd = recv(green_socket, buf, GREEN_BUFSIZE - 1, 0)) <= 0) {
		report_and_die("recv() failed or connection closed prematurely");
	}

	return (buf[0] != '0');
}

/*------------------------------------------------------------------------------
 * End of green.c
 *----------------------------------------------------------------------------*/

