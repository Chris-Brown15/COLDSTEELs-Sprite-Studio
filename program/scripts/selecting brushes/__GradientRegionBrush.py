'''
'''
from cs.csss.engine import Control
from cs.csss.project.utils import Artboards
from cs.csss.scripting import ArrayUtils
from cs.csss.scripting import PrimitiveUtils as prims

tooltip = "Colors a region to a gradient"
stateful = True
isRenderEvent = True
isTransientEvent = False

def __GradientRegionBrush(brush):
	return GradientRegionBrush(brush)

class GradientRegionBrush:
	def __init__(self , brush):
		self.brush = brush

	def use(self , artboard , editor , xIndex , yIndex):
		bounder = self.brush.bounder
		return GradientRegionEvent(artboard , bounder.LX() , bounder.BY() , bounder.width() , bounder.height())

	def canUse(self , artboard , editor , xIndex , yIndex):
		return Control.ARTBOARD_INTERACT.pressed()

	def update(self , artboard , editor):
		cursorPosition = editor.cursorCoords()		
		if Control.MOVE_SELECTION_AREA.pressed():
			self.brush.bounder.moveCorner(prims.toInt(cursorPosition[0]) , prims.toInt(cursorPosition[1]))

		if artboard == None:
			return 

		self.brush.bounder.snapBounderToCoordinates(
			prims.toInt(artboard.leftX()) , 
			prims.toInt(artboard.rightX()) , 
			prims.toInt(artboard.bottomY()) , 
			prims.toInt(artboard.topY())
		)

class GradientRegionEvent:
	def __init__(self , artboard , leftX , bottomY , width , height):
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
		colorArray = ArrayUtils.bytes(length)
		colorArray[length - 1] = -1
		channelValue = 0
		channelValueAsByte = 0
		while regionIter.hasNext():
			regionIndices = regionIter.next()
			regionSpaceIndices = regionSpaceIter.next()
			heightwisePercentage = regionSpaceIndices[1] / self.height
			widthwisePercentage = regionSpaceIndices[0] / self.width
			channelValue = (widthwisePercentage * 127) + (heightwisePercentage * 127)
			channelValueAsByte = prims.toByte(channelValue)
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
