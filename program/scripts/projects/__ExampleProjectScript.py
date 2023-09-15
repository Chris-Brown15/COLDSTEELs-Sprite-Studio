'''

This is an example of a project script. This type of script receives the entire project as its parameter so it can do much more than the
artboard script. This is in fact the only difference between this type of script and the artboard script.

For more information, see the artboard scripts included in Sprite Studio for more information.

'''
from java.lang import System

isRenderEvent = False
isTransientEvent = True

def __ExampleProjectScript(project , editor):
	return PrintProjectInfoEvent(project , editor)

class PrintProjectInfoEvent:
	def __init__(self , project , editor):
		self.project = project
		self.editor = editor

	def _do(self):
		self.project.forEachAnimation(lambda x: System.out.println(x.name()))
		self.project.forEachVisualLayerPrototype(lambda x: System.out.println(x.name()))
		self.project.forEachNonVisualLayerPrototype(lambda x: System.out.println(x.name()))