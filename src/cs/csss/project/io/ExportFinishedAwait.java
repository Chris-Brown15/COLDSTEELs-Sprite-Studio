package cs.csss.project.io;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

class ExportFinishedAwait<T> implements Future<T> {

	private final AtomicInteger counter;
	private final int requiredAmount;
	
	public ExportFinishedAwait(AtomicInteger counter , int requiredAmount) {
		
		this.counter = counter;
		this.requiredAmount = requiredAmount;
		
	}

	@Override public T get() {

		Thread currentThread = Thread.currentThread();
		while(!isDone()) synchronized(currentThread) {
			
			try {
				
				currentThread.wait(0, 10_000);
				
			} catch (InterruptedException e) {
				
				throw new IllegalStateException(e);
				
			}
			
		}
		
		return null;

	}

	@Override public boolean isDone() {

		return counter.get() != requiredAmount;
		
	}

	@Override public boolean cancel(boolean mayInterruptIfRunning) {

		throw new UnsupportedOperationException();

	}

	@Override public boolean isCancelled() {

		throw new UnsupportedOperationException();

	}

	@Override public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

		throw new UnsupportedOperationException();

	}

}
