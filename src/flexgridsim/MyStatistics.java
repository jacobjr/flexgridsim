package flexgridsim;

/**
 * The Class MyStatistics.
 */
public class MyStatistics {
	private static MyStatistics singletonObject;
	private OutputManager plotter;
	private PhysicalTopology pt;
	private TrafficGenerator traffic;
    private int minNumberArrivals;
    private int numberArrivals;
    private int arrivals;
    private int departures;
    private int accepted;
    private int blocked;
    private int requiredBandwidth;
    private int blockedBandwidth;
    private int numNodes;
    private int[][] arrivalsPairs;
    private int[][] blockedPairs;
    private int[][] requiredBandwidthPairs;
    private int[][] blockedBandwidthPairs;
    private double load;

    // Diff
    private int numClasses;
    private int[] arrivalsDiff;
    private int[] blockedDiff;
    private int[] requiredBandwidthDiff;
    private int[] blockedBandwidthDiff;
    private int[][][] arrivalsPairsDiff;
    private int[][][] blockedPairsDiff;
    private int[][][] requiredBandwidthPairsDiff;
    private int[][][] blockedBandwidthPairsDiff;
    private int[][] numberOfUsedTransponders;;
    
    /**
     * A private constructor that prevents any other class from instantiating.
     */
    private MyStatistics() {
    	
        numberArrivals = 0;

        arrivals = 0;
        departures = 0;
        accepted = 0;
        blocked = 0;

        requiredBandwidth = 0;
        blockedBandwidth = 0;
    }
    
    /**
     * Creates a new MyStatistics object, in case it does'n exist yet.
     * 
     * @return the MyStatistics singletonObject
     */
    public static synchronized MyStatistics getMyStatisticsObject() {
        if (singletonObject == null) {
            singletonObject = new MyStatistics();
        }
        return singletonObject;
    }
    
