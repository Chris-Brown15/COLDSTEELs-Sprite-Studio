package cs.csss.engine;

import sc.core.graphics.nanovg.SCNanoVGTypeface;

/**
 * Used to store a name along with a {@code NanoVGTypeface}.
 */
public record NamedNanoVGTypeface(String name , SCNanoVGTypeface typeface) {}
