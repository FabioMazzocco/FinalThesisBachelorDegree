package it.polito.s234844.thesis.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import org.apache.commons.math3.distribution.NormalDistribution;

public class Simulator {
	
	//General variables
	private LocalDate simulationStart;
	private LocalDate simulationEnd;
	private Map<String, Part> partsMap;
	private List<Order> orders;
	private List<Order> newOrder;
	private Integer maxWaitingDays;
	private PriorityQueue<Order> queue;
	private List<Line> lines;
	private Random rand;
	private NormalDistribution normal;
	private LocalDate currentDate;
	private DateTimeFormatter italian;
	
	//Statistics
	private int totalOrders;
	private int totalQuantity;
	private int actualOrders;
	private int actualQuantity;
	private int inTimeOrders;
	private int inStockOrders;
	private int lostOrders;
	private LocalDate orderStart;
	private LocalDate orderEnd;
	private Duration totalIdleness;
	
	public Simulator() {
		//General variables
		this.simulationStart = LocalDate.MIN;
		this.simulationEnd = LocalDate.MIN;
		this.partsMap = new HashMap<String, Part>();
		this.orders = new ArrayList<Order>();
		this.newOrder = new ArrayList<Order>();
		this.maxWaitingDays = null;
		this.queue = new PriorityQueue<Order>();
		this.lines = new ArrayList<Line>();
		this.rand = new Random();
		this.currentDate = null;
		italian = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	}
	
	/**
	 * Initializes the simulator by resetting all the stats and the variables, by setting the given parameters and setting up the received data
	 * @param simulationStart is the {@link LocalDate} in which the simulation starts
	 * @param simulationEnd is the {@link LocalDate} in which the simulation ends
	 * @param partsMap is the {@link HashMap} with the part number and its relative {@link Part} object
	 * @param orders is the {@link List} of orders of the past in the chosen period
	 * @param newOrder is the{@link List} of the selected (new) orders
	 * @param simultaneousParts is the number of parts that can be produced/treated simultaneously
	 */
	public void init(LocalDate simulationStart, LocalDate simulationEnd, Map<String, Part> partsMap, List<Order> orders, List<Order> newOrder, int simultaneousParts,
			Integer maxWaitingDays) {
		//General variables
		this.simulationStart = simulationStart;
		this.simulationEnd = simulationEnd;
		this.partsMap = new HashMap<String, Part>(partsMap);
		this.orders = new ArrayList<Order>(orders);
		this.newOrder = new ArrayList<Order>(newOrder);
		this.maxWaitingDays = maxWaitingDays;
		this.orders.addAll(this.newOrder);
		this.updateOrders();
		this.queue = new PriorityQueue<Order>();
		this.queue.addAll(this.orders);
		this.lines.clear();
		for(int i=0; i<simultaneousParts; i++) {
			Line l = new Line(i, this.simulationStart);
			this.lines.add(l);
		}
		
		//Statistics
		this.totalOrders = this.orders.size();
		this.totalQuantity = 0;
		for(Order o : this.orders)
			this.totalQuantity += o.getQuantity();
		this.actualOrders = 0;
		this.actualQuantity = 0;
		this.inTimeOrders = 0;
		this.inStockOrders = 0;
		this.lostOrders = 0;
		this.orderStart = LocalDate.MIN;
		this.orderEnd = LocalDate.MAX;
		this.totalIdleness = Duration.ofDays(0);
	}
	

	/**
	 * Runs the simulation from the start date to the end one and generates the stats
	 */
	public void run() {
		System.out.println("\n\n\n");
		while(!this.queue.isEmpty()) {
			Order o = this.queue.poll();
			if(this.currentDate==null || this.currentDate.isBefore(o.getOrder_date())) //Current date update
				this.currentDate = o.getOrder_date(); 
			this.freeLines(); //If the line is released before the current date --> set it as free (before finding the best line) 
			
			
			//Choose of the line (first free one or max idleness one)
			Line line = this.findLine(o);
			
			//If the days between the receiving date and the current one is higher than the max days that an order can wait --> lost order
			if(this.maxWaitingDays!=null && o.getOrder_date().plusDays((long)this.maxWaitingDays).isBefore(this.currentDate)){
				this.lostOrders++;
				continue;
			}
			
			//If the current date is after the chosen end for the simulation --> the simulation stops (some order may have not been processed)
			//It is done now because the findLine() method updates the current date (in case no line is currently free --> there's the need
			// to wait for a line to be freed)
			if(this.currentDate.isAfter(this.simulationEnd))
				break;
			
			/*Order Management: updates the #order and the total quantity of the simulation, calculates a random number of days based on the normal distribution of the part,
			 					sets the start and the end day for the part, checks if it is one of the parts of the new order*/
			//Statistics update
			this.actualOrders++;
			this.actualQuantity += o.getQuantity();
			if(o.getOrder_date().equals(this.currentDate))
				this.inTimeOrders ++;
			//Addition of the order to the line
			line.addEvent(o, this.currentDate);
			//Random #days calculation
			Integer days = this.randomDays(o.getPart_number());
			if(days == 0)
				this.inStockOrders++;
			//Setting of the production dates (start&end) to the line
			line.setOccupiedTill(this.currentDate, this.currentDate.plusDays(days));
			
			//Management of the order start date and the order end date
			if(this.newOrder.contains(o)) {
				if(this.orderStart.equals(LocalDate.MIN)) {
					this.orderStart = o.getOrder_date();
					this.orderEnd = o.getOrder_date().plusDays(days);
				}
				else
					this.orderEnd = o.getOrder_date().plusDays(days);
				this.newOrder.remove(o);
			}
			
			System.out.println("-----"+ this.currentDate.format(this.italian) +" - "+o.getPart_number()+" - line: "+line.getId()+" " + line.getOccupiedTill().format(this.italian) + "----");
		}
		
		this.currentDate = this.simulationEnd;
	}
	
