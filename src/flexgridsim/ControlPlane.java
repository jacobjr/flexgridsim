/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flexgridsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import flexgridsim.rsa.ControlPlaneForRSA;
import flexgridsim.rsa.DeadlinedRSA;
import flexgridsim.rsa.RSA;
import flexgridsim.util.Batch;

/**
 * The Control Plane is responsible for managing resources and
 * connection within the network.
 */
public class ControlPlane implements ControlPlaneForRSA {

    private RSA rsa;
    private PhysicalTopology pt;
    private VirtualTopology vt;
    private Map<Flow, Path> mappedFlows; // Flows that have been accepted into the network
    private Map<Long, Flow> activeFlows; // Flows that have been accepted or that are waiting for a decision 
    private Tracer tr = Tracer.getTracerObject();
    private MyStatistics st = MyStatistics.getMyStatisticsObject();
    private EventScheduler eventScheduler;

	/**
	 * Creates a new ControlPlane object.
	 *
	 * @param xml the xml
	 * @param eventScheduler the event scheduler
	 * @param rsaModule the name of the RWA class
	 * @param pt the network's physical topology
	 * @param vt the network's virtual topology
	 * @param traffic the traffic
	 */
    public ControlPlane(Element xml, EventScheduler eventScheduler, String rsaModule, PhysicalTopology pt, VirtualTopology vt, TrafficGenerator traffic) {
        @SuppressWarnings("rawtypes")
		Class RSAClass;
        this.eventScheduler = eventScheduler;
        mappedFlows = new HashMap<Flow, Path>();
        activeFlows = new HashMap<Long, Flow>();
        this.pt = pt;
        this.vt = vt;

        try {
            RSAClass = Class.forName(rsaModule);
            rsa = (RSA) RSAClass.newInstance();
            rsa.simulationInterface(xml, pt, vt, this, traffic);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    /**
     * Deals with an Event from the event queue.
     * If it is of the FlowArrivalEvent kind, adds it to the list of active flows.
     * If it is from the FlowDepartureEvent, removes it from the list.
     * 
     * @param event the Event object taken from the queue 
     */
    public void newEvent(Event event) {
        if (event instanceof FlowArrivalEvent) {
            newFlow(((FlowArrivalEvent) event).getFlow());
            rsa.flowArrival(((FlowArrivalEvent) event).getFlow());
        } else if (event instanceof FlowDepartureEvent) {
            Flow removedFlow = removeFlow(((FlowDepartureEvent) event).getID());
            if (removedFlow == null) 
            	return;
            rsa.flowDeparture(removedFlow);
        } else if (event instanceof DeadlineEvent && rsa instanceof DeadlinedRSA){
        	((DeadlinedRSA)rsa).deadlineArrival(((DeadlineEvent)event).getBatch(), event.getTime());
        }
    }

    /**
     * Adds a given active Flow object to a determined Physical Topology.
     * 
     * @param id unique identifier of the Flow object
     * @param lightpaths the Path, or list of LighPath objects
     * @return true if operation was successful, or false if a problem occurred
     */
    public boolean acceptFlow(long id, LightPath[] lightpaths) {
        Flow flow;

        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (!canAddFlowToPT(flow, lightpaths)) {
                return false;
            } 
            addFlowToPT(flow, lightpaths);
            mappedFlows.put(flow, new Path(lightpaths));
            tr.acceptFlow(flow, lightpaths);
            st.acceptFlow(flow, lightpaths);
            flow.setAccepeted(true);
            return true;
        }
    }

    /**
     * Removes a given Flow object from the list of active flows.
     * 
     * @param id unique identifier of the Flow object
     * @return true if operation was successful, or false if a problem occurred
     */
    public boolean blockFlow(long id) {
        Flow flow;

        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (mappedFlows.containsKey(flow)) {
                return false;
            }
            activeFlows.remove(id);
            tr.blockFlow(flow);
            st.blockFlow(flow);
            return true;
        }
    }
    
