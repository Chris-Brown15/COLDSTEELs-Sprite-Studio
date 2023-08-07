package cs.csss.project.io;

import java.util.concurrent.atomic.AtomicInteger;

import cs.core.utils.threads.Await;

class ExportFinishedAwait extends Await {

	private final AtomicInteger counter;
	private final int requiredAmount;
	
	public ExportFinishedAwait(AtomicInteger counter , int requiredAmount) {
		
		this.counter = counter;
		this.requiredAmount = requiredAmount;
		
	}

	@Override public void await() {

		Thread currentThread = Thread.currentThread();
		while(!isFinished()) synchronized(currentThread) {
			
			try {
				
				currentThread.wait(0, 10_000);
				
			} catch (InterruptedException e) {
				
				throw new IllegalStateException(e);
				
			}
			
		}

	}

	@Override public boolean isFinished() {

		return counter.get() != requiredAmount;
		
	}

}
