def __colored_gradient(artboard , editor):
	return ColoredGradientEvent(artboard , editor)


class ColoredGradientEvent:
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

		colorArray = ArrayUtils.bytes(length)
		colorArray[length - 1] = -1

		channelValue = 0
		channelValueAsByte = 0 

		while row < height:

			heightwisePercentage = row / height

			while col < width:

				widthwisePercentage = col / width
				channelValue = (widthwisePercentage * 127) + (heightwisePercentage * 128)
				channelValueAsByte = PrimitiveUtils.toByte(channelValue)
				colorArray[0] = 0
				colorArray[1] = 0
				colorArray[2] = channelValueAsByte
				pixelColor = artboard.createPalettePixel(colorArray)
				artboard.putColorInImage(col , row , 1 , 1 , pixelColor)
				col = col + 1

			col = 0
			row = row + 1
