package it.polito.s234844.thesis.model;

import java.util.List;
import org.apache.commons.math3.distribution.NormalDistribution;

public class DueDateCalculator {
	
	NormalDistribution normal;
	
	public DueDateCalculator() {
		this.normal = new NormalDistribution();
	}
	
	/**
	 * Due Date Quoting based on the parts list and the given probability for the requested date to be the good-issue date of the whole order
	 * --- Hypothesis: all the parts are produced serially 
	 * @param parts is the {@link List} of {@link Part} of the {@link Order}
	 * @param probability is the given probability to have the whole order cleared by the searched date
	 * @return the {@link Integer} number of days from the order date to have the whole order cleared with the given probability
	 */
	public Integer dueDateQuoting(List<Part> parts, Double probability) {
		//Case: no parts selected (shouldn't be possible)
		if(parts.size()==0)
			return -1;
		//Case: invalid probability (zero and 100% included
		else if (probability<=0 || probability>=1)
			return -2;
		
		//Normal Distribution creation & #days to issue the goods (double value)
		this.normal = new NormalDistribution(this.composedMean(parts), this.composedStdDev(parts));
		Double days = this.normal.inverseCumulativeProbability(probability);
		
//		System.out.println("Due date: "+days);
		//Returning the #days (integer value)
		return this.continuousToDiscrete(days);
	}
	
	/**
	 * Due Date Quoting based on the parts list and the given probability for the requested date to be the good-issue date of the whole order
	 * --- Hypothesis: all the parts are produced in parallel
	 * @param parts is the {@link List} of {@link Part} of the {@link Order}
	 * @param probability is the given probability to have the whole order cleared by the searched date
	 * @return the {@link Integer} number of days from the order date to have the whole order cleared with the given probability. 
	 * -1 if there are no parts selected. -2 if the probability is an invalid number.
	 */
	public Integer dueDateQuotingParallel(List<Part> parts, Double probability) {
		//Case: no parts selected (shouldn't be possible)
		if(parts.size()==0)
			return -1;
		//Case: invalid probability (zero and 100% included
		else if (probability<=0 || probability>=1)
			return -2;
			
		//Normal Distribution creation & #days to issue the goods (double value)
		Double maxDays = 0.0;
		for(Part part : parts) {
			this.normal = new NormalDistribution(part.getMean(), part.getStandDev());
			Double days = this.normal.inverseCumulativeProbability(probability);
			if(days > maxDays)
				maxDays = days;
		}
		
		//Returning the #days (integer value)
		return this.continuousToDiscrete(maxDays);
	}
	
	
	/**
	 * Determines the probability that a given number of days will be enough to clear all the parts
	 * --- Hypothesis: all the parts are produced serially 
	 * @param parts is the {@link List} of {@link Part} of the {@link Order}
	 * @param days is the given #days to the good-issue-date, the probability is determined from this information (based on the parts)
	 * @return the {@link Double} value that represents the probability that the given parts can be produced in the given #days
	 */
	public Double dueDateProbability(List<Part> parts, Integer days) {
		//Case: no parts selected (shouldn't be possible)
		if(parts.size()==0)
			return -1.0;
		
		//Normal Distribution creation & calculation of the cumulative probability for the given days (converted to its continuous value)
		this.normal = new NormalDistribution(this.composedMean(parts), this.composedStdDev(parts));
		Double probability = this.normal.cumulativeProbability(this.discreteToContinuous(days));
		
		return probability;
	}
	
	/**
	 * Determines the probability that a given number of days will be enough to clear all the parts
	 * --- Hypothesis: all the parts are produced in parallel
	 * @param parts is the {@link List} of {@link Part} of the {@link Order}
	 * @param days is the given #days to the good-issue-date, the probability is determined from this information (based on the parts)
	 * @return the {@link Double} value that represents the probability that the given parts can be produced in the given #days
	 */
	public Double dueDateProbabilityParallel(List<Part> parts, Integer days) {
		//Case: no parts selected (impossible)
		if(parts.size()==0)
			return -1.0;
		
		//Normal Distribution creation & calculation of the cumulative probability for the given days (converted to its continuous value)
		Double totalProbability = 0.0;
		for(Part part : parts) {
			this.normal = new NormalDistribution(part.getMean(), part.getStandDev());
			Double probability = this.normal.cumulativeProbability(this.discreteToContinuous(days));
			if(totalProbability == 0.0) {
				totalProbability = probability;
				continue;
			}
			totalProbability *= probability;
		}
		
		return totalProbability;
	}
	
	public NormalDistribution getNormal(List<Part> parts) {
		return new NormalDistribution(this.composedMean(parts), this.composedStdDev(parts));
	}
	
	
	private Double composedMean(List<Part> parts) {
		Double mean = 0.0;
		
		for(Part part : parts)
			mean += part.getMean();
		
		return mean;
	}
	
	/**
	 * The hypotesis is that the random variables are uncorrelated one to each other, so the standard deviation of the sum of the random variables is calculated as the 
	 * squared root of the sum of the variances
	 * @param parts is the {@link List} of the parts of the order
	 * @return
	 */
	private Double composedStdDev(List<Part> parts) {
		Double stdDev = 0.0;
		
		for(Part part : parts)
			stdDev += Math.pow(part.getStandDev(),2); //Hypotesis: uncorrelated variables --> var[A+B+C+...] = var[A]+var[B]+var[C]+....
		
		return Math.sqrt(stdDev);
	}
	
	/**
	 * Since the normal is a continuous distribution, to provide an number of days it is needed to convert the result number into a discrete one. This method
	 * converts the {@link Double} result to the respective {@link Integer}. Since it is about days, the exact number represents the middle of the day, so the continuous one
	 * needs to be converted to the next {@link Integer} if its decimal part is over or equal to 0.5. If the continuous value is before 0 --> there's already stock and
	 * the number of days will be zero
	 * @param cont is the {@link Double} value to be converted into number of days
	 * @return the {@link Integer} number of days
	 */
	private Integer continuousToDiscrete(double cont) {
		if(cont<0.5)
			return 0;

		Double decimal = cont - (int)cont;
		
		if(decimal<0.5)
			return (int)cont;
		else
			return ((int)cont + 1);
	}
	
	/**
	 * To find the probability from the number of days, the discrete number of days must be coverted to the respective continuous one --> it's the opposite of the 
	 * coninuousToDiscrete(...) method, since the discrete number represents the #days till the middle of the last one, less than 0.5 needs to be added 
	 * @param discr is the {@link Integer} value to be converted into a continuous one
	 * @return the {@link Double} continuous value of days
	 */
	private Double discreteToContinuous(Integer discr) {
		return (discr + 0.49999999999999);
	}
	
}
