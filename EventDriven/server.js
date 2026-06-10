/**
 * ============================================================
 * SERVIDOR - Pedra, Papel e Tesoura (Arquitetura Event-Driven)
 * ============================================================
 *
 * Toda a comunicação é feita EXCLUSIVAMENTE por eventos Socket.IO.
 * O estado do jogo é mantido 100% no servidor, nunca no cliente.
 *
 * Fluxo de eventos:
 *  Cliente → Servidor: joinRoom | playerChoice | playAgain
 *  Servidor → Cliente: waitingOpponent | gameStart | waitingOpponentChoice | result | opponentDisconnected
 */

const express = require("express");
const http = require("http");
const { Server } = require("socket.io");
const path = require("path");

// ─── Configuração do servidor Express + Socket.IO ───────────────────────────
const app = express();
const httpServer = http.createServer(app);
const io = new Server(httpServer, {
  cors: { origin: "*" }, // Permite qualquer origem (desenvolvimento local)
});

// Serve o arquivo HTML estático
app.use(express.static(path.join(__dirname, "public")));

const PORT = process.env.PORT || 3000;

// ─── Estado global do jogo (em memória) ────────────────────────────────────
/**
 * Sala de espera: armazena o socket do primeiro jogador que conectou
 * e ainda não tem oponente.
 */
let waitingPlayer = null;

/**
 * Salas ativas: Map<roomId, Room>
 *
 * Estrutura de uma Room:
 * {
 *   id: string,
 *   players: [{ socket, playerNumber, choice }],
 *   roundInProgress: boolean
 * }
 */
const rooms = new Map();

// Contador simples para gerar IDs únicos de sala
let roomCounter = 0;

// ─── Lógica de negócio ──────────────────────────────────────────────────────

/**
 * Determina o vencedor de uma rodada.
 * @param {string} choice1 - Escolha do jogador 1
 * @param {string} choice2 - Escolha do jogador 2
 * @returns {"player1" | "player2" | "draw"}
 */
function determineWinner(choice1, choice2) {
  if (choice1 === choice2) return "draw";

  const winsAgainst = {
    pedra: "tesoura",   // Pedra vence Tesoura
    papel: "pedra",     // Papel vence Pedra
    tesoura: "papel",   // Tesoura vence Papel
  };

  return winsAgainst[choice1] === choice2 ? "player1" : "player2";
}

/**
 * Reseta as escolhas dos jogadores em uma sala para uma nova rodada.
 * @param {object} room - Objeto da sala
 */
function resetRound(room) {
  room.players.forEach((p) => (p.choice = null));
  room.roundInProgress = true;
}

// ─── Manipuladores de Eventos Socket.IO ─────────────────────────────────────

