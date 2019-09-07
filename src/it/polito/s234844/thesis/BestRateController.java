package it.polito.s234844.thesis;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import it.polito.s234844.thesis.model.Order;
import it.polito.s234844.thesis.model.Part;
import it.polito.s234844.thesis.model.ThesisModel;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

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
    private Slider bestRateSliderProbability;

    @FXML
    private Text txtProbability;

    @FXML
    private Slider bestRateSliderPercentage;

    @FXML
    private Text txtPercentage;	
    
    @FXML
    private Button btnBestRate;
    
    @FXML
    private ListView<Order> listView;
    
    @FXML
    private Text txtBestRate;
    
    @FXML
    private Text txtDays;
    
    @FXML
    private StackedBarChart<CategoryAxis, NumberAxis> barParts;

    @FXML
    private StackedBarChart<CategoryAxis, NumberAxis> barQuantity;
    
    @FXML
    private Button btnHome;

    @SuppressWarnings("unchecked")
	@FXML
    void handleBestRateCalculation(ActionEvent event) {
    	HashMap<String, Object> result = this.model.bestRate(this.orderMap, this.bestRateSliderProbability.getValue()/100, this.bestRateSliderPercentage.getValue()/100);
    	
    	//Errors management
    	if(((String)result.get("errors")).compareTo("")!=0) {
    		String errors = "THE FOLLOWING ERRORS HAVE BEEN FOUND:\n"+(String)result.get("errors");
    		JOptionPane.showMessageDialog(null, errors,"ERRORS OCCURRED", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	//List view
    	List<Order> bestRateList = new ArrayList<Order>();
    	for(Part p : (List<Part>)result.get("list")) {
    		Order o = new Order(null, p.getPart_number(), this.orderMap.get(p.getPart_number()), p.getDescription(), null, null);
    		bestRateList.add(o);
    	}
    	this.listView.getItems().clear();
    	this.listView.getItems().addAll(bestRateList);
    	
    	//Best rate text
    	this.txtBestRate.setText(String.format("%.2f", result.get("bestRate")));
    	this.txtDays.setText(String.format("%d", result.get("bestRateDays")));
    	
    	//Bar charts
    	this.barParts.setVisible(true);
        this.barQuantity.setVisible(true);
        
        XYChart.Series quantityData1 = new XYChart.Series();
        XYChart.Data producedQty = new XYChart.Data("Produced quantity", (int)result.get("bestRatePieces"));
        quantityData1.getData().add(producedQty);
        XYChart.Series quantityData2 = new XYChart.Series();
        XYChart.Data missingQty = new XYChart.Data("Produced quantity", ((int)result.get("bestRateTotalPieces")-(int)result.get("bestRatePieces")));
        quantityData2.getData().add(missingQty);
        
        
        this.barQuantity.getData().clear();
        this.barQuantity.layout();
    	this.barQuantity.getData().add(quantityData1);
    	this.barQuantity.getData().add(quantityData2);
        
    	XYChart.Series partsData1 = new XYChart.Series();
        partsData1.getData().add(new XYChart.Data("Produced parts", this.listView.getItems().size()));
        XYChart.Series partsData2 = new XYChart.Series();
        partsData2.getData().add(new XYChart.Data("Produced parts", this.orderMap.size()-this.listView.getItems().size()));
       
        this.barParts.getData().clear();
        this.barParts.layout();
    	this.barParts.getData().add(partsData1);
    	this.barParts.getData().add(partsData2);
    }
    
    @FXML
    void initialize() {
    	assert bestRateSliderProbability != null : "fx:id=\"bestRateSliderProbability\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert txtProbability != null : "fx:id=\"txtProbability\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert bestRateSliderPercentage != null : "fx:id=\"bestRateSliderPercentage\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert txtPercentage != null : "fx:id=\"txtPercentage\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert btnBestRate != null : "fx:id=\"btnBestRate\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert listView != null : "fx:id=\"listView\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert txtBestRate != null : "fx:id=\"txtBestRate\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert txtDays != null : "fx:id=\"txtDays\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert barParts != null : "fx:id=\"barParts\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert barQuantity != null : "fx:id=\"barQuantity\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert btnHome != null : "fx:id=\"btnHome\" was not injected: check your FXML file 'BestRate.fxml'.";
        assert bestRateTop != null : "fx:id=\"bestRateTop\" was not injected: check your FXML file 'BestRate.fxml'.";
        //Top
        this.bestRateTop.setStyle("-fx-background-color: rgb(33, 215, 243);");
        //Sliders
        this.txtProbability.textProperty().bind(Bindings.format("%.2f%%",this.bestRateSliderProbability.valueProperty()));
        this.txtPercentage.textProperty().bind(Bindings.format("%.2f%%",this.bestRateSliderPercentage.valueProperty()));     
        //Bar
//        this.barQuantity.setTitle("Produced quantity");
        this.barParts.setVisible(false);
        this.barQuantity.setVisible(false);
        this.barParts.getXAxis().setVisible(false);
        this.barQuantity.getXAxis().setVisible(false);
        this.barParts.getYAxis().setVisible(false);
        this.barQuantity.getYAxis().setVisible(false);
        this.barParts.getYAxis().setOpacity(0);
        this.barParts.getXAxis().setOpacity(0);
        this.barQuantity.getYAxis().setOpacity(0);
        this.barQuantity.getXAxis().setOpacity(0);
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
