package it.polito.s234844.thesis.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	/**
	 * Load the most and least recent year present in the database
	 */
	private void loadMinMaxYears() {
		int[] minMaxYears = this.dao.loadMinMaxYear();
		
		this.MIN_YEAR = minMaxYears[0];
		this.MAX_YEAR = minMaxYears[1];
	
	}
	
	public Integer getMIN_YEAR() {
		return MIN_YEAR;
	}

	public Integer getMAX_YEAR() {
		return MAX_YEAR;
	}
	
	
	
	
	
	
	
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	/* =============================================================== DUE DATE =============================================================== */
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	


	/**
	 * Due Date Quoting based on the order map (part-number, quantity), the order date and the given probability to estimate the due date based on the mean and the variance of the 
	 * past production times
	 * @param orderMap is the {@link HashMap} of the order (chosen by the user)
	 * @param orderDate is the {@link LocalDate} in which the order has been received (null if no date was chosen --> today)
	 * @param probability is the {@link Double} value which represents the probability requested for the searched date to clear the given order
	 * @param isParallel is {@code true} if the parts can be processed in parallel, {@code false} else
	 * @return the {@link HashMap}<{@link String}, {@link Object}> in which every object is identified by its name in the keySet, the objects are strings, lists, Integers
	 */
	public HashMap<String, Object> dueDateQuoting(HashMap<String, Integer> orderMap, LocalDate orderDate, Double probability, boolean isParallel) {
		
		this.errors = "";
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		//Conversion from String to Part object and check on the orderDate and the partsNumber list (creation of a list of occurred errors if needed)
		//If no parts were selected --> can't continue. If the probability is an invalid value --> can't continue
		List<Part> parts = this.checkInput(orderMap, isParallel);
		if(this.errors.compareTo("Ops! Please choose some products to make this tool work")==0 || 
				this.errors.compareTo("Ops! Please choose a valid number of parts that can be produced simultaneously")==0) {
			result.put("days",null);
			result.put("errors", this.errors);
			return result;
		}
		//If no date was chosen or (even if isn't allowed) it's in the past --> the date considered is today
		if(orderDate == null || orderDate.isBefore(LocalDate.now()))
			orderDate = LocalDate.now();
		
		//The probability can't be outside the interval between 0 and 1 (excluded)
		if(probability<=0 || probability>=1) {
			this.errors += "Ops! The chosen probability should be between 0% and 100% (excluded)";
			result.put("days", null);
			result.put("errors", this.errors);
			return result;
		}
		
		//Due date quoting through the dedicated class and result creation (with errors occurred if needed)
		Integer days = null;
		if(isParallel == false)
			days = dueDate.dueDateQuoting(parts, probability);
		else
			days = dueDate.dueDateQuotingParallel(parts, probability);
		
		
		//Error management to be added TBD
		
		result.put("days", days);
		result.put("errors",this.errors);
		result.put("normal", this.dueDate.getNormal(parts));
		return result;
	}
	
	
	/**
	 * The probability of a given date of being the upper limit of the production of the selected order (P[X <= x])
	 * @param orderMap is the {@link HashMap} with the part-number({@link String}) and the quantity ({@link Integer}) of the order (chosen by the user)
	 * @param orderDate is the {@link LocalDate} in which the order has been received (null if no date was chosen --> today)
	 * @param requestedDate is the {@link LocalDate} in which the order should be cleared, it is the date of which the user wants to know the probability of being
	 * @param isParallel is the {@link Boolean} value that indicates if the production of the parts is simultaneous
	 * the good issue date
	 * @return a {@link HashMap}<{@link String}, {@link Object}> with the result of the analysis and/or the occurred errors 
	 */
	public HashMap<String, Object> dueDateProbability(HashMap<String, Integer> orderMap, LocalDate orderDate, LocalDate requestedDate, boolean isParallel) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		this.errors = "";
		
		//Conversion from String to Part object and check on the orderDate and the partsNumber list (creation of a list of occurred errors if needed)
		//If no parts were selected --> can't continue. If the requested date is null or before the order date --> converted to today's date. If
		//the number of parts that can be produced simultaneously is an invalid number --> can't continue
		List<Part> parts = checkInput(orderMap, isParallel);
		if(this.errors.compareTo("Ops! Please choose some products to make this tool work")==0) {
			result.put("probability", null);
			result.put("error", this.errors);
			return result;
		}
		else if(this.errors.compareTo("Ops! Please choose a valid number of parts that can be produced simultaneously")==0) {
			result.put("probability", null);
			result.put("error", this.errors);
			return result;
		}
		//If no date was chosen or (even if isn't allowed) it's in the past --> the date considered is today
		if(orderDate == null || orderDate.isBefore(LocalDate.now()))
			orderDate = LocalDate.now();
		if(requestedDate == null || requestedDate.isBefore(orderDate))
			requestedDate = orderDate;
		
		Duration days = Duration.between(orderDate.atStartOfDay(), requestedDate.atStartOfDay());
		Double probability = null;
				
		if(isParallel == false)
			probability = dueDate.dueDateProbability(parts, (int)days.toDays());
		else
			probability = dueDate.dueDateProbabilityParallel(parts, (int)days.toDays());
		
		result.put("probability", probability);
		result.put("errors", this.errors);
		return result;
	}
	
	
	/**
	 * Checks if the partsNumber string list contains existing parts, if it contains any part, if the chosen orderDate is valid (not null or in the past).
	 * It also checks if the number of parts that can be produced simultaneously is a valid number
	 * @param orderMap is the {@link HashMap} with the part-number({@link String}) and the quantity ({@link Integer}) of the order (chosen by the user)
	 * @param isParallel is the {@link Boolean} value that indicates if the production of the parts is simultaneous
	 * @return the {@link List} of {@link Part}s object from the string list and creates a errors message if needed
	 */
	private List<Part> checkInput(HashMap<String, Integer> orderMap, boolean isParallel){
		//If no goods were selected --> there's no need to continue (impossible)
		if(orderMap.size() == 0) {
			this.errors = "Ops! Please choose some products to make this tool work";
			return null;
		}
		
		//Conversion from the part-number to the Part object (checking if the p/n exists)
		List<Part> parts = new ArrayList<Part>();
		for(String s : orderMap.keySet()) {
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
	 * @param probability is a {@link Double} value that indicates the probability required in the production of the parts
	 * @param percentageOfParts is the {@link Double} value that represent the percentage of the total quantity that must be issued
	 * @return a {@link HashMap} with the result (best rate, list of parts, produced quantity&parts)
	 */
	public HashMap<String, Object> bestRate(HashMap<String, Integer> orderMap, Double probability, Double percentageOfParts) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		this.errors="";
		
		//Check on the chosen part-numbers (p/n), the number of p/n and updates the occurred errors message if needed
		List<Part> parts = this.checkInput(orderMap, false);
		if(this.errors.compareTo("Ops! Please choose some products to make this tool work")==0) {
			result.put("list", null);
			result.put("errors", this.errors);
			return result;
		}
		
		//The probability can't be outside the interval between 0 and 1 (excluded) 
		if(probability<=0.0 || probability>=1.0) {
			result.put("list", null);
			result.put("errors", "Ops! The chosen probability should be between 0% and 100% (excluded)");
			return result;
		}
		//Useless if there's only one part selected
		if(orderMap.size() == 1) {
			result.put("list", null);
			result.put("errors", "Please select more than one product to calculate the best rate");
			return result;
		}
		
		//Calculation of the minimum number of pieces
		Integer totalQuantity = 0;
		for(String s: orderMap.keySet())
			totalQuantity += orderMap.get(s);
		if(totalQuantity <= orderMap.size()) {
			result.put("list", null);
			result.put("errors", "Please select at least one piece for each part");
			return result;
		}
		double quantityDouble = totalQuantity * percentageOfParts;
		if(quantityDouble - (int)quantityDouble > 0)
			quantityDouble += 1;
		Integer quantity = (int)quantityDouble;
		
		//If it's the same order & probability --> no need to do the recursion again
		if(this.lastOrder.equals(orderMap) && this.lastProbability == probability) {
			result.put("list", this.bestRateList);
			result.put("errors", this.errors);
			return result;
		}
		
		this.bestRateList.clear();
		this.bestRate = 0.0;
		List<Part> partial = new ArrayList<Part>();
		recursive(parts, orderMap, partial, probability, quantity);
		
		//Updating the "last-variables"
		this.lastOrder = orderMap;
		this.lastProbability = probability;
		
		result.put("list", this.bestRateList);
		result.put("bestRate", this.bestRate);
		result.put("bestRatePieces", this.quantity(this.bestRateList, orderMap));
		result.put("bestRateTotalPieces", totalQuantity);
		result.put("bestRateDays", this.daysPartial(this.bestRateList, probability));
		result.put("errors", this.errors);
		return result;
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
		Integer days = this.dueDate.dueDateQuoting(partial, probability);
		return days;
	}

	
	/**
	 * Calculates the quantity of the current solution to check if the #pieces is bigger or equals the minimum quantity
	 */
	private int quantity(List<Part> partial, HashMap<String, Integer> orderMap) {
		int quantity = 0;
		for(Part part : partial)
			quantity += orderMap.get(part.getPart_number());
		return quantity;
	}

	
	
	
	
	
	
	
	
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	/* ============================================================== SIMULATION ============================================================== */
	/* ======================================================================================================================================== */
	/* ======================================================================================================================================== */
	
	
	/**
	 * Simulate the performance of the production of the order in a given period with some defined parameters.
	 * @param orderDate is the {@link LocalDate} in which the order has been received (null if no date was chosen --> today)
	 * @param orderMap is a {@link HashMap} with the part-number and its quantity
	 * @param parallelParts is the {@link Integer} of pats that can be produced simultaneously
	 * @param start is the {@link LocalDate} in which the simulation starts
	 * @param end is the {@link LocalDate} in which the simulation ends
	 * @param yearBefore is the year ({@link Integer}) considered for taking the orders of the simulation
	 * @param maxWaitingDays is the max number ({@link Integer} of days that the item can wait the production, if exceeded it becomes a lost order. Null if is unset
	 * @param simulationsNumber is the number ({@link Integer}) of simulations to be run, null if it is just one simulation
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> simulate(LocalDate orderDate, HashMap<String, Integer> orderMap, Integer parallelParts, LocalDate start, LocalDate end, int yearBefore,
			Integer maxWaitingDays, Integer simulationsNumber) {
		this.errors = "";
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("errors", this.errors);
		
		this.checkInput(orderMap, false);
		if(this.errors.compareTo("Ops! Please choose some products to make this tool work")==0) {
			result.put("errors", this.errors);
			return result;
		}
		
		//Dates check
		if(start == null || start.isBefore(LocalDate.now()))
			start = LocalDate.now();
		if(end == null || end.isBefore(start))
			result.put("errors", "Ops! The end date of the simulation must be equal or after the start date (today if no start date is selected)");
		if(orderDate.isAfter(end))
			result.put("errors", "Ops! The order date must be before the simulation ending date");
		//Useless if there're no parts selected
		if(orderMap.size() < 1)
			result.put("errors", "Please select at least one product to make the simulation possible");
		//Check on the number of parallel parts that can be produced
		if(parallelParts<1)
			result.put("errors", "Ops! Choose a valid number of contemporary producible parts");
		if(((String)result.get("errors")).compareTo("")!=0)
			return result;
			
		
		//From Map to orders
		List<Order> newOrder = new ArrayList<Order>();
		for(String s : orderMap.keySet()) {
			Order o = new Order(null, s, orderMap.get(s), this.partsMap.get(s).getDescription(), null, orderDate);
			newOrder.add(o);
		}
		
		List<Order> orders = this.getOrdersBetweenDates(start, end, yearBefore);
		this.simulator = new Simulator();
		
		//Case: Single simulation
		if(simulationsNumber == null) {
			this.simulator.init(start, end, partsMap, orders, newOrder, parallelParts, maxWaitingDays);
			this.simulator.run();
			
			result.putAll(this.simulator.getResult());
			result.put("errors", this.errors);
		}else { //Case: Simulation+ (more than one simulation)
			if(simulationsNumber<=1) {
				result.put("errors", "Please insert a valid number of simulations (>1)");
				return result;
			}
			result.put("actualOrders", 0);
			result.put("totalOrders", 0);
			result.put("actualQuantity", 0);
			result.put("totalQuantity", 0);
			result.put("inTimeOrders", 0);
			result.put("inStockOrders", 0);
			result.put("lostOrders", 0);
			result.put("success", 0);
			result.put("incomplete", 0);
			result.put("failures", 0);
			result.put("totalIdleness", 0);
			for(int i=0; i<simulationsNumber; i++) {
				this.simulator = new Simulator();
				this.simulator.init(start, end, partsMap, orders, newOrder, parallelParts, maxWaitingDays);
				this.simulator.run();
				HashMap<String, Object> singleSimulation = new HashMap<String, Object>(this.simulator.getResult());
				result.put("actualOrders", (int)result.get("actualOrders") + (int) singleSimulation.get("actualOrders"));
				result.put("totalOrders", (int)singleSimulation.get("totalOrders")); //Always the same
				result.put("actualQuantity", (int)result.get("actualQuantity") + (int) singleSimulation.get("actualQuantity"));
				result.put("totalQuantity", (int)singleSimulation.get("totalQuantity")); //Always the same
				result.put("inTimeOrders", (int)result.get("inTimeOrders") + (int) singleSimulation.get("inTimeOrders"));
				result.put("inStockOrders", (int)result.get("inStockOrders") + (int) singleSimulation.get("inStockOrders"));
				result.put("lostOrders", (int)result.get("lostOrders") + (int) singleSimulation.get("lostOrders"));
				if(((ArrayList<Order>)singleSimulation.get("newOrder")).size() == 0)
					result.put("success", (int)result.get("success") + 1);
				else if(((ArrayList<Order>)singleSimulation.get("newOrder")).size() < orderMap.size())
					result.put("incomplete", (int)result.get("incomplete") + 1);
				else
					result.put("failures", (int)result.get("failures") + 1);
				result.put("totalIdleness", (int)result.get("totalIdleness") + (int)singleSimulation.get("totalIdleness"));
			}
			result.put("actualOrders", (double)(int)result.get("actualOrders")/simulationsNumber);
			result.put("actualQuantity", (double)(int)result.get("actualQuantity")/simulationsNumber);
			result.put("inTimeOrders", (double)(int)result.get("inTimeOrders")/simulationsNumber);
			result.put("inStockOrders", (double)(int)result.get("inStockOrders")/simulationsNumber);
			result.put("lostOrders", (double)(int)result.get("lostOrders")/simulationsNumber);
			result.put("success", (double)(int)result.get("success")/simulationsNumber);
			result.put("incomplete", (double)(int)result.get("incomplete")/simulationsNumber);
			result.put("failures", (double)(int)result.get("failures")/simulationsNumber);
			result.put("totalIdleness", (double)(int)result.get("totalIdleness")/simulationsNumber);
			result.put("errors", this.errors);
		}
		
		return result;
		
	}
	
	/**
	 * Creates a {@link List} of {@link Order} that have the order_date between the start and the end date (included) inserted by the user
	 * @param start is the {@link LocalDate} in which the simulation starts
	 * @param end is the {@link LocalDate} in which the simulation ends
	 * @return the {@link List} of {@link Order}s that started between the two dates
	 */
	private List<Order> getOrdersBetweenDates(LocalDate start, LocalDate end, int yearBefore){
		List<Order> orders = new ArrayList<Order>();
		LocalDate[] simDates = checkSimulationDates(start, end, yearBefore);
		start = simDates[0];
		end = simDates[1];
		System.out.println("\n\n\nINIZIO AGGIUNTA ORDINI DA "+start+" A "+end);
		
		for(Order order : this.ordersList) {
			System.out.print("Ordine "+order+" ");
			if(order.getOrder_date().isEqual(start) || order.getOrder_date().isEqual(end)) {
				orders.add(order.clone());
				System.out.println("aggiunto");
			}
			else if(order.getOrder_date().isAfter(start) && order.getOrder_date().isBefore(end)) {
				orders.add(order.clone());
				System.out.println("aggiunto");
			}
			System.out.println();
		}
		
		return orders;
	}
	
	/**
	 * Checks if the starting and ending simulation dates are in between the MIN_YEAR and the MAX_YEAR (set depending on the existing data of the DB)
	 * @param start is the {@link LocalDate} in which the simulation starts
	 * @param end is the {@link LocalDate} in which the simulation ends
	 * @param yearBefore is the number of years to be subtracted to the starting and ending date to get the data for the simulation
	 * @return an {@link array} of {@link LocalDate}s in which array[0] is the starting date and array[1] is the ending date (both dates are adjusted)
	 */
	private LocalDate[] checkSimulationDates(LocalDate start, LocalDate end, int yearBefore) {
		LocalDate[] date = new LocalDate[2];
		
		start = start.withYear(yearBefore);
		end = end.withYear(yearBefore);
		
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
