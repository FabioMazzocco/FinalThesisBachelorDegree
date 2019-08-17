package it.polito.s234844.thesis.db;

public class TestDAO {

	public static void main(String[] args) {
		OrdersDAO dao = new OrdersDAO();
		
		System.out.println(dao.loadOrders()+"\n\n\n");
		
		System.out.println(dao.loadCustomers()+"\n\n\n");

		System.out.println(dao.loadParts()+"\n\n\n");
	}

}
