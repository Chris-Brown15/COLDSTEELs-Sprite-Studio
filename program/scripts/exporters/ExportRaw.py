from java.io import FileOutputStream
from java.lang import Exception
from cs.csss.engine import Logging

def export(filepath , pixelData , width , height , channels):
	try:
		fileWriter = FileOutputStream(filepath)
		while pixelData.hasRemaining():
			fileWriter.write(pixelData.get())
	except Exception:
		Logging.syserr("Error occurred exporting raw image.")
	finally:
		fileWriter.close()

	pixelData.position(0)
