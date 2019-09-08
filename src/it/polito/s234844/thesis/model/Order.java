package it.polito.s234844.thesis.model;

import java.time.LocalDate;

public class Order implements Comparable<Order>{
	
	private String customer;
	private String part_number;
	private Integer quantity;
	private String description;
	private LocalDate good_issue_date;
	private LocalDate order_date;
	
	public Order(String customer, String part_number, Integer quantity, String description, LocalDate good_issue_date,
			LocalDate order_date) {
		this.customer = customer;
		this.part_number = part_number;
		this.quantity = quantity;
		this.description = description;
		this.good_issue_date = good_issue_date;
		this.order_date = order_date;
	}

	public String getCustomer() {
		return customer;
	}

	public String getPart_number() {
		return part_number;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public String getDescription() {
		return description;
	}

	public LocalDate getGood_issue_date() {
		return good_issue_date;
	}

	public LocalDate getOrder_date() {
		return order_date;
	}
	
	public void setOrder_date(LocalDate order_date) {
		this.order_date = order_date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((order_date == null) ? 0 : order_date.hashCode());
		result = prime * result + ((part_number == null) ? 0 : part_number.hashCode());
		result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
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
		Order other = (Order) obj;
		if (order_date == null) {
			if (other.order_date != null)
				return false;
		} else if (!order_date.equals(other.order_date))
			return false;
		if (part_number == null) {
			if (other.part_number != null)
				return false;
		} else if (!part_number.equals(other.part_number))
			return false;
		if (quantity == null) {
			if (other.quantity != null)
				return false;
		} else if (!quantity.equals(other.quantity))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return part_number + " - "+ description + " - " + quantity
				+ "pcs";
	}

	@Override
	public int compareTo(Order o) {
		int months = this.order_date.getMonthValue()-o.getOrder_date().getMonthValue();
		if(months != 0)
			return months;
		int days = this.order_date.getDayOfMonth()-o.getOrder_date().getDayOfMonth();
		if(days != 0)
			return days;
		return -(this.quantity - o.getQuantity());
	}
	
	public Order clone() {
		Order order = new Order(this.customer, this.part_number, this.quantity, this.description, this.good_issue_date,
			this.order_date);
		return order;
	}
	
	
}
