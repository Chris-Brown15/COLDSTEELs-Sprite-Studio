/*
	Licenced under MIT No Attribution.
*/
/*

	File containing code responsible for loading and parsing CTSA files. CTSA files are files that contain all needed information about creating
	sprite sheet animations. 

*/
/*
	Ported from Java version.
*/

#ifndef CS_CTSA_FILE_H
#define CS_CTSA_FILE_H

#include "CSFileOperations.h"
#include "CSSprites.h"

/*

	Struct modeling a chunk of a .ctsa file which contains data for animations

*/
typedef struct {

	float time;
	uint32_t updates;
	CSFrameSwapType swapType;

} CSFrameChunk;

/*

	Struct modeling a .ctsa file. This is passed to allocation functions for CSAnimations.

*/
typedef struct {

	char* animationName;
	size_t numberFrames;

	float leftU;
	float bottomV;
	float topV;
	float widthU;

	CSFrameChunk* chunks;

} CS_CTSAContents;

/*
	
	Returns a read only CTSAContents struct pointer who can be used to create animations.
	-file — a file from which to read
	-return A heap allocated and initialized CTSAContents struct.

*/
CS_CTSAContents* csReadAnimationFile(CSFile* file);

/*

	Frees a CTSAContents struct and writes NULL to it.
	-freeThis — a pointer to free

*/
void csFreeCS_CTSAContents(CS_CTSAContents* freeThis);

#endif