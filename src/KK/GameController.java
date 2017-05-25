package KK;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;

public class GameController {
	
	private final ExecutorService executorService ;
	private final Game game ;
	private final Player humanPlayer ;
	private final ComputerPlayer computerPlayer ;
	private final MoveChoosingStrategy hintStrategy ;
	private final Map<Location, Square> squares ;
	
	public GameController(Game game, Player humanPlayer, ComputerPlayer computerPlayer) {
		this.game = game ;
		this.humanPlayer = humanPlayer ;
		this.computerPlayer = computerPlayer ;
		executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				return thread;
			}
		});
		this.hintStrategy = new UnbeatableStrategy();
		this.squares = new HashMap<>();
	}

	@FXML
	private GridPane board ;
	@FXML
	private Label statusLabel ;
	@FXML
	private Label currentPlayerLabel ;


	
	public void initialize() throws IOException {
		setUpSquares();
		getComputerToMoveWhenComputerIsCurrentPlayer();
	}
	


	private void getComputerToMoveWhenComputerIsCurrentPlayer() {
		game.currentPlayerProperty().addListener(new ChangeListener<Object>(){
			@Override
			public void changed(ObservableValue<? extends Object> observable,
					Object oldValue, Object newValue) {
				if (game.getCurrentPlayer() == computerPlayer) {

					final Task<Location> computerMoveTask = new Task<Location>() {
						@Override
						public Location call() throws Exception {
							return computerPlayer.chooseMove(game);
						}
					};
					computerMoveTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							game.makeMove(computerPlayer, computerMoveTask.getValue());
						}
					});
					executorService.submit(computerMoveTask);
				}
			}
		});
	}

	private void setUpSquares() throws IOException {
		// dodawanie pol do Gridpane'a
		//
		for (int column = 0 ; column < 3; column++) {
			for (int row = 0 ; row < 3 ; row++) {
				final Square square = new Square(column, row, humanPlayer, game);
				board.getChildren().add(square);
				squares.put(new Location(column, row), square);
			}
		}
	}
	
	// Event handlers
	

	@FXML 
	private void  newGame() {
		game.reset(humanPlayer);
	}

	
	@FXML
	private void quit() {
		board.getScene().getWindow().hide();
	}

}