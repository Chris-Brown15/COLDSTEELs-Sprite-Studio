package cs.csss.project;

import java.util.function.BiConsumer;

class ReservedLayerType {

	final BiConsumer<Layer , Artboard> onCreate;
	
	public ReservedLayerType(final String resrvedName , BiConsumer<Layer , Artboard> onCreate) {

		this.onCreate = onCreate;
		
	}

}
