'''

This file is an example for a selecting brush. Selecting brushes have rectangular bounders which are used to select regions of artboards. This type of brush functions the same 
way as other brush types but they have a few additional features which you can see in action in other files found inside the folder this one is found in.

'''
from cs.csss.engine import Control
from cs.csss.editor.brush import CSSSSelectingBrush

tooltip = "Simple Brush"
stateful = True
isRenderEvent = False
isTransientEvent = False

def __SimpleSelectorBrush2(tooltip):
	return SimpleSelectingBrush(tooltip)

class SimpleSelectingBrush(CSSSSelectingBrush):
	def use(self , artboard , editor , xIndex , yIndex):
		pass

	def canUse(self , artboard , editor , xIndex , yIndex):
		return False

	def update(self , artboard , editor):
		cursorPosition = editor.cursorCoords()
		if Control.MOVE_SELECTION_AREA.pressed():
			self.selectionBounder.moveCorner(cursorPosition[0] , cursorPosition[1])