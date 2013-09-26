package flexgridsim.rsa;

import org.w3c.dom.Element;

import flexgridsim.Flow;
import flexgridsim.LightPath;
import flexgridsim.PhysicalTopology;
import flexgridsim.TrafficGenerator;
import flexgridsim.VirtualTopology;
import flexgridsim.util.Dijkstra;
import flexgridsim.util.MultiGraph;
import flexgridsim.util.WeightedGraph;

/**
 * The Class NDijkstra.
 */
public class NDijkstra implements RSA {

	protected PhysicalTopology pt;
	protected VirtualTopology vt;
	protected ControlPlaneForRSA cp;
	protected WeightedGraph graph;
	protected MultiGraph spectrumGraph;
	protected Element rsaXml;
	protected static final double INFINITE = Double.MAX_VALUE;
	@Override
	public void simulationInterface(Element xml, PhysicalTopology pt, VirtualTopology vt,
			ControlPlaneForRSA cp, TrafficGenerator traffic) {
		this.pt = pt;
		this.vt = vt;
		this.cp = cp;
		this.graph = pt.getWeightedGraph();
		this.spectrumGraph = new MultiGraph(this.graph, pt, traffic);
		this.rsaXml = xml;
	}

	@Override
	public void flowArrival(Flow flow) {
		int demandInSlots = (int) Math.ceil(flow.getRate()
				/ (double) pt.getSlotCapacity());
		int[][] routes = new int[pt.getNumSlots() - demandInSlots][];
		double[] distance = new double [pt.getNumSlots() - demandInSlots];
		for (int i = 0; i < pt.getNumSlots()-demandInSlots; i++) {
			WeightedGraph G = this.generateGraph(pt.getNumNodes());
			routes[i] = Dijkstra.getShortestPath(G, flow.getSource(), flow.getDestination());
			double costSum = 0;
			for (int j = 0; j < routes[i].length-1; j++) {
				costSum += G.getWeight(j, j+1);
			}
			distance[i] = costSum;
		}
		int minDistance = 0;
		for (int i = 0; i < distance.length; i++) {
			if (distance[i] < distance[minDistance]){
				minDistance = i;
			}
		}
		int links[] = new int[routes[minDistance].length-1];
		for (int i = 0; i < routes[i].length-1; i++) {
			links[i] = pt.getLink(routes[minDistance][i], routes[minDistance][i+1]).getID();
		}
		long id = vt.createLightpath(links, minDistance, minDistance+demandInSlots);
		if (id >= 0) {
			LightPath[] lps = new LightPath[1];
			lps[0] = vt.getLightpath(id);
			flow.setLinks(links);
			cp.acceptFlow(flow.getID(), lps);
			return;
		}
		// Block the call
		cp.blockFlow(flow.getID());
	}

	@Override
	public void flowDeparture(Flow flow) {
		
	}
	
	/**
	 * Generate graph.
	 *
	 * @param size the size
	 * @return the double[][]
	 */
	private WeightedGraph generateGraph(int size) {
		WeightedGraph G = new WeightedGraph(size);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (pt.hasLink(i, j)){
					G.setWeight(i, j, this.calculateCost(i,j));
				} else {
					G.setWeight(i, j, INFINITE);
				}
			}
		}
		return G;
	}
	
	/**
	 * Calculate cost.
	 *
	 * @param i the i
	 * @param j the j
	 * @return the int
	 */
	public int calculateCost(int i, int j){
		return 1;
	}
}
