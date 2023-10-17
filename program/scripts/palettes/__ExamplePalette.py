'''
Example for a palette script. This file contains the needed data and functions to properly implement a color palette.

Color palettes in this context refer to the UI elements on the left hand side panel of Sprite Studio. They display a series of colors for the user to choose from. Typically, 

'''
from cs.csss.utils import ScriptingUtils
from cs.csss.editor.palette import ColorPalette
from cs.csss.engine import ChannelBuffer

name = "Example"
initialValueScale = 15

def __ExamplePalette(palette):
	return ExamplePalette(palette)

class ExamplePalette:
	def __init__(self , palette):
		self.palette = palette
		self.colors = ColorPalette.resizePalette(None , ScriptingUtils.channelBuffers(initialValueScale) , lambda: ChannelBuffer())

	def setValueScale(self , newValueScale):
		self.palette.defaultSetValueScale(newValueScale)
		self.colors = ColorPalette.resizePalette(self.colors , ScriptingUtils.channelBuffers(newValueScale) , lambda: ChannelBuffer())

	def generate(self , sourcePixel , channels):
		i = 0
		while i < self.palette.valueScale():
			self.colors[i].set(ScriptingUtils.longToByte(0xca) , ScriptingUtils.longToByte(0xfe) , ScriptingUtils.longToByte(0xba) , ScriptingUtils.longToByte(0xbe))
			i = i + 1
		return self.colors

	def get(self):
		return self.colors
