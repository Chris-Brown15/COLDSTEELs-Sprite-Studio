package cs.csss.misc.graphcs.memory;

import static org.lwjgl.opengl.NVXGPUMemoryInfo.*;
import static org.lwjgl.opengl.WGLAMDGPUAssociation.*;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL30C.GL_VENDOR;
import static org.lwjgl.opengl.GL30C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30C.glGetIntegerv;
import static org.lwjgl.opengl.GL30C.glGetString;

public final class GPUMemoryViewer {
	
	private static Vendors currentVendor;
		
	public static void initialize() { 
		
		String vendor = glGetString(GL_VENDOR);
		
		if(vendor.contains("NVIDIA")) currentVendor = Vendors.NVIDIA;
		else if(vendor.contains("ATI")) currentVendor = Vendors.ATI;
		else currentVendor = Vendors.OTHER;
		
	}
	
	public static int getTotalAvailableVRAM() {
		
		return switch(currentVendor) {
			
			case NVIDIA -> {
				
				try(MemoryStack stack = MemoryStack.stackPush()) {

					IntBuffer result = stack.ints(1);
					glGetIntegerv(GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX , result);
					yield result.get() / 1024;
					
				}
		
			}
			
			case ATI -> {
				
				try(MemoryStack stack = MemoryStack.stackPush()) {
				
					IntBuffer store = null;
					int numberGPUs = wglGetGPUIDsAMD(store);
					store = stack.mallocInt(numberGPUs);
					wglGetGPUIDsAMD(store);
					IntBuffer result = stack.ints(1);
					wglGetGPUInfoAMD(store.get() , WGL_GPU_RAM_AMD , GL_UNSIGNED_INT , result);
					yield result.get();
					
				}
				
			}
			
			default -> -1;
		};
				
	}
	
	public static int getCurrentAvailableVRAM() {
		
		return switch(currentVendor) {
		
		case NVIDIA -> {
		
			try(MemoryStack stack = MemoryStack.stackPush()) {
				
				IntBuffer result = stack.ints(1);
				glGetIntegerv(GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX , result);
				yield result.get() / 1024;
				
			}
			
		}
		
		case ATI -> {
		
			yield -1;
			
		}
		
		default -> -1;
		
		};
		
	}
	
	private GPUMemoryViewer() {}
	
}
