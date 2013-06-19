/*------------------------------------------------------------------------------
 * quitgreen.c
 *
 * Tell the Green server to shut down.
 * Jaco Geldenhuys <jaco@cs.sun.ac.za>
 * 6 June 2013
 *----------------------------------------------------------------------------*/

#include <stdio.h>
#include "green.h"

int main() {
	green_initialize(9408);
	green_issat("QUIT\n");
	green_shutdown();
	return 0;
}

/*------------------------------------------------------------------------------
 * End of quitgreen.c
 *----------------------------------------------------------------------------*/

