package cs.csss.editor;

/**
 * Container for metadata for a brush script.
 */
public record BrushScriptMeta(String tooltip , boolean isStateful , String scriptName , boolean isRenderEvent , boolean isTransientEvent) {}