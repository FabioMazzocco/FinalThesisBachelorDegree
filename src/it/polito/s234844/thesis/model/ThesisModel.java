package it.polito.s234844.thesis.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.commons.math3.distribution.NormalDistribution;

import it.polito.s234844.thesis.db.OrdersDAO;

public class ThesisModel {
	
	//General variables
	private OrdersDAO dao;
	private List<Part> partsList;
	private Map<String, Part> partsMap;
	private List<Order> ordersList;
	private List<String> customersList;
	private String errors;
	
	//Due date variables
	private DueDateCalculator dueDate;
//	private NormalDistribution normal;
	
	//Best rate variables
	private Map<String, Integer> lastOrder;
	private Double lastProbability;
	private List<Part> bestRateList;
	private Double bestRate;
	
	//Simulation variables
	private Simulator simulator;
	private Integer MIN_YEAR;
	private Integer MAX_YEAR;
	
	public ThesisModel() {
		this.dao = new OrdersDAO();
		this.partsList = new ArrayList<Part>();
		this.partsMap = new HashMap<String, Part>();
		this.ordersList = new ArrayList<Order>();
		this.customersList = new ArrayList<String>();
		this.dueDate = new DueDateCalculator();
		this.lastOrder = new HashMap<String,Integer>();
		this.lastProbability = 0.0;
		this.bestRateList = new ArrayList<Part>();
		this.bestRate = 0.0;
		this.simulator = new Simulator();
		this.errors = "";
		this.MIN_YEAR = null;
		this.MAX_YEAR = null;
	}
	

	
	
	
	
	
	
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	/* ============================================================= DATA LOADING ============================================================= */
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	
	
	
	/**
	 * This method combines 4 methods to load data and checks if any of the 4 failed
	 * @return {@code true} if everything worked properly, {@code false} else
	 */
	public boolean loadData() {
		this.loadCustomers();
		this.loadParts();
		this.loadOrders();
		this.loadMinMaxYears();
		
		if(this.customersList.size()==0 || this.partsList.size()==0 || this.ordersList.size()==0 || this.MIN_YEAR==null || this.MAX_YEAR==null)
			return false;
		return true;
	}
	
	
	/**
	 * When needed, this method loads the orders from the DB saving them into the List
	 */
	private void loadOrders(){
		//If it has been already done --> it doesn't need to be done again
		if(ordersList.size()!=0)
			return;
		
		this.ordersList.addAll(this.dao.loadOrders());
		
		//For each order, the customer is added to the part and the time between the order date and the good issue date is added to the data
		//of the part for the stats
		for(Order order : this.ordersList) {
			String customer = order.getCustomer();
			Part part = this.partsMap.get(order.getPart_number());
			
			part.addCustomer(customer);
			
//			Duration days = Duration.between(order.getOrder_date(), order.getGood_issue_date(), ChronoUnit.DAYS);
			Period days2 = Period.between(order.getOrder_date(), order.getGood_issue_date());
			part.addData(days2.getDays());
		}
	}
	
	
	/**
	 * When needed this method loads the parts from the DB, saving them into the List and the Map
	 */
	private void loadParts() {
		//If it has been already done --> there's no need to do it again
		if(this.partsList.size()!=0)
			return;
		
		//Using the DAO, all the parts are loaded and put into both the list and the map
		this.partsList.addAll(this.dao.loadParts());
		for(Part part : this.partsList)
			this.partsMap.put(part.getPart_number(), part);
	}
	
	
	/**
	 * When needed this method loads the customers from the DB saving them into the List
	 */
	private void loadCustomers() {
		//If it has been already done --> there's no need to do it again
		if(this.customersList.size()!=0)
			return;
		
		//Using the DAO, all the customers are loaded and put into the list
		this.customersList.addAll(this.dao.loadCustomers());
	}

	public List<Part> getPartsList() {
		return partsList;
	}

