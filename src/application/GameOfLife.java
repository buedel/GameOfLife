package application;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameOfLife extends Application {

	private static final int WIDTH = 10;
	private static final int HEIGHT = WIDTH; // TODO: I think these can only be equal with current implementation
	private static final int BOARD_SIZE = 320;

	private Map<String, StackPane> boardMap = new HashMap<String, StackPane>();
	private Board board = new Board(BOARD_SIZE / WIDTH);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		// TODO.. learn Timeline
		final Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new EventHandler() {
			@Override
			public void handle(Event event) {
				iterateBoard();
			}
		}), new KeyFrame(Duration.millis(100)));

		timeline.setCycleCount(Timeline.INDEFINITE);

		board.initBoard(0.5);

		Pane root = new Pane();

		Scene scene = new Scene(root, BOARD_SIZE, BOARD_SIZE);
		scene.getStylesheets().add("application/gol.css");

		// Create board with all dead cells
		for (int x = 0; x < BOARD_SIZE; x += WIDTH) {
			for (int y = 0; y < BOARD_SIZE; y += WIDTH) {
				StackPane cell = new StackPane();
				cell.setLayoutX(x);
				cell.setLayoutY(y);
				cell.setPrefHeight(HEIGHT);
				cell.setPrefWidth(WIDTH);
				cell.getStyleClass().add("dead-cell");

				root.getChildren().add(cell);

				// Store the cell in a HashMap for fast access
				// in the iterateBoard method.
				boardMap.put(x + "," + y, cell);
			}
		}

		primaryStage.setTitle("The Game of Life (RIP)");
		primaryStage.setScene(scene);
		primaryStage.show();

		timeline.play();
	}

	private void iterateBoard() {
		board.nextPopulation();
		for (int x = 0; x < board.getSize(); x++) {
			for (int y = 0; y < board.getSize(); y++) {
				StackPane pane = boardMap.get(x * WIDTH + "," + y * HEIGHT);
				pane.getStyleClass().clear();
				// If the cell at (x,y) is a alive use css styling 'alive-cell'
				// otherwise use the styling 'dead-cell'.
				if (board.getField(x, y) == 1) {
					pane.getStyleClass().add("alive-cell");
				} else {
					pane.getStyleClass().add("dead-cell");
				}
			}
		}
	}
}
