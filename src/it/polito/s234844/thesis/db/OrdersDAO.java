package it.polito.s234844.thesis.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import it.polito.s234844.thesis.model.Order;
import it.polito.s234844.thesis.model.Part;

public class OrdersDAO {
	
	/**
	 * This method enables the model to load all the orders present in the DB
	 * @return the full {@link List} of {@link Order}
	 */
	public List<Order> loadOrders(){
		final String sql = "SELECT * FROM orders";
		
		try {
			//Creation of the connection and preparedStatement objects
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			//Creation of the result list
			List<Order> orders = new ArrayList<Order>();
			//SQL Query execution
			ResultSet rs = st.executeQuery();
			//Loop on the result rows
			while(rs.next()) {
				String customer = rs.getString("Customer");
				String part_number = rs.getString("Part_Number");
				Integer quantity = rs.getInt("Quantity");
				String description = rs.getString("Description");
				Date good_issue_date = rs.getDate("Good_Issue_Date");
				Date order_date = rs.getDate("Order_Date");
				//If any of these variables is null --> invalid row
				if(part_number == null || quantity == null || quantity == 0|| good_issue_date == null || 
						order_date == null)
					continue;
				//Creation of an Order object --> addition to the result list
				Order order = new Order(customer, part_number, quantity, description, good_issue_date.toLocalDate(),
						order_date.toLocalDate());
				orders.add(order);
			}
			return orders;
		}catch(Exception e) {
			//If an exception is thrown --> the method returns an empty list
			e.printStackTrace();
			return new ArrayList<Order>();
		}
	}
	
	/**
	 * This method enables the model to load all the parts present in the DB
	 * @return the full {@link List} of {@link Part}
	 */
	public List<Part> loadParts(){
		final String sql ="SELECT DISTINCT Part_Number, Description FROM orders ORDER BY Part_Number ASC";
		
		try {
			//Creation of the connection and preparedStatement objects
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			//Creation of the result list
			List<Part> parts = new ArrayList<Part>();
			//SQL Query execution
			ResultSet rs = st.executeQuery();
			//Loop on the result rows
			while(rs.next()) {
				String part_number = rs.getString("Part_Number");
				String description = rs.getString("Description");
				
				if(part_number != null) {
					Part part = new Part(part_number, description);
					parts.add(part);
				}
			}
			return parts;
		}catch(Exception e) {
			//If an exception is thrown --> the method returns an empty list
			e.printStackTrace();
			return new ArrayList<Part>();
		}
		
	}
	
	/**
	 * This method enables the model to load all the customers present in the DB
	 * @return the full {@link List} of customers as {@link String}
	 */
	public List<String> loadCustomers(){
		final String sql ="SELECT DISTINCT Customer FROM orders ORDER BY Customer ASC";
		
		try {
			//Creation of the connection and preparedStatement objects
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			//Creation of the result list
			List<String> customers = new ArrayList<String>();
			//SQL Query execution
			ResultSet rs = st.executeQuery();
			//Loop on the result rows
			while(rs.next()) {
				String customer = rs.getString("Customer");
				
				if(customer != null)
					customers.add(customer);
			}
			
			return customers;
		}catch(Exception e) {
			//If an exception is thrown --> the method returns an empty list
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	/**
	 * Finds the minimum year and the maximum one to set the static variables that are useful to get the data for the simulation
	 * @return an array in which the first value (array[0]) is the MIN_YEAR and the second one (array[1]) is the maximum year-1 --> MAX_YEAR
	 */
	public int[] loadMinMaxYear() {
		final String sql = "SELECT MIN(YEAR(order_date)) AS MIN_YEAR, MAX(YEAR(order_date)) AS MAX_YEAR FROM orders";
		
		try {
			//Creation of the connection and preparedStatement objects
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			//Creation of the result array
			int[] minMaxYears = new int[2];
			//SQL Query execution
			ResultSet rs = st.executeQuery();
			//Loop on the result rows
			if(rs.next()) {
				minMaxYears[0] = rs.getInt("MIN_YEAR");
				minMaxYears[1] = rs.getInt("MAX_YEAR")-1;
			}
			
			return minMaxYears;
		}catch(Exception e) {
			//If an exception is thrown --> the method returns an empty list
			e.printStackTrace();
			return new int[] {2012, 2018};
		}
	}
}
