package cs.csss.engine;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import cs.core.utils.Lambda;
import cs.core.utils.Temporal;
import cs.core.utils.TemporalEvent;
import cs.core.utils.Test;
import cs.core.utils.TemporalEvent.ContinuousPredicatedEvent;
import cs.core.utils.TemporalEvent.ContinuousTimedEvent;
import cs.core.utils.TemporalEvent.Cooldown;
import cs.core.utils.TemporalEvent.PredicatedCooldown;
import cs.core.utils.TemporalEvent.PredicatedEvent;
import cs.core.utils.TemporalEvent.TimedCooldown;
import cs.core.utils.TemporalEvent.TimedEvent;

/**
 * Implementation of {@link cs.core.utils.Temporal Temporal} that is safe to modify and iterate over concurrently.
 */
public class ConcurrentTemporal implements Temporal {

	protected final ConcurrentLinkedDeque<TimedEvent> timeBasedEvents = new ConcurrentLinkedDeque<>();
	protected final ConcurrentLinkedDeque<ContinuousTimedEvent> continuousTimedEvents = new ConcurrentLinkedDeque<>();
	protected final ConcurrentLinkedDeque<PredicatedEvent> predicatedEvents = new ConcurrentLinkedDeque<>();
	protected final ConcurrentLinkedDeque<ContinuousPredicatedEvent> continuousPredicatedEvents = new ConcurrentLinkedDeque<>();
	protected final ConcurrentHashMap<Long , TimedCooldown> timedCooldowns = new ConcurrentHashMap<>();
	protected final ConcurrentHashMap<Long , PredicatedCooldown> predicatedCooldowns = new ConcurrentHashMap<>();
	
	@Override public Temporal inMillis(double millis, Lambda code) {

	 	TimedEvent newEvent = new ttes.TimedEvent(millis, code);
		newEvent.start();
		timeBasedEvents.add(newEvent);		
		return this;
		
	}

	@Override public Temporal forMillis(double millis, Lambda code) {

		ContinuousTimedEvent newEvent = new ttes.ContinuousTimedEvent(millis, code);
		newEvent.start();
		continuousTimedEvents.add(newEvent);
		return this;
		
	}

	@Override public Temporal onTrue(Test test, Lambda code) { 
		
		PredicatedEvent newEvent = new ttes.PredicatedEvent(test, code);
		predicatedEvents.add(newEvent);
		return this;
		
	}

	@Override public Temporal whileTrue(Test test, Lambda code) {

	 	ContinuousPredicatedEvent newEvent = new ttes.ContinuousPredicatedEvent(test, code);
	 	continuousPredicatedEvents.add(newEvent);
		return this;
		
	}

	@Override public Temporal coolDown(long cooldownUID, double coolDownTime, Lambda code) {
		
		if(!timedCooldowns.containsKey(cooldownUID)) {
			
			TimedCooldown newCooldown = new ttes.TimedCooldown(cooldownUID, coolDownTime, code);
			timedCooldowns.put(cooldownUID , newCooldown);				
			
		}

		return this;
		
	}

	@Override public Temporal coolDown(long cooldownUID, Test test, Lambda lambda) {

		if(!predicatedCooldowns.containsKey(cooldownUID)) {
			
			predicatedCooldowns.put(cooldownUID , new ttes.PredicatedCooldown(test, cooldownUID, lambda));
			
		}

		return this;
		
	}

	@Override public boolean isCoolingDown(long coolDownUID) {

		return predicatedCooldowns.containsKey(coolDownUID) || timedCooldowns.containsKey(coolDownUID);
		
	}

	/**
	 * Must be called whenever it is desired to check and test previously created events. Should be called once per frame. 
	 */
	public void updateAllEvents() {
		
		Iterator<TimedEvent> timeBasedEventIterator = timeBasedEvents.iterator();			
		while(timeBasedEventIterator.hasNext()) if(timeBasedEventIterator.next().test()) timeBasedEventIterator.remove();

		Iterator<ContinuousTimedEvent> continuousTimedEventsIterator = continuousTimedEvents.iterator();			
		while(continuousTimedEventsIterator.hasNext()) if(continuousTimedEventsIterator.next().test()) { 
			
			continuousTimedEventsIterator.remove();
			
		}
		
		Iterator<PredicatedEvent> predicatedEventsIterator = predicatedEvents.iterator();
		while(predicatedEventsIterator.hasNext()) if(predicatedEventsIterator.next().test()) predicatedEventsIterator.remove();
		
		Iterator<ContinuousPredicatedEvent> continuousPredicatedEventsIterator = continuousPredicatedEvents.iterator();
		while(continuousPredicatedEventsIterator.hasNext()) if(continuousPredicatedEventsIterator.next().test()) {
			
			continuousPredicatedEventsIterator.remove();
			
		}
		
		testCooldowns();
		
	}
	
	protected void testCooldowns() {
		
		testCooldown(timedCooldowns);
		testCooldown(predicatedCooldowns);
		
	}
	
	protected <CooldownType extends Cooldown> void testCooldown(ConcurrentHashMap<Long , CooldownType> cooldowns) {
	
		cooldowns.entrySet().forEach(entry -> {
			
			if(entry.getValue().test()) predicatedCooldowns.remove(entry.getKey());
			
		});
		
	}
	
	/**
	 * Returns the total number of created events.
	 * 
	 * @return Number of current events.
	 */
	public int numberEvents() {
		
		return timeBasedEvents.size() + 
			   continuousTimedEvents.size() + 
			   predicatedEvents.size() + 
			   continuousPredicatedEvents.size() + 
			   timedCooldowns.size() + 
			   predicatedCooldowns.size();
		
	}

	/**
	 * Namespace for threaded temporal event objects.
	 */
	private static class ttes {
		
		static class TimedEvent extends TemporalEvent.TimedEvent {

			protected TimedEvent(double timeMillis, Lambda code) {
			
				super(timeMillis, code);
			
			}
			
		}

		static class ContinuousTimedEvent extends TemporalEvent.ContinuousTimedEvent {

			protected ContinuousTimedEvent(double timeMillis, Lambda code) {
			
				super(timeMillis, code);
			
			}
			
		}

		static class PredicatedEvent extends TemporalEvent.PredicatedEvent {

			protected PredicatedEvent(Test test, Lambda code) {
				
				super(test, code);
				
			}

		}

		static class ContinuousPredicatedEvent extends TemporalEvent.ContinuousPredicatedEvent {

			protected ContinuousPredicatedEvent(Test test, Lambda code) {
				
				super(test, code);
				
			}

		}
		
		static class TimedCooldown extends TemporalEvent.TimedCooldown {

			protected TimedCooldown(long cooldownUID, double timeMillis, Lambda code) {
				
				super(cooldownUID, timeMillis, code);
				
			}

			public boolean test() {
				
				return super.test();
				
			}
			
		}
		
		static class PredicatedCooldown extends TemporalEvent.PredicatedCooldown {

			protected PredicatedCooldown(Test test, long cooldownUID, Lambda code) {
				
				super(test, cooldownUID, code);
								
			}
			
		}
				
	}

}
