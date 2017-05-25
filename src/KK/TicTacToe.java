package KK;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TicTacToe extends Application {

	@Override
	public void start(final Stage primaryStage) throws IOException {
		
		final Player humanPlayer = new Player("player");
		final ComputerPlayer computerPlayer = new ComputerPlayer("comp", new UnbeatableStrategy());
		final Game game = new Game(humanPlayer, computerPlayer);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("TicTacToe.fxml"));
		loader.setController(new GameController(game, humanPlayer, computerPlayer));
		Parent root = (Parent) loader.load();
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}
