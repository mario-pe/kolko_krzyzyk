package KK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * @mario
 */
public class Game {
	
	public static enum SquareState {
		O, X, EMPTY {
			@Override
			public String toString() {
				return "-" ;
			}
		}
	}
	
	public static enum GameStatus {
		O_WON {
			@Override
			public String toString() {
				return "O won";
			}
		},
		X_WON {
			@Override
			public String toString() {
				return "X won";
			}
		},
		DRAWN {
			@Override
			public String toString() {
				return "Game drawn";
			}
		},
		OPEN {
			@Override
			public String toString() {
				return "Game on";
			}
		}
	}
		

	public static final List<List<Location>> LINES = populateLines();
	
	private final Player oPlayer ;
	private final Player xPlayer ;
	private final ReadOnlyObjectWrapper<Player> currentPlayer ;
	private final ReadOnlyObjectWrapper<GameStatus> gameStatus ;
	private final ObjectProperty<Location> hint ;
	private final List<List<ReadOnlyObjectWrapper<SquareState>>> board ;
	
	 // tworze gre pomiędzy dwoma graczami, gracze O i X kolejno
	 // gracz O zaczyna jako pierwszy

	public Game(Player oPlayer, Player xPlayer) {
		this.oPlayer = oPlayer ;
		this.xPlayer = xPlayer ;
		this.currentPlayer = new ReadOnlyObjectWrapper<>(this, "currentPlayer", oPlayer);
		this.board = new ArrayList<>(3);
		for (int i=0; i<3; i++) {
			List<ReadOnlyObjectWrapper<SquareState>> row = new ArrayList<>(3);
			for (int j=0; j<3; j++) {
				row.add(new ReadOnlyObjectWrapper<SquareState>(SquareState.EMPTY));   // ustawia pola na EMPTY
			}
			board.add(row);
		}
		this.gameStatus = new ReadOnlyObjectWrapper<GameStatus>(this, "gameStatus", GameStatus.OPEN);
		gameStatus.addListener(new ChangeListener<GameStatus>() {
			@Override
			public void changed(ObservableValue<? extends GameStatus> observable,GameStatus oldValue, GameStatus newValue) {  // listener
				if (gameStatus.get() != GameStatus.OPEN) {
					currentPlayer.set(null);
				}
			}
			
		});
		this.hint = new SimpleObjectProperty<Location>(this, "hint");
		
		createGameStatusBinding();
	}

	private static List<List<Location>> populateLines() {
		List<List<Location>> lines = new ArrayList<>();
		// rows
		for (int rowIndex=0; rowIndex<3; rowIndex++) {
			List<Location> row = new ArrayList<>();
			for (int colIndex=0; colIndex<3; colIndex++) {
				row.add(new Location(colIndex, rowIndex));
			}
			lines.add(Collections.unmodifiableList(row));
		}
		// columns
		for (int columnIndex=0; columnIndex<3; columnIndex++) {
			List<Location> column = new ArrayList<>();
			for (int rowIndex=0; rowIndex<3; rowIndex++) {
				column.add(new Location(columnIndex, rowIndex));
			}
			lines.add(Collections.unmodifiableList(column));
		}
		
		List<Location> leadDiagonal = new ArrayList<>();
		List<Location> offDiagonal = new ArrayList<>();
		for (int index=0; index<3; index++) {
			leadDiagonal.add(new Location(index, index));
			offDiagonal.add(new Location(index, 2-index));
		}
		lines.add(Collections.unmodifiableList(leadDiagonal));
		lines.add(Collections.unmodifiableList(offDiagonal));
		return Collections.unmodifiableList(lines);
	}

	// zwraca gracz O

	public Player getOPlayer() {
		return oPlayer ;
	}

	
	// zwraca gracza X

	public Player getXPlayer() {
		return xPlayer ;
	}

	// status- powizanie gracza

	public ReadOnlyObjectProperty<Player> currentPlayerProperty() {
		return currentPlayer.getReadOnlyProperty();
	}
	public Player getCurrentPlayer() {
		return currentPlayer.get();
	}

	// sprawdzanie statsu locacii