	public List<String> getCustomersList() {
		return customersList;
	}
	
	
	private void loadMinMaxYears() {
		int[] minMaxYears = this.dao.loadMinMaxYear();
		
		this.MIN_YEAR = minMaxYears[0];
		this.MAX_YEAR = minMaxYears[1];
	
	}
	
	
	
	
	
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	/* =============================================================== DUE DATE =============================================================== */
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	
	
	
	/**
	 * Due Date Quoting based on the order parts list, the order date and the given probability to estimate the due date based on the mean and the variance of the 
	 * past production times
	 * @param partsNumber is the {@link List} of {@link Part}s of the order (chosen by the user)
	 * @param orderDate is the {@link LocalDate} in which the order has been received (null if no date was chosen --> today)
	 * @param probability is the {@link Double} value which represents the probability requested for the searched date to clear the given order
	 * @param isParallel is {@code true} if the parts can be processed in parallel, {@code false} else
	 * @return a {@link String} with the result and/or the errors occurred (e.g. non-esistent part number) 
	 */
	public String dueDateQuoting(List<String> partsNumber, LocalDate orderDate, Double probability, int parallelParts) {
		
		String result = "";
		this.errors = "";
		
		//Conversion from String to Part object and check on the orderDate and the partsNumber list (creation of a list of occurred errors if needed)
		//If no parts were selected --> can't continue. If the probability is an invalid value --> can't continue
		List<Part> parts = this.checkDueDateInput(partsNumber, parallelParts);
		if(this.errors.compareTo("Ops! Please choose some products to make this tool work")==0)
			return this.errors;
		else if(this.errors.compareTo("Ops! Please choose a valid number of parts that can be produced simultaneously")==0)
			return this.errors;
		//If no date was chosen or (even if isn't allowed) it's in the past --> the date considered is today
		if(orderDate == null || orderDate.isBefore(LocalDate.now()))
			orderDate = LocalDate.now();
		
		//The probability can't be outside the interval between 0 and 1 (excluded)
		if(probability<=0 || probability>=1)
			return "Ops! The chosen probability should be between 0% and 100% (excluded)";
		
		//Due date quoting through the dedicated class and result creation (with errors occurred if needed)
		Integer days = null;
		if(parallelParts == 1)
			days = dueDate.dueDateQuoting(parts, probability);
		else if(parallelParts >= parts.size())
			days = dueDate.dueDateQuotingParallel(parts, probability);
		else
			days = null; //DA FARE!!! ------------------------------------------ caso intermedio
		
		result = String.format("The selected order will be available (%.2f%% probability) on %s", probability*100, orderDate.plusDays(days));
		if(errors.compareTo("")!=0)
			errors = "\nThese errors occurred: "+errors;
		return result+errors;
	}
	
	
	/**
	 * The probability of a given date of being the upper limit of the production of the selected order (P[ X <= x])
	 * @param partsNumber is the {@link List} of {@link Part}s of the order (chosen by the user)
	 * @param orderDate is the {@link LocalDate} in which the order has been received (null if no date was chosen --> today)
	 * @param requestedDate is the {@link LocalDate} in which the order should be cleared, it is the date of which the user wants to know the probability of being
	 * the good issue date
	 * @param parallelParts is the number
	 * @return a {@link String} with the result of the analysis and/or the occurred errors
	 */
	public String dueDateProbability(List<String> partsNumber, LocalDate orderDate, LocalDate requestedDate, int parallelParts) {
		String result = "";
		this.errors = "";
		
		//Conversion from String to Part object and check on the orderDate and the partsNumber list (creation of a list of occurred errors if needed)
		//If no parts were selected --> can't continue. If the requested date is null or before the order date --> converted to today's date. If
		//the number of parts that can be produced simultaneously is an invalid number --> can't continue
		List<Part> parts = checkDueDateInput(partsNumber, parallelParts);
		if(this.errors.compareTo("Ops! Please choose some products to make this tool work")==0)
			return this.errors;
		else if(this.errors.compareTo("Ops! Please choose a valid number of parts that can be produced simultaneously")==0)
			return this.errors;
		//If no date was chosen or (even if isn't allowed) it's in the past --> the date considered is today
		if(orderDate == null || orderDate.isBefore(LocalDate.now()))
			orderDate = LocalDate.now();
		if(requestedDate == null || requestedDate.isBefore(orderDate))
			requestedDate = orderDate;
		
		Duration days = Duration.between(orderDate.atStartOfDay(), requestedDate.atStartOfDay());
		Double probability = null;
				
		if(parallelParts == 1)
			probability = dueDate.dueDateProbability(parts, (int)days.toDays());
		else if(parallelParts >= parts.size())
			probability = dueDate.dueDateProbabilityParallel(parts, (int)days.toDays());
		else
			probability = null;//DA FARE!!!!! ------------------------------------------ caso intermedio
		
		result = String.format("The chosen due date (%d days) for the selected order has %.2f%% probability", (int)days.toDays() ,probability*100);
		if(errors.compareTo("")!=0)
			errors = "\nThese errors occurred: "+errors;
		return result+errors;
	}
	
	
	/**
	 * Checks if the partsNumber string list contains existing parts, if it contains any part, if the chosen orderDate is valid (not null or in the past).
	 * It also checks if the number of parts that can be produced simultaneously is a valid number
	 * @param partsNumber is the {@link List} of {@link String} of the part-numbers of the chosen goods
	 * @param orderDate is the {@link LocalDate} in which the order is received
	 * @return the {@link List} of {@link Part}s object from the string list and creates a errors message if needed
	 */
	private List<Part> checkDueDateInput(List<String> partsNumber, int parallelParts){
		//If no goods were selected --> there's no need to continue
		if(partsNumber.size() == 0) {
			this.errors = "Ops! Please choose some products to make this tool work";
			return null;
		}
		
		//If the parts that can be produced simultaneously is less than 1 --> error, can't continue
		if(parallelParts < 1) {
			this.errors = "Ops! Please choose a valid number of parts that can be produced simultaneously";
			return null;
		}
		
		//Conversion from the part-number to the Part object (checking if the p/n exists)
		List<Part> parts = new ArrayList<Part>();
		for(String s : partsNumber) {
			Part part = this.partsMap.get(s);
			if(part==null) {
				this.errors += "\n The part number '"+s+"' does not exist!";
				continue;
			}
			else
				parts.add(part);
		}
		return parts;
	}
	
	
	
//	public NormalDistribution getNormalDistribution(HashMap<String, Integer> orderMap) {
//		//Add check on orderMap
//
//		
//	}
	
	
	
	
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	/* ========================================================= BEST RATE PARTS/TIME ========================================================= */
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	
	
	
	/**
	 * Finds the best rate (pieces/days) of the order, every line of the order needs a certain time and has a certain amount of pieces so this method
	 * allows the user to find the best compromise specifying what probability to produce the parts (that affects the #days) there must be and which
	 * percentage of the total pieces of the order must be produced and cleared. This method is the recursion manager.
	 * @param orderMap is a {@link HashMap} with the part-number and its quantity
	 * @param orderDate is a {@link LocalDate} that represents the day of the order
	 * @param probability is a {@link Double} value that indicates the probability required in the production of the parts
	 * @param percentageOfParts is the {@link Double} value that represent the percentage of the total quantity that must be issued
	 * @return a {@link String} with the result
	 */
	public String bestRate(HashMap<String, Integer> orderMap, LocalDate orderDate, Double probability, Double percentageOfParts) {
		
		this.errors="";
		
		//Check on the chosen part-numbers (p/n), the number of p/n and updates the occurred errors message if needed
		List<Part> parts = this.checkInput(orderMap.keySet());
		if(this.errors.compareTo("Ops! Please choose some products to make this tool work")==0)
			return this.errors;
		//If no date was chosen or (even if isn't allowed) it's in the past --> the date considered is today
		if(orderDate == null || orderDate.isBefore(LocalDate.now()))
			orderDate = LocalDate.now();
		//The probability can't be outside the interval between 0 and 1 (excluded) 
		if(probability<=0 || probability>=1)
			return "Ops! The chosen probability should be between 0% and 100% (excluded)";
		//Useless if there's only one part selected
		if(orderMap.size() == 1)
			return "Please select more than one product to calculate the best rate";
		
		//Calculation of the minimum number of pieces
		Integer totalQuantity = 0;
		for(String s: orderMap.keySet())
			totalQuantity += orderMap.get(s);
		if(totalQuantity <= orderMap.size())
			return "Please select at least one piece for each part";
		double quantityDouble = totalQuantity * percentageOfParts;
		if(quantityDouble - (int)quantityDouble > 0)
			quantityDouble += 1;
		Integer quantity = (int)quantityDouble;
		
		//If it's the same order & probability --> no need to do the recursion again
		if(this.lastOrder.equals(orderMap) && this.lastProbability == probability)
			return this.createResult(quantity, orderMap, probability, totalQuantity);
		
		this.bestRateList.clear();
		this.bestRate = 0.0;
		List<Part> partial = new ArrayList<Part>();
		recursive(parts, orderMap, partial, probability, quantity);
		
		//Updating the "last-variables"
		this.lastOrder = orderMap;
		this.lastProbability = probability;
		
		return this.createResult(quantity, orderMap, probability, totalQuantity)+errors;
	}
	

