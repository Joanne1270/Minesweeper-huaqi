let currentGameId = null;
let timerInterval = null;
let elapsedTime = 0;
let isFirstClick = true;
let isGameOver = false;
let isCustomMode = false;
let hasUsedUndo = false;

// 背景图片切换（常态 / 胜利 / 失败）
function setBgImage(type) {
    const bg = document.getElementById("bg-switch");
    if (!bg) return;
    if (type === "win") {
        bg.src = "/img/win.png";
    } else if (type === "lose") {
        bg.src = "/img/lose.png";
    } else {
        bg.src = "/img/default.png";
    }
}

const spinStyle = document.createElement('style');
spinStyle.innerHTML = `
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
    width: 22px !important;
    height: 28px !important;
}
`;
document.head.appendChild(spinStyle);

// 关闭弹窗 → 切回常态图
function closeGamePopup() {
    const pop = document.querySelector('div[style*="position: fixed"][style*="transform: translate"]');
    if (pop) pop.remove();
    setBgImage("default");
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('new-game-btn').addEventListener('click', startNewGame);
    document.getElementById('clear-stats-btn').addEventListener('click', clearCurrentModeStats);
    startNewGame();
});

function clearCurrentModeStats() {
    const mode = document.getElementById('difficulty').value;
    if (mode === 'custom') {
        alert("Custom mode does not save records!");
        return;
    }
    localStorage.removeItem(`ms_${mode}_games`);
    localStorage.removeItem(`ms_${mode}_wins`);
    localStorage.removeItem(`ms_${mode}_bestTime`);
    alert("All records in the current mode have been cleared!");
}

async function startNewGame() {
    closeGamePopup();
    clearInterval(timerInterval);
    elapsedTime = 0;
    isFirstClick = true;
    isGameOver = false;
    isCustomMode = false;
    hasUsedUndo = false;
    setBgImage("default"); // 新游戏 → 常态图
    document.getElementById('timer').textContent = '0';
    document.getElementById('game-message').textContent = '';
    const difficulty = document.getElementById('difficulty').value;

    if (difficulty !== 'custom') {
        try {
            const res = await fetch('/api/game/new', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ difficulty })
            });
            const game = await res.json();
            currentGameId = game.id;
            renderBoard(game);
            updateMinesLeft(game);
        } catch (e) {
            console.error(e);
        }
        return;
    }

    isCustomMode = true;
    let customBox = document.createElement('div');
    customBox.id = 'customSettingBox';
    customBox.innerHTML = `
    <div style="position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);background:#fff;padding:28px 30px;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,0.15);z-index:999999;">
        <h4 style="margin:0 0 20px;text-align:center;color:#222;font-size:17px;font-weight:bold;">Custom Game Settings</h4>
        <div style="margin:15px 0;display:flex;align-items:center;gap:12px;">
            <label style="font-size:15px;width:75px;">Rows :</label>
            <input id="inRows" type="number" value="9" min="5" max="30" style="width:120px;height:36px;padding:0 10px;border:1px solid #ddd;border-radius:6px;font-size:15px;outline:none;">
        </div>
        <div style="margin:15px 0;display:flex;align-items:center;gap:12px;">
            <label style="font-size:15px;width:75px;">Columns :</label>
            <input id="inCols" type="number" value="9" min="5" max="30" style="width:120px;height:36px;padding:0 10px;border:1px solid #ddd;border-radius:6px;font-size:15px;outline:none;">
        </div>
        <div style="margin:15px 0;display:flex;align-items:center;gap:12px;">
            <label style="font-size:15px;width:75px;">Mines :</label>
            <input id="inMines" type="number" value="10" min="1" style="width:120px;height:36px;padding:0 10px;border:1px solid #ddd;border-radius:6px;font-size:15px;outline:none;">
        </div>
        <div style="margin-top:30px;text-align:center;">
            <button id="confirmCustom" style="padding:9px 26px;background:#27ae60;color:#fff;border:none;border-radius:6px;cursor:pointer;margin-right:15px;font-size:15px;">Confirm</button>
            <button id="cancelCustom" style="padding:9px 26px;background:#95a5a6;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:15px;">Cancel</button>
        </div>
    </div>
    `;
    document.body.appendChild(customBox);

    document.getElementById('cancelCustom').onclick = () => {
        customBox.remove();
    };

    document.getElementById('confirmCustom').onclick = async () => {
        const rows = parseInt(document.getElementById('inRows').value);
        const cols = parseInt(document.getElementById('inCols').value);
        const mines = parseInt(document.getElementById('inMines').value);

        if (isNaN(rows) || rows < 5 || rows > 30) {
            alert("Error: Rows must be between 5 and 30!");
            return;
        }
        if (isNaN(cols) || cols < 5 || cols > 30) {
            alert("Error: Columns must be between 5 and 30!");
            return;
        }
        if (isNaN(mines) || mines < 1 || mines >= rows * cols) {
            alert(`Error: Mines must be between 1 and ${rows * cols - 1}!`);
            return;
        }

        customBox.remove();
        try {
            const res = await fetch('/api/game/new', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    difficulty: "custom",
                    rows: rows,
                    cols: cols,
                    mines: mines
                })
            });
            const game = await res.json();
            currentGameId = game.id;
            renderBoard(game);
            updateMinesLeft(game);
        } catch (e) {
            console.error(e);
            alert("Failed to create custom game!");
        }
    };
}

