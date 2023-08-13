package cs.csss.project.io;

public record ProjectSizeAndPositions(
	int leftmostX , 
	int rightmostX , 
	int lowermostY , 
	int uppermostY , 
	int width , 
	int height , 
	int midpointX , 
	int midpointY
) {}