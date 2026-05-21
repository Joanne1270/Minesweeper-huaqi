package com.example.minesweeper.model;

public class Game {
    private String id;
    private int rows;
    private int cols;
    private int mines;
    private Cell[][] board;
    private boolean gameOver;
    private boolean gameWon;
    private long startTime;
    private long endTime;
    private boolean minesGenerated;

    public Game(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.board = new Cell[rows][cols];
        this.gameOver = false;
        this.gameWon = false;
        this.minesGenerated = false;
        // 初始化棋盘
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.board[i][j] = new Cell();
            }
        }
    }

    // Getter 和 Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getMines() { return mines; }

    public Cell[][] getBoard() { return board; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public boolean isGameWon() { return gameWon; }
    public void setGameWon(boolean gameWon) { this.gameWon = gameWon; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public boolean isMinesGenerated() { return minesGenerated; }
    public void setMinesGenerated(boolean minesGenerated) { this.minesGenerated = minesGenerated; }
}
