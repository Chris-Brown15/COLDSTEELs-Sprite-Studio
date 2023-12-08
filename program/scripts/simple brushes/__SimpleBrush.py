'''

This is an example of a basic script file for specifying a Python-powered brush for Sprite Studio. As you can see, this is a more complex type of script than the others, but you 
can see other examples the folders with the word "brushes" in their title for more sophisticated scripted brushes which are provided as examples.

Brushes are individual tools that facilitate making modifications to an artboard. Brushes goal is to emit an event when it is legal to do so by the semantics of the brush. 
Therefore, brushes have two primary methods, 'use()', and 'canUse()'. In addition to these however, they can optionally have the update() and shutDown() methods. They also have a
few fields, a Java String named 'tooltip' and a boolean named 'stateful'.

Scripts that want to implement brushes for Sprite Studio likewise need to provide an object that has a 'use(),' a 'canUse(),' and optionally an 'update(),' and 'shutDown().' 
method, as well as the two fields mentioned. Below is a more detailed explanation of each of these methods and fields.

What should "__init__() have?"
The initializer for the brush takes one argument (besides itself), called the brush. This is the Java brush object that will be using this script to function. The brush object is
useful to have in Python because, at least for most brushes, the brush object provides a few useful methods that almost all brushes will want.

What is "use()?"
use() is a method that takes four arguments and returns an event-like object. The four parameters are the artboard, editor, which you will recall from the non brush scripts, 
and two integers, xIndex, and yIndex, as they're typically called. The artboard is the current artboard, it cannot be null and its what the user just clicked on. The editor is
contains a few useful methods for modifying artboards. Then, the two indices are the indices of the pixel a user clicked. These too will definitely be valid indices. From these
four arguments, the use() method should create and return an event, who as you'll recall, has a _do() and an undo() method.

What is "canUse()?"
This is a method that takes the same four arguments as use() and returns a boolean. This method is always called right before use(), and its purpose is to ensure it makes sense 
to create an event for the current use of the brush. This method is important because without it, Sprite Studio has no idea what events produced by brushes will actually change
anything. Without this knowledge, the user would create many more events than they want, which wastes memory and eats up slots for undo and redo. So if this method returns false,
use() will not be called.

There are two more method to mention but now lets talk about the fields brushes have.

What is "toolip?"
tooltip is a string that is displayed to the user when they hover over a button for a brush or many other buttons, and right click (or whatever their display tooltip control is 
set to). The tooltip string is displayed to them and its purpose is to tell people what the brush does. Make yours short and descriptive. It isn't necessary, but you should have
it.

What is "stateful?"
Some brushes need to update their state every frame. Such brushes are said to be stateful and their stateful field is set to true. You can also make your brushes stateful. Doing 
so allows you to create more sophisticated brushes and features, and all this is done in the update() method.

What is "update()?"
update() is an optional method that will be invoked every frame iff the brush's stateful field is true. It receives two arguments and returns nothing. Its arguments are the 
current artboard (which can be NULL this time) and the editor. Using these two fields, the implementor can do any logic or state mangement they want, and this method is invoked
every frame and before any other editor functions are done. This means changes made in update will be present for the same frame's use() and canUse() methods.

What is "shutDown()?"
This method is invoked if it exists, and it is primarily meant for brushes that hold native memory, or some other objects that need to be manually freed. No such brushes exist in
Sprite Studio, but the function is there if needed. This method will be invoked when a new script is selected for the brush or if the brush reloads.

Like all other scripts for Sprite Studio, everything begins from the function sharing the name of the file (sans .py). This function takes one argument, the Java brush object, 
and needs to return an object that has a "use()" and a "canUse()" method, and the others mentioned above. In addition, the script file itself should contain the values "tooltip"
and "stateful." 

However, in addition to these, the script should also declare two other variables, "isRenderEvent," a boolean, and "isTransientEvent," a boolean. These two values correspond to 
the event this script's brush returns. These two fields are described in files in the "artboards" folder found alongside the folder containing this file.

So, the function named after this file is invoked from Sprite Studio and it returns an instance of a brush object. This brush object must have the methods mentioned above and 
those methods must do as described. The use() method should return an object that looks like an event as mentioned elsewhere.

'''
from cs.csss.engine import Control
from cs.csss.editor.brush import CSSSBrush
from cs.csss.editor.event import CSSSEvent

tooltip = "This is a tooltip"
stateful = False
isRenderEvent = True
isTransientEvent = False

def __SimpleBrush2(tooltip , stateful):
	return SimpleBrush(tooltip , stateful)

class SimpleBrush(CSSSBrush):
	def use(self , artboard , editor , cursorX , cursorY):
		return SimpleBrushEvent(artboard , True , False)

	def canUse(self , artboard , editor , cursorX , cursorY):
		return Control.ARTBOARD_INTERACT.pressed()

class SimpleBrushEvent(CSSSEvent):
	def __init__(self , artboard , isRenderEvent , isTransientEvent):
		super(CSSSEvent , self).__init__(isRenderEvent , isTransientEvent)
		self.artboard = artboard

	def _do(self):
		print("Hello Brush")

	def undo(self):
		print("hsurB olleH")

	def shutDown(self):
		print("Shutting Down Event")