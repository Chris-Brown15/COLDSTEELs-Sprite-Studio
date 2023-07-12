'''

This file is an example for creating scripts that are invoked dynamically when using the script brush within Sprite Studio.

The script brush is a brush that causes a python script to be invoked when the brush is used. It can be used to add additional brush functionality to
Sprite Studio in an easy way. 

Any .py file can be used as the brush script provided it meets the following:

	1) a boolean variable named isRenderEvent is defined,
	2) a function named do is defined, and
	3) a function named undo is defined.

Additionally, the optional canUse function can be defined.

isRenderEvent is a variable Sprite Studio uses to determine whether the script's actions must take place within the render thread. This variable should
technically be true if any OpenGL functions are used within the scripts functions. The rule of thumb for this variable is that if the artboard is modified 
in any way, the variable should be True.

The do and undo functions are what are called when the brush is used and when an undo action is performed. The canUse function can be used to prevent 
unnecessary creation of events when catching that an event would have no effect is easy to do.


'''

#uncomment the below line and add the correct boolean value
#isRenderEvent = 

'''

Invoked each time the brush is used. 

artboard: the artboard which was clicked
editor: the editor of the program, providing useful functions 
pixelX: the x index of the pixel that was clicked
pixelY: the y index of the pixel that was clicked

'''
def onUse(artboard , editor , pixelX , pixelY):
	pass


def canUse(artboard , editor , pixelX , pixelY):
	return True

def do(artboard , editor , pixelX , pixelY):
	pass

def undo(artboard , editor , pixelX , pixelY):
	pass

