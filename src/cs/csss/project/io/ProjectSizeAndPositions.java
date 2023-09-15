package cs.csss.project.io;

/**
 * Container for data about exporting a project.
 */
public record ProjectSizeAndPositions(
	float leftmostX , 
	float rightmostX , 
	float lowermostY , 
	float uppermostY , 
	float width , 
	float height , 
	float midpointX , 
	float midpointY
) {}