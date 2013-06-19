/*------------------------------------------------------------------------------
 * green.h
 *
 * C header file for the GreenServer client.
 * Jaco Geldenhuys <jaco@cs.sun.ac.za>
 * 6 June 2013
 *----------------------------------------------------------------------------*/

#ifndef GREEN_H
#define GREEN_H

void green_initialize();
void green_shutdown();

int green_issat(char* query);

#endif

/*------------------------------------------------------------------------------
 * End of green.h
 *----------------------------------------------------------------------------*/

