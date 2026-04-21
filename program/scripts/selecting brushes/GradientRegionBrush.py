'''
'''
from cs.csss.engine import Control
from cs.csss.project.utils import Artboards
from cs.csss.utils import ScriptingUtils2
from cs.csss.editor.brush import CSSSSelectingBrush
from cs.csss.editor.event import CSSSEvent

tooltip = "Colors a region to a gradient"
stateful = True
isRenderEvent = True
isTransientEvent = False

def GradientRegionBrush(tooltip):
	return GradientRegionBrush(tooltip)

class GradientRegionBrush(CSSSSelectingBrush):
	def use(self , artboard , editor , xIndex , yIndex):
		bounder = self.selectionBounder
		return GradientRegionEvent(artboard , bounder.LX() , bounder.BY() , bounder.width() , bounder.height())

	def canUse(self , artboard , editor , xIndex , yIndex):
		return Control.ARTBOARD_INTERACT.pressed()

	def update(self , artboard , editor):
		cursorPosition = editor.cursorCoords()		
		if Control.MOVE_SELECTION_AREA.pressed():
			self.selectionBounder.moveCorner(ScriptingUtils2.doubleToInt(cursorPosition[0]) , ScriptingUtils2.doubleToInt(cursorPosition[1]))

		if artboard == None:
			return 

		self.selectionBounder.snapBounderToCoordinates(
			ScriptingUtils2.doubleToInt(artboard.leftX()) , 
			ScriptingUtils2.doubleToInt(artboard.rightX()) , 
			ScriptingUtils2.doubleToInt(artboard.bottomY()) , 
			ScriptingUtils2.doubleToInt(artboard.topY())
		)

class GradientRegionEvent(CSSSEvent):
	def __init__(self , artboard , leftX , bottomY , width , height):
		super(CSSSEvent , self).__init__(True , False)
		self.artboard = artboard
		indices = artboard.worldToPixelIndices(leftX , bottomY)
		self.leftX = indices[0]
		self.bottomY = indices[1]
		self.width = width
		self.height = height
		self.regionPreviousContents = artboard.getRegionOfLayerPixels(self.leftX , self.bottomY , width , height)

	def _do(self):
		regionIter = Artboards.region(self.leftX , self.bottomY , self.width , self.height)
		regionSpaceIter = Artboards.region(0 , 0 , self.width , self.height)
		length = self.artboard.activeLayerChannelsPerPixel()
		colorArray = ScriptingUtils2.bytes(length)
		colorArray[length - 1] = -1
		channelValue = 0
		channelValueAsByte = 0
		while regionIter.hasNext():
			regionIndices = regionIter.next()
			regionSpaceIndices = regionSpaceIter.next()
			heightwisePercentage = float(regionSpaceIndices[1]) / float(self.height)
			widthwisePercentage = float(regionSpaceIndices[0]) / float(self.width)
			channelValue = (widthwisePercentage * 127) + (heightwisePercentage * 127)
			channelValueAsByte = ScriptingUtils2.doubleToByte(channelValue)
			#sets up color array
			i = 0
			while i < length -1:
				colorArray[i] = channelValueAsByte
				i = i + 1

			pixelColor = self.artboard.createPalettePixel(colorArray)
			self.artboard.putColorInImage(regionIndices[0] , regionIndices[1] , 1 , 1 , pixelColor)

	def undo(self):
		regionIter = Artboards.region(self.leftX , self.bottomY , self.width , self.height)
		regionSpaceIter = Artboards.region(0 , 0 , self.width , self.height)
		while regionIter.hasNext():
			regionIndices = regionIter.next()
			regionSpaceIndices = regionSpaceIter.next()
			nextPixel = self.regionPreviousContents[regionSpaceIndices[1]][regionSpaceIndices[0]]
			if nextPixel == None:
				self.artboard.removePixel(regionIndices[0] , regionIndices[1])
			else:
				self.artboard.putColorInImage(regionIndices[0] , regionIndices[1] , 1 , 1 , nextPixel)
