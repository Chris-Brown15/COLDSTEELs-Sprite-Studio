package cs.csss.editor.event;

import static cs.core.utils.CSUtils.specify;
import static cs.csss.engine.Logging.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.Engine;
import cs.csss.engine.Pixel;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette.PalettePixel;
import cs.csss.project.LayerPixel;
import cs.csss.project.utils.Artboards;
import cs.csss.project.utils.RegionIterator;

/**
 * Flood fill fills a region of the artboard with the selected color. 
 * 
 * <p>
 * 	The modifications made to the artboard will be stored in the current layer, although the border of the fill can be from any layer.
 * </p>
 * <p>
 * 	The approach of this flood fill is to avoid recursion for evey pixel and avoid visiting pixels numerous times. This algorithm is 
 * 	willing to trade correctness for speed to handle exceptionally large tasks.
 * </p>
 * <p>
 * 	The fill begins with an iteration to the north and south. Once a border is found (a pixel not matching either the clicked 
 * 	pixel's color or the editor's color, or the last pixel on the artboard), iteration reverses direction, going back to the clicked point.
 * 	This step is called walking back.
 * 	For each row traveled on the way back to the clicked pixel, another iteration is spun up going east and west. This step is called
 * 	fanning.
 * </p>
 * <p>
 * 	After the fanning, a block of space has been recorded, but:
 * 	<ul>
 * 		<li> any open spaces touching the bottom or top of the fan that were left or right of the clicked point that are open and </li>
 * 		<li> any interior border shapes would block the east west iterations from reaching the true border </li>
 * 	</ul>
 *  would lead to sections being missed. Therefore, during the east/west iterations of the walking back step, the pixel above or below the 
 *  iteration's current pixel (depending upon if the walk back is walking back from north or walking back from south) is checked. If it is 
 *  not a border and is not marked as full, we know there is an open space, and we keep track of all contiguous open spaces. Once the east
 *  /west iteration completes, new north or south iterations are begun at the middle of each open region.  
 * </p>
 * <p>
 * 	Any filled in regions interior to the border will be missed if they have a pixel on the same row as the clicked pixel, therefore, after checking
 * 	everything else has been done, we iterate over the rows we've created via {@code ArtboardMod} and check the pixels SOUTH of the row. If any is
 * 	not filled in but should be, a new iteration is started.
 * </p>
 * 
 * @author Chris Brown
 *
 */
@RenderThreadOnly public class FloodFillEvent extends CSSSEvent {	

	private static final int NORTH = 0b1, SOUTH = 0b10 , EAST = 0b100 , WEST = 0b1000;
	
	private Artboard artboard;
	
	private int clickedX , clickedY;
	
	private final Pixel activeColor , clickedPixel;
	
	private LayerPixel[][] priorRegion , newRegion;
	private int bottomY = Integer.MAX_VALUE , leftmostX = Integer.MAX_VALUE , width , height;
	
	private List<ArtboardMod> mods = Collections.synchronizedList(new ArrayList<>());	 	
	
	/**
	 * Creates a floor fill event.
	 * 
	 * @param artboard — the artboard to fill in
	 * @param activeColor — the color active in the left hand side panel's color picker
	 * @param clickedX — x coordinate of the clicked pixel
	 * @param clickedY — y coordinate of the clicked pixel
	 */
	public FloodFillEvent(Artboard artboard , Pixel activeColor , int clickedX , int clickedY ) {

		super(true , false);
		
		this.artboard = artboard;
		
		this.activeColor = activeColor;	
		clickedPixel = artboard.getHighestRankingColorForLayerModification(clickedX, clickedY);
		
		this.clickedX = clickedX;
		this.clickedY = clickedY;
	
	}

	private ArtboardMod addMod(int x , int y , int width , int height) {
		
		if(x < 0) x = 0;
		if(y < 0) y = 0;
		
		ArtboardMod mod = new ArtboardMod(x , y , width , height);
		
		synchronized(mods) {
			
			mods.add(mod);
			
		}
		
		return mod;
		
	}
	
