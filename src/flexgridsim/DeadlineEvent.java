package flexgridsim;

import flexgridsim.util.Batch;

/**
 * The Class DeadlineEvent.
 */
public class DeadlineEvent extends Event {
	private long ID;
	private static long numberOfEvents;
	private Batch batch;
	
	/**
	 * Instantiates a new deadline event.
	 *
	 * @param time the time
	 * @param batch the batch
	 */
	public DeadlineEvent(double time, Batch batch){
		super(time);
		numberOfEvents++;
		this.ID = numberOfEvents;
		this.batch = batch;
	}
	
	/**
	 * Gets the batch.
	 *
	 * @return the batch
	 */
	public Batch getBatch() {
		return batch;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public long getID(){
		return this.ID;
	}
}
