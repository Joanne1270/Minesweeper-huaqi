package com.example.minesweeper.controller;

import com.example.minesweeper.model.Game;
import com.example.minesweeper.model.GameRequest;
import com.example.minesweeper.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @PostMapping("/new")
    public ResponseEntity<Game> newGame(@RequestBody GameRequest request) {
        Game game = gameService.createGame(
                request.getDifficulty(),
                request.getRows(),
                request.getCols(),
                request.getMines()
        );
        return ResponseEntity.ok(game);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    // 新增：撤回接口
    @PostMapping("/undo/{gameId}")
    public ResponseEntity<Game> undoGame(@PathVariable String gameId) {
        Game game = gameService.undoGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @PostMapping("/reveal/{gameId}/{row}/{col}")
    public ResponseEntity<Game> revealCell(@PathVariable String gameId,
                                           @PathVariable int row,
                                           @PathVariable int col) {
        Game game = gameService.revealCell(gameId, row, col);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @PostMapping("/flag/{gameId}/{row}/{col}")
    public ResponseEntity<Game> toggleFlag(@PathVariable String gameId,
                                           @PathVariable int row,
                                           @PathVariable int col) {
        Game game = gameService.toggleFlag(gameId, row, col);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }
}
