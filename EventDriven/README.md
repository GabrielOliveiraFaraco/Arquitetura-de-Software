# Pedra, Papel e Tesoura — Como rodar este projeto

Este repositório contém um jogo multiplayer simples (Pedra, Papel e Tesoura) usando Express + Socket.IO com arquitetura orientada a eventos.

**Requisitos**
- Node.js (v14+ recomendado)
- npm (vem junto com Node.js)

**Instalação**
1. Abra um terminal na pasta do projeto (onde estão `package.json` e `server.js`).
2. Instale as dependências:

```bash
npm install
```

**Rodando localmente**
- Modo padrão (produção/deploy local):

```bash
npm start
# ou
node server.js
```

- Modo desenvolvimento:

```bash
npm run dev
```

O servidor usa por padrão a porta `3000`. Abra no navegador:

http://localhost:3000

Abra duas abas para jogar em pares.

**Alterar a porta (opcional)**
- Linux / macOS:

```bash
PORT=5000 npm start
```

- Windows (CMD):

```cmd
set PORT=5000 && npm start
```

- Windows (PowerShell):

```powershell
$env:PORT=5000; npm start
```

**Arquivos importantes**
- `server.js` — servidor Express + Socket.IO (porta padrão: 3000)
- `public/index.html` — cliente estático

**Solução de problemas**
- Erro: `npm: command not found` — instale o Node.js (https://nodejs.org/).
- Erro: porta já em uso — defina a variável `PORT` conforme mostrado acima.
- Conexões não começam — verifique o console do servidor e do navegador (DevTools) para mensagens de Socket.IO.

**Próximos passos / notas**
- Em produção, considere configurar CORS mais restrito e persistência de estado (ex.: Redis) se necessário.
- Testes automatizados não estão incluídos neste repositório.

---

Se quiser, posso adicionar instruções para executar em Docker ou um script `start:prod` mais completo.
