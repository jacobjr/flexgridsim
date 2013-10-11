package flexgridsim.rsa;

import org.w3c.dom.Element;

import flexgridsim.PhysicalTopology;
import flexgridsim.TrafficGenerator;
import flexgridsim.VirtualTopology;


/**
 * The Class DefragmentRSA.
 */
public class FragmentationRatio extends SpectrumGraphRSA implements RSA {
	
	/**
	 * Instantiates a new defragment rsa and set the parameters alpha and beta as 1.
	 */
	public FragmentationRatio() {
		super();
	}
	
	@Override
	public void simulationInterface(Element xml, PhysicalTopology pt, VirtualTopology vt,
			ControlPlaneForRSA cp, TrafficGenerator traffic) {
		super.simulationInterface(xml, pt, vt, cp, traffic);
	}
	
	/* (non-Javadoc)
	 * @see flexgridsim.rsa.SpectrumGraphRSA#calculateCost(flexgridsim.util.MultiGraph, int, int)
	 */
	public double calculateCost(int src, int dst, int firstSlot, int demand) {
		this.spectrumGraph.markEdgesRemoved(src, dst, firstSlot, firstSlot + demand-1);
		double result = ((double) 1 + 1.0 - (this.spectrumGraph.getFragmentationRatio(src, dst, pt.getSlotCapacity())));
		this.spectrumGraph.restoreRemovedEdges(src, dst, firstSlot, firstSlot + demand-1);
		return result;
	}
}

