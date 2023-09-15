'''

This file is an example about how to create an artboard script. An artboard script is a script that receives the current artboard and does something to it. 

Sprite Studio is based on events. Almost everything that happens in Sprite Studio is an event. Users can create their own events via Python scripting, and this file gives an 
outline about how to do that. 

Each .py file should have a function whose name is the name of the script file (sans .py), as well as a a function called isRenderEvent(), which returns a boolean, and a 
function named isTransientEvent(). These three functions are called by Sprite Studio in order to create an event, so they are necessary. 

The function named after the file's name takes two parameters and must return something. The two parameters are the current artboard and the editor. These are what your event 
will use to make some change to the project. The returned object should, at a bare minimum, have a _do() method. In addition to this, you can give the object an undo() method
and a shutDown() method. You should also store all that you need to properly do and undo your event within the class instance. Any native resources you retrieve must be freed
via the shutDown() method. 

Thus, the best option is to return an instance of a class.

What is 'isRenderEvent()?'
Basically, if an event does any graphics-related tasks, it must be a render event. Any functions which interact with the graphics card somewhere along the line must be called in 
the render thread, which is not the main thread (as of now) in Sprite Studio. So the isRenderEvent() function returns a boolean which tells Sprite Studio whether the associated 
event is a render event or not.

What is 'isTransientEvent()?'
This is a function returning a boolean which tells Sprite Studio whether the associated event can be undone. There are a limited number of slots for events that can be undone,
and if an event is not undoable, it is said to be transient. This way, when the event is done its first time, it is not kept in memory (so a user cannot undo it).

What should be in the '__init__()?'
First of all the self parameter should probably be in the init function because your event should contain at least some data so it can do its job. In addition to self, your 
event's constructor must take two arguments, which I recommend you call artboard and editor respectively. Since these are the objects you need to modify the project, they are 
probably necessary for you to cache in your event instance.
The __init__() method may also do some work and gather some other data, but be careful because your __init__() function is not called in the render thread, so avoid doing 
anything relevent to graphics in the constructor, and instead hold off that until the _do() method.

What is '_do()?'
This is the method that is invoked by Sprite Studio when it comes time to actually execute the event. It is also called when the event is redone, i.e., it was previously undone, 
but now the user wants to do it again. This should be the method that does the work of the event. 

What is 'undo()?'
This is the method that is invoked by Sprite Studio if a user wants to undo the event they just did. This is an optional method, but if your event is undoable, you need to also
have your isTransientEvent() return False.

What is 'shutDown()?'
This is another optional method which can be created if your event contains memory, whether a pointer, a file, or something else, that needs to be manually freed. 

Some additional tips:

1) You should launch Sprite Studio with a console so you can see the outputs of print statements.

2) You should also launch Sprite Studio with the -d argument when testing and developing a script. This has a few effects, but mainly, it prevents caching the metadata of your 
   script, so you have hot reloading of scripts.

3) You should hold off storing most data in your event instance until _do(). However, it is highly recommended you keep as little memory attached to your events as possible 
   because they last a long time, and nothing can be freed until (likely) hundreds of additional events have happened.

4) If you have to store offheap data for your event, such as a Java ByteBuffer, make sure to do two things. First, only assign the ByteBuffer one time, i.e., wrap your assignment
   of the instance field in a guard of some type so the assignment doesn't happen if the object has already been assigned before. Secondly, make sure you implement a shutDown()
   method for your instance and free the ByteBuffer there. But try to avoid the need for ByteBuffer at all costs.

5) isRenderEvent() and isTransientEvent() are only invoked one time, so don't rely on them being invoked for anything and never call them yourself, but make sure their return 
   results are accurate to avoid crashes and memory leaks.

6) If you get a strange crash while writing a script which results in an accompanying .log file somewhere (probably in the CSSS.jar), this means either you segmentation faulted 
   or invoked a graphics function outside the graphics thread. If your isRenderEvent() returns False, try making it return True. If that doesn't help, see if you can find a 
   memory leak. If you definitely cannot find any, send the .log file to chris@steelsoftworks.net.

This file shows the basic outline of how to create artboard event scripts, see other .py files in the folder in which this file was found to see more sophisticated examples.

'''
from java.lang import System

def __ExampleArtboardScript(artboard , editor , arguments):
	return ExampleScript(artboard , editor , arguments)

isRenderEvent = False
isTransientEvent = False
takesArguments = True
argumentDialogueText = "This text will appear when the user is prompted to imput arguments. Input text separated by spaces."

class ExampleScript:
	def __init__(self , artboard , editor , arguments):	
		self.artboard = artboard
		self.editor = editor
		self.arguments = arguments

	def _do(self):
		for x in self.arguments:
			System.out.println(x)

	def undo(self):
		for x in self.arguments:
			System.out.println(self.reverse(x))

	def shutDown(self):
		System.out.println("Shutting Down")

	def reverse(self , string):
		from java.lang import StringBuilder
		builder = StringBuilder(string)
		return builder.reverse()
