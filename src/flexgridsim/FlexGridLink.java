package flexgridsim;

import java.util.ArrayList;

/**
 * This class is based on the WDMLink but it's adapted to RSA operations for
 * contiguous slots allocation.
 * 
 * @author pedrom
 */
public class FlexGridLink {

	private int id;
	private int src;
	private int dst;
	private double delay;
	private int slots;
	private boolean[] freeSlots;
	private double weight;

	/**
	 * Creates a new Fiberlink object.
	 * 
	 * @param id
	 *            unique identifier
	 * @param src
	 *            source node
	 * @param dst
	 *            destination node
	 * @param delay
	 *            propagation delay (miliseconds)
	 * @param slots
	 *            number of slots available
	 * @param weight
	 *            optional link weight
	 */
	public FlexGridLink(int id, int src, int dst, double delay, int slots,
			double weight) {
		if (id < 0 || src < 0 || dst < 0 || slots < 1 ) {
			throw (new IllegalArgumentException());
		} else {
			this.id = id;
			this.src = src;
			this.dst = dst;
			this.delay = delay;
			this.slots = slots;
			this.weight = weight;
			this.freeSlots = new boolean[slots];
			for (int i = 0; i < slots; i++) {
				this.freeSlots[i] = true;
			}
		}
	}

	/**
	 * Gets the number of free slots in the link.
	 *
	 * @return the free slots
	 */
	public boolean[] getFreeSlots() {
		return freeSlots;
	}

	/**
	 * Retrieves the unique identifier for a given FlexGridLink.
	 * 
	 * @return the value of the FlexGridLink's id attribute
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * Retrieves the source node for a given FlexGridLink.
	 * 
	 * @return the value of the FlexGridLink's src attribute
	 */
	public int getSource() {
		return this.src;
	}

	/**
	 * Retrieves the destination node for a given FlexGridLink.
	 * 
	 * @return the value of the FlexGridLink's dst attribute
	 */
	public int getDestination() {
		return this.dst;
	}

	/**
	 * Retrieves the number of available slots for a given FlexGridLink.
	 * 
	 * @return the value of the FlexGridLink's slots attribute
	 */
	public int getSlots() {
		return this.slots;
	}

	/**
	 * Retrieves the weight for a given FlexGridLink.
	 * 
	 * @return the value of the FlexGridLink's weight attribute
	 */
	public double getWeight() {
		return this.weight;
	}

	/**
	 * Retrieves the propagation delay for a given FlexGridLink.
	 * 
	 * @return the value of the FlexGridLink's delay attribute
	 */
	public double getDelay() {
		return this.delay;
	}

