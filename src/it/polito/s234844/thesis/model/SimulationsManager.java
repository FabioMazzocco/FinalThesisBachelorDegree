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

public class SimulationsManager {
	
	//General variables
		private Integer simulationsNumber;
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
		
		public SimulationsManager() {
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
	
}