    /**
     * Removes a given Flow object from the Physical Topology and then
     * puts it back, but with a new route (set of LightPath objects). 
     * 
     * @param id unique identifier of the Flow object
     * @param lightpaths list of LightPath objects, which form a Path
     * @return true if operation was successful, or false if a problem occurred
     */
    public boolean rerouteFlow(long id, LightPath[] lightpaths) {
        Flow flow;
        Path oldPath;

        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (!mappedFlows.containsKey(flow)) {
                return false;
            }
            oldPath = mappedFlows.get(flow);
            removeFlowFromPT(flow, lightpaths);
            if (!canAddFlowToPT(flow, lightpaths)) {
                addFlowToPT(flow, oldPath.getLightpaths());
                return false;
            }
            addFlowToPT(flow, lightpaths);
            mappedFlows.put(flow, new Path(lightpaths));
            //tr.flowRequest(id, true);
            return true;
        }
    }
    
    /**
     * Adds a given Flow object to the HashMap of active flows.
     * The HashMap also stores the object's unique identifier (ID). 
     * 
     * @param flow Flow object to be added
     */
    private void newFlow(Flow flow) {
        activeFlows.put(flow.getID(), flow);
    }
    
    /**
     * Removes a given Flow object from the list of active flows.
     * 
     * @param id the unique identifier of the Flow to be removed
     * 
     * @return the flow object
     */
    private Flow removeFlow(long id) {
        Flow flow;
        LightPath[] lightpaths;

        if (activeFlows.containsKey(id)) {
            flow = activeFlows.get(id);
            if (mappedFlows.containsKey(flow)) {
                lightpaths = mappedFlows.get(flow).getLightpaths();
                removeFlowFromPT(flow, lightpaths);
                mappedFlows.remove(flow);
            }
            activeFlows.remove(id);
            return flow;
        }
        return null;
    }
    
    /**
     * Removes a given Flow object from a Physical Topology. 
     * 
     * @param flow the Flow object that will be removed from the PT
     * @param lightpaths a list of LighPath objects
     */
    private void removeFlowFromPT(Flow flow, LightPath[] lightpaths) {
        int[] links;
        int firstSlot;
        int lastSlot;
        for (int i = 0; i < lightpaths.length; i++) {
            links = lightpaths[i].getLinks();
            firstSlot = lightpaths[i].getFirstSlot();
            lastSlot = lightpaths[i].getLastSlot();
            for (int j = 0; j < links.length; j++) {
                pt.getLink(links[j]).releaseSlots(firstSlot, lastSlot);
            }
            // Can the lightpath be removed?
            if (vt.isLightpathIdle(lightpaths[i].getID())) {
                vt.removeLightPath(lightpaths[i].getID());
            } 
        }

    }
    
    /**
     * Says whether or not a given Flow object can be added to a 
     * determined Physical Topology, based on the amount of bandwidth the
     * flow requires opposed to the available bandwidth.
     * 
     * @param flow the Flow object to be added 
     * @param lightpaths list of LightPath objects the flow uses
     * @return true if Flow object can be added to the PT, or false if it can't
     */
    private boolean canAddFlowToPT(Flow flow, LightPath[] lightpaths) {
        int[] links;
        // Test the availability of resources
        for (int i = 0; i < lightpaths.length; i++) {
            links = lightpaths[i].getLinks();
            for (int j = 0; j < links.length; j++) {
                if (pt.getLink(links[j]).areSlotsAvailable(lightpaths[i].getFirstSlot(), lightpaths[i].getLastSlot())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Adds a Flow object to a Physical Topology.
     * This means adding the flow to the network's traffic,
     * which simply decreases the available bandwidth.
     * 
     * @param flow the Flow object to be added 
     * @param lightpaths list of LightPath objects the flow uses
     */
    private void addFlowToPT(Flow flow, LightPath[] lightpaths) {
        int[] links;
        int firstSlot, lastSlot;

        // Implements it
        for (int i = 0; i < lightpaths.length; i++) {
            firstSlot = lightpaths[i].getFirstSlot();
            lastSlot = lightpaths[i].getLastSlot();
            links = lightpaths[i].getLinks();
            for (int j = 0; j < links.length; j++) {
                pt.getLink(links[j]).reserveSlots(firstSlot, lastSlot);
            }
        }
    }
    
    /**
     * Retrieves a Path object, based on a given Flow object.
     * That's possible thanks to the HashMap mappedFlows, which
     * maps a Flow to a Path.
     * 
     * @param flow Flow object that will be used to find the Path object
     * @return Path object mapped to the given flow 
     */
    public Path getPath(Flow flow) {
        return mappedFlows.get(flow);
    }
    
    /**
     * Retrieves the complete set of Flow/Path pairs listed on the
     * mappedFlows HashMap.
     * 
     * @return the mappedFlows HashMap
     */
    public Map<Flow, Path> getMappedFlows() {
        return mappedFlows;
    }
    
    /**
     * Retrieves a Flow object from the list of active flows.
     * 
     * @param id the unique identifier of the Flow object
     * @return the required Flow object
     */
    public Flow getFlow(long id) {
        return activeFlows.get(id);
    }
    
    /**
     * Counts number of times a given LightPath object
     * is used within the Flow objects of the network.
     * 
     * @param id unique identifier of the LightPath object
     * @return integer with the number of times the given LightPath object is used
     */
    public int getLightpathFlowCount(long id) {
        int num = 0;
        Path p;
        LightPath[] lps;
        ArrayList<Path> ps = new ArrayList<Path>(mappedFlows.values());
        for (int i = 0; i < ps.size(); i++) {
            p = ps.get(i);
            lps = p.getLightpaths();
            for (int j = 0; j < lps.length; j++) {
                if (lps[j].getID() == id) {
                    num++;
                    break;
                }
            }
        }
        return num;
    }
    
//    /**
//     * Schedule deadline.
//     *
//     * @param time the time
//     */
//    public void scheduleDeadline(double time){
//    	DeadlineEvent deadlineEvent = new DeadlineEvent();
//    	deadlineEvent.setTime(time);
//    	eventScheduler.addEvent(deadlineEvent);
//    }
    
    /**
     * Update deadline event.
     *
     * @param batch the batch
     */
    public void updateDeadlineEvent(Batch batch){
    	eventScheduler.updateDeadlineEvent(batch.getOldShortestDeadlineEvent(), batch.getShortestDeadlineEvent());
    }
    
    /**
     * Removes the deadline event.
     *
     * @param batch the batch
     */
    public void removeDeadlineEvent(Batch batch){
    	eventScheduler.removeDeadlineEvent(batch.getShortestDeadlineEvent());
    }
    
    /**
     * Adds the flow arrival event.
     *
     * @param event the event
     */
    public void addFlowArrivalEvent(FlowArrivalEvent event){
    	eventScheduler.addFlowArrivalEvent(event);
    }
    
    /**
     * Adds the flow departure event.
     *
     * @param event the event
     */
    public void addFlowDepartureEvent(FlowDepartureEvent event){
    	eventScheduler.addFlowDeparture(event);
    }
    
    /**
     * Update departure event.
     *
     * @param flow the flow
     * @param event the event
     */
    public void updateDepartureEvent(Flow flow, FlowDepartureEvent event){
    	eventScheduler.updateFlowDepartureEvent(flow, event);
    }
}