	/**
	 * Says whether or not a determined set of contiguous slots are available.
	 * 
	 * @param lastSlot
	 *            last slot of the contiguous slots being checked
	 * @param firstSlot
	 *            first slot of the contiguous slots being checked
	 * @return true if the slots are available
	 */
	public Boolean areSlotsAvailable(int firstSlot, int lastSlot) {

		if (firstSlot < 0 || lastSlot >= this.slots) {
			throw (new IllegalArgumentException());
		} else {
			for (int i = firstSlot; i <= lastSlot; i++) {
				if (!freeSlots[i]) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Says whether or not a determined slot is available.
	 * 
	 * @param slot
	 *            first slot of the contiguous slots being checked
	 * @return true if the slot is available
	 */
	public Boolean isSlotAvailable(int slot) {
		return areSlotsAvailable(slot, slot);
	}

	/**
	 * Retrieves the list of all available slots in a given FlexGridLink.
	 * 
	 * @return list of available slots
	 */
	public int[] hasSlotAvailable() {
		ArrayList<Integer> wls = new ArrayList<Integer>();
		for (int i = 0; i < this.slots; i++) {
			if (this.isSlotAvailable(i)) {
				wls.add(i);
			}
		}
		int[] a = new int[wls.size()];
		for (int i = 0; i < wls.size(); i++) {
			a[i] = wls.get(i);
		}
		return a;
	}

	
	/**
	 * Gets the num free slots.
	 *
	 * @return the num free slots
	 */
	public int getNumFreeSlots() {
		int numFreeSlots =0;
		for (int i = 0; i < freeSlots.length; i++) {
			if (freeSlots[i]){
				numFreeSlots++;
			}
		}
		return numFreeSlots;
	}

	/**
	 * Retrieves the lowest available set of contiguous slots in a given
	 * FlexGridLink.
	 *
	 * @param numberOfSlots the number of slots to look
	 * @return first slot of the sequence of contiguous free slots
	 */
	public int firstSlotsAvailable(int numberOfSlots) {
		for (int i = 0; i < this.slots; i++) {
			if (this.isSlotAvailable(i)) {
				try{
					if (areSlotsAvailable(i, i + numberOfSlots - 1)) {
						return i;
					} else {
						i += numberOfSlots;
					}
				} catch (IllegalArgumentException e){
					System.out.println("Illegal argument for areSlotsAvailable");
					return -1;
				}
				
			}
		}
		return -1;
	}


	/**
	 * By attributing false to a given slot inside the freSlots array, this
	 * function "reserves" a set of contiguous slots.
	 * 
	 * @param lastSlot
	 *            the number of contiguous slots that will be reserved
	 * @param firstSlot
	 *            first slot of the contiguous slots to be reserved
	 * @return true if operation was successful, or false otherwise
	 */
	public boolean reserveSlots(int firstSlot, int lastSlot) {
		if (firstSlot < 0 || lastSlot >= this.slots) {
			throw (new IllegalArgumentException());
		} else {
			try{
				if (areSlotsAvailable(firstSlot, lastSlot)) {
					for (int i = firstSlot; i <= lastSlot; i++) {
						freeSlots[i] = false;
					}
					return true;
				} else {
					return false;
				}
			} catch (IllegalArgumentException e){
				System.out.println("Illegal argument for areSlotsAvailable");
				return false;
			}
		}
	}

	/**
	 * By attributing true to a given set of slots inside the freeSlots array,
	 * this function "releases" a set of slots.
	 * 
	 * @param lastSlot
	 *            the number of contiguous slots that will be released
	 * @param firstSlot
	 *            first slot of the contiguous slots to be released
	 */
	public void releaseSlots(int firstSlot, int lastSlot) {
		if (firstSlot < 0 || lastSlot >= this.slots) {
			throw (new IllegalArgumentException());
		} else {
			for (int i = firstSlot; i <= lastSlot; i++) {
				freeSlots[i] = true;
			}
		}
	}
	
	/**
	 * Number of fragments.
	 *
	 * @return the int
	 */
	public int numberOfFragments(){
		int counter = 0;
		boolean currentValue = true;
		for (int i = 0; i < this.freeSlots.length; i++) {
			if(this.freeSlots[i] != currentValue){
				if (currentValue == true)
					counter++;
				currentValue = !currentValue;
			}
		}
		return counter;
	}
	

	/**
	 * Gets the fragmentation ratio, a metric that states the potential of each free contiguous set of slots
	 * by telling the number of traffic calls it could fit in. then calculating the mean of that
	 *
	 * @param trafficCalls the traffic calls
	 * @param slotCapacity the slot capacity
	 * @return the fragmentation ratio
	 */
	public double getFragmentationRatio(TrafficInfo[] trafficCalls, double slotCapacity){
		ArrayList<Double> fragmentsPotential = new ArrayList<Double>();
		for (int i = 0; i < this.freeSlots.length-1; i++) {
			if (this.freeSlots[i] == true){
				i++;
				int fragmentSize = 1;
				while (freeSlots[i] == true && i < freeSlots.length-2 ){
					fragmentSize++;
					i++;
				}
				double counter = 0;
				for (TrafficInfo call : trafficCalls) {
					if (call.getRate()/slotCapacity >= fragmentSize){
						counter++;
					}
				}
				fragmentsPotential.add(counter/trafficCalls.length) ;
			}
		}
		double sum = 0;
		for (Double potential : fragmentsPotential) {
			sum += potential.doubleValue();
		}
		return sum/fragmentsPotential.size();
	}
	/**
	 * Prints all information related to the FlexGridLink object.
	 * 
	 * @return string containing all the values of the link's parameters.
	 */
	@Override
	public String toString() {
		String link = Long.toString(id) + ": " + Integer.toString(src) + "->"
				+ Integer.toString(dst) + " delay: " + Double.toString(delay)
				+ " wvls: " + Integer.toString(slots) 
				+ " weight:" + Double.toString(weight);
		return link;
	}
	
	/**
	 * Verify if the array of booleans has n contiguous true values.
	 *
	 * @param n number of contiguous slots
	 * @return return true in case it has n contiguous slots and false in case
	 * it doesnt
	 */
	public boolean contiguousSlotsAvailable(int n) {
		int j;
		for (int i = 0; i < this.freeSlots.length; i++) {
			if (this.freeSlots[i]) {
				for (j = i; j < i + n && j < this.freeSlots.length; j++) {
					if (!this.freeSlots[j]) {
						i = j;
						break;
					}
				}
				if (j == i + n)
					return true;
			}
		}
		return false;
	}

	/**
	 * Max number of contiguous edges.
	 *
	 * @return the int
	 */
	public int maxNumberOfContiguousSlots() {
		int maxValue = 0;
		int localCount = 0;
		for (int i = 0; i < this.freeSlots.length; i++) {
			if (this.freeSlots[i]) {
				localCount++;
			} else {
				if (maxValue < localCount) {
					maxValue = localCount;
				}
				localCount = 0;
			}
		}
		if (maxValue < localCount) {
			maxValue = localCount;
		}
		return maxValue;
	}
	
	/**
	 * Print spectrum.
	 */
	
	public void printSpectrum(){
		System.out.println("Espectro do link "+ this.src +"->"+this.dst);
		for (int i = 0; i < freeSlots.length; i++) {
			System.out.printf("%-3s", i);
		}
		System.out.println();
		for (int i = 0; i < freeSlots.length; i++) {
			if (freeSlots[i]){
				System.out.print("0  ");
			} else {
				System.out.print("1  ");
			}
		}
		System.out.println();
	}
	


}
