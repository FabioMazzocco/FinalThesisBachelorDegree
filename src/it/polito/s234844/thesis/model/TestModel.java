package it.polito.s234844.thesis.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestModel {

	public static void main(String[] args) {
		ThesisModel model = new ThesisModel();
		
		//Data loading (w/ time)
		long start = System.currentTimeMillis();
		if(model.loadData()) {
			long end = System.currentTimeMillis();
			System.out.println("Everything ok in "+(end-start)+" milliseconds");
		}
		
		//Descriptive Stats
		for(Part p : model.getPartsList()) {
			System.out.println(p+" - mean: "+p.getMean()+" - variance: "+p.getStandDev()*p.getStandDev()+" - min/max: "+p.getMin()+"/"+p.getMax());
		}
		
		//Due Date
		List<String> parts = new ArrayList<String>();
		parts.add(model.getPartsList().get(0).getPart_number());
		parts.add(model.getPartsList().get(14).getPart_number());
		parts.add(model.getPartsList().get(20).getPart_number());
		
		System.out.println("\n\n" + model.dueDateQuoting(parts,LocalDate.of(2019, 5, 20), 0.7, 1));
		
		System.out.println("\n\n"+model.dueDateProbability(parts, LocalDate.now(), LocalDate.now().plusDays(17), 5));
		
		
		//Best rate
		HashMap<String, Integer> orderMap = new HashMap<String, Integer>();
		
		orderMap.put(model.getPartsList().get(0).getPart_number(), 89);
		orderMap.put(model.getPartsList().get(14).getPart_number(), 418);
		orderMap.put(model.getPartsList().get(20).getPart_number(), 274);
		orderMap.put(model.getPartsList().get(5).getPart_number(), 600);
		
//		System.out.println(model.bestRate(orderMap, LocalDate.now(), 0.7, ((double)2/3)));
		
		
		//Simulation
		System.out.println(model.simulate(LocalDate.now().plusDays(5), orderMap, 10, LocalDate.now(), LocalDate.now().plusDays(7), 0));
	}

}
