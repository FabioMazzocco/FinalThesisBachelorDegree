package it.polito.s234844.thesis;

import java.util.HashMap;
import it.polito.s234844.thesis.model.ThesisModel;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.apache.commons.math3.distribution.NormalDistribution;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;

public class DueDateQuotingController {

	private ThesisModel model;
	private ThesisController home;
	private HashMap<String, Integer> orderMap;
	private LocalDate orderDate;
	private boolean isParallel;
	
	private final String bold = "-fx-font-weight: bold;";
	private final String normal = "-fx-font-weight: 400";
	
	@FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    
    @FXML
    private Pane dueDateQuotingTop;
    
    @FXML
    private Slider dueDateQuotingSlider;
    
    @FXML
    private TextField txtProbability;

    @FXML
    private ToggleButton btnSerialParallelProduction;
    
    @FXML
    private Label txtSerialProduction;

    @FXML
    private Label txtParallelProduction;
    
    @FXML
    private Button btnCalculate;
    
    @FXML
    private Text txtDays;

    @FXML
    private Text txtDate;
    
    @FXML
    private AreaChart<Number, Number> dueDateQuotingChart;
    
    @FXML
    private Button btnHome;
    
    public DueDateQuotingController() {
    	this.model = null;
    	this.orderMap = null;
    	this.isParallel = false;
    }
    
