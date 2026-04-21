/**
 * 
 */
/**
 * 
 */
module CS_Sprite_Studio {
	
	requires transitive org.lwjgl;
	requires transitive steel.Module;
	requires org.joml;
	requires org.lwjgl.opengl;
	requires transitive steamworks4j;
	requires java.desktop;
	requires org.lwjgl.lz4;
	requires java.sql;
	requires org.python.jython2.standalone;
	exports cs.csss.editor;
	exports cs.csss.editor.event;
	exports cs.csss.editor.brush;
	exports cs.csss.editor.line;
	exports cs.csss.editor.shape;
	exports cs.csss.editor.ui;
	exports cs.csss.editor.palette;
	exports cs.csss.project;
	exports cs.csss.engine;
	exports cs.ext.steamworks;
	exports cs.csss.misc.files;
	exports cs.csss.ui.menus;
	exports cs.csss.utils;
	exports cs.csss.project.io;
	exports cs.bringover.cs.core.utils.data;
	exports cs.csss.steamworks;	
	
}