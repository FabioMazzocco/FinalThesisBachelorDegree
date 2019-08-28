package it.polito.s234844.thesis;

import java.util.HashMap;
import it.polito.s234844.thesis.model.ThesisModel;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
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
//	private HashMap<String, Part> partsMap;
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
    private LineChart<String, Double> dueDateQuotingChart;
    
    @FXML
    private Button btnHome;
    
    public DueDateQuotingController() {
    	this.model = null;
    	this.orderMap = null;
//    	this.partsMap = null;
    	this.isParallel = false;
    }
    
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
    void handleDueDateQuotation(ActionEvent event) {
    	HashMap<String, Object> result = this.model.dueDateQuoting(this.orderMap, this.orderDate, this.dueDateQuotingSlider.getValue()/100, this.isParallel);
    	//Errors management
    	if(((String)result.get("errors")).compareTo("")!=0) {
    		String errors = "THE FOLLOWING ERRORS HAVE BEEN FOUND:\n"+(String)result.get("errors");
    		JOptionPane.showMessageDialog(null, errors,"ERRORS OCCURRED", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	this.txtDays.setText(""+(Integer) result.get("days"));
    	this.txtDate.setText(this.orderDate.plusDays((Integer)result.get("days")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    	this.setChart((NormalDistribution)result.get("normal"));
    	   	
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
        
        //Chart
        this.dueDateQuotingChart.getXAxis().setTickLabelsVisible(false);
    	this.dueDateQuotingChart.getXAxis().setTickMarkVisible(false);
    	this.dueDateQuotingChart.getYAxis().setTickLabelsVisible(false);
    	this.dueDateQuotingChart.getYAxis().setTickMarkVisible(false);
    	
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
	
	public void setChart(NormalDistribution normal) {		
		this.dueDateQuotingChart.getData().clear();
		
        XYChart.Series<String, Double> serie= new XYChart.Series<String, Double>();
        
		for(double i=0; i<normal.inverseCumulativeProbability(0.9999999)+1; i = i+0.7) {
			XYChart.Data<String, Double> data = new XYChart.Data<String, Double>(Double.toString(i), normal.density(i));
			
			serie.getData().add(data);
		}
		this.dueDateQuotingChart.getData().add(serie);
	}

}

