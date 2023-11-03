#include "CSFileOperations.h"
#include "CSSprites.h"

/*
	Function Prototypes
*/
static CSByteOrder determineByteOrder();
static void* format(void* formatDestination , void* formatThis , const size_t length);
static CSOperationResult writeSize(CSFile* destination , size_t listLength);
static CSOperationResult writePointer(CSFile* destination , void* pointer , const size_t elementSize , const size_t elements);
static void verifyResult(CSFile* file , CSOperationResult result);
static size_t readLength(CSFile* file);

//Determines the size of the temporary buffer csAllocated on the stack for formatting, reading, and writing.
#define MAX_IO_SIZE 8

#define zero(array , size) for(size_t i = 0 ; i < size ; i++) array[i] = 0

/*
	IO Helpers and Constants
*/
static const uint32_t 
	maxValue6Bits  = 0b111111 ,
	maxValue14Bits = 0b11111111111111 ,
	maxValue22Bits = 0b1111111111111111111111 ,
	maxValue30Bits = 0b111111111111111111111111111111;

static CSByteOrder 	
	nativeOrder ,
	currentByteOrder;

void csInitializeFileOperations(void) {
		
		//runtime check the native byte order
		nativeOrder = determineByteOrder();		
		//current byte order always defaults to big endian.
		currentByteOrder = CS_BIG_ENDIAN;

}

CSByteOrder csGetNativeByteOrder() {

	return nativeOrder;

}

void csSetCurrentByteOrder(const CSByteOrder newCurrentByteOrder) {

	currentByteOrder = newCurrentByteOrder;

}
 
CSByteOrder csGetCurrentByteOrder(){

	return currentByteOrder;

}

void csPutByte(CSFile* destination , int8_t value) {

	csassert(destination);
	verifyResult(destination , write(destination , &value , 1));

}

void csPutWord(CSFile* destination , int16_t value) {

	csassert(destination);	
	int8_t writeBuffer[MAX_IO_SIZE];
	verifyResult(destination , write(destination , format(writeBuffer , &value , 2) , 2));

}

void csPutDWord(CSFile* destination , int32_t value) {

	csassert(destination);
	int8_t writeBuffer[MAX_IO_SIZE];
	verifyResult(destination , write(destination , format(writeBuffer , &value , 4) , 4));

}

void csPutQWord(CSFile* destination , int64_t value) {

	csassert(destination);
	int8_t writeBuffer[MAX_IO_SIZE];
	verifyResult(destination , write(destination , format(writeBuffer , &value , 8) , 8));

}

void csPutFloat(CSFile* destination , float value) {

	csassert(destination);
	int8_t writeBuffer[MAX_IO_SIZE];
	size_t size = sizeof(float);
	verifyResult(destination , write(destination , format(writeBuffer , &value , size) , size));

}

void csPutDFloat(CSFile* destination , double value) {

	csassert(destination);
	int8_t writeBuffer[MAX_IO_SIZE];
	size_t size = sizeof(double);
	verifyResult(destination , write(destination , format(writeBuffer , &value , size) , size));

}

void csPutBytes(CSFile* destination , void* pointer , const size_t numberElements) {

	verifyResult(destination , writePointer(destination , pointer , 1 , numberElements));

}

void csPutWords(CSFile* destination , void* pointer , const size_t numberElements) {

	verifyResult(destination , writePointer(destination , pointer , 2 , numberElements));

}

void csPutDWords(CSFile* destination , void* pointer , const size_t numberElements) {

	verifyResult(destination , writePointer(destination , pointer , 4 , numberElements));	

}

void csPutQWords(CSFile* destination , void* pointer , const size_t numberElements) {

	verifyResult(destination , writePointer(destination , pointer , 8 , numberElements));

}

void csPutFloats(CSFile* destination , float* pointer , const size_t numberElements) {
	
	verifyResult(destination , writePointer(destination , pointer , sizeof(float) , numberElements));

}

void csPutDFloats(CSFile* destination , double* pointer , const size_t numberElements) {

	verifyResult(destination , writePointer(destination , pointer , sizeof(double) , numberElements));

}

int8_t csGetByte(CSFile* source) {

	csassert(source);
	int8_t readBuffer = 0;
	verifyResult(source , read(source , &readBuffer , 1));
	return readBuffer;

}

int16_t csGetWord(CSFile* source) {

	csassert(source);
	int16_t readBuffer = 0;
	int16_t formatBuffer = 0;
	verifyResult(source , read(source , &readBuffer , 2));
	format(&formatBuffer , &readBuffer , 2);
	return formatBuffer;

}

