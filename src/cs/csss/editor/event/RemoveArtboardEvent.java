/**
 * 
 */
package cs.csss.editor.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import cs.core.utils.CSRefInt;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Animation;
import cs.csss.project.AnimationFrame;
import cs.csss.project.AnimationSwapType;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.utils.FloatReference;

/**
 * Event for removing artboards from the program. Only nonshallow copied artboards can be removed. When removed, any shallow copies are removed and 
 * deleted. When undone, the artboard is added back to the project and any animations it was previously in.
 */
public class RemoveArtboardEvent extends CSSSMemoryEvent {

	private final CSSSProject project;
	private final Artboard artboard;
	private final int artboardIndex;
	
	private final ArrayList<AnimationStatusBackup> statuses = new ArrayList<>();
	private AnimationStatusBackup removedArtboardStatusBackup = null;
	
	/**
	 * Creates a new remove artboard event.
	 * 
	 * @param project the project whose artboard is getting removed
	 * @param removeThis artboard to remove
	 * @throws NullPointerException if either parameter is <code>null</code>.
	 * @throws IllegalArgumentException if {@code removeThis} is a shallow copy.
	 */
	public RemoveArtboardEvent(CSSSProject project , Artboard removeThis) {
		
		super(true , false , SHUTDOWN_ON_REMOVE_FROM_UNDO);
		this.project = Objects.requireNonNull(project);
		this.artboard = Objects.requireNonNull(removeThis);
		if(removeThis.isShallowCopy()) throw new IllegalArgumentException(removeThis + " is a shallow copy artboard");
		
		artboardIndex = project.getArtboardIndex(removeThis);
	
		List<Artboard> shallowCopies = project.shallowCopiesOf(removeThis);
		//look for either a shallow copy or the removed artboard in each animation and create a backup for its frame
		FindShallowCopies: for(Iterator<Animation> animations = project.animations() ; animations.hasNext() ; ) {
			
			Animation animation = animations.next();

			if(removedArtboardStatusBackup == null) { 
				
				removedArtboardStatusBackup = getBackupFrom(animation, removeThis);
				//there cant be a shallow copy in this animation if we have found the original in it
				if(removedArtboardStatusBackup != null) continue;
				
			}
			
			ListIterator<Artboard> shallowCopiesIterator = shallowCopies.listIterator();
			while(shallowCopiesIterator.hasNext()) {
				
				AnimationStatusBackup backup = getBackupFrom(animation, shallowCopiesIterator.next());
			
				if(backup != null) {
					
					statuses.add(backup);
					shallowCopiesIterator.remove();
					if(shallowCopies.isEmpty() && removedArtboardStatusBackup != null) break FindShallowCopies; 
					
				}
				
			}
			
		}
		
	}

	@Override public void _do() {

		project.removeArtboard(artboard);
		
	}

	@Override public void undo() {
		
		if(removedArtboardStatusBackup != null) {
			
			restore(removedArtboardStatusBackup, artboard , true);
			for(AnimationStatusBackup x : statuses) restore(x , artboard , false);
			
		} else project.addLooseArtboard(artboardIndex , artboard);
		
	}

	@Override public void shutDown() {

		if(isFreed()) return;
		
		artboard.shutDown();

	}

	@Override public boolean isFreed() {

		return artboard.isFreed();
		
	}
	
	private AnimationStatusBackup getBackupFrom(Animation animation , Artboard artboard) {
		
		int frameIndex = animation.indexOf(artboard);
		if(frameIndex != -1) {
			
			AnimationFrame frame = animation.getFrame(frameIndex);
			return new AnimationStatusBackup(
				animation , 
				frameIndex , 
				frame.swapType() , 
				frame.updatesContainer() , 
				frame.timeContainer()
			);
			
		}
		
		return null;
		
	}
	
	@RenderThreadOnly private void restore(AnimationStatusBackup backup , Artboard artboard , boolean addLoose) {

		Animation animation = backup.animation;
		int index = backup.frameIndex;
		AnimationSwapType swapType = backup.swapType;
		CSRefInt updatesContainer = backup.updatesContainer;
		FloatReference timeContainer = backup.timeContainer;
		
		if(addLoose) project.addLooseArtboard(artboardIndex, artboard);

		project.appendArtboardToAnimation(animation, artboard);
		animation.setFramePosition(animation.numberFrames() - 1, index);
		//set each field if nondefault
		if(swapType != animation.defaultSwapType()) animation.setFrameSwapType(index, swapType);
		if(updatesContainer != animation.defaultUpdateAmount()) animation.setFrameUpdates(index, updatesContainer);
		if(timeContainer != animation.defaultSwapTime()) animation.setFrameTime(index, timeContainer);
		
		project.arrangeArtboards();
		
	}
		
	//used to remember data about the animation frames the removed artboard was in 
	private record AnimationStatusBackup(
		Animation animation , 
		int frameIndex , 
		AnimationSwapType swapType , 
		CSRefInt updatesContainer , 
		FloatReference timeContainer
	) {}

}
