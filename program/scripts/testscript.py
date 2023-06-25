#init
isRenderEvent = True

def _do(artboard , editor):
	
	for i in Range(0 , 10 , 1):
		print(i)

	'''
	color = artboard.createPalettePixel([-1 , 0 , 0 , -1])

	for i in Range(0 , artboard.height() , step = 2):
		for j in Range(1 , artboard.width() , step = 2):
			artboard.writeToIndexTexture(j , i , 1 , 1 , color)
	'''
def undo(artboard , editor):	
	artboard.setToCheckeredBackground()