/*
	Licenced under MIT No Attribution.
*/
#include "CSSprites.h"
#include "CSFileOperations.h"

static CSAllocate _csAllocate;
static CSFree _csFree;
static CSWrite _write;
static CSRead _read;
static CSGetMilliseconds _getTime;
static CSErrorOut _errorOut;

#ifdef CS_STEELS_SPRITES_USING_STD_LIB

	static size_t std_write(CSFile* file , void* source , size_t bytes) {

		return fwrite(source , 1 , bytes , file);

	}

	static size_t std_read(CSFile* source , void* destination , size_t bytes) {

		return fread(destination , 1 , bytes , source);

	}

	void csSteelSpritesInitialize(CSGetMilliseconds getTime , CSErrorOut errorOut) {

		_csAllocate = &malloc;
		_csFree = &free;
		_write = &std_write;
		_read = &std_read;
		_getTime = getTime;
		_errorOut = errorOut;
		csInitializeFileOperations();

	}

#else

	void csSteelSpritesInitialize(CSAllocate allocate , CSFree free , CSGetMilliseconds getTime , CSErrorOut errorOut) {

		_csAllocate = allocate;
		_csFree = free;
		_getTime = getTime;
		_errorOut = errorOut;
		csInitializeFileOperations();

}

#endif

void* csAllocate(const size_t bytes) {

	return _csAllocate(bytes);

}

void csFree(void* memory) {

	_csFree(memory);

}

size_t write(CSFile* file , void* source , size_t bytes) {

	return _write(file , source , bytes);

}

size_t read(CSFile* source , void* destination , size_t bytes) {

	return _read(source , destination , bytes);

}

void errorOut(const CSOperationResult result) {

	_errorOut(result);

}

double csGetTime(void) {

	return _getTime();

}