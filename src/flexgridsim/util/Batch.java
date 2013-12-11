package flexgridsim.util;

import java.util.ArrayList;

import flexgridsim.DeadlineEvent;
import flexgridsim.Flow;

/**
 * The Class Batch.
 */
public class Batch extends ArrayList<Flow>{

	private static final long serialVersionUID = -5949815745169072537L;
	private DeadlineEvent shortestDeadlineEvent;
	private DeadlineEvent oldShortestDeadlineEvent;
	private int src;
	private int dst;
	private int rate;
	static long flowId = 0;
	static int IDCounter = 0;
	int ID;
	
	/**
	 * Instantiates a new batch.
	 *
	 * @param src the src
	 * @param dst the dst
	 */
	public Batch(int src, int dst){
		this.shortestDeadlineEvent = new DeadlineEvent(Double.MAX_VALUE, this);
		this.oldShortestDeadlineEvent = new DeadlineEvent(Double.MAX_VALUE, this);
		this.src = src;
		this.dst = dst;
		this.rate = 0;
		this.ID = IDCounter;
		IDCounter++;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getID() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	@Override
	public boolean add(Flow flow){
		super.add(flow);
		this.rate += flow.getRate();
		if (flow.getDeadline() < this.shortestDeadlineEvent.getTime()){
			this.oldShortestDeadlineEvent = shortestDeadlineEvent;
			this.shortestDeadlineEvent = new DeadlineEvent(flow.getDeadline(), this);
		}
		return true;
	}

	@Override
	public boolean remove(Object o){
		super.remove((Flow)o);
		rate -= ((Flow)o).getRate();
		return true;
	}
	/**
	 * Gets the old shortest deadline event.
	 *
	 * @return the old shortest deadline event
	 */
	public DeadlineEvent getOldShortestDeadlineEvent() {
		return oldShortestDeadlineEvent;
	}

	/**
	 * Gets the src.
	 *
	 * @return the srcflowId
	 */
	public int getSource() {
		return src;
	}

	/**
	 * Gets the dst.
	 *
	 * @return the dst
	 */
	public int getDestination() {
		return dst;
	}
	
	/**
	 * Gets the shortest deadline.
	 *
	 * @return the shortest deadline
	 */
	public double getShortestDeadline() {
		return shortestDeadlineEvent.getTime();
	}

	/**
	 * Gets the shortest deadline event.
	 *
	 * @return the shortest deadline event
	 */
	public DeadlineEvent getShortestDeadlineEvent() {
		return shortestDeadlineEvent;
	}

	/**
	 * Convert batch to single flow.
	 *
	 * @param time the time
	 * @return the flow
	 */
	public Flow convertBatchToSingleFlow(double time){
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		int rateSum = 0;
		int maxCos = 0;
		double maxDuration = 0;
//		System.out.print("Rates:");
		for (Flow flow : this) {
			rateSum += flow.getRate();
			sizes.add(flow.getRate());
			//rateSum += (flow.getDuration() / (flow.getTime() + flow.getDuration() - time)) * flow.getRate();
			
//			System.out.print(flow.getRate()+"-");
//			System.out.println("Rate="+flow.getRate() +" rateSum=" + (flow.getDuration() / (flow.getTime() + flow.getDuration() - time)) * flow.getRate());
			if (flow.getCOS() > maxCos) {
				maxCos = flow.getCOS();
			}
			if (flow.getDuration() > maxDuration) {
				maxDuration = flow.getDuration();
			}
		}
		
//		System.out.println();
		flowId++;
		Flow flow = new Flow(Long.MAX_VALUE - flowId, this.getSource(), this.getDestination(), time, rateSum, maxDuration, maxCos, 0);
		flow.setBatch(true);
		flow.setSizes(sizes);
		flow.setNumberOfFlowsGroomed(this.size());
		return flow;
	}

	/**
	 * Gets the rate.
	 *
	 * @return the rate
	 */
	public int getRate() {
		return rate;
	}
	
	/**
	 * Mininum deadline flow.
	 *
	 * @return the flow
	 */
	public Flow mininumDeadlineFlow(){
		if (this.size() == 0){
			return null;
		} else {
			Flow min = this.get(0);
			for (Flow f:this){
				if (min.getDeadline() < f.getDeadline()){
					min = f;
				}
			}
			return min;
		}
	}
	
}
