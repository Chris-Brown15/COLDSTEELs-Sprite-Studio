/*
	Licenced under MIT No Attribution.
*/
#include "CTSAFile.h"

/*
	Function Prototypes.
*/
static char* wordPointerToChar(int16_t* words , const size_t numberWords);
static CS_CTSAContents* allocateCS_CTSAContents();
static CSFrameChunk* getChunks(CSFile* file , const size_t numberChunks);

CS_CTSAContents* csReadAnimationFile(CSFile* file) {

	CS_CTSAContents* fileContents = allocateCS_CTSAContents();

	size_t nameLength = 0;
	int16_t* name = csGetWords(file , &nameLength);

	fileContents->animationName = wordPointerToChar(name , nameLength);

	CSByteOrder currentOrder = csGetCurrentByteOrder();
	csSetCurrentByteOrder(csGetNativeByteOrder());

	fileContents->numberFrames = csGetDWord(file);
	fileContents->leftU = csGetFloat(file);
	fileContents->bottomV = csGetFloat(file);
	fileContents->topV = csGetFloat(file);
	fileContents->widthU = csGetFloat(file);
	fileContents->chunks = getChunks(file , fileContents->numberFrames);

	csSetCurrentByteOrder(currentOrder);

	return fileContents;

}

void csFreeCS_CTSAContents(CS_CTSAContents* freeThis) {

	csFree(freeThis->animationName);
	csFree(freeThis->chunks);
	csFree(freeThis);

}

static char* wordPointerToChar(int16_t* words , const size_t numberWords) {

	size_t sizeOfChar = sizeof(char);
	//assume big endian for the contents of the words
	char* asChars = (char*)csAllocate((numberWords * sizeOfChar) + 1);
	for(size_t i = 0 ; i < numberWords ; i++) asChars[i] = (char)words[i];
	asChars[numberWords] = '\0';
	return asChars;
	
}

static CS_CTSAContents* allocateCS_CTSAContents(void) {

	CS_CTSAContents* fileContentsStruct = (CS_CTSAContents*)csAllocate(sizeof(CS_CTSAContents));
	return fileContentsStruct;

}

static CSFrameChunk* getChunks(CSFile* file , const size_t numberChunks) {

	CSFrameChunk* chunks = (CSFrameChunk*)csAllocate(sizeof(CSFrameChunk) * numberChunks);
	for(size_t i = 0 ; i < numberChunks ; i++) {

		chunks[i].time = csGetFloat(file);
		chunks[i].updates = csGetDWord(file);
		chunks[i].swapType = csGetByte(file);

	}

	return chunks;

}