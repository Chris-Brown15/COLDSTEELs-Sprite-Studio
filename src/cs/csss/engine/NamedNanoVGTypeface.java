package cs.csss.engine;

import cs.coreext.nanovg.NanoVGTypeface;

/**
 * Used to store a name along with a {@code NanoVGTypeface}.
 */
public record NamedNanoVGTypeface(String name , NanoVGTypeface typeface) {}
