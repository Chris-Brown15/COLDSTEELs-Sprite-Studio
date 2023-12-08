'''
Example for a palette script. This file contains the needed data and functions to properly implement a color palette.

Color palettes in this context refer to the UI elements on the left hand side panel of Sprite Studio. They display a series of colors for the user to choose from. Typically, 

'''
from cs.csss.utils import ScriptingUtils2
from cs.csss.editor.palette import ColorPalette
from cs.csss.engine import ChannelBuffer

name = "Example"
initialValueScale = 15

def __ExamplePalette(name , valueScale):
	return ExamplePalette(name , valueScale)

class ExamplePalette(ColorPalette):
	def __init__(self , name , valueScale):
		self.colors = ColorPalette.resizePalette(None , ScriptingUtils2.channelBuffers(initialValueScale) , lambda: ChannelBuffer())
		super(ColorPalette , self).__init__(name , valueScale)

	def setValueScale(self , newValueScale):
		self.defaultSetValueScale(newValueScale)
		self.colors = ColorPalette.resizePalette(self.colors , ScriptingUtils2.channelBuffers(newValueScale) , lambda: ChannelBuffer())

	def generate(self , sourcePixel , channels):
		i = 0
		while i < self.valueScale():
			self.colors[i].set(ScriptingUtils2.longToByte(0xca) , ScriptingUtils2.longToByte(0xfe) , ScriptingUtils2.longToByte(0xba) , ScriptingUtils2.longToByte(0xbe))
			i = i + 1
		return self.colors

	def get(self):
		return self.colors
