'''
Example implementation of the mandelbrot set based on:
https://en.wikipedia.org/wiki/Mandelbrot_set

Normalization function based on
https://stats.stackexchange.com/questions/281162/scale-a-number-between-a-range

This implementation is not optimized and may lock up the program for a time, especially for large artboards and slower machines.

'''

from cs.csss.scripting import PrimitiveUtils
from cs.csss.scripting import ArrayUtils
from java.lang import Math
from java.util import ArrayList
isRenderEvent = True
receiveArguments = True
argumentPopupMessage = "Input the number of iterations and scale mod. Default iterations to 1000 , the scale mod to 0."

iterations = 19

MANDELBROT_X_SCALE_MIN = -2.0
MANDELBROT_X_SCALE_MAX = 0.47
MANDELBROT_Y_SCALE_MIN = -1.12
MANDELBROT_Y_SCALE_MAX = 1.12

scaleMod = 0

def args(args):
	global palette
	global scaleMod
	global iterations
	iterations = int(args.get(0))
	scaleMod = float(args.get(1))
	palette = ArrayList(iterations)
	
def _do(artboard , editor):
	initializePalette(artboard)
	global width
	global height
	row = 0
	col = 0
	width = artboard.width()
	height = artboard.height()

	while row < height:
		y0 = mandelbrotNormalize(False , row)

		while col < width:
			x0 = mandelbrotNormalize(True , col)
			x = 0
			y = 0
			i = 0

			while x * x + y * y <= 2 * 2 and i < iterations:
				xtemp = x * x - y * y + x0
				y = 2 * x * y + y0
				x = xtemp
				i = i + 1

			if(i == iterations):
				i = iterations - 1
			artboard.putColorInImage(col , row , 1 , 1 , palette.get(i))
			col = col + 1			

		col = 0
		row = row + 1

def undo(artboard , editor):
	artboard.setToCheckeredBackground()

def mandelbrotNormalize(isX , coordinate):
	if isX:
		return (coordinate - 0) / (width - 0) * ((MANDELBROT_X_SCALE_MAX - scaleMod) - (MANDELBROT_X_SCALE_MIN + scaleMod)) + (MANDELBROT_X_SCALE_MIN + scaleMod)
	else:
		return (coordinate - 0) / (height - 0) * ((MANDELBROT_Y_SCALE_MAX - scaleMod) - (MANDELBROT_Y_SCALE_MIN + scaleMod)) + (MANDELBROT_Y_SCALE_MIN + scaleMod)

def initializePalette(artboard):	
	i = 0
	colorArray = ArrayUtils.bytes(4)
	
	colorArray[3] = -1
	
	while i < iterations:

		colorArray[0] = PrimitiveUtils.toByte(Math.abs(Math.pow(i , i)))
		colorArray[1] = PrimitiveUtils.toByte(Math.abs(Math.pow(i , 2)))
		colorArray[2] = PrimitiveUtils.toByte(Math.abs(Math.pow(i , 3)))
		putPixel(artboard , colorArray)
		i = i + 1
	
def putPixel(artboard , colorArray):
	pixel = artboard.createPalettePixel(colorArray)
	palette.add(pixel)
