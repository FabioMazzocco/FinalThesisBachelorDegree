package it.polito.s234844.thesis;

import java.util.HashMap;
import java.util.List;
import it.polito.s234844.thesis.model.Part;
import it.polito.s234844.thesis.model.ThesisModel;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;

public class DueDateQuotingController {

	private ThesisModel model;
	private ThesisController home;
	private HashMap<String, Integer> orderMap;
	private LocalDate orderDate;
	private HashMap<String, Part> partsMap;
	private boolean isParallel;
	
	@FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    
    @FXML
    private Label dueDateQuotingTop;
    
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
    private AreaChart<Double, Double> dueDateQuotingChart;
    
    @FXML
    private Button btnHome;
    
    public DueDateQuotingController() {
    	this.model = null;
    	this.orderMap = null;
    	this.partsMap = null;
    	this.isParallel = false;
    }
    
    @FXML
    void handleSerialParallelProduction(ActionEvent event) {
    	if(this.isParallel) {
    		this.isParallel = false;
    		this.txtParallelProduction.setDisable(true);
    		this.txtSerialProduction.setDisable(false);
    		this.btnSerialParallelProduction.setText("Serial production");
    	}else {
    		this.isParallel = true;
    		this.txtParallelProduction.setDisable(false);
    		this.txtSerialProduction.setDisable(true);
    		this.btnSerialParallelProduction.setText("Parallel production");
    	}
    }

    
    @FXML
    void handleDueDateQuotation(ActionEvent event) {
    	System.out.println("CIAO");
    	List<Object> result = this.model.dueDateQuoting(this.orderMap, this.orderDate, this.dueDateQuotingSlider.getValue(), this.isParallel);
    	if(((String)result.get(1)).compareTo("")!=0)
    		System.out.println((String)result.get(1));
    	else {
    		this.txtDays.setText(""+(int) result.get(0));
    		this.txtDate.setText(this.orderDate.plusDays((Integer)result.get(0)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    	}
    	
    }
    
    @FXML
    void changeProbability(ActionEvent event) {
    	try {
    		Double newProbability = Double.parseDouble(this.txtProbability.getText())/100;
    		if(newProbability<=0 || newProbability>=1)
    			throw new Exception();
    		this.dueDateQuotingSlider.setValue(newProbability);
    		this.txtProbability.setText((newProbability*100)+"%");
    	}catch(Exception e) {
    		this.txtProbability.clear();
    		this.txtProbability.setPromptText("0.00%");
    	}
    }
    
    @FXML
    void initialize() {
        assert dueDateQuotingSlider != null : "fx:id=\"dueDateQuotingSlider\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
        assert btnSerialParallelProduction != null : "fx:id=\"btnSerialParallelProduction\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
        assert dueDateQuotingChart != null : "fx:id=\"dueDateQuotingChart\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";

    }
    
    @FXML
    void goBackHome(ActionEvent event) {
//    	this.home.backHome();
    }

	public void setOrder(ThesisController home, ThesisModel model, HashMap<String, Integer> orderMap, LocalDate orderDate) {
		this.home = home;
		this.model = model;
		this.orderMap = orderMap;
		this.orderDate = orderDate;
		
//		for(double i=0; i<300; i=i+0,001) {
//			this.dueDateQuotingChart.getData().add()
//		}
	}

}

