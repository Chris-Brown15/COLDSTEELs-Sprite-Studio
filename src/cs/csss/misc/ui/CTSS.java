/**
 * 
 */
package cs.csss.misc.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.LinkedList;
import java.util.Objects;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkStyle;
import org.lwjgl.nuklear.NkStyleButton;
import org.lwjgl.nuklear.NkStyleChart;
import org.lwjgl.nuklear.NkStyleCombo;
import org.lwjgl.nuklear.NkStyleEdit;
import org.lwjgl.nuklear.NkStyleItem;
import org.lwjgl.nuklear.NkStyleProgress;
import org.lwjgl.nuklear.NkStyleProperty;
import org.lwjgl.nuklear.NkStyleScrollbar;
import org.lwjgl.nuklear.NkStyleSelectable;
import org.lwjgl.nuklear.NkStyleSlider;
import org.lwjgl.nuklear.NkStyleTab;
import org.lwjgl.nuklear.NkStyleText;
import org.lwjgl.nuklear.NkStyleToggle;
import org.lwjgl.nuklear.NkStyleWindow;
import org.lwjgl.nuklear.NkStyleWindowHeader;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

/**
 * Class representing an {@link org.lwjgl.nuklear.NkStyle NkStyle} struct on disk. This is a file that contains the necessary data of an 
 * {@code NkStyle}. The result of reading this file is an {@code NkStyle} struct whicn can be used to style UIs.
 */
public class CTSS {

	public static final String path = "assets/themes/";
	public static final String ending = ".ctss";
	
	private final String fileName;
	private NkStyle style;
	private NkColor clearColor;
	private LinkedList<NkColor> colorPalette;
	
	/**
	 *  Write constructor. When {@link #write()} is called, the given {@code NkStyle} is written to disk.
	 *  
	 *  @param filepath — name of this theme
	 *  @param source — source {@link NkStyle} struct to write
	 *  @param colorPalette — container of colors used in {@code source}
	 *  @param clearColor — color of the window background 
	 *  
	 *  @throws NullPointerException if an of the parameters are <code>null</code>.
	 */
	public CTSS(String filepath , NkStyle source , LinkedList<NkColor> colorPalette , NkColor clearColor) {
		
		this.fileName = Objects.requireNonNull(filepath);
		this.style = Objects.requireNonNull(source);
		this.colorPalette = Objects.requireNonNull(colorPalette);
		this.clearColor = clearColor;
		
	}

	/**
	 * Read constructor. When {@link #read()} is called, the {@code destination} struct will be filled out with the contents of the file at
	 * {@code fileName}. 
	 * 
	 * @param fileName name of a file
	 * @param destination
	 * @throws FileNotFoundException if {@code fileName} does not point to a file 
	 * @throws NullPointerException if {@code fileName} or {@code destination} is <code>null</code>.
	 */
	public CTSS(String fileName , NkStyle destination) throws FileNotFoundException {
		
		this.fileName = Objects.requireNonNull(fileName);		
		//verify fileName is valid
		if(!fileName.endsWith(ending)) fileName += ending;
		if(!new File(path + fileName).exists()) throw new FileNotFoundException(path + fileName + " does not point to a file.");
				
		style = destination;
		colorPalette = new LinkedList<>();
		clearColor = NkColor.malloc();
		
	}
	
