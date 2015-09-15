package main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class Degree_Centrality {
	
	// number of problems to generate
	private static final int BENCHMARKS = 5000;
	
	private static final boolean USE_CONSTRAINTS = true;
	
	// rang of the voters and issues
	private static final int[] 
			VOTERS_RANGE = new int[] { 10, 10 },
			ISSUES_RANGE = new int[] { 5, 5 };
	
	private static int VOTERS, ISSUES;
	
	// main matrices
	private static Matrix voterToIssue;
	private static Matrix issueToVoter;
	
	ArrayList<ArrayList<Integer>> degree;
	
	double[] alpha = { 0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5};
	
	// the sets of distances of the measure to the base vector per benchmarks
	private ArrayList<Double> avr_dist;
	private ArrayList<ArrayList<Double>> degree_dist;
	
	Random generator = new Random(); 
	
	public static void main(String args[]) {
		(new Degree_Centrality()).go();
	}
	
	public void go() {
		
		init();
		
		// perform the experiment
		for (int i=0; i<BENCHMARKS; i++) {
			// reset measures but keep total distance and total outcomes
			reset();
		
			// fill matrix with random values
			voterToIssue = generateVotes(VOTERS, ISSUES);
			
			// obtain issueToVoter matrix by transposing
			issueToVoter = Matrix.transpose(voterToIssue);
			
			double maj[] = new double[ISSUES];
			
			// we will now obtain the distance vector, the majority vote and the average voter for each issue
			for (int j=0; j<ISSUES; j++) {
				
				// we use the issueToVoter matrix because a single issue then corresponds to a row
				double sum = ((double) issueToVoter.sumRow(j));
				double cols = ((double) VOTERS);
				
				// obtain distance vector as base measure
				//distVector[j] = sum/cols;
								
				// obtain majority outcome. if no outcome set -1 and choose later
				// note that the majority outcome corresponds to the 
				// distance-based rule (DBR) when there are no integrity constraints. 
				if (sum  == (cols/2)) {
					maj[j] = -1;
				} else {
					maj[j] = (sum > (cols/2)) ? 1 : 0;
				}
			}
											
			// obtain distance with base measure and sum to distance
			// since all measures have the same distance to the vector, we can simply pick one
			ArrayList<double[]> majority = generateMajorities(maj);
			ArrayList<Integer> avr = computeAVR(voterToIssue);
			
			avr_dist.addAll(getDistances(getBallots(avr), majority));
						
			// transform matrices for graph measures
			Matrix voterToIssueSim = Matrix.toSimilarity(voterToIssue);
			Matrix issueToVoterSim = Matrix.toSimilarity(issueToVoter);
			
			Matrix voterToVoter = Matrix.normalize(Matrix.multiply(voterToIssueSim, issueToVoterSim), voterToIssue.getNcols());
			Matrix voterToVoterAdj = Matrix.adjacency(voterToVoter);
			
			// computer degree
			degree = computeDegree(voterToIssue, voterToVoter, voterToVoterAdj);
			
			// count the number of outcomes and the weighted distances
			for (int j=0; j<alpha.length; j++) {
				degree_dist.get(j).addAll(getDistances(getBallots(degree.get(j)), majority));
			}
		}
		
		// analyze all measures
		ArrayList<Measure> measures = new ArrayList<Measure>();
		measures.add(new Measure("AVR", avr_dist, BENCHMARKS));
		
		for (int i=0; i<alpha.length; i++) {
			String name = "Degree (a=" + alpha[i] + ")";
			ArrayList<Double> m = degree_dist.get(i);
				
			measures.add(new Measure(name, m, BENCHMARKS));
		}
				
		printResults(measures);		
	}
	
	private void printResults(ArrayList<Measure> measures) {
		print("DONER!!");
		print("\n\n[ PARAMETERS ]\n");
		print("> Voters range: ["+VOTERS_RANGE[0]+", "+VOTERS_RANGE[1]+"]");
		print("> Issues range: ["+ISSUES_RANGE[0]+", "+ISSUES_RANGE[1]+"]");
		print("> Number of Benchmarks: " + BENCHMARKS);
		print("> Constraints: " + USE_CONSTRAINTS);
		
		print("\n[ RESULTS ]\n");
		
		String DELIM = " | ";
		
		int cols[] = new int[] { 20, 7, 10, 10, 15, 10 };
		
		print(fill("VOTING RULE", cols[0]) + DELIM + 
					fill("MEAN", cols[1]) + DELIM + 
					fill("VARIANCE", cols[2]) + DELIM + 
					fill("STAN. DEV.", cols[3]) + DELIM + 
					fill("AVG OUTCOMES", cols[4]) + DELIM);
		
		print(repeat("-", cols[0]) + DELIM + 
				repeat("-", cols[1]) + DELIM + 
				repeat("-", cols[2]) + DELIM + 
				repeat("-", cols[3]) + DELIM + 
				repeat("-", cols[4]) + DELIM);
		
		for (Measure m : measures) {			
			
			print(fill(m.name, cols[0]) + DELIM + 
					fill(""+m.mean, cols[1]) + DELIM + 
					fill(""+m.variance, cols[2]) + DELIM + 
					fill(""+m.standardDeviation, cols[3]) + DELIM +
					fill(""+m.averageOutcomes, cols[4]) + DELIM);
		}
		
	}

	private String repeat(String string, int i) {
		String ret = "";
		while (ret.length() < i) {
			ret += string;
		}
		
		return ret;
	}

	private String fill(String string, int i) {
		String ret = string;
		
		while (ret.length() < i) {
			ret += " ";
		}
		
		return ret;
	}

	private ArrayList<double[]> getBallots(ArrayList<Integer> avr) {
		ArrayList<double[]> ret = new ArrayList<double[]>();
		
		for (int voter : avr) {
			ret.add(voterToIssue.getRowAsDouble(voter));
		}
		
		return ret;
		
	}

	private void print(String string) {
		System.out.println(string);		
	}

	private void init() {		
		// the measure from social network analysis have four values for resp. alpha = 0, 0.5, 1, 1.5
		// we store this as an arraylist containing [0; 0.5; 1.0; 1.5] where each element point to a
		// list containing the identifiers of the selected voters
		degree = new ArrayList<ArrayList<Integer>>();
		
		// distances of all the measures to the base measure (distance based rule)
		avr_dist = new ArrayList<Double>();
		degree_dist = new ArrayList<ArrayList<Double>>();
		
		initList(degree_dist, alpha.length);		
	}
	
	private void initList(ArrayList<ArrayList<Double>> list, int i) {
		for (int j=0; j<i; j++) {
			list.add(new ArrayList<Double>());
		}
		
	}

	private void reset() {
		// obtain number of voters and issues
		VOTERS = getRand(VOTERS_RANGE[0],VOTERS_RANGE[1]);
		ISSUES = getRand(ISSUES_RANGE[0], ISSUES_RANGE[1]);
		
		// main matrices
		voterToIssue = new Matrix(VOTERS, ISSUES);
		issueToVoter = new Matrix(ISSUES, VOTERS);	
	}
	
	double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}

	private ArrayList<double[]> generateMajorities(double[] maj) {
		ArrayList<double[]> ret = new ArrayList<double[]>();
		boolean valid = true;
						
		for (int i=0; i<maj.length; i++) {
			if (maj[i] == -1) {
				valid = false;
				
				double[] copy = (double[]) maj.clone();
				
				maj[i] = 0;
				ret.addAll(generateMajorities(maj));
				copy[i] = 1;
				ret.addAll(generateMajorities(copy));
			}
		}
		
		if (valid) ret.add(maj);
		
		return ret;		
	}

	private ArrayList<ArrayList<Integer>> computeDegree(Matrix voterToIssue, Matrix voterToVoter, Matrix voterToVoterAdj) 
	{
		
		ArrayList<ArrayList<Integer>> ret = new ArrayList<ArrayList<Integer>>();
		
		for (int n=0; n<this.alpha.length; n++) {
			double alpha = this.alpha[n];
			double degree[] = new double[VOTERS];
			
			// first computer degree for each voter
			for (int i=0; i<voterToVoter.getNrows(); i++) {
				int k = voterToVoterAdj.sumRow(i);
				int s = voterToVoter.sumRow(i);
				
				degree[i] = Math.pow(k, 1.0-alpha) * Math.pow(s, alpha);
			}
			
			// find the maximal degree and store if it occurs multiple times
			double maxDist = Double.MIN_VALUE;
			ArrayList<Integer> degreeVoters = new ArrayList<Integer>();
			
			for (int i=0; i<degree.length; i++) {
				if (degree[i] > maxDist) {
					maxDist = degree[i];
					degreeVoters.clear();
					degreeVoters.add(i);
				} else if (degree[i] == maxDist) {
					tryAdd(i, degreeVoters, voterToIssue);
				}
			}
			
			ret.add(degreeVoters);
		}
		
		
		return ret;		
		
	}

	private ArrayList<Integer> computeAVR(Matrix m) {
		// we assume that each row is a ballot
		// first compute Hamming distance to profile for each voter
		
		int dists[] = new int[VOTERS];
		
		for (int i=0; i<m.getNrows(); i++) {
			dists[i] = computeHammingToProfile(m, i);
		}
		
		// find the minimal distance and store if it occurs multiple times
		int minDist = Integer.MAX_VALUE;
		ArrayList<Integer> avrVoters = new ArrayList<Integer>();
		
		for (int i=0; i<dists.length; i++) {
			if (dists[i] < minDist) {
				minDist = dists[i];
				avrVoters.clear();
				avrVoters.add(i);
			} else if (dists[i] == minDist) {
				tryAdd(i, avrVoters, m); // dont add duplicates
			}
		}
		
		return avrVoters;
	}

	private void tryAdd(int i, ArrayList<Integer> avrVoters, Matrix m) {
		Iterator<Integer> iter = avrVoters.iterator();
		
		boolean exists = false;
		
		while (iter.hasNext()) {
			int j = iter.next();
			if (Arrays.equals(m.getRow(i),m.getRow(j))) {
				exists = true;
			}
		}
		
		if (!exists) avrVoters.add(i);
		
	}

	private int computeHammingToProfile(Matrix m, int i) {
		return computeHammingToProfile(m, m.getRow(i));
	}
		
	private int computeHammingToProfile(Matrix m, int[] ballot) {
		int ret = 0;
		
		for (int i=0; i<m.getNrows(); i++) {
			ret += computeHammingDistance(m.getRow(i), ballot);
		}
		
		return ret;
	}

	private int computeHammingDistance(int[] row1, int[] row2) {
		int ret = 0;
		
		for (int i=0; i<row1.length;i++) {
			ret += Math.abs(row2[i]-row1[i]);
		}
		
		return ret;		
	}

	private ArrayList<Double> getDistances(ArrayList<double[]> measures, ArrayList<double[]> base) {
		ArrayList<Double> ret = new ArrayList<Double>();
		
		// calculate the distance of each measure to its closest base
		for (double[] measure : measures) {
			
			double minDist = Double.MAX_VALUE;
			
			for (double[] b : base) {
				double dist =0;
			
				for (int i=0; i<measure.length; i++) {
					dist += Math.abs(b[i]-measure[i]);
				}
				if (dist < minDist) {
					minDist = dist;
				}
			}
			ret.add(minDist);
		}
		
		return ret;		
	}

	public int getRand(int min, int max) {
		return min + generator.nextInt(max-min+1);
	}
	
	public static void printOutcome(double[] measure) {
		for (int i=0;i<measure.length;i++) System.out.print(measure[i] + " ");
		System.out.println("");
	}
	
	
	public Matrix generateVotes(int row, int col) {
		
		int[][] m = new int[row][col];
		
		for (int i=0; i<VOTERS; i++) {
			boolean onlyOnes = true;
			for (int j=0; j<ISSUES; j++) {
				if (j == ISSUES-1 && USE_CONSTRAINTS) {
					m[i][j] = (onlyOnes? 1 : 0);
				} else {
					int num = getRand(0, 1);
					if (num == 0) onlyOnes = false;
					m[i][j] = num;
				}				
			}
		}
		
		return new Matrix(m);
	}
}
