package KK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import KK.Game.SquareState;

public class UnbeatableStrategy implements MoveChoosingStrategy {

	@Override
	public Location chooseMove(Game gameState) {
		// status gracz
		final SquareState me = getMe(gameState);
		// status przeciwnik
		final SquareState opponent = getOpponentOf(me);

		// Lista mozliwych ruchów
		List<Location> emptyLocations = new ArrayList<>();
		for (int column = 0 ; column < 3; column++) {
			for (int row = 0 ; row < 3; row++) {
				SquareState square = gameState.getSquare(column, row);
				if (square == SquareState.EMPTY) {
					emptyLocations.add(new Location(column, row));
				}
			}
		}
		// randomowy wybor ruchow
		Collections.shuffle(emptyLocations);
		
		// poszukiwanie zwycieskich lini:
		List<Location> winningLine = findWinningLine(gameState, me, opponent);
		if (winningLine != null) {
			// zwraca square ktory moze dac zwycieństwo
			for (Location loc : winningLine) {
				if (gameState.squareProperty(loc).get() == SquareState.EMPTY) {
					return loc ;
				}
			}
		}
		
		// przeszukiwanie bordera aby zablokowac przeciwnika:
		List<Location> opponentsWinningLine = findWinningLine(gameState, opponent, me) ;
		if (opponentsWinningLine != null) {
			// jesli jest winningLine to zablokuj przeciwnika
			// (blokowanie opponenta)
			for (Location loc : opponentsWinningLine) {
				if (gameState.squareProperty(loc).get() == SquareState.EMPTY) {
					return loc ;
				}
			}
		}
		

		// przeszukuje w celu znalezienia rozwidleń, które mogą dac zwyciestwo w kolejnym ruchu
		for (Location location : emptyLocations) {
			if (createsFork(location, gameState, me)) {
				return location ;
			}
		}
		
		//szukanie czy przeciwnik moze zrobic forka
		
		for (Location location : emptyLocations) {
			List<List<Location>> intersectingLines = getIntersectingLines(location);
			for (List<Location> line : intersectingLines) {
				// sprawdzam czy bede mogl wygrac z tego miejsca w kolejnym ruchu
				if (count(line, me, gameState)==1 && count(line, opponent, gameState)==0) {
					// poszukiwanie innego wolnego miejsca
					for (Location loc : line) {
						if (!loc.equals(location) && gameState.getSquare(loc)==SquareState.EMPTY) {
							// sparwdzam czy przeciwnik po tym ruchu niebedzie mial mozliwosci do wygrania
							if (! createsFork(loc, gameState, opponent)) {
								return location ;
							}
						}
					}
				}
			}
		}


		
		// domyslne pola ...
		List<Location> orderedLocs = Arrays.asList(
			new Location(1, 1),
			new Location(0, 0),
			new Location(2, 0),
			new Location(0, 2), 
			new Location(2, 2),
			new Location(1, 0), 
			new Location(0, 1), 
			new Location(2, 1), 
			new Location(1, 2)
		);
		for (Location loc : orderedLocs) {
			if (gameState.squareProperty(loc).get() == SquareState.EMPTY) {
				return loc ;
			}
		}
		// tylko jesli tablica jest pełna
		return null ;
	}
	// sprawdzanie czy przeciwnik bedzie mogl stworzyc "rozwidlenie"
	private boolean createsFork(Location location, Game game, SquareState playerMark) {
		SquareState otherMark = getOpponentOf(playerMark);
		List<List<Location>> intersectingLines = getIntersectingLines(location);
		int countPossibleWinningLines = 0 ;
		for (List<Location> line : intersectingLines) {
			if (count(line, playerMark, game) == 1 && count(line, otherMark, game) == 0) {
				countPossibleWinningLines++ ;
			}
		}
		return countPossibleWinningLines >= 2 ;
	}
	// zwraca znak przeciwnika, jesli x to o i odwrotnie

	private SquareState getOpponentOf(SquareState playerMark) {
		SquareState otherMark ;
		if (playerMark == SquareState.O) {
			otherMark = SquareState.X ;
		} else if (playerMark == SquareState.X) {
			otherMark = SquareState.O ;
		} else {
			otherMark = null ;
		}
		return otherMark;
	}

	// zwraca liste linii z danego miejsca (square'a)
	private List<List<Location>> getIntersectingLines(Location location) {
		List<List<Location>> intersectingLines = new ArrayList<>();
		for (List<Location> line : Game.LINES) {
			if (line.contains(location)) {
				intersectingLines.add(line);
			}
		}
		return intersectingLines;
	}

	// If there is a line for which the specified player can win, return it
	private List<Location> findWinningLine(Game game, SquareState player, SquareState opponent) {
		for (List<Location> line : Game.LINES) {
			if (count(line, player, game) == 2 && count(line, opponent, game) == 0) {
				return line ;
			}
		}
		return null ;
	}
	
	// zwraca znak gracza ktory wykonuje ruch
	private SquareState getMe(Game game) {
		if (game.getCurrentPlayer() == game.getOPlayer()) {
			return SquareState.O ;
		} else if (game.getCurrentPlayer() == game.getXPlayer()) {
			return SquareState.X ;
		} else {
			return null ;
		}
	}
	// zlicza ilosc znakow dla danej linii

	private int count(List<Location> line, SquareState target, Game game) {
		int count = 0 ;
		for (Location loc : line ) {
			SquareState square = game.squareProperty(loc).get();
			if (square == target) {
				count ++ ;
			}
		}
		return count ;
	}
	
}