	/**
	 * Returns whether the given region has marked as modded or not. This will return true if the given point is within the bounds defined 
	 * by the {@code ArtboardMod}.
	 *
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @return true if this point is within a region that has been marked as modded.
	 */
	private boolean markedAsModded(int xIndex , int yIndex) {
		
		if(xIndex < 0 || yIndex < 0) return true;
		if(xIndex >= artboard.width() || yIndex >= artboard.height()) return true;
		
		synchronized(mods) {

			for(ArtboardMod mod : mods) {
				
				if(xIndex >= mod.x && mod.x + mod.width > xIndex && yIndex >= mod.y && mod.y + mod.height > yIndex) return true;
				
			}
			
		}
		
		return false;
		
	}
	
	/**
	 * Pixels are valid borders if they are the edge of the artboard or are not the color the user clicked or the active color.
	 * 
	 * @param xIndex — x index of a pixel
	 * @param xIndex — y index of a pixel
	 * @return {@code true} if the given pixel is a valid border.
	 */
	private boolean isValidBorder(int xIndex , int yIndex) {
		
		//we'll say a point is a valid border if it less than 0 || greater than the artboard's dimension
		
		if(xIndex < 0 || yIndex < 0) return true;
		if(xIndex >= artboard.width() || yIndex >= artboard.height()) return true;
		
		PalettePixel index = artboard.getHighestRankingColorForLayerModification(xIndex, yIndex);
		return index != null && index.compareTo(clickedPixel) != 0;
				
	}

	/* 
	 * [Find Border Methods]
	 */

	/**
	 * Returns the x index of a pixel that is a valid border. This method goes from the east to the end of the artboard and returns the 
	 * index of the border itself.
	 * 
	 * @param startingX — x index of a point to start at
	 * @param startingY — y index of a point to start at
	 * @return x index of a border pixel to the east of {@code (startingX , startingY)}
	 */
	private int findEasternBorder(int startingX , int startingY) { 
		
		while(!isValidBorder(startingX , startingY)) startingX++;
		return startingX;
		
	}

	/**
	 * Returns the x index of a pixel that is a valid border. This method goes from the west to the end of the artboard and returns the 
	 * index of the border itself.
	 * 
	 * @param startingX — x index of a point to start at
	 * @param startingY — y index of a point to start at
	 * @return x index of a border pixel to the west of {@code (startingX , startingY)}
	 */
	private int findWesternBorder(int startingX , int startingY) {
		
		while(!isValidBorder(startingX , startingY)) startingX--;		
		return startingX;
		
	}
		
	/*
	 * Walk Methods
	 * 
	 * These are intended the first steps of filling a region of pixels
	 *
	 * They go from a starting position until a valid border is found. They return the index of the corresponding direction of the last 
	 * pixel that was found before a border
	 * 
	 */
	
	private int walkNorth(int startingX , int startingY) {
		
		specify(startingX >= 0 && startingX < artboard.width() , startingX + " is not a valid start x index.");		
		if(startingY >= artboard.height() || startingY < 0) return startingY;
		
		while(!isValidBorder(startingX , startingY)) startingY++;
		return startingY - 1;
		
	}

	private int walkSouth(int startingX , int startingY) {

		specify(startingX >= 0 && startingX < artboard.width() , startingX + " is not a valid start x index.");
		if(startingY >= artboard.height() || startingY < 0) return startingY;
		
		while(!isValidBorder(startingX , startingY)) startingY--;		
		return startingY + 1;
		
	}
	