    /**
     * Due date quotation
     * @param event
     */
    @FXML
    void handleDueDateQuotation(ActionEvent event) {
    	HashMap<String, Object> result = this.model.dueDateQuoting(this.orderMap, this.orderDate, this.dueDateQuotingSlider.getValue()/100, this.isParallel);
    	
    	//Errors management
    	if(((String)result.get("errors")).compareTo("")!=0) {
    		String errors = "THE FOLLOWING ERRORS HAVE BEEN FOUND:\n"+(String)result.get("errors");
    		JOptionPane.showMessageDialog(null, errors,"ERRORS OCCURRED", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	//Text&chart update
    	this.txtDays.setText(""+(Integer) result.get("days"));
    	this.txtDate.setText(this.orderDate.plusDays((Integer)result.get("days")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    	this.setChart((NormalDistribution)result.get("normal"));
    }
    
    /**
     * Handling of the switch between serial and parallel production
     * @param event
     */
    @FXML
    void handleSerialParallelProduction(ActionEvent event) {
    	if(this.isParallel) {
    		this.isParallel = false;
    		this.txtParallelProduction.setDisable(true);
    		this.txtParallelProduction.setStyle(this.normal);
    		this.txtSerialProduction.setDisable(false);
    		this.txtSerialProduction.setStyle(this.bold);
    		this.btnSerialParallelProduction.setText("Serial production");
    	}else {
    		this.isParallel = true;
    		this.txtParallelProduction.setDisable(false);
    		this.txtParallelProduction.setStyle(this.bold);
    		this.txtSerialProduction.setDisable(true);
    		this.txtSerialProduction.setStyle(this.normal);
    		this.btnSerialParallelProduction.setText("Parallel production");
    	}
    }

    @FXML
    void changeProbability(ActionEvent event) {
    	try {
    		Double newProbability = Double.parseDouble(this.txtProbability.getText());
    		if(newProbability<=0 || newProbability>=1)
    			throw new Exception();
    		this.dueDateQuotingSlider.setValue(newProbability);
    		this.txtProbability.setText(newProbability+"%");
    	}catch(Exception e) {
    		this.txtProbability.clear();
    		this.txtProbability.setPromptText("0.00%");
    	}
    }
    
    @FXML
    void initialize() {
    	 assert dueDateQuotingTop != null : "fx:id=\"dueDateQuotingTop\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert dueDateQuotingSlider != null : "fx:id=\"dueDateQuotingSlider\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert txtProbability != null : "fx:id=\"txtProbability\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert txtSerialProduction != null : "fx:id=\"txtSerialProduction\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert btnSerialParallelProduction != null : "fx:id=\"btnSerialParallelProduction\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert txtParallelProduction != null : "fx:id=\"txtParallelProduction\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert btnCalculate != null : "fx:id=\"btnCalculate\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert txtDays != null : "fx:id=\"txtDays\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert txtDate != null : "fx:id=\"txtDate\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert dueDateQuotingChart != null : "fx:id=\"dueDateQuotingChart\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
         assert btnHome != null : "fx:id=\"btnHome\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
        
        //Top bar
        this.dueDateQuotingTop.setStyle("-fx-background-color: rgb(33, 215, 243);");
        
        //Slider
        this.txtProbability.textProperty().bind(
                Bindings.format(
                    "%.2f%%",
                    this.dueDateQuotingSlider.valueProperty()
                )
            );
 	
    }
    
    @FXML
    void goBackHome(ActionEvent event) {
    	this.home.returnToPrimary();
    }

	public void setOrder(ThesisController home, ThesisModel model, HashMap<String, Integer> orderMap, LocalDate orderDate) {
		this.home = home;
		this.model = model;
		this.orderMap = orderMap;
		this.orderDate = orderDate;
	}
	
	/**
	 * Set the chart data
	 * @param normal
	 */
	public void setChart(NormalDistribution normal) {
		//Clear all the already present data
		this.dueDateQuotingChart.getData().clear();
		
		//Cumulative probability (till the selected data)
		double maxProbability = this.dueDateQuotingSlider.getValue()/100;
		if(maxProbability>0.9999)
			maxProbability = 0.9999;
        XYChart.Series<Number, Number> serie0 = new XYChart.Series<Number, Number>();
        
		for(double i=0; i<=normal.inverseCumulativeProbability(maxProbability); i = i+0.7) {
			XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(i, normal.density(i));
			
			serie0.getData().add(data);
		}
		XYChart.Data<Number, Number> lastData = new XYChart.Data<Number, Number>(normal.inverseCumulativeProbability(maxProbability), normal.density(normal.inverseCumulativeProbability(maxProbability)));
		serie0.getData().add(lastData);
		this.dueDateQuotingChart.getData().add(serie0);
		
		//Cumulative probability (till 100%)
        XYChart.Series<Number, Number> serie1 = new XYChart.Series<Number, Number>();
        
		for(double i=0; i<normal.inverseCumulativeProbability(0.9999); i = i+0.7) {
			XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(i, normal.density(i));
			
			serie1.getData().add(data);
		}
		serie1.getData().add(new XYChart.Data<Number,Number>(normal.inverseCumulativeProbability(0.9999),normal.density(normal.inverseCumulativeProbability(0.9999))));
		this.dueDateQuotingChart.getData().add(serie1);
		
		//Point
		XYChart.Series<Number, Number> serie2 = new XYChart.Series<Number, Number>();
		XYChart.Data<Number, Number> point = new XYChart.Data<Number, Number>(normal.inverseCumulativeProbability(maxProbability), normal.density(normal.inverseCumulativeProbability(maxProbability)));
		serie2.getData().add(point);
		this.dueDateQuotingChart.getData().add(serie2);
		
		//Two lines
		XYChart.Series<Number, Number> serie3 = new XYChart.Series<Number, Number>();
		serie3.getData().add(new XYChart.Data<Number, Number>(0, normal.density(normal.inverseCumulativeProbability(maxProbability))));
		serie3.getData().add(new XYChart.Data<Number, Number>(normal.inverseCumulativeProbability(maxProbability), normal.density(normal.inverseCumulativeProbability(maxProbability))));
		
		XYChart.Series<Number, Number> serie4 = new XYChart.Series<Number, Number>();
		serie4.getData().add(new XYChart.Data<Number, Number>(normal.inverseCumulativeProbability(maxProbability), 0));
		serie4.getData().add(new XYChart.Data<Number, Number>(normal.inverseCumulativeProbability(maxProbability), normal.density(normal.inverseCumulativeProbability(maxProbability))));
		
		this.dueDateQuotingChart.getData().add(serie3);
		this.dueDateQuotingChart.getData().add(serie4);
		
		this.dueDateQuotingChart.setAnimated(false);
	}

}

