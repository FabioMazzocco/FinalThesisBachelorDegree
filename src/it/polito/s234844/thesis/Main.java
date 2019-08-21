package it.polito.s234844.thesis;
	
import it.polito.s234844.thesis.model.ThesisModel;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Thesis.fxml"));
			BorderPane root = loader.load();
			ThesisController controller = loader.getController();
			ThesisModel model = new ThesisModel();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			controller.setModel(model, primaryStage, scene);
			primaryStage.setScene(scene);
			primaryStage.setTitle("DUE DATE TOOL by Fabio Mazzocco");
			primaryStage.setResizable(false);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
