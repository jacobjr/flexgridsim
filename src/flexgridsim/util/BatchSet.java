package flexgridsim.util;

import java.util.ArrayList;

import flexgridsim.Flow;

/**
 * The Class BatchSet.
 */
public class BatchSet extends ArrayList<Batch> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6955514003876428494L;
	
	/**
	 * Instantiates a new batch set.
	 */
	public BatchSet() {
	}

	/**
	 * Adds the f low.
	 * 
	 * @param flow
	 *            the flow
	 * @return true, if successful
	 */
	public Batch addFLow(Flow flow) {
		boolean existsBatch = false;
		Batch currentBatch = null;
		for (Batch b : this) {
			if (b.getSource() == flow.getSource()
					&& b.getDestination() == flow.getDestination()) {
				b.add(flow);
				currentBatch = b;
				existsBatch = true;
				break;
			}
		}
		if (!existsBatch) {
			Batch newBatch = new Batch(flow.getSource(), flow.getDestination());
			newBatch.add(flow);
			currentBatch = newBatch;
			this.add(newBatch);
		}
		return currentBatch;
	}

}
