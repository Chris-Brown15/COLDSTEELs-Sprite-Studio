#init
isRenderEvent = True

def _do(artboard , editor):
	from cs.csss.scripting import ArrayUtils
	array = ArrayUtils.bytes(4)
	array[0] = -1
	array[1] = 0
	array[2] = 0
	array[3] = -1
	i = 0 
	width = artboard.width()
	height = artboard.height()
	pixel = artboard.createPalettePixel(array)
	while i < width:
		artboard.putColorInImage(i , 0 , 1 , height , pixel)
		i = i + 2

	i = 0
	while i < height:
		artboard.putColorInImage(0 , i , width , 1 , pixel)
		i += 2

def undo(artboard , editor):	
	artboard.setToCheckeredBackground()