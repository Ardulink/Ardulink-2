package org.ardulink.console.fx;

import javafx.application.Application;
import javafx.stage.Stage;

public class ConsoleApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		new ConsoleWindow(primaryStage).show();
	}
}
