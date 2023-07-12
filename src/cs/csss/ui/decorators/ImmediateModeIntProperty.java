package cs.csss.ui.decorators;

import static org.lwjgl.nuklear.Nuklear.nnk_propertyi;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkContext;

import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSIntProperty;

public class ImmediateModeIntProperty extends CSIntProperty {

	public ImmediateModeIntProperty(
		NkContext context , 
		CSLayout row , 
		String title , 
		int step , 
		float increasePerPixel , 
		int min , 
		int max ,
		Supplier<IntConsumer> setterGetter ,
		Supplier<IntSupplier> getterGetter 
	) {

		row.super(title , step , increasePerPixel , min , max , val -> {} , getterGetter.get());
		
		setCode(() -> {
			
			setterGetter.get().accept(nnk_propertyi(
				context.address() , 
				text , 
				min ,
				getterGetter.get().getAsInt() , 
				max , 
				step , 
				increasePerPixel
			));
			
		});
		
	}

}
