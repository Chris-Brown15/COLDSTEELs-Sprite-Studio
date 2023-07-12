package cs.csss.ui.decorators;

import static org.lwjgl.nuklear.Nuklear.nnk_propertyf;

import java.util.function.Supplier;

import org.lwjgl.nuklear.NkContext;

import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSFloatProperty;
import cs.core.utils.FloatConsumer;
import cs.core.utils.FloatSupplier;

public class ImmediateModeFloatProperty extends CSFloatProperty {

	public ImmediateModeFloatProperty(
		NkContext context ,
		CSLayout layout , 
		String title , 
		float min , 
		float max ,
		float step , 
		float increasePerPixel , 
		Supplier<FloatConsumer> setterGetter , 
		Supplier<FloatSupplier> getterGetter
	) {
		
		layout.super(title, step, increasePerPixel , value -> {} , getterGetter.get());
		
		super.setCode(() -> {
			
			setterGetter.get().accept(nnk_propertyf(
				context.address() , 
				text , 
				min , 
				getterGetter.get().getAsFloat() , 
				max , 
				step , 
				increasePerPixel
			));
			
		});
		
	}	
		
}
