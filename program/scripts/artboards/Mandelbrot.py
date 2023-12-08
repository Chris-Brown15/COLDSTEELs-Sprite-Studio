'''
Example implementation of the mandelbrot set based on:
https://en.wikipedia.org/wiki/Mandelbrot_set

Normalization function based on
https://stats.stackexchange.com/questions/281162/scale-a-number-between-a-range

This implementation is not optimized and may lock up the program for a time, especially for large artboards and slower machines.

'''

from cs.csss.utils import ScriptingUtils2
from java.lang import Math
from java.util import ArrayList

isRenderEvent = True
isTransientEvent = False
takesArguments = True
argumentDialogueText = "Input the number of iterations. Default iterations to 1000."

def Mandelbrot(artboard , editor , arguments):
	return MandelbrotEvent(artboard , editor , arguments)

class MandelbrotEvent:
	def __init__(self , artboard , editor , arguments):
		self.MANDELBROT_X_SCALE_MIN = -2.0
		self.MANDELBROT_X_SCALE_MAX = 0.47
		self.MANDELBROT_Y_SCALE_MIN = -1.12
		self.MANDELBROT_Y_SCALE_MAX = 1.12
		self.artboard = artboard
		self.editor = editor
		self.iterations = int(arguments.get(0))
		self.palette = ArrayList(self.iterations)
		self.width = artboard.width()
		self.height = artboard.height()

	def _do(self):
		self.initializePalette(self.artboard)
		row = 0
		col = 0
		while row < self.height:
			y0 = self.mandelbrotNormalize(False , row)

			while col < self.width:
				x0 = self.mandelbrotNormalize(True , col)
				x = 0
				y = 0
				i = 0

				while (x * x) + (y * y) <= 4 and i < self.iterations:
					xtemp = (x * x) - (y * y) + x0
					y = (2 * x * y) + y0
					x = xtemp
					i = i + 1

				if(i == self.iterations):
					i = self.iterations - 1

				self.artboard.putColorInImage(col , row , 1 , 1 , self.palette.get(i))
				col = col + 1			

			col = 0
			row = row + 1

	def undo(self):
		self.artboard.setToCheckeredBackground()

	def mandelbrotNormalize(self , isX , coordinate):
		if isX:
			return (float(coordinate)) / (self.width) * ((self.MANDELBROT_X_SCALE_MAX) - (self.MANDELBROT_X_SCALE_MIN)) + (self.MANDELBROT_X_SCALE_MIN)
		else:
			return (float(coordinate)) / (self.height) * ((self.MANDELBROT_Y_SCALE_MAX) - (self.MANDELBROT_Y_SCALE_MIN)) + (self.MANDELBROT_Y_SCALE_MIN)

	def initializePalette(self , artboard):	
		i = 0
		colorArray = ScriptingUtils2.bytes(4)		
		colorArray[3] = -1
		
		while i < self.iterations:
			colorArray[0] = ScriptingUtils2.doubleToByte(Math.abs(Math.pow(i , i)))
			colorArray[1] = ScriptingUtils2.doubleToByte(Math.abs(Math.pow(i , 2)))
			colorArray[2] = ScriptingUtils2.doubleToByte(Math.abs(Math.pow(i , 3)))
			self.putPixel(artboard , colorArray)
			i = i + 1
	
	def putPixel(self , artboard , colorArray):
		pixel = self.artboard.createPalettePixel(colorArray)
		self.palette.add(pixel)