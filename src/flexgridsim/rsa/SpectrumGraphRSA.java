/*
 * 
 */
package flexgridsim.rsa;


import java.util.ArrayList;

import org.w3c.dom.Element;


import flexgridsim.FlexGridLink;
import flexgridsim.Flow;
import flexgridsim.LightPath;
import flexgridsim.PhysicalTopology;
import flexgridsim.TrafficGenerator;
import flexgridsim.VirtualTopology;
import flexgridsim.util.ConstantsRSA;
import flexgridsim.util.MultiGraph;
import flexgridsim.util.WeightedGraph;
/**
 * A weighted graph associates a label (weight) with every edge in the graph. If
 * a pair of nodes has a array of weights equal to zero, it means the edge between them
 * doesn't exist.
 * 
 * @author pedrom
 */
public class SpectrumGraphRSA implements RSA {
	protected PhysicalTopology pt;
	protected VirtualTopology vt;
	protected ControlPlaneForRSA cp;
	protected WeightedGraph graph;
	protected MultiGraph spectrumGraph;
	protected Element rsaXml;

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
		LightPath[] lps = new LightPath[1];
		int demandInSlots = (int) Math.ceil(flow.getRate()
				/ (double) pt.getSlotCapacity());
		// Shortest-Path routing
		LightPath shortestPath = getShortestPath(this.spectrumGraph,
				flow.getSource(), flow.getDestination(), demandInSlots);
		System.out.print("Sizes");
		for (Integer size : flow.getSizes()) {
			System.out.print(size+",");
		}
		System.out.println();
		int rate = flow.getRate();
		if (flow.isBatch()){
			rate -= flow.getSizes().get(0);
			flow.getSizes().remove(0);
			while (rate > 0 && shortestPath == null){
				shortestPath = getShortestPath(this.spectrumGraph, flow.getSource(), flow.getDestination(), (int) Math.ceil(rate/ (double) pt.getSlotCapacity()));
				rate -= flow.getSizes().get(0);
				flow.getSizes().remove(0);
			}
		}
		if (shortestPath == null) {
			cp.blockFlow(flow.getID());
			return;
		} else {
			flow.setNumberOfFLowsAccepted(rate);
		}
		// Create the links vector
		long id = vt.createLightpath(shortestPath.getLinks(),
				shortestPath.getFirstSlot(), shortestPath.getLastSlot());
		if (id >= 0) {
			// Single-hop routing (end-to-end lightpath)
			lps[0] = vt.getLightpath(id);
			flow.setFirstSlot(shortestPath.getFirstSlot());
			flow.setLastSlot(shortestPath.getLastSlot());
			flow.setLinks(shortestPath.getLinks());
			cp.acceptFlow(flow.getID(), lps);
			for (int i = 0; i < shortestPath.getLinks().length; i++) {
				FlexGridLink curentLink = pt.getLink(shortestPath.getLink(i));
				this.spectrumGraph
						.markEdgesRemoved(curentLink.getSource(),
								curentLink.getDestination(),
								shortestPath.getFirstSlot(),
								shortestPath.getLastSlot());
			}
			return;
		}
		// Block the call
		cp.blockFlow(flow.getID());
	}

	@Override
	public void flowDeparture(Flow flow) {
		if (flow.getLinks() == null)
			return;
		for (int i = 0; i < flow.getLinks().length; i++) {
			FlexGridLink curentLink = pt.getLink(flow.getLink(i));
			this.spectrumGraph.restoreRemovedEdges(curentLink.getSource(), curentLink.getDestination(), flow.getFirstSlot(), flow.getLastSlot());
		}
	}

	/**
	 * Retrieves the shortest path between a source and a destination node,
	 * within a weighted graph.
	 * 
	 * @param G
	 *            the weighted graph in which the shortest path will be found
	 * @param src
	 *            the source node
	 * @param dst
	 *            the destination node
	 * @param demand
	 *            size of the demand
	 * @return the shortest path, as a vector of integers that represent node
	 *         coordinates
	 */
	public LightPath getShortestPath(MultiGraph G, int src, int dst,
			int demand) {
		double[][] distance = new double[spectrumGraph.size()][pt.getNumSlots()];
		int[][] previous = new int[spectrumGraph.size()][pt.getNumSlots()];
		for (int i = 0; i < spectrumGraph.size(); i++) {
			for (int j = 0; j < pt.getNumSlots(); j++) {
				distance[i][j] = ConstantsRSA.INFINITE;
				previous[i][j] = -1;
			}
		}
		for (int i = 0; i < pt.getNumSlots(); i++) {
			distance[src][i] = 0;
		}
		ArrayList<Integer> Q = new ArrayList<Integer>();
		for (int i = 0; i < spectrumGraph.size(); i++) {
			Q.add(Integer.valueOf(i));
		}
		while (!Q.isEmpty()) {
			int u = minMatrixIndex(distance, Q);
			int uIndex = getListIndex(Q, u);
			Q.remove(uIndex);
			int[] neighbors = spectrumGraph.neighbors(u);
			for (int i = 0; i < neighbors.length; i++) {
				int v = neighbors[i];
				if (!contain(Q, v))
					continue;
				for (int s = 0; s < pt.getNumSlots() - demand - 1; s++) {

					if (spectrumGraph.hasSetOfEdges(u, v, s, s + demand - 1)) {
						
						double cost = distance[u][s] + calculateCost(u, v, s, demand);
						if (cost < distance[v][s]) {
							distance[v][s] = cost;
							previous[v][s] = u;
						}
						
					}
				}
			}
		}
		ArrayList<Integer> path = new ArrayList<Integer>();
		int minDistanceSlot = minArrayIndex(distance[dst]);
		int last = dst;
		while (last != src) {
			path.add(0,last);
			last = previous[last][minDistanceSlot];
			// No path
			if (last == -1) {
				return null;
			}
		}
		path.add(0, src);
		int[] links = new int[path.size() - 1];
		for (int i = 0; i < path.size() - 1; i++) {
			links[i] = pt.getLink(path.get(i), path.get(i+1)).getID();
		}
		if (links.length == 0){
			System.out.println("Erro doido");
		}
		return new LightPath(Integer.MAX_VALUE, src, dst,
				links, minDistanceSlot, minDistanceSlot + demand - 1);
	}

	/**
	 * Check if and array list contais an integer value.
	 *
	 * @param list the list
	 * @param value the value
	 * @return true, if it contains
	 */
	public boolean contain(ArrayList<Integer> list, int value) {
		for (int i = 0; i < list.size(); i++) {
			if (value == list.get(i).intValue()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * implement a function that.
	 *
	 * @param src the source node
	 * @param dst the destination node
	 * @param firstSlot the first slot
	 * @param demand the demand
	 * @return the double
	 */
	public double calculateCost(int src, int dst, int firstSlot, int demand) {
		return 1;
	}

	/**
	 * Min array index.
	 *
	 * @param array the array
	 * @return the int
	 */
	public static int minArrayIndex(double[] array) {
		double min = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
				index = i;
			}
		}
		return index;
	}

	/**
	 * Min matrix index.
	 *
	 * @param array the array
	 * @param Q the q
	 * @return the int
	 */
	public static int minMatrixIndex(double[][] array, ArrayList<Integer> Q) {
		double min = Double.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (array[i][j] < min && containsValue(Q, i)) {
					min = array[i][j];
					index = i;
				}
			}

		}
		return index;
	}

	/**
	 * Contains value.
	 *
	 * @param Q the q
	 * @param value the value
	 * @return true, if successful
	 */
	public static boolean containsValue(ArrayList<Integer> Q, int value) {
		for (int i = 0; i < Q.size(); i++) {
			if (Q.get(i).intValue() == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the list index.
	 *
	 * @param Q the q
	 * @param value the value
	 * @return the list index
	 */
	public static int getListIndex(ArrayList<Integer> Q, int value) {
		for (int i = 0; i < Q.size(); i++) {
			if (Q.get(i).intValue() == value) {
				return i;
			}
		}
		return -1;
	}
}