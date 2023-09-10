from cs.csss.scripting import ArrayUtils

isRenderEvent = True

def _do(artboard , editor):
	colorArray = ArrayUtils.bytes(artboard.activeLayerChannelsPerPixel())
	colorArray[0] = 0xff
	colorArray[1] = 0x0
	colorArray[2] = 0x0
	colorArray[3] = 0xff
	artboard.putColorInImage(0 , 0 , 1 , artboard.height() , artboard.createPalettePixel(colorArray))
	colorArray[0] = 0
	colorArray[1] = 0xff
	artboard.putColorInImage(artboard.width() - 1 , 0 , 1 , artboard.height() , artboard.createPalettePixel(colorArray))
