from cs.csss.scripting import ArrayUtils
from cs.csss.scripting import PrimitiveUtils

isRenderEvent = True

def _do(artboard , editor):
	row = 0
	col = 0
	height = artboard.height()
	width = artboard.width()

	colorArray = ArrayUtils.bytes(artboard.activeLayerChannelsPerPixel())
	#sets the last byte to 255
	#TODO: do programmatically to acommodate different number of channels pixels
	colorArray[3] = -1

	channelValue = 0
	channelValueAsByte = 0 

	while row < height:

		heightwisePercentage = row / height

		while col < width:

			widthwisePercentage = col / width
			channelValue = (widthwisePercentage * 127) + (heightwisePercentage * 128)
			channelValueAsByte = PrimitiveUtils.toByte(channelValue)

			#sets the values of the array to the value of the pixel at the current position
			#TODO: do programmatically to acommodate different number of channels pixels
			colorArray[0] = channelValueAsByte
			colorArray[1] = channelValueAsByte
			colorArray[2] = channelValueAsByte

			pixelColor = artboard.createPalettePixel(colorArray)
			artboard.putColorInImage(col , row , 1 , 1 , pixelColor)
			col = col + 1

		col = 0
		row = row + 1

def undo(artboard , editor):
	artboard.setToCheckeredBackground()