package flexgridsim.rsa;

import org.w3c.dom.Element;

import flexgridsim.ControlPlane;
import flexgridsim.Flow;
import flexgridsim.FlowArrivalEvent;
import flexgridsim.FlowDepartureEvent;
import flexgridsim.PhysicalTopology;
import flexgridsim.TrafficGenerator;
import flexgridsim.VirtualTopology;
import flexgridsim.util.Batch;
import flexgridsim.util.BatchSet;

/**
 * The Class DeadlineBatchGroom.
 */
public class DeadlineBatchGroom implements	DeadlinedRSA {
	protected PhysicalTopology pt;
	protected VirtualTopology vt;
	protected ControlPlaneForRSA cp;
	protected BatchSet batches;
	protected RSA rsa;
	
	/**
	 * Instantiates a new deadline batch groom.
	 */
	public DeadlineBatchGroom() {
		super();
		this.batches = new BatchSet();
	}

	@Override
	public void simulationInterface(Element xml, PhysicalTopology pt,
			VirtualTopology vt, ControlPlaneForRSA cp, TrafficGenerator traffic) {
		this.pt = pt;
		this.vt = vt;
		this.cp = cp;
		String rsaModule = "flexgridsim.rsa." + xml.getAttribute("rsaAlgorithm");
		try {
			@SuppressWarnings("rawtypes")
			Class RSAClass = Class.forName(rsaModule);
			this.rsa = (RSA) RSAClass.newInstance();
			this.rsa.simulationInterface(xml, pt, vt, cp, traffic);
		} catch (Throwable t) {
		    t.printStackTrace();
		}
	}

	@Override
	public void flowArrival(Flow flow) {
		if (flow.isBatch()){
			this.rsa.flowArrival(flow);
		} else {
			Batch updatedBatch = this.batches.addFLow(flow);
			this.updateDeadlineEvent(updatedBatch);
		}
	}

	@Override
	public void flowDeparture(Flow flow) {
		this.rsa.flowDeparture(flow);
	}

	@Override
	public void deadlineArrival(Batch batch, double time) {
		Flow groomedFLow = batch.convertBatchToSingleFlow(time);
		this.addNewFlowToCp(groomedFLow);
		this.batches.remove(batch);
	}

	/**
	 * Update deadline event.
	 *
	 * @param batch the batch
	 */
	public void updateDeadlineEvent(Batch batch) {
		 ((ControlPlane)this.cp).updateDeadlineEvent(batch);
	}
	
	/**
	 * Removes the deadline event.
	 *
	 * @param batch the batch
	 */
	public void removeDeadlineEvent(Batch batch){
		((ControlPlane)this.cp).removeDeadlineEvent(batch);
	}
	
	/**
	 * Adds the new flow to cp.
	 *
	 * @param flow the flow
	 */
	public void addNewFlowToCp(Flow flow){
		((ControlPlane)this.cp).addFlowArrivalEvent(new FlowArrivalEvent(flow.getTime(), flow));
		((ControlPlane)this.cp).addFlowDepartureEvent(new FlowDepartureEvent(flow.getTime()+flow.getDuration(), flow.getID(), flow));
	}
}
