'''
	Converts a subregion of an artboard to a new artboard.
'''
from cs.csss.engine import Control
from cs.csss.project.utils import Artboards
from cs.csss.utils import ScriptingUtils
from cs.csss.editor.event import NOPEvent

tooltip = "Copies the contents of the selected region into a new artboard."
stateful = True
isRenderEvent = True
isTransientEvent = True

def __SubRegionToArtboard(brush):
	return SubRegionToArtboardBrush(brush)

class SubRegionToArtboardBrush:
	def __init__(self , brush):
		self.brush = brush

	def use(self , artboard , editor , xIndex , yIndex):
		bounder = self.brush.bounder
		indices = artboard.worldToPixelIndices(bounder.LX() , bounder.BY())
		bounderWidth = bounder.width()
		if bounderWidth % 2 == 0:
			region = artboard.getRegionOfIndexPixelsAlternate(indices[0] , indices[1] , bounderWidth , bounder.height())
		else:
			region = artboard.getRegionOfIndexPixels(indices[0] , indices[1] , bounderWidth , bounder.height())	
		return SubRegionToArtboardEvent(region , editor.project() , bounder.width() , bounder.height())

	def canUse(self , artboard , editor , xIndex , yIndex):
		return Control.ARTBOARD_INTERACT.struck()

	def update(self , artboard , editor):
		cursorPosition = editor.cursorCoords()		
		if Control.MOVE_SELECTION_AREA.pressed():
			self.brush.bounder.moveCorner(ScriptingUtils.doubleToInt(cursorPosition[0]) , ScriptingUtils.doubleToInt(cursorPosition[1]))

		from java.lang import System
		if Control.MOVE_OBJECT_UP.pressed():
			self.brush.bounder.translate(0 , 1)
			System.out.println("updating")

		if Control.MOVE_OBJECT_DOWN.pressed():
			self.brush.bounder.translate(0 ,-1)
			System.out.println("updating")

		if Control.MOVE_OBJECT_LEFT.pressed():
			self.brush.bounder.translate(-1 , 0)
			System.out.println("updating")

		if Control.MOVE_OBJECT_RIGHT.pressed():
			self.brush.bounder.translate(1 , 0)
			System.out.println("updating")


		if artboard == None:
			return

		self.brush.bounder.snapBounderToCoordinates(
			ScriptingUtils.doubleToInt(artboard.leftX()) ,
			ScriptingUtils.doubleToInt(artboard.rightX()) ,
			ScriptingUtils.doubleToInt(artboard.bottomY()) ,
			ScriptingUtils.doubleToInt(artboard.topY()) ,
		)


class SubRegionToArtboardEvent:
	def __init__(self , pixelRegion , project , regionWidth , regionHeight):
		self.region = pixelRegion
		self.project = project
		self.regionWidth = regionWidth
		self.regionHeight = regionHeight

	def _do(self):
		newArtboard = self.project.createArtboard(self.regionWidth , self.regionHeight)
		newArtboard.putColorsInImage(0 , 0 , self.regionWidth , self.regionHeight , self.region)