	/**
	 * Saves the contents of the given style struct to disk. 
	 * 
	 * @throws IOException if an error occurs during IO operations.
	 */
	public void write() throws IOException {
		
		File asFile = new File(path + fileName + ending);
		try {

			asFile.createNewFile();
			
		} catch (IOException e) {
			
			throw e;			
			
		}
		
		try (RandomAccessFile writer = new RandomAccessFile(asFile , "rw")) {
			
			FileChannel channel = writer.getChannel();
			
			try(MemoryStack stack = MemoryStack.stackPush()) {
				
				try {
					
					int size = NkStyleText.SIZEOF + 
						(NkStyleButton.SIZEOF * 3) + 
						(NkStyleToggle.SIZEOF * 2) + 
						NkStyleSelectable.SIZEOF + 
						NkStyleSlider.SIZEOF + 
						NkStyleProgress.SIZEOF + 
						NkStyleProperty.SIZEOF + 
						NkStyleEdit.SIZEOF + 
						NkStyleChart.SIZEOF + 
						(NkStyleScrollbar.SIZEOF * 2) + 
						NkStyleTab.SIZEOF + 
						NkStyleCombo.SIZEOF + 
						NkStyleWindow.SIZEOF;
					
					MappedByteBuffer mappedFile = channel.map(MapMode.READ_WRITE , 0 , size);
					writeColor(mappedFile , clearColor);
					writeColorPalette(mappedFile, colorPalette);
					writeText(mappedFile , style.text());
					writeButton(mappedFile , style.button());
					writeButton(mappedFile , style.contextual_button());
					writeButton(mappedFile , style.menu_button());
					writeToggle(mappedFile , style.option());
					writeToggle(mappedFile , style.checkbox());
					writeSelectable(mappedFile, style.selectable());
					writeSlider(mappedFile , style.slider());
					writeProgress(mappedFile , style.progress());
					writeProperty(mappedFile , style.property());
					writeEdit(mappedFile , style.edit());
					writeChart(mappedFile , style.chart());
					writeScrollbar(mappedFile , style.scrollh());
					writeScrollbar(mappedFile , style.scrollv());
					writeTab(mappedFile , style.tab());			
					writeCombo(mappedFile , style.combo());
					writeWindow(mappedFile , style.window());
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
			
			}
			
		} catch (FileNotFoundException e) {
			
			throw e;
			
		} catch (IOException e) {
			
			//thrown during mapping file to memory.
			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * Reads the contents of the given file and fills out the fields of this class for later access.
	 * 
	 * @throws IOException if an IO error occurs during reading operations.
	 */
	public void read() throws IOException {
		
		try(RandomAccessFile file = new RandomAccessFile(path + fileName , "r")) {
			
			FileChannel channel = file.getChannel();
			MappedByteBuffer mappedFile = channel.map(MapMode.READ_ONLY, 0, file.length());
			readColor(mappedFile , clearColor);
			readColorPalette(mappedFile);
			readText(mappedFile , style.text());
			readButton(mappedFile , style.button());
			readButton(mappedFile , style.contextual_button());
			readButton(mappedFile , style.menu_button());
			readToggle(mappedFile, style.option());
			readToggle(mappedFile, style.checkbox());
			readSelectable(mappedFile , style.selectable());
			readSlider(mappedFile , style.slider());
			readProgress(mappedFile, style.progress());
			readProperty(mappedFile, style.property());
			readEdit(mappedFile , style.edit());
			readChart(mappedFile, style.chart());
			readScrollbar(mappedFile, style.scrollh());
			readScrollbar(mappedFile, style.scrollv());
			readTab(mappedFile , style.tab());
			readCombo(mappedFile , style.combo());
			readWindow(mappedFile, style.window());
						
		} catch (FileNotFoundException e) {

			//shouldn't happen
			e.printStackTrace();
			
		} catch (IOException e) {

			throw e;
			
		}
		
	}

	/**
	 * Returns the color the background should be.
	 * 
	 * @return {@link FreeAfterUse @FreeAfterUse} Color the window should be.
	 */
	public NkColor windowColor() {
		
		return clearColor;
		
	}
	
	/**
	 * Returns the list of colors used in the theme.
	 * 
	 * @return List of colors in the theme.
	 */
	public LinkedList<NkColor> colorPalette() {
		
		return colorPalette;
		
	}
	
	private void readColorPalette(ByteBuffer source) {
		
		int length = source.getInt();
		for(int i = 0 ; i < length ; i++) colorPalette.add(NkColor.malloc().set(source.get() , source.get() , source.get() , source.get()));
		
	}
	
	private void readText(ByteBuffer source , NkStyleText destination) {

		readColor(source , destination.color());
		readVec2(source , destination.padding());

	}
	
	private void readButton(ByteBuffer source , NkStyleButton destination) {
	
		readStyleItem(source, destination.normal());
		readStyleItem(source, destination.hover());
		readStyleItem(source, destination.active());
		readColor(source , destination.border_color());
		readColor(source , destination.text_background());
		readColor(source , destination.text_normal());
		readColor(source , destination.text_hover());
		readColor(source , destination.text_active());
		destination.text_alignment(source.getInt());
		destination.border(source.getFloat());
		destination.rounding(source.getFloat());
		
		readVec2(source, destination.padding());
		readVec2(source, destination.image_padding());
		readVec2(source, destination.touch_padding());

	}
	
	private void readToggle(ByteBuffer source, NkStyleToggle destination) {
		
		readStyleItem(source, destination.normal());
		readStyleItem(source, destination.hover());
		readStyleItem(source, destination.active());
		readColor(source, destination.border_color());
		readStyleItem(source, destination.cursor_normal());
		readStyleItem(source, destination.cursor_hover());
		readColor(source, destination.text_normal());
		readColor(source, destination.text_hover());
		readColor(source, destination.text_active());
		readColor(source, destination.text_background());
		destination.text_alignment(source.getInt());
		readVec2(source , destination.padding());
		readVec2(source , destination.touch_padding());
		destination.spacing(source.getFloat());
		destination.border(source.getFloat());
		
	}
	
	private void readSelectable(ByteBuffer source , NkStyleSelectable selectable) {
		
		readStyleItem(source , selectable.normal());
		readStyleItem(source , selectable.hover());
		readStyleItem(source , selectable.pressed());
		readStyleItem(source , selectable.normal_active());
		readStyleItem(source , selectable.hover_active());
		readStyleItem(source , selectable.pressed_active());
		
		readColor(source , selectable.text_normal());
		readColor(source , selectable.text_hover());
		readColor(source , selectable.text_pressed());
		readColor(source , selectable.text_normal_active());
		readColor(source , selectable.text_hover_active());
		readColor(source , selectable.text_pressed_active());
		readColor(source , selectable.text_background());
		
		selectable.text_alignment(source.getInt());
		selectable.rounding(source.getFloat());
		
		readVec2(source , selectable.padding());
		readVec2(source , selectable.touch_padding());
		readVec2(source , selectable.image_padding());
		
	}
	
	private void readSlider(ByteBuffer source , NkStyleSlider slider) {
		
		readStyleItem(source , slider.normal());
		readStyleItem(source , slider.hover());
		readStyleItem(source , slider.active());
		readColor(source , slider.border_color());
		readColor(source , slider.bar_normal());
		readColor(source , slider.bar_hover());
		readColor(source , slider.bar_active());
		readColor(source , slider.bar_filled());
		readStyleItem(source , slider.cursor_normal());
		readStyleItem(source , slider.cursor_hover());
		readStyleItem(source , slider.cursor_active());
		
		slider.border(source.getFloat());
		slider.rounding(source.getFloat());
		slider.bar_height(source.getFloat());
		
		readVec2(source , slider.padding());
		readVec2(source , slider.spacing());
		readVec2(source , slider.cursor_size());
		
		slider.show_buttons(source.getInt());
		
		readButton(source , slider.inc_button());
		readButton(source, slider.dec_button());
		
		slider.inc_symbol(source.getInt());
		slider.dec_symbol(source.getInt());
		
	}
	
	private void readProgress(ByteBuffer source , NkStyleProgress progress) {

		readStyleItem(source , progress.normal());
		readStyleItem(source , progress.hover());
		readStyleItem(source , progress.active());
		readColor(source, progress.border_color());
		readStyleItem(source, progress.cursor_normal());
		readStyleItem(source, progress.cursor_hover());
		readStyleItem(source, progress.cursor_active());
		readColor(source, progress.cursor_border_color());
		
		progress.rounding(source.getFloat());
		progress.border(source.getFloat());
		progress.cursor_border(source.getFloat());
		progress.cursor_rounding(source.getFloat());
		
		readVec2(source, progress.padding());

	}
	
	private void readProperty(ByteBuffer source , NkStyleProperty property) {
	
		readStyleItem(source, property.normal());
		readStyleItem(source, property.hover());
		readStyleItem(source, property.active());
		
		readColor(source , property.border_color());
		readColor(source , property.label_normal());
		readColor(source , property.label_hover());
		readColor(source , property.label_active());
		
		property.sym_left(source.getInt());
		property.sym_right(source.getInt());
		property.border(source.getFloat());
		property.rounding(source.getFloat());
		
		readVec2(source, property.padding());
		
		readEdit(source , property.edit());
		
		readButton(source, property.inc_button());
		readButton(source, property.dec_button());
				
	}
	
	private void readEdit(ByteBuffer source , NkStyleEdit edit) {
	
		readStyleItem(source, edit.normal());
		readStyleItem(source, edit.hover());
		readStyleItem(source, edit.active());
		readColor(source, edit.border_color());
		readScrollbar(source , edit.scrollbar());
		readColor(source, edit.cursor_normal());
		readColor(source, edit.cursor_hover());
		readColor(source, edit.cursor_text_normal());
		readColor(source, edit.cursor_text_hover());
		readColor(source, edit.text_normal());
		readColor(source, edit.text_hover());
		readColor(source, edit.text_active());
		readColor(source, edit.selected_normal());
		readColor(source, edit.selected_hover());
		readColor(source, edit.selected_text_normal());
		readColor(source, edit.selected_text_hover());
		
		edit.border(source.getFloat());
		edit.rounding(source.getFloat());
		edit.cursor_size(source.getFloat());
		
		readVec2(source, edit.scrollbar_size());
		readVec2(source, edit.padding());
		
		edit.row_padding(source.getFloat());
		
	}
	
	private void readScrollbar(ByteBuffer source , NkStyleScrollbar scrollbar) {

		readStyleItem(source, scrollbar.normal());
		readStyleItem(source, scrollbar.hover());
		readStyleItem(source, scrollbar.active());
		readColor(source, scrollbar.border_color());
		readStyleItem(source, scrollbar.cursor_normal());
		readStyleItem(source, scrollbar.cursor_hover());
		readStyleItem(source, scrollbar.cursor_active());
		readColor(source, scrollbar.cursor_border_color());
		
		scrollbar.border(source.getFloat());
		scrollbar.rounding(source.getFloat());
		scrollbar.border_cursor(source.getFloat());
		scrollbar.rounding_cursor(source.getFloat());
		
		readVec2(source, scrollbar.padding());
		
		scrollbar.show_buttons(source.getInt());
		
		readButton(source , scrollbar.inc_button());
		readButton(source , scrollbar.dec_button());
		
		scrollbar.inc_symbol(source.getInt());
		scrollbar.dec_symbol(source.getInt());
		
	}
	
	private void readChart(ByteBuffer source , NkStyleChart chart) {

		readStyleItem(source, chart.background());
		readColor(source, chart.border_color());
		readColor(source, chart.selected_color());
		readColor(source, chart.color());
		chart.border(source.getFloat());
		chart.rounding(source.getFloat());
		readVec2(source, chart.padding());

	}
	
	private void readTab(ByteBuffer source , NkStyleTab tab) {

		readStyleItem(source, tab.background());
		readColor(source, tab.border_color());
		readColor(source, tab.text());
		readButton(source , tab.tab_maximize_button());
		readButton(source , tab.tab_minimize_button());
		readButton(source , tab.node_maximize_button());
		readButton(source , tab.node_minimize_button());
		
		tab.sym_minimize(source.getInt());
		tab.sym_maximize(source.getInt());
		
		tab.border(source.getFloat());
		tab.rounding(source.getFloat());
		tab.indent(source.getFloat());
		
		readVec2(source, tab.padding());
		readVec2(source, tab.spacing());
		
	}
	
	private void readCombo(ByteBuffer source , NkStyleCombo combo) {
	
		readStyleItem(source, combo.normal());
		readStyleItem(source, combo.hover());
		readStyleItem(source, combo.active());
		readColor(source, combo.border_color());
		readColor(source, combo.label_normal());
		readColor(source, combo.label_hover());
		readColor(source, combo.label_active());
		readColor(source, combo.symbol_normal());
		readColor(source, combo.symbol_hover());
		readColor(source, combo.symbol_active());
		readButton(source , combo.button());
		
		combo.sym_normal(source.getInt());
		combo.sym_hover(source.getInt());
		combo.sym_active(source.getInt());
		combo.border(source.getFloat());
		combo.rounding(source.getFloat());
		
		readVec2(source, combo.content_padding());
		readVec2(source, combo.button_padding());
		readVec2(source, combo.spacing());

	}
	
	private void readWindow(ByteBuffer source , NkStyleWindow window) {

		readWindowHeader(source , window.header());
		readStyleItem(source , window.fixed_background());
		readColor(source, window.background());
		readColor(source, window.border_color());
		readColor(source, window.popup_border_color());
		readColor(source, window.combo_border_color());
		readColor(source, window.contextual_border_color());
		readColor(source, window.menu_border_color());
		readColor(source, window.group_border_color());
		readColor(source, window.tooltip_border_color());
		readStyleItem(source, window.scaler());
		
		window.border(source.getFloat());
		window.combo_border(source.getFloat());
		window.contextual_border(source.getFloat());
		window.menu_border(source.getFloat());
		window.group_border(source.getFloat());
		window.tooltip_border(source.getFloat());
		window.popup_border(source.getFloat());
		window.min_row_height_padding(source.getFloat());
		window.rounding(source.getFloat());

		readVec2(source , window.spacing());
		readVec2(source , window.scrollbar_size());
		readVec2(source , window.min_size());
		readVec2(source , window.padding());
		readVec2(source , window.group_padding());
		readVec2(source , window.popup_padding());
		readVec2(source , window.combo_padding());
		readVec2(source , window.contextual_padding());
		readVec2(source , window.menu_padding());
		readVec2(source , window.tooltip_padding());
		
	}
	
	private void readWindowHeader(ByteBuffer source , NkStyleWindowHeader header) {

		readStyleItem(source, header.normal());
		readStyleItem(source, header.hover());
		readStyleItem(source, header.active());
		readButton(source, header.close_button());
		readButton(source, header.minimize_button());
		
		header.close_symbol(source.getInt());
		header.minimize_symbol(source.getInt());
		header.maximize_symbol(source.getInt());
		
		readColor(source , header.label_normal());
		readColor(source , header.label_hover());
		readColor(source , header.label_active());
		
		header.align(source.getInt());
		
		readVec2(source, header.padding());
		readVec2(source, header.label_padding());
		readVec2(source, header.spacing());
		
	}
	
	private void writeColorPalette(ByteBuffer destination , LinkedList<NkColor> palette) {
		
		destination.putInt(palette.size());
		palette.forEach(color -> destination.put(color.r()).put(color.g()).put(color.b()).put(color.a()));
		
	}
	
	private void writeText(ByteBuffer destination , NkStyleText text) {
		
		writeColor(destination , text.color());
		writeVec2(destination , text.padding());
		
	}
	
	private void writeButton(ByteBuffer destination , NkStyleButton button) {
		
		writeStyleItem(destination, button.normal());
		writeStyleItem(destination, button.hover());
		writeStyleItem(destination, button.active());
		writeColor(destination , button.border_color());
		writeColor(destination , button.text_background());
		writeColor(destination , button.text_normal());
		writeColor(destination , button.text_hover());
		writeColor(destination , button.text_active());
		destination.putInt(button.text_alignment());
		destination.putFloat(button.border());
		destination.putFloat(button.rounding());
		writeVec2(destination, button.padding());
		writeVec2(destination, button.image_padding());
		writeVec2(destination, button.touch_padding());
		
	}
	
	private void writeToggle(ByteBuffer destination , NkStyleToggle toggle) {
		
		writeStyleItem(destination, toggle.normal());
		writeStyleItem(destination, toggle.hover());
		writeStyleItem(destination, toggle.active());
		writeColor(destination , toggle.border_color());
		writeStyleItem(destination, toggle.cursor_normal());
		writeStyleItem(destination, toggle.cursor_hover());
		writeColor(destination , toggle.text_normal());
		writeColor(destination , toggle.text_hover());
		writeColor(destination , toggle.text_active());
		writeColor(destination , toggle.text_background());
		destination.putInt(toggle.text_alignment());
		writeVec2(destination , toggle.padding());
		writeVec2(destination , toggle.touch_padding());
		destination.putFloat(toggle.spacing()).putFloat(toggle.border());
		
	}
	
	private void writeSelectable(ByteBuffer destination , NkStyleSelectable selectable) {
		
		writeStyleItem(destination, selectable.normal());
		writeStyleItem(destination, selectable.hover());
		writeStyleItem(destination, selectable.pressed());
		writeStyleItem(destination, selectable.normal_active());
		writeStyleItem(destination, selectable.hover_active());
		writeStyleItem(destination, selectable.pressed_active());
		writeColor(destination , selectable.text_normal());
		writeColor(destination , selectable.text_hover());
		writeColor(destination , selectable.text_pressed());
		writeColor(destination , selectable.text_normal_active());
		writeColor(destination , selectable.text_hover_active());
		writeColor(destination , selectable.text_pressed_active());
		writeColor(destination , selectable.text_background());
		
		destination.putInt(selectable.text_alignment()).putFloat(selectable.rounding());
		
		writeVec2(destination , selectable.padding());
		writeVec2(destination , selectable.touch_padding());
		writeVec2(destination , selectable.image_padding());
			
	}
	
	private void writeSlider(ByteBuffer destination , NkStyleSlider slider) {
		
		writeStyleItem(destination , slider.normal());
		writeStyleItem(destination , slider.hover());
		writeStyleItem(destination , slider.active());
		writeColor(destination , slider.border_color());
		writeColor(destination , slider.bar_normal());
		writeColor(destination , slider.bar_hover());
		writeColor(destination , slider.bar_active());
		writeColor(destination , slider.bar_filled());
		writeStyleItem(destination, slider.cursor_normal());
		writeStyleItem(destination, slider.cursor_hover());
		writeStyleItem(destination, slider.cursor_active());
		
		destination.putFloat(slider.border()).putFloat(slider.rounding()).putFloat(slider.bar_height());
		
		writeVec2(destination , slider.padding());
		writeVec2(destination , slider.spacing());
		writeVec2(destination , slider.cursor_size());
		
		destination.putInt(slider.show_buttons());
		
		writeButton(destination , slider.inc_button());
		writeButton(destination , slider.dec_button());
		destination.putInt(slider.inc_symbol()).putInt(slider.dec_symbol());
		
	}
	
	private void writeProgress(ByteBuffer destination , NkStyleProgress progress) {
		
		writeStyleItem(destination, progress.normal());
		writeStyleItem(destination, progress.hover());
		writeStyleItem(destination, progress.active());
		writeColor(destination, progress.border_color());
		writeStyleItem(destination, progress.cursor_normal());
		writeStyleItem(destination, progress.cursor_hover());
		writeStyleItem(destination, progress.cursor_active());
		writeColor(destination, progress.cursor_border_color());
		destination.putFloat(progress.rounding());
		destination.putFloat(progress.border());
		destination.putFloat(progress.cursor_border());
		destination.putFloat(progress.cursor_rounding());
		writeVec2(destination , progress.padding());
		
	}
	
	private void writeProperty(ByteBuffer destination , NkStyleProperty property) {
		
		writeStyleItem(destination , property.normal());
		writeStyleItem(destination , property.hover());
		writeStyleItem(destination , property.active());
		writeColor(destination, property.border_color());
		writeColor(destination, property.label_normal());
		writeColor(destination, property.label_hover());
		writeColor(destination, property.label_active());
		destination.putInt(property.sym_left()).putInt(property.sym_right()).putFloat(property.border()).putFloat(property.rounding());
		
		writeVec2(destination , property.padding());
		
		writeEdit(destination , property.edit());
		
		writeButton(destination , property.inc_button());
		writeButton(destination , property.dec_button());
		
	}
	
	private void writeEdit(ByteBuffer destination , NkStyleEdit edit) {
		
		writeStyleItem(destination , edit.normal());
		writeStyleItem(destination , edit.hover());
		writeStyleItem(destination , edit.active());
		writeColor(destination , edit.border_color());
		writeScrollbar(destination, edit.scrollbar());
		writeColor(destination , edit.cursor_normal());
		writeColor(destination , edit.cursor_hover());
		writeColor(destination , edit.cursor_text_normal());
		writeColor(destination , edit.cursor_text_hover());
		writeColor(destination , edit.text_normal());
		writeColor(destination , edit.text_hover());
		writeColor(destination , edit.text_active());
		writeColor(destination , edit.selected_normal());
		writeColor(destination , edit.selected_hover());
		writeColor(destination , edit.selected_text_normal());
		writeColor(destination , edit.selected_text_hover());
		
		destination.putFloat(edit.border()).putFloat(edit.rounding()).putFloat(edit.cursor_size());
		
		writeVec2(destination , edit.scrollbar_size());
		writeVec2(destination , edit.padding());
		
		destination.putFloat(edit.row_padding());
			
	}
	
	private void writeScrollbar(ByteBuffer destination , NkStyleScrollbar scrollbar) {
		
		writeStyleItem(destination , scrollbar.normal());
		writeStyleItem(destination , scrollbar.hover());
		writeStyleItem(destination , scrollbar.active());
		writeColor(destination , scrollbar.border_color());
		writeStyleItem(destination, scrollbar.cursor_normal());
		writeStyleItem(destination, scrollbar.cursor_hover());
		writeStyleItem(destination, scrollbar.cursor_active());
		writeColor(destination, scrollbar.cursor_border_color());
		destination
			.putFloat(scrollbar.border()).putFloat(scrollbar.rounding())
			.putFloat(scrollbar.border_cursor()).putFloat(scrollbar.rounding_cursor());
		
		writeVec2(destination , scrollbar.padding());
		destination.putInt(scrollbar.show_buttons());
		writeButton(destination, scrollbar.inc_button());
		writeButton(destination, scrollbar.dec_button());
		destination.putInt(scrollbar.inc_symbol()).putInt(scrollbar.dec_symbol());
		
	}
	
	private void writeChart(ByteBuffer destination , NkStyleChart chart) {
		
		writeStyleItem(destination , chart.background());
		writeColor(destination , chart.border_color());
		writeColor(destination , chart.selected_color());
		writeColor(destination , chart.color());
		
		destination.putFloat(chart.border()).putFloat(chart.rounding());
		
		writeVec2(destination , chart.padding());
		
	}
	
	private void writeTab(ByteBuffer destination , NkStyleTab tab) {
		
		writeStyleItem(destination, tab.background());
		writeColor(destination , tab.border_color());
		writeColor(destination, tab.text());
		writeButton(destination , tab.tab_maximize_button());
		writeButton(destination , tab.tab_minimize_button());
		writeButton(destination , tab.node_maximize_button());
		writeButton(destination , tab.node_minimize_button());
		
		destination.putInt(tab.sym_minimize()).putInt(tab.sym_maximize()).putFloat(tab.border()).putFloat(tab.rounding()).putFloat(tab.indent());
		
		writeVec2(destination , tab.padding());
		writeVec2(destination , tab.spacing());
		
	}
	
	private void writeCombo(ByteBuffer destination , NkStyleCombo combo) {
		
		writeStyleItem(destination, combo.normal());
		writeStyleItem(destination, combo.hover());
		writeStyleItem(destination, combo.active());
		writeColor(destination, combo.border_color());
		writeColor(destination, combo.label_normal());
		writeColor(destination, combo.label_hover());
		writeColor(destination, combo.label_active());
		writeColor(destination, combo.symbol_normal());
		writeColor(destination, combo.symbol_hover());
		writeColor(destination, combo.symbol_active());
		
		writeButton(destination , combo.button());
		
		destination
			.putInt(combo.sym_normal()).putInt(combo.sym_hover()).putInt(combo.sym_active())
			.putFloat(combo.border()).putFloat(combo.rounding());
		
		writeVec2(destination , combo.content_padding());
		writeVec2(destination , combo.button_padding());
		writeVec2(destination , combo.spacing());
		
	}
	
	private void writeWindow(ByteBuffer destination , NkStyleWindow window) {
		
		writeWindowHeader(destination, window.header());
		writeStyleItem(destination , window.fixed_background());
		writeColor(destination , window.background());
		writeColor(destination , window.border_color());
		writeColor(destination , window.popup_border_color());
		writeColor(destination , window.combo_border_color());
		writeColor(destination , window.contextual_border_color());
		writeColor(destination , window.menu_border_color());
		writeColor(destination , window.group_border_color());
		writeColor(destination , window.tooltip_border_color());
		writeStyleItem(destination , window.scaler());
		
		destination
			.putFloat(window.border()).putFloat(window.combo_border()).putFloat(window.contextual_border()).putFloat(window.menu_border())
			.putFloat(window.group_border()).putFloat(window.tooltip_border()).putFloat(window.popup_border())
			.putFloat(window.min_row_height_padding()).putFloat(window.rounding());
		
		writeVec2(destination , window.spacing());
		writeVec2(destination , window.scrollbar_size());
		writeVec2(destination , window.min_size());
		writeVec2(destination , window.padding());
		writeVec2(destination , window.group_padding());
		writeVec2(destination , window.popup_padding());
		writeVec2(destination , window.combo_padding());
		writeVec2(destination , window.contextual_padding());
		writeVec2(destination , window.menu_padding());
		writeVec2(destination , window.tooltip_padding());
		
	}
	
	private void writeWindowHeader(ByteBuffer destination , NkStyleWindowHeader header) {
		
		writeStyleItem(destination, header.normal());
		writeStyleItem(destination, header.hover());
		writeStyleItem(destination, header.active());
		writeButton(destination , header.close_button());
		writeButton(destination, header.minimize_button());
		
		destination.putInt(header.close_symbol()).putInt(header.minimize_symbol()).putInt(header.maximize_symbol());
		
		writeColor(destination , header.label_normal());
		writeColor(destination , header.label_hover());
		writeColor(destination , header.label_active());
		
		destination.putInt(header.align());
		
		writeVec2(destination , header.padding());
		writeVec2(destination , header.label_padding());
		writeVec2(destination , header.spacing());
		
	}
	
	private void writeStyleItem(ByteBuffer destination , NkStyleItem item) {
		
		writeColor(destination , item.data().color());		
		
	}
	
	private void writeColor(ByteBuffer destination , NkColor color) {

		destination.put(color.r()).put(color.g()).put(color.b()).put(color.a());
		
	}

	private void writeVec2(ByteBuffer destination , NkVec2 vec) {
		
		destination.putFloat(vec.x()).putFloat(vec.y());
		
	}
	
	private void readStyleItem(ByteBuffer source , NkStyleItem destination) {
		
		destination.data().color().set(source.get() , source.get() , source.get() , source.get());
		
	}
	
	private void readColor(ByteBuffer source , NkColor destination) {
	
		destination.set(source.get() , source.get() , source.get() , source.get());
		
	}
	
	private void readVec2(ByteBuffer source , NkVec2 destination) {

		destination.set(source.getFloat() , source.getFloat());

	}
	
}