io.on("connection", (socket) => {
  console.log(`[CONNECT] Socket conectado: ${socket.id}`);

  // ── Evento: joinRoom ──────────────────────────────────────────────────────
  /**
   * Disparado quando o cliente solicita entrar em uma sala.
   * Se já existe um jogador aguardando → cria a sala e inicia a partida.
   * Caso contrário → coloca este jogador na fila de espera.
   */
  socket.on("joinRoom", () => {
    console.log(`[EVENT] joinRoom recebido de ${socket.id}`);

    if (waitingPlayer && waitingPlayer.id !== socket.id) {
      // ── Dois jogadores disponíveis: cria uma sala ──────────────────────
      const roomId = `room-${++roomCounter}`;

      const room = {
        id: roomId,
        players: [
          { socket: waitingPlayer, playerNumber: 1, choice: null },
          { socket: socket, playerNumber: 2, choice: null },
        ],
        roundInProgress: true,
      };

      rooms.set(roomId, room);

      // Coloca ambos os sockets no canal da sala
      waitingPlayer.join(roomId);
      socket.join(roomId);

      // Limpa a fila de espera
      waitingPlayer = null;

      console.log(`[ROOM] Sala criada: ${roomId}`);

      // ── Evento emitido: gameStart ──────────────────────────────────────
      /**
       * Informa a cada jogador que a partida começou e qual é o seu número.
       * O cliente usa isso para saber sua identidade durante o jogo.
       */
      room.players.forEach((player) => {
        player.socket.emit("gameStart", {
          roomId,
          playerNumber: player.playerNumber,
          message: `Partida iniciada! Você é o Jogador ${player.playerNumber}.`,
        });
      });

      console.log(`[EVENT] gameStart emitido para sala ${roomId}`);
    } else {
      // ── Apenas um jogador: aguarda oponente ────────────────────────────
      waitingPlayer = socket;

      // ── Evento emitido: waitingOpponent ───────────────────────────────
      /**
       * Informa ao cliente que ele está aguardando um segundo jogador.
       */
      socket.emit("waitingOpponent", {
        message: "Aguardando oponente...",
      });

      console.log(`[EVENT] waitingOpponent emitido para ${socket.id}`);
    }
  });

  // ── Evento: playerChoice ──────────────────────────────────────────────────
  /**
   * Disparado quando o cliente envia sua escolha ("pedra", "papel" ou "tesoura").
   * Quando ambos os jogadores escolherem, o servidor calcula o resultado.
   */
  socket.on("playerChoice", ({ roomId, choice }) => {
    console.log(`[EVENT] playerChoice recebido: ${socket.id} escolheu "${choice}" na sala ${roomId}`);

    const validChoices = ["pedra", "papel", "tesoura"];
    const room = rooms.get(roomId);

    // Validações de segurança: nunca confiar no cliente
    if (!room) {
      console.warn(`[WARN] Sala ${roomId} não encontrada.`);
      return;
    }
    if (!validChoices.includes(choice)) {
      console.warn(`[WARN] Escolha inválida recebida: "${choice}"`);
      return;
    }
    if (!room.roundInProgress) {
      console.warn(`[WARN] Rodada não está em progresso na sala ${roomId}`);
      return;
    }

    // Encontra o jogador que enviou a escolha
    const player = room.players.find((p) => p.socket.id === socket.id);
    if (!player) return;

    // Registra a escolha (estado mantido no servidor)
    player.choice = choice;
    console.log(`[STATE] Jogador ${player.playerNumber} escolheu "${choice}" na sala ${roomId}`);

    const [p1, p2] = room.players;

    if (!p1.choice || !p2.choice) {
      // ── Um jogador escolheu, aguarda o outro ───────────────────────────

      // ── Evento emitido: waitingOpponentChoice ─────────────────────────
      /**
       * Informa ao jogador que já escolheu que está aguardando o oponente.
       */
      socket.emit("waitingOpponentChoice", {
        message: "Você escolheu! Aguardando o oponente...",
      });

      console.log(`[EVENT] waitingOpponentChoice emitido para ${socket.id}`);
    } else {
      // ── Ambos escolheram: calcula o resultado ──────────────────────────
      const winner = determineWinner(p1.choice, p2.choice);
      room.roundInProgress = false;

      let resultMessage;
      if (winner === "draw") {
        resultMessage = "Empate! 🤝";
      } else {
        const winnerNumber = winner === "player1" ? 1 : 2;
        resultMessage = `Jogador ${winnerNumber} venceu! 🏆`;
      }

      // ── Evento emitido: result ─────────────────────────────────────────
      /**
       * Enviado para AMBOS os jogadores com as escolhas e o resultado.
       * Cada cliente recebe os dados completos para renderizar o estado final.
       */
      io.to(roomId).emit("result", {
        player1Choice: p1.choice,
        player2Choice: p2.choice,
        winner,          // "player1" | "player2" | "draw"
        message: resultMessage,
      });

      console.log(`[EVENT] result emitido para sala ${roomId}: ${resultMessage}`);
    }
  });

  // ── Evento: playAgain ─────────────────────────────────────────────────────
  /**
   * Disparado quando o cliente solicita jogar novamente.
   * Conta os votos; quando ambos solicitarem, reinicia a rodada.
   */
  socket.on("playAgain", ({ roomId }) => {
    console.log(`[EVENT] playAgain recebido de ${socket.id} na sala ${roomId}`);

    const room = rooms.get(roomId);
    if (!room || room.roundInProgress) return;

    const player = room.players.find((p) => p.socket.id === socket.id);
    if (!player) return;

    // Marca que este jogador quer jogar novamente
    player.wantsPlayAgain = true;

    const bothWantToPlay = room.players.every((p) => p.wantsPlayAgain);

    if (bothWantToPlay) {
      // Reseta o estado para nova rodada
      room.players.forEach((p) => {
        p.choice = null;
        p.wantsPlayAgain = false;
      });
      room.roundInProgress = true;

      // ── Evento emitido: gameStart (nova rodada) ────────────────────────
      /**
       * Reutiliza o evento gameStart para sinalizar o início de uma nova rodada,
       * mantendo os mesmos números de jogador.
       */
      room.players.forEach((p) => {
        p.socket.emit("gameStart", {
          roomId,
          playerNumber: p.playerNumber,
          message: `Nova rodada! Você é o Jogador ${p.playerNumber}.`,
        });
      });

      console.log(`[EVENT] gameStart (nova rodada) emitido para sala ${roomId}`);
    } else {
      // Informa quem está esperando
      socket.emit("waitingOpponentChoice", {
        message: "Aguardando o oponente aceitar a revanche...",
      });
    }
  });

  // ── Evento: disconnect ────────────────────────────────────────────────────
  /**
   * Disparado automaticamente pelo Socket.IO quando um cliente desconecta.
   * Notifica o oponente e limpa a sala.
   */
  socket.on("disconnect", () => {
    console.log(`[DISCONNECT] Socket desconectado: ${socket.id}`);

    // Remove da fila de espera se for o jogador esperando
    if (waitingPlayer && waitingPlayer.id === socket.id) {
      waitingPlayer = null;
      console.log(`[STATE] Jogador removido da fila de espera`);
      return;
    }

    // Procura em todas as salas ativas
    for (const [roomId, room] of rooms.entries()) {
      const playerIndex = room.players.findIndex((p) => p.socket.id === socket.id);

      if (playerIndex !== -1) {
        const opponent = room.players.find((p) => p.socket.id !== socket.id);

        if (opponent) {
          // ── Evento emitido: opponentDisconnected ───────────────────────
          /**
           * Informa ao jogador restante que o oponente saiu.
           * O cliente deve exibir uma mensagem e liberar a interface.
           */
          opponent.socket.emit("opponentDisconnected", {
            message: "Seu oponente desconectou. Aguardando novo oponente...",
          });

          console.log(`[EVENT] opponentDisconnected emitido para ${opponent.socket.id}`);

          // Coloca o oponente restante de volta na fila de espera
          opponent.socket.leave(roomId);
          waitingPlayer = opponent.socket;
        }

        // Remove a sala
        rooms.delete(roomId);
        console.log(`[ROOM] Sala ${roomId} removida`);
        break;
      }
    }
  });
});

// ─── Inicialização ───────────────────────────────────────────────────────────
httpServer.listen(PORT, () => {
  console.log(`\n🚀 Servidor rodando em http://localhost:${PORT}`);
  console.log(`   Abra duas abas no navegador para jogar!\n`);
});
