from cs.csss.utils import ScriptingUtils
from cs.csss.editor.event import CSSSEvent

isRenderEvent = True
isTransientEvent = True

def ColoredGradient(artboard , editor):
	return ColoredGradientEvent(artboard , editor)

class ColoredGradientEvent(CSSSEvent):
	def __init__(self , artboard , editor):
		super(CSSSEvent , self).__init__(True , True)
		self.artboard = artboard
		self.editor = editor

	def _do(self):
		row = 0
		col = 0
		artboard = self.artboard
		height = artboard.height()
		width = artboard.width()
		length = artboard.activeLayerChannelsPerPixel()

		colorArray = ScriptingUtils.bytes(length)
		colorArray[length - 1] = -1

		channelValue = 0
		channelValueAsByte = 0 

		while row < height:

			heightwisePercentage = float(row) / float(height)

			while col < width:

				widthwisePercentage = float(col) / float(width)
				channelValue = (widthwisePercentage * 127) + (heightwisePercentage * 128)
				channelValueAsByte = ScriptingUtils.doubleToByte(channelValue)
				colorArray[0] = 0
				colorArray[1] = 0
				colorArray[2] = channelValueAsByte
				pixelColor = artboard.createPalettePixel(colorArray)
				artboard.putColorInImage(col , row , 1 , 1 , pixelColor)
				col = col + 1

			col = 0
			row = row + 1
