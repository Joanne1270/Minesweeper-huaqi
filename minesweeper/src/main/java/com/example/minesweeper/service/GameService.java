//package com.example.minesweeper.service;
//
//import com.example.minesweeper.model.Cell;
//import com.example.minesweeper.model.Game;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Random;
//import java.util.UUID;
//
//@Service
//public class GameService {
//    private final Map<String, Game> games = new HashMap<>();
//    private final Random random = new Random();
//
//    public Game createGame(String difficulty) {
//        int rows, cols, mines;
//
//        switch (difficulty.toLowerCase()) {
//            case "easy":
//                rows = 9;
//                cols = 9;
//                mines = 10;
//                break;
//            case "medium":
//                rows = 16;
//                cols = 16;
//                mines = 40;
//                break;
//            case "hard":
//                rows = 16;
//                cols = 30;
//                mines = 99;
//                break;
//            default:
//                rows = 9;
//                cols = 9;
//                mines = 10;
//        }
//
//        Game game = new Game(rows, cols, mines);
//        String gameId = UUID.randomUUID().toString();
//        game.setId(gameId);
//
//        // 首次点击后再生成地雷
//        games.put(gameId, game);
//        return game;
//    }
//
//    public Game getGame(String gameId) {
//        return games.get(gameId);
//    }
//
//    public Game revealCell(String gameId, int row, int col) {
//        Game game = games.get(gameId);
//        if (game == null || game.isGameOver() || game.isGameWon()) {
//            return game;
//        }
//
//        Cell cell = game.getBoard()[row][col];
//        if (cell.isRevealed() || cell.isFlagged()) {
//            return game;
//        }
//
//        // 首次点击：安全生成地雷，避开点击位置
//        if (!game.isMinesGenerated()) {
//            placeMinesSafe(game, row, col);
//            calculateAdjacentMines(game);
//            game.setMinesGenerated(true);
//        }
//
//        if (cell.isMine()) {
//            game.setGameOver(true);
//            game.setEndTime(System.currentTimeMillis());
//            revealAllMines(game);
//            return game;
//        }
//
//        revealCellRecursive(game, row, col);
//        checkWinCondition(game);
//
//        return game;
//    }
//
//    public Game toggleFlag(String gameId, int row, int col) {
//        Game game = games.get(gameId);
//        if (game == null || game.isGameOver() || game.isGameWon()) {
//            return game;
//        }
//
//        Cell cell = game.getBoard()[row][col];
//        if (!cell.isRevealed()) {
//            cell.setFlagged(!cell.isFlagged());
//        }
//
//        return game;
//    }
//
//    // 安全放雷：避开首次点击的格子和周边8格
//    private void placeMinesSafe(Game game, int safeRow, int safeCol) {
//        int minesPlaced = 0;
//        int rows = game.getRows();
//        int cols = game.getCols();
//
//        while (minesPlaced < game.getMines()) {
//            int r = random.nextInt(rows);
//            int c = random.nextInt(cols);
//
//            // 禁止在安全区域放雷
//            boolean isSafeArea = Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1;
//
//            if (!isSafeArea && !game.getBoard()[r][c].isMine()) {
//                game.getBoard()[r][c].setMine(true);
//                minesPlaced++;
//            }
//        }
//    }
//
//    // 原有放雷方法（保留）
//    private void placeMines(Game game) {
//        int minesPlaced = 0;
//        int rows = game.getRows();
//        int cols = game.getCols();
//
//        while (minesPlaced < game.getMines()) {
//            int row = random.nextInt(rows);
//            int col = random.nextInt(cols);
//
//            if (!game.getBoard()[row][col].isMine()) {
//                game.getBoard()[row][col].setMine(true);
//                minesPlaced++;
//            }
//        }
//    }
//
//    private void calculateAdjacentMines(Game game) {
//        int rows = game.getRows();
//        int cols = game.getCols();
//
//        for (int i = 0; i < rows; i++) {
//            for (int j = 0; j < cols; j++) {
//                if (!game.getBoard()[i][j].isMine()) {
//                    int count = 0;
//                    for (int di = -1; di <= 1; di++) {
//                        for (int dj = -1; dj <= 1; dj++) {
//                            int ni = i + di;
//                            int nj = j + dj;
//                            if (ni >= 0 && ni < rows && nj >= 0 && nj < cols && game.getBoard()[ni][nj].isMine()) {
//                                count++;
//                            }
//                        }
//                    }
//                    game.getBoard()[i][j].setAdjacentMines(count);
//                }
//            }
//        }
//    }
//
//    private void revealCellRecursive(Game game, int row, int col) {
//        int rows = game.getRows();
//        int cols = game.getCols();
//
//        if (row < 0 || row >= rows || col < 0 || col >= cols) {
//            return;
//        }
//
//        Cell cell = game.getBoard()[row][col];
//        if (cell.isRevealed() || cell.isFlagged() || cell.isMine()) {
//            return;
//        }
//
//        cell.setRevealed(true);
//
//        if (cell.getAdjacentMines() == 0) {
//            for (int di = -1; di <= 1; di++) {
//                for (int dj = -1; dj <= 1; dj++) {
//                    revealCellRecursive(game, row + di, col + dj);
//                }
//            }
//        }
//    }
//
//    private void revealAllMines(Game game) {
//        int rows = game.getRows();
//        int cols = game.getCols();
//
//        for (int i = 0; i < rows; i++) {
//            for (int j = 0; j < cols; j++) {
//                if (game.getBoard()[i][j].isMine()) {
//                    game.getBoard()[i][j].setRevealed(true);
//                }
//            }
//        }
//    }
//
//    private void checkWinCondition(Game game) {
//        int rows = game.getRows();
//        int cols = game.getCols();
//        int unrevealedSafeCells = 0;
//
//        for (int i = 0; i < rows; i++) {
//            for (int j = 0; j < cols; j++) {
//                Cell cell = game.getBoard()[i][j];
//                if (!cell.isRevealed() && !cell.isMine()) {
//                    unrevealedSafeCells++;
//                }
//            }
//        }
//
//        if (unrevealedSafeCells == 0) {
//            game.setGameWon(true);
//            game.setEndTime(System.currentTimeMillis());
//        }
//    }
//}

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
    private final Random random = new Random();
    // 8个方向偏移量
    private final int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
    private final int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

    public Game createGame(String difficulty) {
        int rows, cols, mines;

        switch (difficulty.toLowerCase()) {
            case "easy":
                rows = 9;
                cols = 9;
                mines = 10;
                break;
            case "medium":
                rows = 16;
                cols = 16;
                mines = 40;
                break;
            case "hard":
                rows = 16;
                cols = 30;
                mines = 99;
                break;
            default:
                rows = 9;
                cols = 9;
                mines = 10;
        }

        Game game = new Game(rows, cols, mines);
        String gameId = UUID.randomUUID().toString();
        game.setId(gameId);
        games.put(gameId, game);
        return game;
    }

    public Game getGame(String gameId) {
        return games.get(gameId);
    }

    public Game revealCell(String gameId, int row, int col) {
        Game game = games.get(gameId);
        if (game == null || game.isGameOver() || game.isGameWon()) {
            return game;
        }

        Cell cell = game.getBoard()[row][col];

        // ========== 核心新功能：点击已翻开数字格子 自动扩开周边 ==========
        if (cell.isRevealed()) {
            int flagCount = countAroundFlags(game, row, col);
            // 周围旗子数 等于 数字雷数，自动点开周围
            if (flagCount == cell.getAdjacentMines() && cell.getAdjacentMines() > 0) {
                autoOpenAround(game, row, col);
                checkWinCondition(game);
            }
            return game;
        }

        // 已经插旗 禁止点击
        if (cell.isFlagged()) {
            return game;
        }

        // 首次点击安全生成地雷（保留之前功能）
        if (!game.isMinesGenerated()) {
            placeMinesSafe(game, row, col);
            calculateAdjacentMines(game);
            game.setMinesGenerated(true);
        }

        // 踩到雷
        if (cell.isMine()) {
            game.setGameOver(true);
            game.setEndTime(System.currentTimeMillis());
            revealAllMines(game);
            return game;
        }

        // 普通翻开
        revealCellRecursive(game, row, col);
        checkWinCondition(game);
        return game;
    }

    // 统计当前格子周围8格旗子数量
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

    // 自动打开周围合法格子
    private void autoOpenAround(Game game, int x, int y) {
        int rows = game.getRows();
        int cols = game.getCols();
        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                Cell near = game.getBoard()[nx][ny];
                // 没翻开、没插旗 才自动点开
                if (!near.isRevealed() && !near.isFlagged()) {
                    if (near.isMine()) {
                        // 点错雷直接游戏结束
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

    // 安全放雷：避开首次点击区域
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

    private void placeMines(Game game) {
        int minesPlaced = 0;
        int rows = game.getRows();
        int cols = game.getCols();
        while (minesPlaced < game.getMines()) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            if (!game.getBoard()[row][col].isMine()) {
                game.getBoard()[row][col].setMine(true);
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