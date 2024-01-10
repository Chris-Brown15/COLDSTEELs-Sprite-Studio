'''

This is an example of a modifying brush script. A modifying brush is type of brush with two additional components. 

1) radius variable
2) centerAroundRadius() method

The radius variable is used to model a modification radius for the brush. Its just a number but it can be adjusted by the slider in the left hand side panel to choose the 
radius. The centerAroundRadius() method is used to protect against out of bounds index errors. This method takes the indices the user clicked and the width and height of the 
artboard they clicked on, and returns an array of values which can be used to ensure no out of bounds indices are used. The array's contents are laid out as follows:
[0] is the left artboard coordinate of the region
[1] is the bottom artboard coordinate of the region
[2] is the width of the safe region
[3] is the height of the region
So you can iterate over the region defined by your radius by calling the centerAroundRadius() method and using its returned result to model a region of pixels on the artboard.

See the various comments in the code for my reasoning through this code.

'''
from cs.csss.engine import Control
from java.util import ArrayList
from cs.csss.project.utils import Artboards
from cs.csss.editor.brush import CSSSModifyingBrush
from cs.csss.editor.event import CSSSEvent
from cs.csss.utils import ScriptingUtils2

#our variables for this brush
tooltip = "Draws a diagonal line on th artboard."
stateful = False
#our variables for the event
isRenderEvent = True
isTransientEvent = False

#called from Java and creates the brush instance
def __DiagonalBrush(tooltip , stateful):
	return DiagonalBrush(tooltip , stateful)

class DiagonalBrush(CSSSModifyingBrush):
	#use must return an event object, it cannot return None
	def use(self , artboard , editor , xIndex , yIndex):
		#the editor supplies the color the user has selected from the color picker on the left hand side panel
		color = editor.currentColor()		
		#here we use the centerAroundRadius method. It uses the parameters we give it as well as the radius value of the brush (which is modified by the slider on the LHS 
		#panel), and produces an array which is very useful to stop from going out of bounds with modifications. Try revising this script by removing this method and see what 
		#happens when you try to modify an area close to the edge of the artboard (you'll crash).
		self.centerAroundRadius(xIndex , yIndex , artboard.width() , artboard.height())
		return DiagonalEvent(artboard , self.values[0] , self.values[1] , self.values[2] , self.values[3] , color)

	#canUse returns a boolean notating whether the use method should be called
	#Since this brush modifies a diagonal region of pixels, we can use this brush if at least one of the pixels on the current layer who lies on the region we want to change 
	#does
	#not match the pixel selected by the editor.
	def canUse(self , artboard , editor , xIndex , yIndex):
		#This is a typical idiom for canUse() methods within Sprite Studio. If the button the user has bound for interacting with the artboard is not pressed we usually can 
		#say
		#that we are not in a valid state to use the brush. Sometimes you may not want this to be the case, but most of the time, you do.
		if not Control.ARTBOARD_INTERACT.pressed():
			return False
		#cache the active layer
		activeLayer = artboard.activeLayer()
		#centerAroundRadius again to make sure our checks don't go out of bounds
		self.centerAroundRadius(xIndex , yIndex , artboard.width() , artboard.height())
		#pull out these variable for convenience.
		leftX = self.values[0]
		bottomY = self.values[1]
		totalHeight = bottomY + self.values[3]

		currentColor = editor.currentColor()

		#while we haven't crossed the total region. Since our rise over run is 1/1 and we have a square region of pixels, we only need to check one of the two variables; I use
		#the y coordinate to track the iteration
		while bottomY < totalHeight:
			#this method checks whether the given color (the first parameter), matches the color found in the artboard's current layer at the given indices. If they are not
			#identical, we know the event will have some effect, so we return true
			if not artboard.doColorsMatch(currentColor , leftX , bottomY):
				return True
			#advance iterators
			leftX = leftX + 1
			bottomY = bottomY + 1
		#if we haven't returned true by this point, we know we cannot modify the artboard, so return false. Since we returned False, use() will not be called.
		return False

#this is the event returned by the brush when we can actually use it.
class DiagonalEvent(CSSSEvent):
	#notice the init function does not do anything besides store variables in the instance.
	def __init__(self , artboard , leftX , bottomY , width , height , color):
		#initialize the super class, CSSSEvent
		super(CSSSEvent , self).__init__(True , False)
		self.artboard = artboard
		self.leftX = leftX
		self.bottomY = bottomY
		self.width = width
		self.height = height
		self.activeColor = color
		self.finalHeight = self.bottomY + self.height
		self.finalWidth =  self.leftX + self.width
		self.previousLayerPixels = ArrayList()

	def _do(self):
		#This if block will only happen the first time we invoke this method (remember it can be invoked again if the user redoes it, which can happen if the previously undid 
		#it) It stores the layer pixels in the active layer prior to making any change to the artboard.
		if(self.previousLayerPixels.size() == 0):			
			activeLayer = self.artboard.activeLayer()
			self.forEachOfRegion(lambda x , y: self.previousLayerPixels.add(activeLayer.get(x , y)))

		#iterate diagonally over the region and put the color the user selected in the artboard
		self.forEachOfRegion(lambda x , y: self.artboard.putColorInImage2(x , y , 1 , 1 , self.activeColor))
		
	#We can undo this event by putting what was previously in the layer back in it. This is why we cached the previous contents and now we can just put them back.
	def undo(self):		
		activeLayer = self.artboard.activeLayer()
		#this is a Java iterator
		layerPixelsIter = self.previousLayerPixels.iterator()
		currentX = self.leftX
		currentY = self.bottomY
		while layerPixelsIter.hasNext():			
			layerPixel = layerPixelsIter.next()			
			#A layer pixel would be None if that position of the layer was never modified. In this case we need to get either a pixel of a layer below it or a background color 
			#for the present position, which can be done with the removePixel method
			if layerPixel == None:
				self.artboard.removePixel(currentX , currentY)
			else:
				#Layer pixels store the indices they were located in, so we can use those in the putColorInImage() method.
				self.artboard.putColorInImage(currentX , currentY , 1 , 1 , layerPixel)

			currentX = currentX + 1
			currentY = currentY + 1

	#helper method for iterating diagonally
	def forEachOfRegion(self , callback):
		xIter = self.leftX
		yIter = self.bottomY
		while yIter < self.finalHeight and xIter < self.finalWidth:
			callback(xIter , yIter)
			xIter = xIter + 1
			yIter = yIter + 1