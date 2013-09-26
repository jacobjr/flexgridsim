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

//	private Batch shortestDeadlineBatch;
	
	/**
	 * Instantiates a new batch set.
	 */
	public BatchSet() {
//		shortestDeadlineBatch = null;
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
//		if (shortestDeadlineBatch == null) {
//			shortestDeadlineBatch = currentBatch;
//		} else if (flow.getDeadline() < shortestDeadlineBatch
//				.getShortestDeadline()) {
//			if (currentBatch == null) {
//				throw new RuntimeException();
//			}
//			shortestDeadlineBatch = currentBatch;
//		}
		return currentBatch;
	}

//	/**
//	 * Gets the shortest deadline batch.
//	 * 
//	 * @return the shortest deadline batch
//	 */
//	public Batch getShortestDeadlineBatch() {
//		return shortestDeadlineBatch;
//	}

//	/**
//	 * Gets the shortest deadline.
//	 * 
//	 * @return the shortest deadline
//	 */
//	public double getShortestDeadline() {
//		return shortestDeadlineBatch.getShortestDeadline();
//	}
//	

}