function renderBoard(game) {
    const board = document.getElementById('game-board');
    board.innerHTML = '';
    board.style.gridTemplateColumns = `repeat(${game.cols}, 1fr)`;
    for (let i = 0; i < game.rows; i++) {
        for (let j = 0; j < game.cols; j++) {
            const cell = document.createElement('div');
            cell.className = 'cell';
            cell.dataset.row = i;
            cell.dataset.col = j;
            const gameCell = game.board[i][j]; // 修正原代码的变量名笔误

            // 渲染格子状态（和原来一致）
            if (gameCell.revealed) {
                cell.classList.add('revealed');
                if (gameCell.mine) {
                    cell.classList.add('mine');
                } else if (gameCell.adjacentMines > 0) {
                    cell.innerText = gameCell.adjacentMines;
                    cell.dataset.count = gameCell.adjacentMines;
                }
            } else if (gameCell.flagged) {
                cell.classList.add('flagged');
            }

            // 电脑端事件（完全不变）
            cell.addEventListener('click', () => handleOpen(i, j));
            cell.addEventListener('contextmenu', e => {
                e.preventDefault(); // 阻止右键菜单
                handleFlag(i, j);
            });

            // --- 新增：手机端长按插旗、短按打开 ---
            let pressTimer;
            let pressStartTime;
            cell.addEventListener('touchstart', (e) => {
                e.preventDefault(); // 阻止浏览器默认行为（如长按菜单）
                pressStartTime = Date.now();
                // 按下超过300ms触发插旗
                pressTimer = setTimeout(() => {
                    handleFlag(i, j);
                }, 300);
            });

            cell.addEventListener('touchend', () => {
                clearTimeout(pressTimer); // 清除长按定时器
                const pressDuration = Date.now() - pressStartTime;
                // 按下时间<300ms → 触发打开格子
                if (pressDuration < 300) {
                    handleOpen(i, j);
                }
            });

            board.appendChild(cell);
        }
    }
}

async function handleOpen(r, c) {
    if (!currentGameId || isGameOver) return;
    try {
        const res = await fetch(`/api/game/reveal/${currentGameId}/${r}/${c}`, {
            method: 'POST'
        });
        const game = await res.json();
        if (isFirstClick && !game.gameOver && !game.gameWon) {
            isFirstClick = false;
            timerInterval = setInterval(() => {
                elapsedTime++;
                document.getElementById('timer').innerText = elapsedTime;
            }, 1000);
        }
        renderBoard(game);
        updateMinesLeft(game);
        if (game.gameOver) {
            setTimeout(() => endGame(false), 200);
        }
        if (game.gameWon) {
            endGame(true);
        }
    } catch (e) {
        console.error(e);
    }
}

async function handleFlag(r, c) {
    if (!currentGameId || isGameOver) return;
    try {
        const res = await fetch(`/api/game/flag/${currentGameId}/${r}/${c}`, {
            method: 'POST'
        });
        const game = await res.json();
        renderBoard(game);
        updateMinesLeft(game);
    } catch (e) {
        console.error(e);
    }
}

// 撤回 → 切回常态图
async function handleUndo() {
    if (!currentGameId || hasUsedUndo) return;
    hasUsedUndo = true;
    isGameOver = false;

    try {
        const res = await fetch(`/api/game/undo/${currentGameId}`, {
            method: 'POST'
        });
        const game = await res.json();
        closeGamePopup();
        renderBoard(game);
        updateMinesLeft(game);
        setBgImage("default");

        if (!isFirstClick && elapsedTime > 0) {
            clearInterval(timerInterval);
            timerInterval = setInterval(() => {
                elapsedTime++;
                document.getElementById('timer').innerText = elapsedTime;
            }, 1000);
        }
    } catch (e) {
        console.error(e);
    }
}