int32_t csGetDWord(CSFile* source) {

	csassert(source);
	int32_t readBuffer = 0;
	int32_t formatBuffer = 0;	
	verifyResult(source , read(source , &readBuffer , 4));
	format(&formatBuffer , &readBuffer , 4);
	return formatBuffer;

}

int64_t csGetQWord(CSFile* source) {

	csassert(source);
	int64_t readBuffer = 0;
	int64_t formatBuffer = 0;
	verifyResult(source , read(source , &readBuffer , 8));
	format(&formatBuffer , &readBuffer , 8);
	return formatBuffer;

}

float csGetFloat(CSFile* source) {
	
	csassert(source);
	float readBuffer = 0;
	float formatBuffer = 0;
	verifyResult(source , read(source , &readBuffer , sizeof(float)));
	format(&formatBuffer , &readBuffer , sizeof(float));
	return formatBuffer;

}

double csGetDFloat(CSFile* source) {

	csassert(source);
	double readBuffer = 0 , formatBuffer = 0;
	verifyResult(source , read(source , &readBuffer , 8));
	format(&formatBuffer , &readBuffer , 8);
	return formatBuffer;

}

int8_t* csGetBytes(CSFile* source , size_t* length) {

	csassert(source) ; csassert(length);

	size_t pointerLength = *length = readLength(source);
	int8_t* pointer = (int8_t*)csAllocate(pointerLength);
	for(uint32_t i = 0 ; i < pointerLength ; i ++) pointer[i] = csGetByte(source);
	return pointer;

}

int16_t* csGetWords(CSFile* source , size_t* length) {

	csassert(source) ; csassert(length);

	size_t pointerLength = *length = readLength(source);	
	int16_t* pointer = (int16_t*)csAllocate(2 * pointerLength);
	for(uint32_t i = 0 ; i < pointerLength ; i ++) pointer[i] = csGetWord(source);
	return pointer;

}

int32_t* csGetDWords(CSFile* source , size_t* length) {

	csassert(source) ; csassert(length);

	size_t pointerLength = *length = readLength(source);
	int32_t* pointer = (int32_t*) csAllocate(4 * pointerLength);
	for(uint32_t i = 0 ; i < pointerLength ; i ++) pointer[i] = csGetDWord(source);
	return pointer;

}

int64_t* csGetQWords(CSFile* source , size_t* length) {

	csassert(source);
	csassert(length);

	size_t pointerLength = *length = readLength(source);	
	int64_t* pointer = (int64_t*)csAllocate(8 * pointerLength);
	for(uint32_t i = 0 ; i < pointerLength ; i ++) pointer[i] = csGetQWord(source);

	return pointer;

}

float* csGetFloats(CSFile* source , size_t* length) {

	csassert(source);
	csassert(length);

	size_t pointerLength = *length = readLength(source);	
	float* pointer = (float*)csAllocate(sizeof(float) * pointerLength);
	for(uint32_t i = 0 ; i < pointerLength ; i ++) pointer[i] = csGetFloat(source);

	return pointer;

}

double* csGetDoubles(CSFile* source , size_t* length) {

	csassert(source);
	csassert(length);

	size_t pointerLength = *length = readLength(source);	
	double* pointer = (double*)csAllocate(sizeof(double) * pointerLength);
	for(uint32_t i = 0 ; i < pointerLength ; i ++) pointer[i] = csGetFloat(source);

	return pointer;

}

/*

	Formats the given memory into the current byte order, storing the conversion in buffer. This function assumes its memory parameter is
	laid out according to the native byte order.

	-buffer — a buffer to store results in
	-memory — memory to format to the current byte order
	-size — number of bytes of memory memory occupies; must not be more than 8
	-return The buffer to write to disk.

*/
static inline void* format(void* buffer , void* memory , const size_t size) {

	//convert to writable types for convenience
	int8_t* destination = (int8_t*) buffer;
	int8_t* source = (int8_t*) memory;

	//reverses the bytes of memory into buffer
	if(currentByteOrder != nativeOrder) for(int offset = size - 1 , index = 0 ; offset >= 0 ; offset-- , index++) {

		destination[index] = source[offset];

	} else {

		for(size_t i = 0 ; i < size ; i++) destination[i] = source[i];

	}
	
	return destination;

}

