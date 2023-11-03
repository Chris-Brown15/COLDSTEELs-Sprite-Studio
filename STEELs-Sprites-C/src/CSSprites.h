/*
	Licensed under MIT No Attribution.
*/
/*
	
	This file is the 'entrypoint' of the Steel's Sprites Library. It defines types used in other files and it stores data that other modules of 
	this library require.

	This file's csSteelSpritesInitialize function should be called by library users one time, and then CTSAContents structs and CSAnimation 
	structs can be created using the appropriate functions, found in "CTSAFile.h" and "CSAnimation.h" files respectively.

*/
#ifndef CS_CS_SPRITES_H
#define CS_CS_SPRITES_H

/*

	Leave this section unmodified if you are OK with Steel's Sprites using the standard library for its File IO and memory management.

	Remove the #define CS_STEELS_SPRITES_USING_STD_LIB 1 line if you want to provide:
	1) Your own File IO functions,
	2) Own FILE type, and 
	3) Memory allocation and freeing functions
	
	If you remove the define, you need to add the line:
		typedef [YOUR_FILE_TYPE_HERE] CSFile;

*/
#define CS_STEELS_SPRITES_USING_STD_LIB 1
#ifdef CS_STEELS_SPRITES_USING_STD_LIB

	#include <stdio.h>
	#include <stdlib.h>
	#include <errno.h>
	typedef FILE CSFile;

#endif

//stdint is a hard requirement for compatability with Java
#include <stdint.h>
#include <stddef.h>

/*
	Container for byte orders.
*/
typedef enum {

	CS_BIG_ENDIAN = 1 ,
	CS_LITTLE_ENDIAN = 2

} CSByteOrder;

//result of an IO operation 
typedef size_t CSOperationResult;

/*

	Allocation callback, used on all csGet___s functions.

	-bytes — number of bytes to csAllocate
	-return Heap-csAllocated memory.

*/
typedef void* (*CSAllocate)(const size_t bytes);

/*

	csFree callback, never used currently.
	-csFreeThis — memory to csFree.

*/
typedef void (*CSFree)(void* csFreeThis);

/*

	Callback for retrieving a millisecond time, preferably in a microsecond resolution. Used by real time based animation timing. This function
	should always return a greater number than ever before with each invokation.

*/
typedef double (*CSGetMilliseconds)();

/*
	
	Typedef for swap type identifiers.

*/
typedef uint8_t CSFrameSwapType;

/*

	Error callback, called when an IO operation results in error state; no more IO operations should be performed when this is called
	-errorCode — value mapping to a reason for error
	
*/
typedef void (*CSErrorOut)(const CSOperationResult errorCode);

/*
	
	Write callback, used on all csPut____ functions.

	-destination — user-provided file pointer
	-writeSource — a location from which bytes will be copied 
	-sourceSizeBytes — number of bytes to copy from writeSource to destination
	-return Number of bytes written, or 0 if an error occurs.

*/
typedef size_t (*CSWrite)(CSFile* destination , void* writeSource , size_t sourceSizeBytes);

/*

	Read callback, used on all csGet____ functions.
	-source — used-provided file to read from
	-readDestination — a location to read into
	-readSizeBytes — a number of bytes to read
	-return Number of bytes read, or 0 if an error occurs.

*/
typedef size_t (*CSRead)(CSFile* source , void* readDestination , size_t readSizeBytes);

//swap type literals
#define CS_SWAP_BY_TIME 0
#define CS_SWAP_BY_UPDATES 1

//error codes
const CSOperationResult CS_NO_ERROR = 1;
const CSOperationResult CS_IO_ERROR = -1;
const CSOperationResult CS_EOF_ERROR = -2;

//define the initialize function by whether we are using the standard library
#ifdef CS_STEELS_SPRITES_USING_STD_LIB

/*

	Initializes the core portion of this library. 

*/
void csSteelSpritesInitialize(CSGetMilliseconds getTime , CSErrorOut errorOut);

#else

/*

	Initializes the core portion of this library. 

	-csAllocate — a callback this library will use when it allocates memory of any kind
	-csFree — a callback this library will use to csFree memory
	-csWrite — a callback this library will invoke for its file write operations
	-csRead — a callback this library will invoke for its file reading operations
	-getTime — a callback this library will use to get a millisecond time
	-errorOut — a callback this library will use when a fatal error occurs

*/
void csSteelSpritesInitialize(
	CSAllocate csAllocate , 
	CSFree csFree , 
	CSWrite csWrite , 
	CSRead csRead , 
	CSGetMilliseconds getTime , 
	CSErrorOut errorOut
);

#endif

/*

	csAllocates memory of the given size. Used by this library, and directly calls the given csAllocate callback.

	-bytes — amount of bytes to csAllocate
	-return csAllocated memory

*/
void* csAllocate(const size_t bytes);

/*

	csFrees the given memory used by this library, and directly calls the given csFree callback.
	
	-memory — memory to csFree

*/
void csFree(void* memory);

/*

	Write bytes of source into the given file .

	-file — a file to write to
	-source — source for bytes to write
	-bytes — number of bytes to write
	-return Number of bytes written, or 0 if an error occurs.

*/
size_t write(CSFile* file , void* source , size_t bytes);

/*

	Reads bytes of suorce into destination.

	-source — a file to read from
	-destination — a destination for read-in bytes
	-bytes — number of bytes to read
	-return Number of bytes read, or 0 if an error occurs.

*/
size_t read(CSFile* source , void* destination , size_t bytes);

/*

	Gets a current time in milliseconds. This function should always return a greater number after every call.

	-return Number of milliseconds.

*/
double csGetTime(void);

void errorOut(const CSOperationResult result);

#define csassert(x) if(!x) errorOut(-1)

#endif