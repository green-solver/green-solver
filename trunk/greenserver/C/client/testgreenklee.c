/*------------------------------------------------------------------------------
 * testgreenklee.c
 *
 * Test program for the Green client with actual Klee constraints.
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
	issat("array a[4] : w32 -> w8 = symbolic (query [(Eq false (Eq 0 N0:(ReadLSB w32 0 a))) (Eq false (Slt N0 0))] false)\n");
	issat("array a[4] : w32 -> w8 = symbolic (query [(Eq false (Eq 0 N0:(ReadLSB w32 0 a))) (Slt N0 0)] false)\n");
	issat("array a[4] : w32 -> w8 = symbolic (query [(Eq 0 (ReadLSB w32 0 a))] false)\n");
	green_shutdown();
	return 0;
}

/*------------------------------------------------------------------------------
 * End of testgreenklee.c
 *----------------------------------------------------------------------------*/

