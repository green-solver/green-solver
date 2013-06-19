/*------------------------------------------------------------------------------
 * testgreen.c
 *
 * Test program for the Green client.
 * Jaco Geldenhuys <jaco@cs.sun.ac.za>
 * 6 June 2013
 *----------------------------------------------------------------------------*/

#include <stdio.h>
#include "green.h"

void issat(char* query) {
	int answer = green_issat(query);
	printf("QUERY: \"%s\"\nANSWER: %d\n\n", query, answer);
}

int main() {
	green_initialize(9408);
	issat("piet\n");
	issat("pompies\n");
	green_shutdown();
	return 0;
}

/*------------------------------------------------------------------------------
 * End of testgreen.c
 *----------------------------------------------------------------------------*/

