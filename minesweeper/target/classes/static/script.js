let currentGameId = null;
let timerInterval = null;
let elapsedTime = 0;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('new-game-btn').addEventListener('click', startNewGame);
    startNewGame();
});

async function startNewGame() {
    clearInterval(timerInterval);
    elapsedTime = 0;
    document.getElementById('timer').textContent = '0';
    document.getElementById('game-message').textContent = '';
    document.getElementById('game-message').className = 'game-message';

    const difficulty = document.getElementById('difficulty').value;

    try {
        const response = await fetch('/api/game/new', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ difficulty }),
        });

        if (!response.ok) {
            throw new Error('Failed to create new game');
        }

        const game = await response.json();
        currentGameId = game.id;

        renderBoard(game);
        updateMinesLeft(game);

        timerInterval = setInterval(() => {
            elapsedTime++;
            document.getElementById('timer').textContent = elapsedTime;
        }, 1000);

    } catch (error) {
        console.error('Error starting new game:', error);
        alert('Failed to start new game. Please try again.');
    }
}

// 🔥 完全适配你的后端：board二维数组 + isRevealed 字段
function renderBoard(game) {
    const boardElement = document.getElementById('game-board');
    boardElement.innerHTML = '';
    boardElement.style.gridTemplateColumns = `repeat(${game.cols}, 30px)`;

    for (let i = 0; i < game.rows; i++) {
        for (let j = 0; j < game.cols; j++) {
            // 用你后端的 board 二维数组（正确！）
            const cell = game.board[i][j];
            const cellElement = document.createElement('div');
            cellElement.className = 'cell';
            cellElement.dataset.row = i;
            cellElement.dataset.col = j;

            // 用你后端的 isRevealed（正确！）
            if (cell.revealed) {
                cellElement.classList.add('revealed');
                if (cell.mine) {
                    cellElement.classList.add('mine');
                } else if (cell.adjacentMines > 0) {
                    cellElement.textContent = cell.adjacentMines;
                    cellElement.dataset.count = cell.adjacentMines;
                }
            } else if (cell.flagged) {
                cellElement.classList.add('flagged');
            }

            cellElement.addEventListener('click', () => handleCellClick(i, j));
            cellElement.addEventListener('contextmenu', (e) => {
                e.preventDefault();
                handleRightClick(i, j);
            });

            boardElement.appendChild(cellElement);
        }
    }
}

async function handleCellClick(row, col) {
    if (!currentGameId) return;

    try {
        const response = await fetch(`/api/game/${currentGameId}/reveal/${row}/${col}`, {
            method: 'POST',
        });

        if (!response.ok) {
            throw new Error('Failed to reveal cell');
        }

        const game = await response.json();
        renderBoard(game);
        updateMinesLeft(game);

        if (game.gameOver) {
            endGame(false);
        } else if (game.gameWon) {
            endGame(true);
        }

    } catch (error) {
        console.error('Error revealing cell:', error);
    }
}

async function handleRightClick(row, col) {
    if (!currentGameId) return;

    try {
        const response = await fetch(`/api/game/${currentGameId}/flag/${row}/${col}`, {
            method: 'POST',
        });

        if (!response.ok) {
            throw new Error('Failed to toggle flag');
        }

        const game = await response.json();
        renderBoard(game);
        updateMinesLeft(game);

    } catch (error) {
        console.error('Error toggling flag:', error);
    }
}

// 修复：适配后端 board 二维数组
function updateMinesLeft(game) {
    let flaggedCount = 0;
    for (let i = 0; i < game.rows; i++) {
        for (let j = 0; j < game.cols; j++) {
            if (game.board[i][j].flagged) {
                flaggedCount++;
            }
        }
    }
    document.getElementById('mines-left').textContent = game.mines - flaggedCount;
}

function endGame(won) {
    clearInterval(timerInterval);

    const messageElement = document.getElementById('game-message');
    if (won) {
        messageElement.textContent = '🎉 You Win! 🎉';
        messageElement.className = 'game-message win';
    } else {
        messageElement.textContent = '💥 Game Over! 💥';
        messageElement.className = 'game-message lose';
    }
}