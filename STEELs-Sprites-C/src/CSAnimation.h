/*
	Licenced under MIT No Attribution.
*/
/*

	Container of animations for this library. The CSAnimation struct contains data for animations, and the update function processes the current
	state of the data.

*/
/*
	Ported from Java
*/
#ifndef CS_CS_STEELS_ANIMATIONS_H
#define CS_CS_STEELS_ANIMATIONS_H

#include "CSSprites.h"
#include "CTSAFile.h"
/*

	Timer object. Used by animations as a timing mechanism, specifically when the current animation frame is a swap by time frame.

*/
typedef struct {

	uint8_t started;
	double startTime;

} CSAnimationTimer;

/*
	
	Struct for individual frames. No members of this struct should be changed after its initial creation.

*/
typedef struct {

	float time;
	uint32_t updates;
	CSFrameSwapType swapType;

} CSAnimationFrame;

/*
	
	Container of CSAnimationFrames. No members of this struct should be changed after its initial creation.

*/
typedef struct {

	size_t numberFrames;
	CSAnimationFrame* frames;

} CSFrameSet;

/*

	Struct modeling an animation. This animation contains the only mutable state for animations. 

*/
typedef struct {

	CSFrameSet frameSet;
	CSAnimationTimer timer;

	char* name;

	float leftU;
	float bottomV;
	float topV;
	float widthU;

	uint32_t 
		frameWidthPixels ,
		frameHeightPixels;

	//these variables should not be cached just to be safe because its likely that a user could have multiple threads reading the UV coordinates
	//i.e., they do rendering in one thread, and other processing in another.

	volatile uint32_t
		currentFrame ,
		updates;

	volatile float
		currentLeftU ,
		currentRightU;

	void (*onUpdate)();
	void (*onUpdateReceiveUs)(const float newU , const float newV);

} CSAnimation;

/*

	Allocates a new CSAnimation struct on the heap with the library's allocate function, using the given data for initialization.

	-source — a CS_CTSAContents containing information from a .ctsa file; used to fill out the members of the returned struct	
	-sourceImagWidth — width in pixels of the image the resulting animation corresponds to
	-sourceImagHeight — height in pixels of the image the resulting animation corresponds to
	-return A newly allocated and initialized CSAnimation struct.

*/
CSAnimation* csAllocateCSAnimation(CS_CTSAContents* source , uint32_t sourceImageWidth , uint32_t sourceImageHeight);

/*

	Allocates a new CSAnimation struct on the heap, deep copying the members of the source animation, excluding callbacks. 
	
	-source — an existing animation
	-return A newly created, deep copied animation.

*/
CSAnimation* csAllocateCSAnimationCopy(CSAnimation* source);

/*

	Frees a given animation struct.
	
	-freeThis — address of a struct pointer to free

*/
void csFreeCSAnimation(CSAnimation* freeThis);

/*

	Updates the given animation. This checks its current frame's data against its timing mechanisms and advances to the next frame if needed.
	This method additionally updates U coordinates when a frame advance occurs.

	-animation — an animation to update

*/
void csUpdateAnimation(CSAnimation* animation);

/*

	Calculates and returns the total time in milliseconds this animation should take to complete. The resulting time from this function is based 
	on the assumption that every frame will take exactly millisecondsPerUpdate to complete.

	-animation — an animation whose total time is being queried
	-millisecondsPerUpdate — number of milliseconds an update is expected to take; can be 0 if all frames of the animation swap by time
	-return Number of milliseconds animation takes.

*/
double csGetAnimationTotalMilliseconds(CSAnimation* animation , double millisecondsPerUpdate);

#endif