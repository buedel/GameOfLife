package application;

import java.util.Random;

public class Board {

	private int[][] board;
	private int sizeX;
	private int sizeY;

	public Board(int sizeX, int sizeY) {
		board = new int[sizeX][sizeY];
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	public void setField(int x, int y, int value) {
		board[x][y] = value;
	}

	public int getField(int x, int y) {
		return board[x][y];
	}

	public int getSizeX() {
		return this.sizeX;
	}

	public int getSizeY() {
		return this.sizeY;
	}

	public void initBoard(int[][] newBoard) {
		this.board = newBoard;
	}

	public void initBoard(double density) {
		Random random = new Random();
		for (int x = 0; x < this.sizeX; x++) {
			for (int y = 0; y < this.sizeY; y++) {
				if (random.nextDouble() > density) {
					this.board[x][y] = 0;
				} else {
					this.board[x][y] = 1;
				}
			}
		}
	}

	// Return the number of neighbors for a given cell
	private int numNeighbors(int x, int y) {

		// how to get to the eight neighbors
		int[] indexX = { -1, 0, 1, -1, 1, -1, 0, 1 };
		int[] indexY = { 1, 1, 1, 0, 0, -1, -1, -1 };

		// count the neighbors
		int neighbors = 0; // Count the neighbors
		for (int i = 0; i < 8; i++) {
			if (x + indexX[i] >= 0 && y + indexY[i] >= 0 && x + indexX[i] < this.sizeX && y + indexY[i] < this.sizeY) {
				neighbors += getField(x + indexX[i], y + indexY[i]);
			}
		}
		return neighbors;
	}

	// Return the new value for a given cell
	private int getNewCell(int x, int y) {

		int retval = board[x][y]; // get cell to check
		int neighbors = numNeighbors(x, y); // Count the neighbors

		if (neighbors < 2) {
			// if less than 2 neighbors die
			retval = 0;
		} else if (neighbors == 3) {
			// if 3 alive neighbors rebirth
			retval = 1;
		} else if (neighbors > 3) {
			// if more than 3 neighbors, starve (die)
			retval = 0;
		}

		return retval;
	}

	public void nextPopulation() {
		int[][] newBoard = new int[this.sizeX][this.sizeY];
		for (int x = 0; x < this.sizeX; x++) {
			for (int y = 0; y < this.sizeY; y++) {
				newBoard[x][y] = getNewCell(x, y);
			}
		}
		this.initBoard(newBoard);
	}
}
