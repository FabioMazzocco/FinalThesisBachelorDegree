package it.polito.s234844.thesis;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import it.polito.s234844.thesis.model.ThesisModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class BestRateController {
	
	private ThesisController home;
	private ThesisModel model;
	private HashMap<String, Integer> orderMap;
	
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Pane bestRateTop;
    
    @FXML
    private PieChart pieOrdersNumber;

    @FXML
    private PieChart pieQuantity;
    
    @FXML
    private Button btnHome;

    @FXML
    void initialize() {
    	assert btnHome != null : "fx:id=\"btnHome\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert bestRateTop != null : "fx:id=\"bestRateTop\" was not injected: check your FXML file 'BestRate.fxml'.";
        
        //Top
        this.bestRateTop.setStyle("-fx-background-color: rgb(33, 215, 243);");
        
        
        
    }
    
    @FXML
    void goBackHome(ActionEvent event) {
    	this.home.returnToPrimary();
    }
    
    public void setOrder(ThesisController home, ThesisModel model, HashMap<String, Integer> orderMap) {
    	this.home = home;
    	this.model = model;
    	this.orderMap = orderMap;
    }
}
