from java.io import FileOutputStream

def export(filepath , pixelData , width , height , channels):
	with FileOutputStream(filepath) as fileWriter:
		while pixelData.hasRemaining():
			fileWriter.write(pixelData.get())

	pixelData.position(0)
