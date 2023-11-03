/*
	Licensed under MIT No Attribution.
*/
/*

	This is the header for Steel Sprites File Operations. This file contains header declarations for Steel's Sprites IO operations which 
	it uses to load .ctsa files from disk.

*/
/*

	This is a port of CSFileOperations.java

*/
#ifndef INCLUDED_CS_FILE_OPERATION_H
#define INCLUDED_CS_FILE_OPERATION_H

#include "CSSprites.h"

/*

	Initializes the IO portion of the library. This function is called when the library is initialized, there is no need to call it again.

*/
void csInitializeFileOperations(void);

/*
	
	Returns the byte order of the underlying machine.

	-return CS_BIG_ENDIAN if the underlying machine is big endian byte order, or CS_LITTLE_ENDIAN if the underlying machine is little 
		    endian.

*/
CSByteOrder csGetNativeByteOrder(void);

/*
	
	Returns the current byte order of the library.

	-return Current byte order of the library, one of CS_BIG_ENDIAN or CS_LITTLE_ENDIAN

*/
CSByteOrder csGetCurrentByteOrder(void);

/*

	Sets the current byte order of the library.

	-param newCurrentByteOrder — the new byte order of the library, one of CS_BIG_ENDIAN or CS_LITTLE_ENDIAN

*/
void csSetCurrentByteOrder(const CSByteOrder newCurrentByteOrder);

/*

	Writes a single byte to the given file destination.

	-destination — a destination file
	-value — a byte to write

*/
void csPutByte(CSFile* destination , int8_t value);

/*

	Writes a word to the given file destination.

	-destination — a destination file
	-value — a word to write

*/
void csPutWord(CSFile* destination , int16_t value);

/*

	Writes a double word to the given file destination.

	-destination — a destination file
	-value — a double word to write

*/
void csPutDWord(CSFile* destination , int32_t value);

/*

	Writes a quad word to the given file destination.

	-destination — a destination file
	-value — a quad word to write

*/
void csPutQWord(CSFile* destination , int64_t value);

/*

	Writes a C float word to the given file destination.

	-destination — a destination file
	-value — a C float to write

*/
void csPutFloat(CSFile* destination , float value);

/*

	Writes a C double float word to the given file destination.

	-destination — a destination file
	-value — a C double float to write

*/
void csPutDouble(CSFile* destination , double value);

/*

	Writes an array of bytes to the given file destination. 

	This function treats the data pointed to by pointer to be 1 byte elements, and numberElements quantity is read and written.

	-destination — a destination file
	-pointer — a pointer to bytes
	-numberElements — number of elements

*/
void csPutBytes(CSFile* destination , void* pointer , const size_t numberElements);

/*

	Writes an array of words to the given file destination. 

	This function treats the data pointed to by pointer to be 2 byte elements, and numberElements quantity is read and written.

	-destination — a destination file
	-pointer — a pointer to bytes
	-numberElements — number of elements

*/
void csPutWords(CSFile* destination , void* pointer , const size_t numberElements);

/*

	Writes an array of double words to the given file destination. 

	This function treats the data pointed to by pointer to be 4 byte elements, and numberElements quantity is read and written.

	-destination — a destination file
	-pointer — a pointer to bytes
	-numberElements — number of elements

*/
void csPutDWords(CSFile* destination , void* pointer , const size_t numberElements);

/*

	Writes an array of quad words to the given file destination. 

	This function treats the data pointed to by pointer to be 8 byte elements, and numberElements quantity is read and written.

	-destination — a destination file
	-pointer — a pointer to bytes
	-numberElements — number of elements

*/
void csPutQWords(CSFile* destination , void* pointer , const size_t numberElements);

/*

	Writes an array of floats to the given file destination. 

	This function treats the data pointed to by pointer to be float elements, and numberElements quantity is read and written.

	-destination — a destination file
	-pointer — a pointer to bytes
	-numberElements — number of elements

*/
void csPutFloats(CSFile* destination , float* pointer , const size_t numberElements);

/*

	Writes an array of doubles to the given file destination. 

	This function treats the data pointed to by pointer to be double elements, and numberElements quantity is read and written.

	-destination — a destination file
	-pointer — a pointer to bytes
	-numberElements — number of elements

*/
void csPutDoubles(CSFile* destination , double* pointer , const size_t numberElements);

/*
	
	Reads the given file for a byte and returns it.
	-source — file to read from
	-return A byte.

*/
int8_t csGetByte(CSFile* source);

/*
	
	Reads the given file for a word and returns it.
	-source — file to read from
	-return A word.

*/
int16_t csGetWord(CSFile* source);

/*
	
	Reads the given file for a double word and returns it.
	-source — file to read from
	-return A double word.

*/
int32_t csGetDWord(CSFile* source);

/*
	
	Reads the given file for a quad word and returns it.
	-source — file to read from
	-return A quad word.

*/
int64_t csGetQWord(CSFile* source);

/*
	
	Reads the given file for a float and returns it.
	-source — file to read from
	-return A float.

*/
float csGetFloat(CSFile* source);

/*
	
	Reads the given file for a double float and returns it.

	-source — file to read from
	-return A double float.

*/
double csGetDouble(CSFile* source);

/*

	Reads the given file for an array of bytes and returns it, storing its length in length.

	-source — the file to read
	-length — pointer to a size which will be filled in by this function
	-return Heap csAllocated pointer to an array of bytes.

*/
int8_t* csGetBytes(CSFile* source , size_t* length);

/*

	Reads the given file for an array of words and returns it, storing its length in length.

	-source — the file to read
	-length — pointer to a size which will be filled in by this function
	-return Heap csAllocated pointer to an array of words.

*/
int16_t* csGetWords(CSFile* source , size_t* length);

/*

	Reads the given file for an array of double words and returns it, storing its length in length.

	-source — the file to read
	-length — pointer to a size which will be filled in by this function
	-return Heap csAllocated pointer to an array of double words.

*/
int32_t* csGetDWords(CSFile* source , size_t* length);

/*

	Reads the given file for an array of quad words and returns it, storing its length in length.

	-source — the file to read
	-length — pointer to a size which will be filled in by this function
	-return Heap csAllocated pointer to an array of quad words.

*/
int64_t* csGetQWords(CSFile* source , size_t* length);

/*

	Reads the given file for an array of floats and returns it, storing its length in length.

	-source — the file to read
	-length — pointer to a size which will be filled in by this function
	-return Heap csAllocated pointer to an array of floats.

*/
float* csGetFloats(CSFile* source , size_t* length);

/*

	Reads the given file for an array of double floats and returns it, storing its length in length.

	-source — the file to read
	-length — pointer to a size which will be filled in by this function
	-return Heap csAllocated pointer to an array of double floats.

*/
double* csGetDoubles(CSFile* source , size_t* length);

#endif