    /**
     * Throws an exception to stop a cloned MyStatistics object from
     * being created.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    /**
     * Attributes initializer.
     *
     * @param plotter the graph plotter
     * @param pt the pt
     * @param traffic 
     * @param numNodes number of nodes in the network
     * @param numClasses number of classes of service
     * @param minNumberArrivals minimum number of arriving events
     * @param load the load of the network
     */
    public void statisticsSetup(OutputManager plotter, PhysicalTopology pt, TrafficGenerator traffic, int numNodes, int numClasses, int minNumberArrivals, double load) {
    	this.plotter = plotter;
    	this.pt = pt;
    	this.traffic = traffic;
        this.numNodes = numNodes;
        this.load = load;
        this.arrivalsPairs = new int[numNodes][numNodes];
        this.blockedPairs = new int[numNodes][numNodes];
        this.requiredBandwidthPairs = new int[numNodes][numNodes];
        this.blockedBandwidthPairs = new int[numNodes][numNodes];

        this.minNumberArrivals = minNumberArrivals;
        numberOfUsedTransponders = new int[numNodes][numNodes];
        //Diff
        this.numClasses = numClasses;
        this.arrivalsDiff = new int[numClasses];
        this.blockedDiff = new int[numClasses];
        this.requiredBandwidthDiff = new int[numClasses];
        this.blockedBandwidthDiff = new int[numClasses];
        for (int i = 0; i < numClasses; i++) {
            this.arrivalsDiff[i] = 0;
            this.blockedDiff[i] = 0;
            this.requiredBandwidthDiff[i] = 0;
            this.blockedBandwidthDiff[i] = 0;
        }
        this.arrivalsPairsDiff = new int[numNodes][numNodes][numClasses];
        this.blockedPairsDiff = new int[numNodes][numNodes][numClasses];
        this.requiredBandwidthPairsDiff = new int[numNodes][numNodes][numClasses];
        this.blockedBandwidthPairsDiff = new int[numNodes][numNodes][numClasses];
    }
	/**
	 * Calculate last statistics for the graph generation.
	 */
	public void calculateLastStatistics(){
		//bandwidth block graph
		plotter.addDotToGraph("mbbr", load, ((float) blockedBandwidth) / ((float) requiredBandwidth));
		
		plotter.addDotToGraph("bp", load, ((float) blocked) / ((float) arrivals) * 100);
		int count = 0;
        float bbr, jfi, sum1 = 0, sum2 = 0;
        if (blocked == 0) {
            bbr = 0;
        } else {
            bbr = ((float) blockedBandwidth) / ((float) requiredBandwidth) * 100;
        }
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (i != j) {
                    if (blockedPairs[i][j] == 0) {
                        bbr = 0;
                    } else {
                        bbr = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    count++;
                    sum1 += bbr;
                    sum2 += bbr * bbr;
                }
            }
        }
        jfi = (sum1 * sum1) / ((float) count * sum2);
        plotter.addDotToGraph("jfi", load, jfi);
        //all pairs fairness
//        float blockProb = 0;
//        int k=1;
//        for (int i = 0; i < numNodes; i++) {
//            for (int j = i + 1; j < numNodes; j++) {
//                if (blockedPairs[i][j] == 0) {
//                    blockProb = 0;
//                    bbr = 0;
//                } else {
//                    blockProb = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
//                    bbr = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
//                }
//                System.out.println(k+"\t"+blockProb);
//                k++;
//            }
//        }
	}
	
	/**
	 * Calculate periodical statistics.
	 */
	public void calculatePeriodicalStatistics(){
		//fragmentation graph
		double fragmentationMean = 0;
    	for (int i = 0; i < pt.getNumLinks(); i++) {
    		try {
    			fragmentationMean += pt.getLink(i).getFragmentationRatio(traffic.getCallsTypeInfo(), pt.getSlotCapacity());
    		} catch (NullPointerException e) {
    			
    		}
		}
    	fragmentationMean = fragmentationMean / pt.getNumLinks();
    	plotter.addDotToGraph("fragmentation", load, fragmentationMean);
    	double meanTransponders = 0;
    	for (int i = 0; i < numberOfUsedTransponders.length; i++) {
			for (int j = 0; j < numberOfUsedTransponders[i].length; j++) {
				if (numberOfUsedTransponders[i][j]>0){
					meanTransponders += numberOfUsedTransponders[i][j];
				}
			}
		}
//    	meanTransponders = meanTransponders / size;
    	if (Double.compare(meanTransponders, Double.NaN)!=0){
    		plotter.addDotToGraph("transponders", load, meanTransponders);
    	}
    	//BFR
    	double BFR = 0;
    	for (int i = 0; i < pt.getNumLinks(); i++) {
			FlexGridLink link = pt.getLink(i);
			double sumbe = link.getNumFreeSlots();
			int B = link.getSlots();
			double psi =0;
			
			if (sumbe < B) {
				psi = (double)1.0 - (link.maxNumberOfContiguousSlots()/sumbe);
			} else {
				psi = 0;
			}
			BFR += psi;
		}
    	BFR = BFR/pt.getNumLinks();
    	plotter.addDotToGraph("bfr", load, BFR);
    	
	}
	
    /**
     * Adds an accepted flow to the statistics.
     * 
     * @param flow the accepted Flow object
     * @param lightpaths list of lightpaths in the flow
     */
    public void acceptFlow(Flow flow, LightPath[] lightpaths) {
        if (this.numberArrivals > this.minNumberArrivals){
        	if (flow.isBatch()){
        		this.accepted += flow.getNumberOfFlowsGroomed();
//        		this.accepted += flow.getNumberOfFLowsAccepted();
//        		this.blocked += flow.getNumberOfFlowsGroomed() - flow.getNumberOfFLowsAccepted();
        	} else {
	            this.accepted++;
        	}
        	int links =  flow.getLinks().length+1;
            plotter.addDotToGraph("hops", load, links);
            numberOfUsedTransponders[flow.getSource()][flow.getDestination()]++;
        }
    }
    
    /**
     * Adds a blocked flow to the statistics.
     * 
     * @param flow the blocked Flow object
     */
    public void blockFlow(Flow flow) {
        if (this.numberArrivals > this.minNumberArrivals) {
        	if (flow.isBatch()){
//        		this.blocked += flow.getNumberOfFlowsGroomed();
        		this.blocked += flow.getNumberOfFlowsBlocked();
        		this.accepted += flow.getNumberOfFLowsAccepted();
        	} else {
	            this.blocked++;
        	}
            int cos = flow.getCOS();
            this.blocked++;
            this.blockedDiff[cos]++;
            this.blockedBandwidth += flow.getRate();
            this.blockedBandwidthDiff[cos] += flow.getRate();
            this.blockedPairs[flow.getSource()][flow.getDestination()]++;
            this.blockedPairsDiff[flow.getSource()][flow.getDestination()][cos]++;
            this.blockedBandwidthPairs[flow.getSource()][flow.getDestination()] += flow.getRate();
            this.blockedBandwidthPairsDiff[flow.getSource()][flow.getDestination()][cos] += flow.getRate();
        }
    }
    
    /**
     * Adds an event to the statistics.
     * 
     * @param event the Event object to be added
     */
    public void addEvent(Event event) {
        try {
            if (event instanceof FlowArrivalEvent) {
                this.numberArrivals++;
                if (this.numberArrivals > this.minNumberArrivals) {
                    int cos = ((FlowArrivalEvent) event).getFlow().getCOS();
                    this.arrivals++;
                    this.arrivalsDiff[cos]++;
                    this.requiredBandwidth += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.requiredBandwidthDiff[cos] += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.arrivalsPairs[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()]++;
                    this.arrivalsPairsDiff[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()][cos]++;
                    this.requiredBandwidthPairs[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()] += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.requiredBandwidthPairsDiff[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()][cos] += ((FlowArrivalEvent) event).getFlow().getRate();
                }
                if (Simulator.verbose && Math.IEEEremainder((double) arrivals, (double) 10000) == 0) {
                    System.out.println(Integer.toString(arrivals));
                }
            }
            else if (event instanceof FlowDepartureEvent) {
                if (this.numberArrivals > this.minNumberArrivals) {
                    this.departures++;
                }
                Flow f = ((FlowDepartureEvent)event).getFlow();
                if (f.isAccepeted()){
                	this.numberOfUsedTransponders[f.getSource()][f.getDestination()]--;
                }
            }
            if (this.numberArrivals % 100 == 0){
            	calculatePeriodicalStatistics();
            	
            }
            if (this.numberArrivals % 5000 == 0){
            	
//            	System.out.println(event.getTime()+","+BFR);
            }
        }
        
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * This function is called during the simulation execution, but only if
     * verbose was activated.
     * 
     * @return string with the obtained statistics
     */
    public String fancyStatistics() {
        float acceptProb, blockProb, bbr;
        if (accepted == 0) {
            acceptProb = 0;
        } else {
            acceptProb = ((float) accepted) / ((float) arrivals) * 100;
        }
        if (blocked == 0) {
            blockProb = 0;
            bbr = 0;
        } else {
            blockProb = ((float) blocked) / ((float) arrivals) * 100;
            bbr = ((float) blockedBandwidth) / ((float) requiredBandwidth) * 100;
        }

        String stats = "Arrivals \t: " + Integer.toString(arrivals) + "\n";
        stats += "Required BW \t: " + Integer.toString(requiredBandwidth) + "\n";
        stats += "Departures \t: " + Integer.toString(departures) + "\n";
        stats += "Accepted \t: " + Integer.toString(accepted) + "\t(" + Float.toString(acceptProb) + "%)\n";
        stats += "Blocked \t: " + Integer.toString(blocked) + "\t(" + Float.toString(blockProb) + "%)\n";
        stats += "BBR     \t: " + Float.toString(bbr) + "%\n";
        stats += "\n";
        stats += "Blocking probability per s-d pair:\n";
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                stats += "Pair (" + Integer.toString(i) + "->" + Integer.toString(j) + ") ";
                stats += "Calls (" + Integer.toString(arrivalsPairs[i][j]) + ")";
                if (blockedPairs[i][j] == 0) {
                    blockProb = 0;
                    bbr = 0;
                } else {
                    blockProb = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                    bbr = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                }
                stats += "\tBP (" + Float.toString(blockProb) + "%)";
                stats += "\tBBR (" + Float.toString(bbr) + "%)\n";
            }
        }

        return stats;
    }
    
    /**
     * Prints all the obtained statistics, but only if verbose was not activated.
     *
     */
    public void printStatistics() {
        int count = 0;
        float bp, bbr, jfi, sum1 = 0, sum2 = 0;
        float bpDiff[], bbrDiff[];

        if (blocked == 0) {
            bp = 0;
            bbr = 0;
        } else {
            bp = ((float) blocked) / ((float) arrivals) * 100;
            bbr = ((float) blockedBandwidth) / ((float) requiredBandwidth) * 100;
        }
        bpDiff = new float[numClasses];
        bbrDiff = new float[numClasses];
        for (int i = 0; i < numClasses; i++) {
            if (blockedDiff[i] == 0) {
                bpDiff[i] = 0;
                bbrDiff[i] = 0;
            } else {
                bpDiff[i] = ((float) blockedDiff[i]) / ((float) arrivalsDiff[i]) * 100;
                bbrDiff[i] = ((float) blockedBandwidthDiff[i]) / ((float) requiredBandwidthDiff[i]) * 100;
            }
        }
        System.out.println("MBP " + Float.toString(bp));
        for (int i = 0; i < numClasses; i++) {
            System.out.println("MBP-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]));
        }
        System.out.println("MBBR " + Float.toString(bbr));
        for (int i = 0; i < numClasses; i++) {
            System.out.println("MBBR-" + Integer.toString(i) + " " + Float.toString(bbrDiff[i]));
        }

        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (i != j) {
                    System.out.print(Integer.toString(i) + "-" + Integer.toString(j) + " ");
                    System.out.print("A " + Integer.toString(arrivalsPairs[i][j]) + " ");
                    if (blockedPairs[i][j] == 0) {
                        bp = 0;
                        bbr = 0;
                    } else {
                        bp = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                        bbr = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    count++;
                    sum1 += bbr;
                    sum2 += bbr * bbr;
                    System.out.print("BP " + Float.toString(bp) + " ");
                    System.out.println("BBR " + Float.toString(bbr));
                }
            }
        }
        jfi = (sum1 * sum1) / ((float) count * sum2);
        System.out.println("JFI " + Float.toString(jfi));
        //Diff
        for (int c = 0; c < numClasses; c++) {
            count = 0;
            sum1 = 0;
            sum2 = 0;
            for (int i = 0; i < numNodes; i++) {
                for (int j = i + 1; j < numNodes; j++) {
                    if (i != j) {
                        if (blockedPairsDiff[i][j][c] == 0) {
                            bp = 0;
                            bbr = 0;
                        } else {
                            bp = ((float) blockedPairsDiff[i][j][c]) / ((float) arrivalsPairsDiff[i][j][c]) * 100;
                            bbr = ((float) blockedBandwidthPairsDiff[i][j][c]) / ((float) requiredBandwidthPairsDiff[i][j][c]) * 100;
                        }
                        count++;
                        sum1 += bbr;
                        sum2 += bbr * bbr;
                    }
                }
            }
            jfi = (sum1 * sum1) / ((float) count * sum2);
            System.out.println("JFI-" + Integer.toString(c) + " " + Float.toString(jfi));
        }
    }
	
    
    /**
     * Terminates the singleton object.
     */
    public void finish()
    {
        singletonObject = null;
    }
}