	/**
	 * Recursive algorithm that is called by itself until the termination conditions
	 */
	private void recursive(List<Part> parts, HashMap<String, Integer> orderMap, List<Part> partial, Double probability, Integer minQuantity) {
		//Check if the new rate is better (when current-quantity >= minQuantity)
		if(this.quantity(partial, orderMap)>=minQuantity)
			this.checkRate(partial, orderMap, probability);
		
		//TERMINATION CONDITIONS
		if(orderMap.size() == partial.size())
			return;
		
		//RECURSIVE ALGORITHM
		for(Part part : parts) {
			if(partial.contains(part))
				continue;
			partial.add(part);
			recursive(parts, orderMap, partial, probability, minQuantity);
			partial.remove(part);
		}
		
	}
	
	
	/**
	 * Checks if the current rate is better than the best rate and, in case, updates it and the respective {@link List}
	 */
	private void checkRate(List<Part> partial, HashMap<String, Integer> orderMap, double probability) {
		double rate = ((double)this.quantity(partial, orderMap)/this.daysPartial(partial, probability));
		if(rate > this.bestRate) {
			this.bestRate = rate;
			this.bestRateList = new ArrayList<Part>(partial);
		}
	}
	
	
	/**
	 * Returns the number of days that are needed for the production and clearance of the parts with a certain probability
	 */
	private int daysPartial(List<Part> partial, double probability) {
		//Pensare se fare la somma dei singoli tempi al 70% o se fare la due date
		Integer days = this.dueDate.dueDateQuoting(partial, probability);
//		Double dDays = 0.0;
//		for(Part part : partial) {
//			this.normal = new NormalDistribution(part.getMean(), part.getStandDev());
//			dDays += this.normal.inverseCumulativeProbability(probability);
//		}
//		System.out.println("Singoli: "+dDays);
		return days;
	}

	
	/**
	 * Calculates the quantity of tbe current solution to check if the #pieces is bigger or equals the minimum quantity
	 */
	private int quantity(List<Part> partial, HashMap<String, Integer> orderMap) {
		int quantity = 0;
		for(Part part : partial)
			quantity += orderMap.get(part.getPart_number());
		return quantity;
	}
	
	
	/**
	 * Creates the {@link String} with all the data of the best rate solution
	 */
	private String createResult(int quantity, HashMap<String, Integer> orderMap, Double probability, Integer totalQuantity) {
		//If there's no solution (useless, at least the solution is the full order)
		if(this.bestRateList.size()==0)
			return "There's no solution to this problem"; 
		
		Integer pieces = this.quantity(this.bestRateList, orderMap);
		String result = String.format("\nThe best combination (%.2f%% probability) is:  " + this.bestRateList + "\n"
				+ "with a rate of %.2f pcs/day, %d pieces (>=%d, min qty) in %d days\n"
				+ "%.2f%% of the pcs in order",
				probability, this.bestRate, pieces, quantity, this.daysPartial(this.bestRateList, probability) ,((double)pieces)/totalQuantity);
					
		return result;
	}
	
	
	/**
	 * Tranforms the {@link Set} into a {@link List} to use the method to check the validity of the data already used for the Due Date
	 */
	private List<Part> checkInput(Set<String> keySet) {
		List<String> partsNumber = new ArrayList<String>();
		for(String s : keySet)
			partsNumber.add(s);
		return this.checkDueDateInput(partsNumber, 1);
	}
	
	
	
	
	
	
	
	
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	/* ============================================================== SIMULATION ============================================================== */
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	
	
	
	public String simulate(LocalDate orderDate, HashMap<String, Integer> orderMap, Integer parallelParts, LocalDate start, LocalDate end, int yearsBefore) {
		String result="";
		
		this.checkInput(orderMap.keySet());
		if(this.errors.compareTo("Ops! Please choose some products to make this tool work")==0)
			return this.errors;
		//Dates check
		if(start == null || start.isBefore(LocalDate.now()))
			start = LocalDate.now();
		if(end == null || end.isBefore(start))
			return "Ops! The end date of the simulation must be equal or after the start date (today if no start date is selected)";
		if(orderDate.isAfter(end))
			return "Ops! The order date must be before the simulation ending date";
		//Useless if there're no parts selected
		if(orderMap.size() < 1)
			return "Please select at least one product to make the simulation possible";
		//Check on the number of parallel parts that can be produced
		if(parallelParts<1)
			return "Ops! Choose a valid number of contemporary producible parts";
		
		//From Map to orders
		List<Order> newOrder = new ArrayList<Order>();
		for(String s : orderMap.keySet()) {
			Order o = new Order(null, s, orderMap.get(s), null, null, orderDate);
			newOrder.add(o);
		}
		
		this.simulator = new Simulator();
		this.simulator.init(start, end, partsMap, this.getOrdersBetweenDates(start, end, yearsBefore), newOrder, parallelParts);
		this.simulator.run();
		
		result = this.simulator.getResult();
		
		return result+errors;
	}
	
