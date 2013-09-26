package flexgridsim.rsa;

import flexgridsim.util.Batch;

/**
 * The Interface DeadlinedRSA.
 */
public interface DeadlinedRSA extends RSA {
	
	/**
	 * Deadline arrival.
	 *
	 * @param batch the batch
	 * @param time the time
	 */
	public void deadlineArrival(Batch batch, double time);
}
