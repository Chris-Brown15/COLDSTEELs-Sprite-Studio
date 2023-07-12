package cs.csss.project;

import java.util.Iterator;

import cs.csss.project.ArtboardPalette.PalettePixel;
import cs.csss.project.ArtboardTexture.IndexPixel;

public class ArtboardRegion implements Iterable<ArtboardPixel> {

	private final Artboard source;
	
	private final int 
		bottomLeftX ,
		bottomLeftY ,
		width ,
		height
	;
	
	ArtboardRegion(Artboard source , int bottomLeftX , int bottomLeftY , int width , int height) {
	
		this.source = source;
		this.bottomLeftX = bottomLeftX;
		this.bottomLeftY = bottomLeftY;
		this.width = width;
		this.height = height;
		
	}

	@Override public ArtboardRegionIterator iterator() {

		return new ArtboardRegionIterator(source , bottomLeftX , bottomLeftY , width , height);
		
	}	

	private static class ArtboardRegionIterator implements Iterator<ArtboardPixel> , AutoCloseable{

		private final int width , height;
		
		private final Artboard source;
		
		private int 
			col = 0 ,
			row = 0
		;
		
		private final ArtboardPixel[][] region;
		private ArtboardPixel next;
		
		private ArtboardRegionIterator(Artboard source , int x , int y , int width , int height) {
			
			this.width = width;
			this.height = height;
			this.source = source;
			
			this.region = new ArtboardPixel[height][width];
			for(; row < height ; row++ , col = 0 , y++) for(; col < width ; col++ , x++) {
				
				IndexPixel indexPixel = source.getIndexPixelAtIndices(x, y);
				PalettePixel palette = source.getColorFromIndicesOfPalette(indexPixel.xIndex, indexPixel.yIndex);
				region[row][col] = new ArtboardPixel(
					col , 
					row , 
					x , 
					y , 
					indexPixel.xIndex , 
					indexPixel.yIndex , 
					palette.red() , 
					palette.green() , 
					palette.blue() , 
					palette.alpha()
				);
				
			}
			
			row = 0;
			col = 0;
			
		}
		
		@Override public void remove() {
			
			next.markRemoved();
			
		}
				
		@Override public boolean hasNext() {

			return row < height && col < width;
			
		}

		@Override public ArtboardPixel next() {

			next = region[row][col];
			
			col++;
			if(col == width) {
				
				row++;
				col = 0;
			}
			
			return next;
			
		}

		@Override public void close() {

			row = 0;
			col = 0;
			
			if(source.isActiveLayerVisual()) {

				while(hasNext()) {
					
					if(next().removed()) {
						
//						VisualLayer nextHighest = source.getHighestLowerRankLayerModifying(
//							source.getLayerRank((VisualLayer)source.activeLayer()) , 
//							col , 
//							row
//						);
						
						
						
						
						
					}
					
				}			
				
			} else {
				
			}			
			
		}
				
	}
	
}