	/**
	 * Main operator method for this event. This method goes from its starting position either west or east by the 
	 * {@code horizontalDirection} parameter, looking for a border in the horizontal direction. While it looks for the border, pixels above
	 * or below the current one determined by {@code backFrom} are checked and if they are not valid borders and are not marked as visited,
	 * a new iteration task begins from the midpoint of the unvisited row.
	 * 
	 * @param startingX — starting x of a pixel
	 * @param startingY — starting y of a pixel
	 * @param horizontalDirection — one of {@link FloodFillEvent#EAST} or {@link FloodFillEvent#WEST}
	 * @param backFrom — one of {@link FloodFillEvent#NORTH} or {@link FloodFillEvent#SOUTH}
	 * @return Index of the pixel that touches a border when seeking a border toward the {@code horizontalDirection}.
	 */
	private int findHorizontalBorderWalkingBack(int startingX , int startingY , int horizontalDirection , int backFrom) {
		
		//verify parameters
		specify(startingY >= 0 && startingY < artboard.height() , startingY + " is not a valid start y index.");
		if(startingX >= artboard.width() || startingX < 0) return startingX;
		
		//store parameters as booleans
		boolean eastward = horizontalDirection == EAST , northward = backFrom == NORTH;
		
		//this loop looks for a valid border on the current row
		while(!isValidBorder(startingX , startingY) && !markedAsModded(startingX , startingY)) {
			
			//this section looks at pixels above or below the current row based on what we direction we are walking back from and begins a
			//new set of iterations on those regions
			//check index of the y index of the row above or below this one
			int checkIndex = northward ? startingY + 1 : startingY - 1;
			if(!isValidBorder(startingX, checkIndex) && !markedAsModded(startingX, checkIndex)) {
				
				//this finds the index of the end of the row which we know is invalid because we passed the if condition above
				int otherRowEndpoint = eastward ? findEasternBorder(startingX, checkIndex) - 1 : findWesternBorder(startingX, checkIndex) + 1;
				//computes the middle of the invalid row, which is where we start our iteration
				int min = min(startingX , otherRowEndpoint);
				int max = max(startingX , otherRowEndpoint);
				int mid = min + (max - min) / 2;

				//begin the new set of iterations on the invalid region. This starts the new iteration in the middle of the invalid row as
				//a best guess as to what point would have the highest ceiling.
				initialRow(startingX , checkIndex);				
				if(northward) startNorthernIteration(mid , checkIndex);
				else startSouthernIteration(mid , checkIndex);

			}
			
			//here is where we actually look for the border on the current row, the variables change baseds on the direction we are going. 
			if(eastward) startingX++;
			else startingX--;		
			
		}

		return eastward ? startingX - 1 : startingX + 1; 
		
	}
	
	/**
	 * Iterates over the row described by the given mod, then finds if any pixel in the check direction (which should be covered by the previous 
	 * mod) is not a valid border or marked pixel. If such a pixel is found, a new iteration is begun. 
	 * 
	 * @param mod — the mod to check
	 * @param checkDirection — direction to check for missing mods
	 */
	private void verifyPreviousRow(ArtboardMod mod , int checkDirection) {
		
		int checkY = checkDirection == NORTH ? mod.y + 1 : mod.y - 1;
		int max = mod.x + mod.width;
		for(int i = mod.x ; i < max ; i++) {
		
			if(isValidBorder(i , checkY) || markedAsModded(i , checkY)) continue;
			
			//start iteration and return
			int east = findEasternBorder(i, checkY);
			int west = findWesternBorder(i, checkY);

			int mid = west + (east - west) / 2;
			initialRow(mid , checkY);
			if(checkDirection == NORTH) startNorthernIteration(mid, checkY); 
			else startSouthernIteration(mid, checkY);		
			return;
				
		}		
		
	}
	
	/*
	 * Walk Backward methods
	 */
	
	private void walkBackFromNorth(int north , int startingX , int startingY) {
		
		while(north != startingY) {

			int east = findHorizontalBorderWalkingBack(startingX + 1 , north , EAST , NORTH);
			int west = findHorizontalBorderWalkingBack(startingX - 1 , north , WEST , NORTH);		
			addMod(west , north , (east - west) + 1 , 1);
			north--;
			
		}
		
	}
	
	private void walkUpFromSouth(int south , int startingX , int startingY) {
		
		while(south != startingY) {
			
			int east = findHorizontalBorderWalkingBack(startingX + 1 , south , EAST , SOUTH);	
			int west = findHorizontalBorderWalkingBack(startingX - 1 , south , WEST , SOUTH);
			addMod(west , south , (east - west) + 1 , 1);
			south++;
			
		}
		
	}
	
	private void startNorthernIteration(int startingX , int startingY) {

		if(startingY == artboard.height() - 1) return;
		
		int northern = walkNorth(startingX, startingY + 1);	
		walkBackFromNorth(northern , startingX , startingY);
		
	}
	
	private void startSouthernIteration(int startingX , int startingY) {

		if(startingY == 0) return;
		
		int southern = walkSouth(startingX , startingY - 1);
		walkUpFromSouth(southern , startingX , startingY);
		
	}

	private void initialRow(int startingX , int startingY) { 

		int eastern = findEasternBorder(startingX , startingY) - 1;
		int western = findWesternBorder(startingX , startingY) + 1;		
		addMod(western , startingY , (eastern - western) + 1 , 1);
		
	}

