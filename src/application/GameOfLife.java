package application;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameOfLife extends Application {

	private static final int MIN_WIDTH = 10;
	private static final int MIN_HEIGHT = 10;
	private static final int MAX_WIDTH = 100;
	private static final int MAX_HEIGHT = 100;
	private static final int WIDTH = 10;
	private static final int HEIGHT = WIDTH; // TODO: I think these can only be equal with current implementation
	private static final int BOARD_SIZE = 320;

	private Map<String, StackPane> boardMap = new HashMap<String, StackPane>();
	private Board board = new Board(BOARD_SIZE / WIDTH);
	private BorderPane borderPane;

	// Properties
	private double density = 0.5;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		final Timeline timeline = new Timeline();
		KeyFrame updateFrame = new KeyFrame(Duration.ZERO, (e -> iterateBoard()));
		KeyFrame delayFrame = new KeyFrame(Duration.millis(200));
		timeline.getKeyFrames().addAll(updateFrame, delayFrame);
		timeline.setCycleCount(Timeline.INDEFINITE);

		borderPane = new BorderPane(); // Main pain

		Node gameBoard = initializeGameBoard(); // UI

		board.initBoard(this.density);
		refreshGameBoard();

		Node properties = createPropertyLayout();
		HBox buttonBar = new HBox();
		Button playPauseButton = new Button("Play");
		Button restartButton = new Button("Restart");
		buttonBar.getChildren().addAll(playPauseButton, restartButton);

		borderPane.setCenter(gameBoard);
		borderPane.setBottom(buttonBar);
		borderPane.setLeft(properties);

		playPauseButton.setOnAction(e -> {
			if (playPauseButton.getText().contentEquals("Pause")) {
				timeline.stop();
				playPauseButton.setText("Play");
			} else {
				timeline.play();
				playPauseButton.setText("Pause");
			}
		});

		board.initBoard(this.density); // Random Board
		restartButton.setOnAction(e -> setupBoard());

		Scene scene = new Scene(borderPane);
		scene.getStylesheets().add("application/gol.css");

		primaryStage.setTitle("The Game of Life (RIP)");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private Node createPropertyLayout() {
		VBox properties = new VBox();

		// Size
		HBox sizeHbox = new HBox();
		Label sizeLabel = new Label("Size");
		TextField widthField = new TextField();
		TextField heightField = new TextField();
		widthField.setPromptText("width[" + MIN_WIDTH + "-" + MAX_WIDTH + "]");
		heightField.setPromptText("height[" + MIN_HEIGHT + "-" + MAX_HEIGHT + "]");
		sizeHbox.getChildren().addAll(sizeLabel, heightField, widthField);
		widthField.setMaxWidth(100);
		heightField.setMaxWidth(100);

		properties.getChildren().add(sizeHbox);

		// Density
		HBox densityHbox = new HBox();
		Label densityLabel = new Label("Density");
		TextField densityField = new TextField();
		densityField.setPromptText("0-100");
		densityField.setMaxWidth(50);

		// Validate key input:
		densityField.setOnKeyTyped(e -> {
			if (!e.getCharacter().matches("[0-9]"))
				e.consume();
		});
		heightField.setOnKeyTyped(e -> {
			if (!e.getCharacter().matches("[0-9]"))
				e.consume();
		});
		widthField.setOnKeyTyped(e -> {
			if (!e.getCharacter().matches("[0-9]"))
				e.consume();
		});

		// Validate property values:
		heightField.focusedProperty().addListener(
				(v, oldVal, newVal) -> validateIntField(heightField, oldVal, newVal, MIN_WIDTH, MAX_WIDTH));
		widthField.focusedProperty().addListener(
				(v, oldVal, newVal) -> validateIntField(widthField, oldVal, newVal, MIN_HEIGHT, MAX_HEIGHT));
		densityField.focusedProperty()
				.addListener((v, oldVal, newVal) -> validateIntField(densityField, oldVal, newVal, 0, 100));

		densityHbox.getChildren().addAll(densityLabel, densityField);
		properties.getChildren().add(densityHbox);

		// Apply
		Button applyButton = new Button("Apply");
		properties.getChildren().add(applyButton);

		// Apply button action
		applyButton.setOnAction(e -> {
			this.density = Integer.parseInt(densityField.getText()) / 100.0;
			System.out.println("density = " + this.density);
			this.board.initBoard(this.density);
			refreshGameBoard();
		});

		return properties;
	}

	private void validateIntField(TextField field, boolean oldVal, boolean newVal, int min, int max) {
		if (!newVal) { // Focus lost
			String newText = field.getText();
			try {
				int val = Integer.parseInt(newText);
				if (val >= min && val <= max) {
					return;
				}
			} catch (Exception e) {
			}
			field.setText("");
		}
	}

	// Initialize the graphical game board
	private Node initializeGameBoard() {
		Pane gameBoard = new Pane();
		gameBoard.setMinSize(BOARD_SIZE, BOARD_SIZE);
		// Create board with all dead cells
		for (int x = 0; x < BOARD_SIZE; x += WIDTH) {
			for (int y = 0; y < BOARD_SIZE; y += WIDTH) {
				StackPane cell = new StackPane();
				cell.setLayoutX(x);
				cell.setLayoutY(y);
				cell.setPrefHeight(HEIGHT);
				cell.setPrefWidth(WIDTH);
				cell.getStyleClass().add("dead-cell");
				gameBoard.getChildren().add(cell);

				cell.setOnMouseClicked(e -> toggleCell(cell));

				// Store the cell in a HashMap for fast access
				// in the iterateBoard method.
				boardMap.put(x + "," + y, cell);
			}
		}
		return gameBoard;
	}

	// setup the life board model using the populated board map GUI
	private void setupBoard() {
		for (int x = 0; x < board.getSize(); x++) {
			for (int y = 0; y < board.getSize(); y++) {
				StackPane cell = boardMap.get(x * WIDTH + "," + y * HEIGHT);
				if (cell.getStyleClass().contains("alive-cell")) {
					board.setField(x, y, 1);
				} else {
					board.setField(x, y, 0);
				}
			}
		}
	}
	// setup the game board gui based on the current life board model
	private void refreshGameBoard() {
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

	// toggle the Cells dead or alive status
	private void toggleCell(StackPane cell) {
		if (cell.getStyleClass().contains("dead-cell")) {
			cell.getStyleClass().clear();
			cell.getStyleClass().add("alive-cell");
		} else {
			cell.getStyleClass().clear();
			cell.getStyleClass().add("dead-cell");
		}
	}

	private void iterateBoard() {
		board.nextPopulation();
		refreshGameBoard();
	}
}