	/**
	 * The {@link Line}s are released on the current date
	 */
	private void freeLines() {
		for(Line l : this.lines) {
			if(!l.isFree())
				l.freeLine(this.currentDate);
		}
	}
	
	/**
	 * Since all the orders must be in the same year, the order date needs to be updated (same month and day, different year)
	 */
	private void updateOrders() {
		for(Order o : this.orders) {
			LocalDate date = LocalDate.of(this.simulationStart.getYear(), o.getOrder_date().getMonthValue(), o.getOrder_date().getDayOfMonth());
			o.setOrder_date(date);
		}
	}

	
	/**
	 * Finds the best line to produce the order: if there are free lines, the one with the most idleness is picked, otherwise 
	 * the first one that is going to be free is picked and the current date is modified to the date when the line is released
	 * @param o is the {@link Order} that will be on the picked line
	 * @return the {@link Line} picked
	 */
	private Line findLine(Order o) {
		Line line = null;
		
		//If there are free lines --> the one with the most idleness is the one picked
		for(Line l : this.lines) {
			if(l.isFree()) {
				if(line==null)
					line = l;
				else if(l.idleness(o.getOrder_date()).compareTo(line.idleness(o.getOrder_date()))>0)
					line = l;
			}
		}
		
		//if there are no free lines --> the one that is going to be free sooner is the one picked (current date updated)
		if(line == null) {
			LocalDate min = LocalDate.MAX;
			for(Line l : this.lines) {
				if(l.getOccupiedTill().isBefore(min)) {
					min = l.getOccupiedTill();
					line = l;
				}
			}
			this.currentDate = line.getOccupiedTill(); //Update of the current date
		}
		
		return line;
	}
	
	/**
	 * Gives the random number of days based on the normal distribution of the {@link Part} (the random number between 0 an 1 represents
	 * the probability of the quantile that is the #days (when converted to the respective discrete value)
	 * @param part is the {@link Part}
	 * @return an {@link Integer} value that represent the random number of days
	 */
	private Integer randomDays(String partNumber) {
		Part part = this.partsMap.get(partNumber);
		
		//Generation of the probability that corresponds to a quantile in the normal distribution of the part
		double random = this.rand.nextDouble();
		this.normal = new NormalDistribution(part.getMean(), part.getStandDev());
		double doubleDays = this.normal.inverseCumulativeProbability(random);
		
		//if the value is below 0 --> converted to 0
		if(doubleDays <= 0.0)
			return 0;
		
		//Since the day is a continuous value it needs to be converted to a discrete one --> hypothesis, the exact value represent the middle of the 
		//working day --> when the value is >x+0,5 it becomes x+1
		if(doubleDays - (int)doubleDays >= 0.5)
			doubleDays ++;
		Integer days = (int) doubleDays;
		return days;
	}
	
	/**
	 * Generates the {@link HashMap} with the result
	 * @return the {@link HashMap}<{@link String}, {@link Object}> result (String = index, object = number/String/LocalDate)
	 */
	public HashMap<String, Object> getResult() {
		this.freeLines();
		
		for(Line l : this.lines)
			this.totalIdleness = this.totalIdleness.plus(l.getIdleness());
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("actualOrders", this.actualOrders);
		result.put("totalOrders", this.totalOrders);
		result.put("actualQuantity", this.actualQuantity);
		result.put("totalQuantity", this.totalQuantity);
		result.put("inTimeOrders", this.inTimeOrders);
		result.put("inStockOrders", this.inStockOrders);
		result.put("lostOrders", this.lostOrders);
		result.put("orderStart", this.orderStart);
		result.put("orderEnd", this.orderEnd);
		result.put("newOrder", this.newOrder);
		result.put("totalIdleness", this.totalIdleness.toDays());
		
		return result;
	}
}