/*
	Determines the Byte Order of the native machine by a runtime check. From Pointers in C, A Hands on Approach, page 45.

	-return The underlying machine's byte order.
*/
static CSByteOrder determineByteOrder() {

    int test = 0x0001;
    char* byte = (char*) &test;
    return byte[0] == 1 ? CS_LITTLE_ENDIAN : CS_BIG_ENDIAN;
    
}

/* DIRECTLY LIFTED FROM THE JAVA VERSION */

/*
	Computes the number of additional bytes a list size prefix would be for a list of size {@code length}.
	
	-length — arbitrary size of a list
	-return Number of additional bytes beyond the first a list size prefix would be for a list of size {@code length}.
 */
static uint32_t additionalListSizePrefixBytes(const size_t length) {

	if(length <= maxValue6Bits) return 0;
	else if(length <= maxValue14Bits) return 1;
	else if(length <= maxValue22Bits) return 2;
	else if(length <= maxValue30Bits) return 3;
	return -1;
	
}

static CSOperationResult writeSize(CSFile* destination , size_t listLength) {

	CSByteOrder currentOrder = csGetCurrentByteOrder();
	int doChangeOrder = currentOrder != CS_BIG_ENDIAN;
	if(doChangeOrder) csSetCurrentByteOrder(CS_BIG_ENDIAN);

	uint32_t listSizePrefixSize = additionalListSizePrefixBytes(listLength);
	//creates the integer used to store the size of the list and the two bit ending.
	//the listSizePrefixSize is the two bit part. It's shifted to the end of the integer, then the rest of the size is ORed on.
	uint32_t sizeComposed = (((listSizePrefixSize << (6 + (8 * listSizePrefixSize))) | listLength));

	int8_t writeBuffer[MAX_IO_SIZE];

	format(writeBuffer , &sizeComposed , listSizePrefixSize + 1);
	CSOperationResult result = write(destination , writeBuffer , listSizePrefixSize + 1);

	if(doChangeOrder) csSetCurrentByteOrder(currentOrder);

	return result;

}

/*

	Writes a pointer type to the given file destination.
	-destination — a file to write to
	-pointer — a pointer to a region of memory that will be written
	-elementSize — size in bytes of a single element 
	-elements — number of elements to write

*/
static CSOperationResult writePointer(CSFile* destination , void* pointer , const size_t elementSize , const size_t elements) {

	csassert(destination);
	csassert(pointer);
	
	CSOperationResult result = writeSize(destination , elements);
	if(result != CS_NO_ERROR) return result;
		
	if(elementSize <= 8) {
	
		int8_t writeBuffer[MAX_IO_SIZE];
		for(size_t i = 0 ; i < elements ; i++) { 

			result = write(destination , format(writeBuffer , ((uint8_t*)pointer + (i * elementSize)) , elementSize) , elementSize);
			if(result != CS_NO_ERROR) return result;

		}

	} else {

		char largerBuffer[elementSize];	
		for(size_t i = 0 ; i < elements ; i++) { 
			
			result = write(destination , format(largerBuffer , (uint8_t*)pointer + (i * elementSize) , elementSize) , elementSize);
			if(result != CS_NO_ERROR) return result;

		}

	}

	return result;

}

static void verifyResult(CSFile* file , CSOperationResult result) {

	if(result <= 0) {

		#ifdef CS_STEELS_SPRITES_USING_STD_LIB 			

			if(feof(file)) errorOut(CS_EOF_ERROR);

		#endif

		errorOut(CS_IO_ERROR);

	}

}

static size_t readLength(CSFile* file) {

	CSByteOrder currentOrder = csGetCurrentByteOrder();
	int doChangeOrder = currentOrder != CS_BIG_ENDIAN;
	if(doChangeOrder) csSetCurrentByteOrder(CS_BIG_ENDIAN);

	uint8_t first = 0;	
	verifyResult(file , read(file , &first , 1));
	uint8_t remaining = first >> 6;
	first &= maxValue6Bits;
	size_t result = first;

	if(remaining > 0) { 

		size_t sizeSize = sizeof(size_t);
		uint8_t 
			readBuffer[sizeSize] ,
			formatBuffer[sizeSize];

		zero(readBuffer , sizeSize) ; zero(formatBuffer , sizeSize);
		verifyResult(file , read(file , readBuffer , remaining));
		format(formatBuffer , readBuffer , remaining);
		formatBuffer[remaining] = first;
		result = *(size_t*)formatBuffer;

	}

	if(doChangeOrder) csSetCurrentByteOrder(currentOrder);

	return result;

}