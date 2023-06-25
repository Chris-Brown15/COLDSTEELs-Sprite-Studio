package cs.csss.core;

public class Launch {

	public static void main(String[] args) {
	
		Engine.preinitialize(args);
		
		Engine spriteStudio = new Engine();
		spriteStudio.run();
		spriteStudio.shutDown();
		
		Engine.finalShutDown();
		
		System.out.println("Closing Sprite Studio.");
		
	}

}
