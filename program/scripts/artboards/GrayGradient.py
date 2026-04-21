from cs.csss.utils import ScriptingUtils2

def GrayGradient(artboard , editor):
	return GrayGradientEvent(artboard , editor)

isRenderEvent = True
isTransientEvent = False
takesArguments = False

class GrayGradientEvent:
	def __init__(self , artboard , editor):		
		self.artboard = artboard
		self.editor = editor

	def _do(self):
		row = 0
		col = 0
		artboard = self.artboard
		height = artboard.height()
		width = artboard.width()
		length = artboard.activeLayerChannelsPerPixel()

		colorArray = ScriptingUtils2.bytes(length)
		#sets the last byte to 255
		#TODO: do programmatically to acommodate different number of channels pixels
		colorArray[length - 1] = -1

		channelValue = 0
		channelValueAsByte = 0 

		while row < height:

			heightwisePercentage = float(row) / float(height)

			while col < width:

				widthwisePercentage = float(col) / float(width)
				channelValue = (widthwisePercentage * 127) + (heightwisePercentage * 128)
				channelValueAsByte = ScriptingUtils2.doubleToByte(channelValue)

				#sets the values of the array to the value of the pixel at the current position
				#TODO: do programmatically to acommodate different number of channels pixels
				i = 0
				while i < length -1:
					colorArray[i] = channelValueAsByte
					i = i + 1

				pixelColor = artboard.createPalettePixel(colorArray)
				artboard.putColorInImage(col , row , 1 , 1 , pixelColor)
				col = col + 1

			col = 0
			row = row + 1

	def undo(self):
		self.artboard.setToCheckeredBackground()
