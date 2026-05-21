package com.example.minesweeper.service;

import com.example.minesweeper.model.Cell;
import com.example.minesweeper.model.Game;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class GameService {
    private final Map<String, Game> games = new HashMap<>();
    // 新增：保存每局最后一步的历史状态
    private final Map<String, Game> gameHistory = new HashMap<>();
    private final Random random = new Random();
    private final int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
    private final int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

    public Game createGame(String difficulty, int customRows, int customCols, int customMines) {
        int rows, cols, mines;

        if ("custom".equalsIgnoreCase(difficulty)) {
            rows = Math.max(5, Math.min(30, customRows));
            cols = Math.max(5, Math.min(30, customCols));
            int maxPossibleMines = rows * cols - 1;
            mines = Math.max(1, Math.min(maxPossibleMines, customMines));
        } else {
            switch (difficulty.toLowerCase()) {
                case "easy":
                    rows = 9; cols = 9; mines = 10;
                    break;
                case "medium":
                    rows = 16; cols = 16; mines = 40;
                    break;
                case "hard":
                    rows = 16; cols = 30; mines = 99;
                    break;
                default:
                    rows = 9; cols = 9; mines = 10;
            }
        }

        Game game = new Game(rows, cols, mines);
        String gameId = UUID.randomUUID().toString();
        game.setId(gameId);
        games.put(gameId, game);
        // 新游戏清空历史
        gameHistory.remove(gameId);
        return game;
    }

    public Game getGame(String gameId) {
        return games.get(gameId);
    }

    // 新增：撤回功能（恢复最后一步）
    public Game undoGame(String gameId) {
        Game historyGame = gameHistory.get(gameId);
        if (historyGame == null) return null;
        // 恢复历史状态
        games.put(gameId, historyGame);
        // 撤回后删除历史，防止多次撤回
        gameHistory.remove(gameId);
        return historyGame;
    }

    public Game revealCell(String gameId, int row, int col) {
        Game game = games.get(gameId);
        if (game == null || game.isGameOver() || game.isGameWon()) {
            return game;
        }

        // 每次点击前，保存当前状态到历史（用于撤回）
        gameHistory.put(gameId, deepCopyGame(game));

        Cell cell = game.getBoard()[row][col];

        if (cell.isRevealed()) {
            int flagCount = countAroundFlags(game, row, col);
            if (flagCount == cell.getAdjacentMines() && cell.getAdjacentMines() > 0) {
                autoOpenAround(game, row, col);
                checkWinCondition(game);
            }
            return game;
        }

        if (cell.isFlagged()) {
            return game;
        }

        if (!game.isMinesGenerated()) {
            placeMinesSafe(game, row, col);
            calculateAdjacentMines(game);
            game.setMinesGenerated(true);
        }

        if (cell.isMine()) {
            game.setGameOver(true);
            game.setEndTime(System.currentTimeMillis());
            revealAllMines(game);
            return game;
        }

        revealCellRecursive(game, row, col);
        checkWinCondition(game);
        return game;
    }

    // 新增：深拷贝游戏状态（防止引用覆盖）
    private Game deepCopyGame(Game original) {
        Game copy = new Game(original.getRows(), original.getCols(), original.getMines());
        copy.setId(original.getId());
        copy.setGameOver(original.isGameOver());
        copy.setGameWon(original.isGameWon());
        copy.setStartTime(original.getStartTime());
        copy.setEndTime(original.getEndTime());
        copy.setMinesGenerated(original.isMinesGenerated());

        Cell[][] originalBoard = original.getBoard();
        Cell[][] copyBoard = copy.getBoard();
        for (int i = 0; i < original.getRows(); i++) {
            for (int j = 0; j < original.getCols(); j++) {
                Cell origCell = originalBoard[i][j];
                Cell copyCell = copyBoard[i][j];
                copyCell.setMine(origCell.isMine());
                copyCell.setRevealed(origCell.isRevealed());
                copyCell.setFlagged(origCell.isFlagged());
                copyCell.setAdjacentMines(origCell.getAdjacentMines());
            }
        }
        return copy;
    }

    private int countAroundFlags(Game game, int x, int y) {
        int count = 0;
        int rows = game.getRows();
        int cols = game.getCols();
        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                if (game.getBoard()[nx][ny].isFlagged()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void autoOpenAround(Game game, int x, int y) {
        int rows = game.getRows();
        int cols = game.getCols();
        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                Cell near = game.getBoard()[nx][ny];
                if (!near.isRevealed() && !near.isFlagged()) {
                    if (near.isMine()) {
                        game.setGameOver(true);
                        game.setEndTime(System.currentTimeMillis());
                        revealAllMines(game);
                        return;
                    }
                    revealCellRecursive(game, nx, ny);
                }
            }
        }
    }

    public Game toggleFlag(String gameId, int row, int col) {
        Game game = games.get(gameId);
        if (game == null || game.isGameOver() || game.isGameWon()) {
            return game;
        }
        Cell cell = game.getBoard()[row][col];
        if (!cell.isRevealed()) {
            cell.setFlagged(!cell.isFlagged());
        }
        return game;
    }

    private void placeMinesSafe(Game game, int safeRow, int safeCol) {
        int minesPlaced = 0;
        int rows = game.getRows();
        int cols = game.getCols();
        while (minesPlaced < game.getMines()) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            boolean isSafeArea = Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1;
            if (!isSafeArea && !game.getBoard()[r][c].isMine()) {
                game.getBoard()[r][c].setMine(true);
                minesPlaced++;
            }
        }
    }

    private void calculateAdjacentMines(Game game) {
        int rows = game.getRows();
        int cols = game.getCols();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!game.getBoard()[i][j].isMine()) {
                    int count = 0;
                    for (int di = -1; di <= 1; di++) {
                        for (int dj = -1; dj <= 1; dj++) {
                            int ni = i + di;
                            int nj = j + dj;
                            if (ni >= 0 && ni < rows && nj >= 0 && nj < cols && game.getBoard()[ni][nj].isMine()) {
                                count++;
                            }
                        }
                    }
                    game.getBoard()[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    private void revealCellRecursive(Game game, int row, int col) {
        int rows = game.getRows();
        int cols = game.getCols();
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;
        Cell cell = game.getBoard()[row][col];
        if (cell.isRevealed() || cell.isFlagged() || cell.isMine()) return;
        cell.setRevealed(true);
        if (cell.getAdjacentMines() == 0) {
            for (int di = -1; di <= 1; di++) {
                for (int dj = -1; dj <= 1; dj++) {
                    revealCellRecursive(game, row + di, col + dj);
                }
            }
        }
    }

    private void revealAllMines(Game game) {
        int rows = game.getRows();
        int cols = game.getCols();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (game.getBoard()[i][j].isMine()) {
                    game.getBoard()[i][j].setRevealed(true);
                }
            }
        }
    }

    private void checkWinCondition(Game game) {
        int rows = game.getRows();
        int cols = game.getCols();
        int unrevealedSafeCells = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = game.getBoard()[i][j];
                if (!cell.isRevealed() && !cell.isMine()) {
                    unrevealedSafeCells++;
                }
            }
        }
        if (unrevealedSafeCells == 0) {
            game.setGameWon(true);
            game.setEndTime(System.currentTimeMillis());
        }
    }
}
