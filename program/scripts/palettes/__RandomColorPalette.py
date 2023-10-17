from cs.csss.utils import ScriptingUtils
from cs.csss.editor.palette import ColorPalette
from cs.csss.engine import ChannelBuffer
from java.lang import Math

name = "Random Colors"
initialValueScale = 15

def __RandomColorPalette(palette):
	return RandomColor(palette)

class RandomColor:
	def __init__(self , palette):
		self.palette = palette
		self.colors = ColorPalette.resizePalette(None , ScriptingUtils.channelBuffers(initialValueScale) , lambda: ChannelBuffer())

	def setValueScale(self , newValueScale):
		self.palette.defaultSetValueScale(newValueScale)
		self.colors = ColorPalette.resizePalette(self.colors , ScriptingUtils.channelBuffers(newValueScale) , lambda: ChannelBuffer())

	def generate(self , sourcePixel , channels):
		i = 0
		while i < self.palette.valueScale():
			red = ScriptingUtils.doubleToByte(Math.random() * 255)
			green = -1 if channels < 2 else ScriptingUtils.doubleToByte(Math.random() * 255)
			blue = -1 if channels < 3 else ScriptingUtils.doubleToByte(Math.random() * 255)
			alpha = -1 if channels < 4 else ScriptingUtils.doubleToByte(Math.random() * 255)
			self.colors[i].set(red , green , blue , alpha)
			i = i + 1
		return self.colors

	def get(self):
		return self.colors
