'''
	Converts a subregion of an artboard to a new artboard.
'''
from cs.csss.engine import Control
from cs.csss.project.utils import Artboards
from cs.csss.utils import ScriptingUtils2
from cs.csss.editor.event import NOPEvent
from cs.csss.editor.brush import CSSSSelectingBrush
from cs.csss.editor.event import CSSSEvent

tooltip = "Copies the contents of the selected region into a new artboard."
stateful = True
isRenderEvent = True
isTransientEvent = True

def SubRegionToArtboard(tooltip):
	return SubRegionToArtboardBrush(tooltip)

class SubRegionToArtboardBrush(CSSSSelectingBrush):
	def use(self , artboard , editor , xIndex , yIndex):
		bounder = self.selectionBounder
		indices = artboard.worldToPixelIndices(bounder.LX() , bounder.BY())
		bounderWidth = bounder.width()
		region = artboard.getRegionOfLayerPixels(indices[0] , indices[1] , bounderWidth , bounder.height())
		return SubRegionToArtboardEvent(region , editor.project() , bounder.width() , bounder.height())

	def canUse(self , artboard , editor , xIndex , yIndex):
		self.selectionBounder.snapBounderToCoordinates(
			ScriptingUtils2.doubleToInt(artboard.leftX()) ,
			ScriptingUtils2.doubleToInt(artboard.rightX()) ,
			ScriptingUtils2.doubleToInt(artboard.bottomY()) ,
			ScriptingUtils2.doubleToInt(artboard.topY()) ,
		)
		return Control.ARTBOARD_INTERACT.struck()

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
			ScriptingUtils2.doubleToInt(artboard.topY()) ,
		)

class SubRegionToArtboardEvent(CSSSEvent):
	def __init__(self , pixelRegion , project , regionWidth , regionHeight):
		super(CSSSEvent , self).__init__(True , True)
		self.region = pixelRegion
		self.project = project
		self.regionWidth = regionWidth
		self.regionHeight = regionHeight

	def _do(self):
		newArtboard = self.project.createArtboard(self.regionWidth , self.regionHeight)
		newArtboard.putColorsInImage(0 , 0 , self.regionWidth , self.regionHeight , self.region)