	/**
	 * Creates a {@link List} of {@link Order} that have the order_date between the start and the end date (included) inserted by the user
	 * @param start is the {@link LocalDate} in which the simulation starts
	 * @param end is the {@link LocalDate} in which the simulation ends
	 * @return the {@link List} of {@link Order}s that started between the two dates
	 */
	private List<Order> getOrdersBetweenDates(LocalDate start, LocalDate end, int yearsBefore){
		List<Order> orders = new ArrayList<Order>();
		
		LocalDate[] simDates = checkSimulationDates(start, end, yearsBefore);
		start = simDates[0];
		end = simDates[1];
		
		for(Order order : this.ordersList) {
			if(order.getOrder_date().isEqual(start) || order.getOrder_date().isEqual(end))
				orders.add(order);
			else if(order.getOrder_date().isAfter(start) && order.getOrder_date().isBefore(end))
				orders.add(order);
		}
		
		return orders;
	}
	
	/**
	 * Checks if the starting and ending simulation dates are in between the MIN_YEAR and the MAX_YEAR (set depending on the existing data of the DB)
	 * @param start is the {@link LocalDate} in which the simulation starts
	 * @param end is the {@link LocalDate} in which the simulation ends
	 * @param yearsBefore is the number of years to be subtracted to the starting and ending date to get the data for the simulation
	 * @return an {@link array} of {@link LocalDate}s in which array[0] is the starting date and array[1] is the ending date (both dates are adjusted)
	 */
	private LocalDate[] checkSimulationDates(LocalDate start, LocalDate end, int yearsBefore) {
		LocalDate[] date = new LocalDate[2];
		
		start = start.minusYears(yearsBefore);
		end = end.minusYears(yearsBefore);
		
		//The data are from 2012 to 2019 (incomplete) --> data before 2012 or after 2018 would be zero or very few
		//This years can be updated with the variables MIN_YEAR and MAX_YEAR (it is done automatically with the data in the DB)
		if(start.getYear()<this.MIN_YEAR)
			start = start.withYear(this.MIN_YEAR);
		else if(start.getYear()>this.MAX_YEAR)
			start = start.withYear(this.MAX_YEAR);
		if(end.getYear()<this.MIN_YEAR)
			end = end.withYear(this.MIN_YEAR);
		else if(end.getYear()>this.MAX_YEAR)
			end = end.withYear(this.MAX_YEAR);
		
		date[0] = start;
		date[1] = end;
		return date;
	}
	
	
}
