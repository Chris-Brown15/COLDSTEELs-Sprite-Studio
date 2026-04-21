from cs.csss.utils import ScriptingUtils2
from cs.csss.editor.palette import ColorPalette
from cs.csss.engine import ChannelBuffer
from java.lang import Math

name = "Random Colors"
initialValueScale = 15

def RandomColorPalette(name , valueScale):
	return RandomColor(name , valueScale)

class RandomColor(ColorPalette):
	def __init__(self , name , valueScale):
		self.colors = ColorPalette.resizePalette(None , ScriptingUtils2.channelBuffers(initialValueScale) , lambda: ChannelBuffer())
		super(ColorPalette , self).__init__(name , valueScale)

	def setValueScale(self , newValueScale):
		self.defaultSetValueScale(newValueScale)
		self.colors = ColorPalette.resizePalette(self.colors , ScriptingUtils2.channelBuffers(newValueScale) , lambda: ChannelBuffer())

	def generate(self , sourcePixel , channels):
		i = 0
		while i < self.valueScale():
			red = ScriptingUtils2.doubleToByte(Math.random() * 255)
			green = -1 if channels < 2 else ScriptingUtils2.doubleToByte(Math.random() * 255)
			blue = -1 if channels < 3 else ScriptingUtils2.doubleToByte(Math.random() * 255)
			alpha = -1 if channels < 4 else ScriptingUtils2.doubleToByte(Math.random() * 255)
			self.colors[i].set(red , green , blue , alpha)
			i = i + 1
		return self.colors

	def get(self):
		return self.colors