	public ReadOnlyObjectProperty<SquareState> squareProperty(Location location) {
		return board.get(location.getColumn()).get(location.getRow()).getReadOnlyProperty();
	}

	public SquareState getSquare(Location location) {
		return board.get(location.getColumn()).get(location.getRow()).get() ;
	}

//	public ReadOnlyObjectProperty<SquareState> squareProperty(int column, int row) {
//		return squareProperty(new Location(column, row));
//	}
	public SquareState getSquare(int column, int row) {
		return getSquare(new Location(column, row));
	}
	
	// pobranie status obiektu game

	public ReadOnlyObjectProperty<GameStatus> gameStatusProperty() {
		return gameStatus.getReadOnlyProperty();
	}
	public GameStatus getGameStatus() {
		return gameStatus.get();
	}



//	public void makeMove(Player player, int column, int row) {
//		makeMove(player, new Location(column, row));
//	}
	


	public void makeMove(Player player, Location location) {
		if (player != currentPlayer.get()) {
			throw new IllegalArgumentException("It is not "+player+"\'s turn");
		}
		final ReadOnlyObjectWrapper<SquareState> squareState = board.get(location.getColumn()).get(location.getRow());
		if (squareState.get() != SquareState.EMPTY) {
			throw new IllegalArgumentException(String.format("%s is already occupied with %s", location, squareState.get()));
		}
		if (player == xPlayer) {
			squareState.set(SquareState.X);
			if (gameStatus.get() == GameStatus.OPEN) {
				currentPlayer.set(oPlayer);
			} 
		} else {
			squareState.set(SquareState.O);
			if (gameStatus.get() == GameStatus.OPEN) {
				currentPlayer.set(xPlayer);
			}
		}
		hint.set(null);
	}
	// reset gry

	public void reset(Player firstPlayer) {
		if (firstPlayer != oPlayer && firstPlayer != xPlayer) {
			throw new IllegalArgumentException(firstPlayer + " is not a player in this game.");
		}
		for (List<ReadOnlyObjectWrapper<SquareState>> row : board) {
			for (ReadOnlyObjectWrapper<SquareState> square : row ) {
				square.set(SquareState.EMPTY);
			}
		}
		hint.set( null );
		currentPlayer.set(firstPlayer);
	}
	 // reset gry, kolejna zacznie gracz O

	public void reset() {
		reset(oPlayer);
	}
	
	private void createGameStatusBinding() {
		final List<Observable> allSquares = new ArrayList<>();
		for (List<ReadOnlyObjectWrapper<SquareState>> row : board) 
			for (ObjectProperty<SquareState> square : row) 
				allSquares.add(square);
		ObjectBinding<GameStatus> gameStatusBinding = new ObjectBinding<GameStatus>() {
			{ super.bind(allSquares.toArray(new Observable[9])); }
			@Override
			public GameStatus computeValue() {
				
				for (List<Location> line : LINES) {
					GameStatus check = checkForWinner(line);
					if (check != null) {
						return check ;
					}
				}

				// check for empty square, return GameStatus.OPEN if one is found:
				for (List<ReadOnlyObjectWrapper<SquareState>> row : board) {
					for (ReadOnlyObjectWrapper<SquareState> square: row) {
						if (square.get() == SquareState.EMPTY) {
							return GameStatus.OPEN ;
						}
					}
				}
				
				// no winner and full board; game is drawn:
				
				return GameStatus.DRAWN ;
			}
		};
		gameStatus.bind(gameStatusBinding);
	}
	
	private GameStatus checkForWinner(List<Location> line) {
		int oCount = 0 ;
		int xCount = 0 ;
		for (Location location : line) {
			SquareState square = getSquare(location);
			if (square==SquareState.O) {
				oCount++ ;
			} else if (square == SquareState.X){
				xCount++;
			}
		}
		if (oCount==3) {
			return GameStatus.O_WON;
		} else if (xCount==3) {
			return GameStatus.X_WON;
		} else return null ;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (List<ReadOnlyObjectWrapper<SquareState>> row : board) {
			for (ReadOnlyObjectWrapper<SquareState> square : row) {
				builder.append(square.get());
			}
			builder.append("\n");
		}
		return builder.toString();
	}
}