function updateMinesLeft(game) {
    let flag = 0;
    game.board.forEach(row => row.forEach(cell => {
        if (cell.flagged) flag++;
    }));
    document.getElementById('mines-left').innerText = game.mines - flag;
}

function endGame(win) {
    if (isGameOver) return;
    isGameOver = true;

    // 胜利/失败 → 切换对应图片
    if (win) {
        setBgImage("win");
    } else {
        setBgImage("lose");
    }

    if (win || hasUsedUndo) {
        clearInterval(timerInterval);
    }

    if (isCustomMode) {
        const resultDiv = document.createElement('div');
        resultDiv.style.cssText = `
            position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
            background: white; padding: 20px 25px; border-radius: 8px;
            z-index: 999999; font-size: 13px; text-align: center;
            box-shadow: 0 3px 15px rgba(0,0,0,0.2); min-width: 220px;
        `;
        let undoBtn = '';
        if (!win && !hasUsedUndo) {
            undoBtn = `<button onclick="handleUndo()" style="
                padding: 7px 22px; background: #1677ff; color: white; border: none;
                border-radius: 5px; cursor: pointer; font-size: 14px; font-weight: bold; margin-left:10px;">Undo</button>`;
        }
        resultDiv.innerHTML = `
            <p style="margin:0; color:#333; font-size:0.85rem;">
                ${win ? 'Congrats! You Win!' : 'Sorry, You Lose！'}&nbsp;&nbsp; Time: ${elapsedTime}s<br>
            </p>
            <div style="margin-top:15px; display:flex; justify-content:center; gap:10px;">
                <button onclick="closeGamePopup()" style="
                    padding:7px 22px; background:#4CAF50; color:white; border:none;
                    border-radius:5px; cursor:pointer; font-size:14px; font-weight:bold;">Confirm</button>
                ${undoBtn}
            </div>
        `;
        document.body.appendChild(resultDiv);
        return;
    }

    const mode = document.getElementById('difficulty').value;
    let games = parseInt(localStorage.getItem(`ms_${mode}_games`) || "0");
    let wins = parseInt(localStorage.getItem(`ms_${mode}_wins`) || "0");
    let bestTime = parseInt(localStorage.getItem(`ms_${mode}_bestTime`) || "0");
    let isNewRecord = false;

    if (win || (hasUsedUndo && !win)) {
        games += 1;
        if (win) {
            wins += 1;
            if (bestTime === 0 || elapsedTime < bestTime) {
                bestTime = elapsedTime;
                isNewRecord = true;
                localStorage.setItem(`ms_${mode}_bestTime`, bestTime);
            }
        }
        localStorage.setItem(`ms_${mode}_games`, games);
        localStorage.setItem(`ms_${mode}_wins`, wins);
    }

    const winRate = games > 0 ? ((wins / games) * 100).toFixed(1) : "0.0";
    let stats = `<br><br>Game Count：${games} <br>Win Rate：${winRate}%`;
    const bestTimeText = bestTime > 0 ? `${bestTime}s` : "Failed";
    stats += `<br>Current Mode's Shortest Record：${bestTimeText}`;
    if (win && isNewRecord) {
        stats += `<br><br><span style="font-size:0.8rem; color:#1677ff;">Congratulations! Your clearance time is the shortest record in the current mode!</span>`;
    }

    const resultDiv = document.createElement('div');
    resultDiv.style.cssText = `
        position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
        background: white; padding: 20px 25px; border-radius: 8px;
        z-index: 999999; font-size: 13px; text-align: center;
        box-shadow: 0 3px 15px rgba(0,0,0,0.2); min-width: 220px;
    `;

    let undoBtn = '';
    if (!win && !hasUsedUndo) {
        undoBtn = `<button onclick="handleUndo()" style="
            padding: 7px 22px; background: #1677ff; color: white; border: none;
            border-radius: 5px; cursor: pointer; font-size: 14px; font-weight: bold; margin-left:10px;">Undo</button>`;
    }

    resultDiv.innerHTML = `
        <p style="margin: 0 0 15px 0; color: #333; font-size: 0.85rem;">
            ${win ? 'Congrats! You Win!' : 'Sorry, You Lose！'}&nbsp;&nbsp; Time: ${elapsedTime}s
            ${stats}
        </p>
        <div style="display:flex; justify-content:center; gap:10px;">
            <button onclick="closeGamePopup()" style="
                padding: 7px 22px; background: #4CAF50; color: white; border: none;
                border-radius: 5px; cursor: pointer; font-size: 14px; font-weight: bold;">Confirm</button>
            ${undoBtn}
        </div>
    `;
    document.body.appendChild(resultDiv);
}
