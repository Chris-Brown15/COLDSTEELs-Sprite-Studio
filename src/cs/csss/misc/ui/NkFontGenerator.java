/**
 * 
 */
package cs.csss.misc.ui;

import static cs.core.graphics.StandardRendererConstants.MAG_FILTER_LINEAR;
import static cs.core.graphics.StandardRendererConstants.MIN_FILTER_LINEAR;
import static org.lwjgl.nuklear.Nuklear.NK_UTF_INVALID;
import static org.lwjgl.nuklear.Nuklear.nnk_utf_decode;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetPackedQuad;
import static org.lwjgl.system.MemoryUtil.memAddress;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import org.lwjgl.nuklear.NkUserFont;
import org.lwjgl.nuklear.NkUserFontGlyph;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.system.MemoryStack;

import cs.core.graphics.CSTexture;
import cs.core.utils.files.TTF;
import cs.csss.annotation.RenderThreadOnly;

/**
 * Extraction of the code used to create NkFont's within the CSNuklear class.
 */
public class NkFontGenerator {

	private final TTF font;
	private CSTexture fontTexture;
		
	/**
	 * @param fontSource the font to generate an NkUserFont out of
	 */
	public NkFontGenerator(TTF fontSource , CSTexture fontTexture) {

		this.font = Objects.requireNonNull(fontSource);
		this.fontTexture = fontTexture;
		
	}
	
	/**
	 * Creates a texture for the given font.
	 * 
	 * @return Texture created from the given font.
	 */
	@RenderThreadOnly public CSTexture generateTexture() {
		
		fontTexture = new CSTexture(font.asGraphic(), MAG_FILTER_LINEAR|MIN_FILTER_LINEAR);
		return fontTexture;		
		
	}
	
	/**
	 * Creates a {@link NkUserFont} from the {@link TTF} and {@link CSTexture} given to the constructor of this object. 
	 * 
	 * @return Newly created {@link NkUserFont}.
	 */
	public NkUserFont generate() {
		
		Objects.requireNonNull(fontTexture);
		
		return NkUserFont.malloc().width((handle, height , textCharPtr , textLength) -> {

			float totalWidth = 0;
			try (MemoryStack stack = MemoryStack.stackPush()) {

				IntBuffer unicodePtr = stack.mallocInt(1);
									
				int glyphLength = nnk_utf_decode(textCharPtr, memAddress(unicodePtr), textLength);
				int currentTextLength  = glyphLength;

				if (glyphLength == 0) return 0;

				IntBuffer advancePtr = stack.mallocInt(1);
				while (currentTextLength <= textLength && glyphLength != 0) {

					if (unicodePtr.get(0) == NK_UTF_INVALID) break;

					stbtt_GetCodepointHMetrics(font.fontInfo() , unicodePtr.get(0), advancePtr , null);
					totalWidth += advancePtr.get(0) * font.scale;

					glyphLength = nnk_utf_decode(
						textCharPtr + currentTextLength , 
						memAddress(unicodePtr) , 
						textLength - currentTextLength
					);
					
					currentTextLength += glyphLength;

				}

			}

			return totalWidth;

		})
		.height(font.characterHeight)
		.query((handle, height, glyph, codepoint, next_codepoint) -> {
	
			try (MemoryStack stack = MemoryStack.stackPush()) {
	
				FloatBuffer x = stack.floats(0.0f);
				FloatBuffer y = stack.floats(0.0f);
	
				STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
				IntBuffer advance = stack.mallocInt(1);
	
				stbtt_GetPackedQuad(font.charData() , TTF.BITMAP_W , TTF.BITMAP_W, codepoint - 32, x, y, q, false);
				stbtt_GetCodepointHMetrics(font.fontInfo() , codepoint, advance, null);
	
				NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);
	
				ufg.width(q.x1() - q.x0());
				ufg.height(q.y1() - q.y0());
				ufg.offset().set(q.x0(), q.y0() + (font.characterHeight + font.scanLineDescent));
				ufg.xadvance(advance.get(0) * font.scale);
				ufg.uv(0).set(q.s0(), q.t0());
				ufg.uv(1).set(q.s1(), q.t1());
	
			}
	
		})
		.texture(it -> it.id(fontTexture.textureID()));

	}

}
