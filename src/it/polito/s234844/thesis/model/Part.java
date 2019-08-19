package it.polito.s234844.thesis.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class Part {

	String part_number;
	String description;
	List<String> customers;
	SummaryStatistics stat;
	
	public Part(String part_number, String description) {
		this.part_number = part_number;
		this.description = description;
		this.customers = new ArrayList<String>();
		this.stat = new SummaryStatistics();
	}
	
	public String getPart_number() {
		return part_number;
	}
	
	public String getDescription() {
		return description;
	}

	public void addCustomer(String customer) {
		if(!this.customers.contains(customer))
			this.customers.add(customer);
	}
	
	public void addData(double value) {
		this.stat.addValue(value);
	}
	
	public double getMean() {
		return this.stat.getMean();
	}
	
	public double getStandDev() {
		return this.stat.getStandardDeviation();
	}
	
	public double getMin() {
		return this.stat.getMin();
	}
	
	public double getMax() {
		return this.stat.getMax();
	}
	
	public double size() {
		return this.stat.getN();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((part_number == null) ? 0 : part_number.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Part other = (Part) obj;
		if (part_number == null) {
			if (other.part_number != null)
				return false;
		} else if (!part_number.equals(other.part_number))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[" + part_number + "]";
	}
	
}
