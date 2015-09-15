package main;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Measure {
	
	public String name;
	public double mean, variance, standardDeviation;
	public double averageOutcomes;
	
	public Measure(String argName, ArrayList<Double> distances, int benchmarks) {
		this.name = argName;
		
		double sum = 0;
		double len = ((double) distances.size());
		double bm = ((double) benchmarks);
		
		for (double d : distances) {
			sum += d;
		}
		
		mean = roundTwoDecimals(sum / len);
		
		sum = 0;
		
		for (double d : distances) {
			sum += Math.pow(d-mean,2);
		}
		
		variance = roundTwoDecimals(sum / len);
		
		standardDeviation = roundTwoDecimals(Math.sqrt(variance));
		
		averageOutcomes = roundTwoDecimals(len/bm);
	}
	
	double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
	
}
