package it.polito.s234844.thesis.model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Line {
	
	private int id;
	private boolean isFree;
	private List<String> partsList;
	private String currentPart;
	private LocalDate lastOccupancy;
	private LocalDate occupiedTill;
	private Duration idleness;
	
	public Line(int id, LocalDate occupiedTill) {
		this.id = id;
		this.isFree = true;
		this.partsList = new ArrayList<String>();
		this.currentPart = null;
		this.lastOccupancy = LocalDate.MIN;
		this.occupiedTill = occupiedTill;
		this.idleness = Duration.ofDays(0);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCurrentPart() {
		return currentPart;
	}

	public boolean isFree() {
		return isFree;
	}
	
	public LocalDate getOccupiedTill() {
		return occupiedTill;
	}

	public void setOccupiedTill(LocalDate currentDate, LocalDate occupiedTill) {
		this.idleness = this.idleness.plus(Duration.between(this.occupiedTill.atStartOfDay(), currentDate.atStartOfDay()));
		this.lastOccupancy = currentDate;
		this.occupiedTill = occupiedTill;
	}
	
	public Duration getIdleness() {
		return idleness;
	}
	
	public void addEvent(Order o, LocalDate date) {
		this.isFree = false;
		this.partsList.add(o.getPart_number());
		this.currentPart = o.getPart_number();
	}
	
	public void freeLine(LocalDate date) {
		if(date.isAfter(this.occupiedTill))
			this.isFree = true;
	}
	
	public Duration idleness(LocalDate date) {
		return Duration.between(this.occupiedTill.atStartOfDay(), date.atStartOfDay());
	}

	@Override
	public String toString() {
		String free = "free";
		if(!this.isFree)
			free="occupied";
		return "[L" + id + " - " + free + " - " + occupiedTill + "]";
	}

	
	
}
