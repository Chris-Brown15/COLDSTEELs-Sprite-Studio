/*
	Licenced under MIT No Attribution.
*/
#include "CSAnimation.h"
#include "CSSprites.h"

/*
	Function Prototypes
*/
static void initializeFrameSet(CSFrameSet* set , const size_t numberFrames , CSFrameChunk* sourceChunks);
static void freeFrameSet(CSFrameSet* frameSet);
static char* deepCopyString(char* source);
static void deepCopyFrameSet(CSFrameSet* destination , CSFrameSet* source);
static double elapsed(CSAnimationTimer* timer);
static void startTimer(CSAnimationTimer* timer);
static void bumpCurrentFrame(CSAnimation* animation);
static void bumpUs(CSAnimation* animation);

CSAnimation* csAllocateCSAnimation(CS_CTSAContents* source , uint32_t sourceImageWidth , uint32_t sourceImageHeight) {

	CSAnimation* animation = (CSAnimation*) csAllocate(sizeof(CSAnimation));
	animation->name = deepCopyString(source->animationName);	

	animation->leftU = source->leftU;
	animation->bottomV = source->bottomV;
	animation->topV = source->topV;
	animation->widthU = source->widthU;

	animation->frameWidthPixels = (uint32_t) (sourceImageWidth * animation->widthU);
	animation->frameHeightPixels = (uint32_t) (sourceImageHeight * (animation->topV - animation->bottomV));

	animation->currentFrame = 0;
	animation->updates = 0;
	animation->currentLeftU = animation->leftU;
	animation->currentRightU = animation->leftU + animation->widthU;

	initializeFrameSet(&animation->frameSet , source->numberFrames , source->chunks);

	animation->timer = (CSAnimationTimer) {0 , 0};

	return animation;

}

CSAnimation* csAllocateCSAnimationCopy(CSAnimation* source) {

	CSAnimation* animation = (CSAnimation*) csAllocate(sizeof(CSAnimation));

	animation->name = deepCopyString(source->name);
	deepCopyFrameSet(&animation->frameSet , &source->frameSet);

	animation->leftU = source->leftU;
	animation->bottomV = source->bottomV;
	animation->topV = source->topV;
	animation->widthU = source->widthU;
	animation->frameWidthPixels = source->frameWidthPixels;
	animation->frameHeightPixels = source->frameHeightPixels;

	animation->currentFrame = 0;
	animation->updates = 0;
	animation->currentLeftU = animation->leftU;
	animation->currentRightU = animation->leftU + animation->widthU;

	animation->timer = (CSAnimationTimer) {0 , 0};

	return animation;

}

void csFreeCSAnimation(CSAnimation* freeThis) {

	freeFrameSet(&freeThis->frameSet);
	csFree(freeThis);

}

void csUpdateAnimation(CSAnimation* animation) {

	CSAnimationFrame current = animation->frameSet.frames[animation->currentFrame];

	uint8_t goToNextFrame = 0;

	if(current.swapType == CS_SWAP_BY_TIME) {

		if(elapsed(&animation->timer) >= current.time) goToNextFrame = 1;

	} else if(current.swapType == CS_SWAP_BY_UPDATES) {

		animation->updates++;
		if(animation->updates == current.updates) goToNextFrame = 1;

	}

	if(goToNextFrame) {

		bumpCurrentFrame(animation);
		bumpUs(animation);

		if(animation->onUpdate != NULL) animation->onUpdate();
		if(animation->onUpdateReceiveUs != NULL) animation->onUpdateReceiveUs(animation->currentLeftU , animation->currentRightU);

		startTimer(&animation->timer);
		animation->updates = 0;

	}

}

static void initializeFrameSet(CSFrameSet* set , const size_t numberFrames , CSFrameChunk* sourceChunks) {

	set->frames = (CSAnimationFrame*)csAllocate(sizeof(CSAnimationFrame) * numberFrames);
	set->numberFrames = numberFrames;

	for(size_t i = 0 ; i < numberFrames ; i++) {

		set->frames[i] = (CSAnimationFrame) {sourceChunks[i].time , sourceChunks[i].updates , sourceChunks[i].swapType};

	} 
	
}

static void freeFrameSet(CSFrameSet* frameSet) {

	csFree(frameSet->frames);

}

static char* deepCopyString(char* source) {

	size_t nameLength = 0;
	
	//get length of name and copy it
	while(source[nameLength++] != '\0');
	char* destination = (char*)csAllocate(sizeof(char) * nameLength);
	for(size_t i = 0 ; i < nameLength ; i++) destination[i] = source[i];

	return destination;

}

static void deepCopyFrameSet(CSFrameSet* destination , CSFrameSet* source) {

	size_t numberFrames = destination->numberFrames = source->numberFrames;
	destination->frames = (CSAnimationFrame*)csAllocate(sizeof(CSAnimationFrame) * numberFrames);

	for(size_t i = 0 ; i < numberFrames ; i++) {

		destination->frames[i] = (CSAnimationFrame) {source->frames[i].time , source->frames[i].updates , source->frames[i].swapType};

	} 
	
}

static double elapsed(CSAnimationTimer* timer) {

	double time = csGetTime();
	double start = timer->startTime;
	return time - start;

}

static void startTimer(CSAnimationTimer* timer) {

	timer->started = 1;
	timer->startTime = csGetTime();

}

 static void bumpCurrentFrame(CSAnimation* animation) {

	animation->currentFrame++;
	if(animation->currentFrame == animation->frameSet.numberFrames) animation->currentFrame = 0;

 }

 static void bumpUs(CSAnimation* animation) {

	if(animation->currentFrame > 0) {

		animation->currentLeftU += animation->widthU;
		animation->currentRightU += animation->widthU;

	} else {

		animation->currentLeftU = animation->leftU;
		animation->currentRightU = animation->leftU + animation->widthU;

	}

 }

 double csGetAnimationTotalMilliseconds(CSAnimation* animation , double millisecondsPerUpdate) {

 	double timeAccum = 0;
 	size_t numberFrames = animation->frameSet.numberFrames;
 	CSAnimationFrame* set = animation->frameSet.frames;

 	for(size_t i = 0 ; i < numberFrames ; i++) {

 		if(set[i].swapType == CS_SWAP_BY_TIME) timeAccum += set[i].time;
 		else timeAccum += set[i].updates * millisecondsPerUpdate;

 	}

 	return timeAccum;

 }