	private void handleMods() {

		sysDebug("Number of Mods: " + mods.size());
		synchronized(mods) {
			
			for(int i = 0 ; i < mods.size() ; i ++) { 
				
				ArtboardMod x = mods.get(i);
				artboard.putColorInImage2(x.x, x.y, x.width, x.height, activeColor);
				
			}
			
		}
		
		newRegion = artboard.getRegionOfLayerPixels(leftmostX, bottomY, width, height);
		
		mods.clear();
		
	}
	
	@Override public void _do() {

		Future<?> initialRow = Engine.THE_THREADS.submit(() -> initialRow(clickedX , clickedY));
		Future<?> northern = Engine.THE_THREADS.submit(() -> startNorthernIteration(clickedX , clickedY));
		Future<?> southern = Engine.THE_THREADS.submit(() -> startSouthernIteration(clickedX , clickedY));
		try {

			initialRow.get();
			northern.get();
			southern.get();
			
		} catch (InterruptedException | ExecutionException e) {
			
			e.printStackTrace();
						
		}
		
		//find missed parts by iterating over mods 
		mods.sort((mod1 , mod2) -> mod2.y - mod1.y);
		for(int i = 0 ; i < mods.size() ; i++) verifyPreviousRow(mods.get(i), SOUTH);
		if(priorRegion == null) {
			
			int greatestX = Integer.MIN_VALUE , highestY = Integer.MIN_VALUE;
			ArtboardMod x;
			for(int i = 0 ; i < mods.size() ; i++) {
				
			 	x = mods.get(i);
			 
			 	leftmostX = min(leftmostX , x.x);
				bottomY = min(bottomY , x.y);
				greatestX = max(greatestX , x.x + x.width);
				highestY = max(highestY , x.y + x.height);
				
			}
			
			width = greatestX - leftmostX;
			height = highestY - bottomY;
			
			priorRegion = artboard.activeLayer().get(leftmostX, bottomY, width , height);
						
		}
		
		if(newRegion == null) handleMods();
		else artboard.putColorsInImage(leftmostX , bottomY , width , height , newRegion);
		
	}

	@Override public void undo() {
		
		RegionIterator iter = Artboards.region(leftmostX, bottomY, width , height);
				
		int[] next;
		while(iter.hasNext()) {
			
			next = iter.next();
			int regionXIndex = next[0] - leftmostX;
			int regionYIndex = next[1] - bottomY;
			if(priorRegion[regionYIndex][regionXIndex] == null) artboard.removePixel(next[0], next[1]);
			
		}
		
		artboard.putColorsInImage(leftmostX , bottomY , width , height, priorRegion);
				
	}
		
	private record ArtboardMod(int x , int y , int width , int height) implements Comparable<ArtboardMod> {

		private static final String toStringFormat = "Mod: X: %d, Y: %d, W: %d, H: %d";
		
		@Override public int compareTo(ArtboardMod o) {

			if(this.y == o.y && this.x == o.x) return 0;
			if(this.y > o.y || (this.y == o.y && this.x > o.x)) return 1;
			return -1;
			
		}

		@Override public boolean equals(Object obj) {
			
			return compareTo((ArtboardMod) obj) == 0;
		
		}

		@Override public String toString() {
			
			return String.format(toStringFormat, x, y , width , height);
			
		}
		
	}

	@SuppressWarnings("unused") private void stackApproach() {
		
		Stack<PixelPosition> nextPositions = new Stack<>();
		
		nextPositions.add(new PixelPosition(clickedX , clickedY));
		
		while(!nextPositions.isEmpty()) { 
			
			PixelPosition current = nextPositions.pop();
			if(!isValidBorder(current.x, current.y) && !markedAsModded(current.x, current.y)) {
				
				addMod(current.x , current.y , 1 , 1);
								
				if(current.y + 1 < artboard.height() && !markedAsModded(current.x , current.y + 1)) {
					
					nextPositions.add(new PixelPosition(current.x , current.y + 1));

				}
				
				if(current.x + 1 < artboard.width() && !markedAsModded(current.x + 1, current.y)) { 

					nextPositions.add(new PixelPosition(current.x + 1, current.y));
					
				}
				
				if(current.x > 0 && !markedAsModded(current.x - 1 , current.y)) {
					
					nextPositions.add(new PixelPosition(current.x - 1 , current.y));

				}
				
				if(current.y > 0 && !markedAsModded(current.x , current.y - 1)) { 
					
					nextPositions.add(new PixelPosition(current.x , current.y - 1));

				}
			
			}
			
		}
		
	}

	private record PixelPosition(int x , int y) {}
	
}
