import java.io.*;
import java.util.ArrayList;
import java.rmi.*;

class Server extends UnicastRemoteObject implements ServerRMI {
    private ArrayList<Client> clients = new ArrayList<Client>();
    private ServerBoard board = new ServerBoard();
    private int playerOfTurn = -1;
    private int[] playerPieces = {12,12};
    private boolean canMove = true;

    public Server() throws RemoteException {
    	// super();
        try {
            Naming.rebind("//localhost/YoteServer", this);
            System.out.println("Servidor Registrado!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void registerClient(String clientName, int id) {
    	this.clients.add((Client)Naming.lookup(clientName));
    	if(this.clients.length == 2) {
            for(Client client : this.clients)
                client.startGame();
    	}
    }

    public void waitClients(){
        try {
            while(true) {
                if(this.clientListeners.size() < 2) {
                    for(int i=0; i<2; i++) {
                        System.out.println("Aguardando conexão...");
                        this.acceptClient(i);
                        System.out.println("Conexão Estabelecida.");
                    }

                    this.playerOfTurn = 0;
                    this.sendGameUpdate(this.playerOfTurn, "t", null, null);
                } else {
                    int remove = 0;
                    for(ServerClientListener client : this.clientListeners) {
                        if(client == null || !client.isAlive()) {
                            remove++;
                        }
                    }
                    if(remove == 2) {
                        this.resetClientSockets();
                    }
                }
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void resetGameState() {
        this.playerOfTurn = -1;
        this.playerPieces = new int[]{12,12};
        this.canMove = true;
        this.board.resetBoard();
    }

    public void resetClientSockets() {
        for(ServerClientListener client : this.clientListeners) {
            client.closeClient();
        }
        this.outputs.clear();
        this.clientListeners.clear();
    }

    public int getPlayerOfTurn() {
        return playerOfTurn;
    }



    ///////////////////// AQUII ////////////////////

    public void insertPiece(int player, int[] pos) {
        if(this.playerPieces[player] > 0 && this.board.posIsEmpty(pos[0], pos[1])) {
            this.playerPieces[player]--;
            this.board.insertPieceOnBoard(player+1, pos);

            for(Client client : this.clients)
                client.insertPiece(player, pos);

            int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
            if(winState != -1) {
                this.resetGameState();
                for(Client client : this.clients)
                    client.playerWin(winState);
            } else {
                this.changeTurn();
            }
        } else {
            System.out.println("Jogada Inválida");
        }
    }

    public void testAndMovePiece(String move, int[] selected, int player) {
        int[] nearPiece = null;
        int i = selected[0];
        int j = selected[1];

        switch(move) {
            case "u": nearPiece = i > 0 ? new int[]{i-1, j} : null; break;
            case "d": nearPiece = i < 4 ? new int[]{i+1, j} : null; break;
            case "l": nearPiece = j > 0 ? new int[]{i, j-1} : null; break;
            case "r": nearPiece = j < 5 ? new int[]{i, j+1} : null; break;
            default: break;
        }

        if(this.board.canKillOnDirection(player, i, j, move)) {
            this.movePiece(move, selected, nearPiece, player);
        } else if(this.board.canMoveOnDirection(i, j, move) && this.canMove) {
            this.movePiece(move, selected, null, player);
        } else {
            System.out.println("Jogada Inválida");
        }
    }

    public void movePiece(String move, int[] movedPiece, int[] killedPiece, int player) {
        int[] oldPos = {movedPiece[0], movedPiece[1]};

        switch(move) {
            case "u": movedPiece[0] += killedPiece != null ? -2 : -1; break;
            case "d": movedPiece[0] += killedPiece != null ?  2 :  1; break;
            case "l": movedPiece[1] += killedPiece != null ? -2 : -1; break;
            case "r": movedPiece[1] += killedPiece != null ?  2 :  1; break;
            default: break;
        }

        this.board.updateBoard(oldPos, movedPiece, killedPiece);
        for(Client client : this.clients)
            client.movePiece(player, move, oldPos, killedPiece);

        int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
        if(winState != -1) {
            this.resetGameState();
            for(Client client : this.clients)
                client.playerWin(winState);
        } else {
            if(killedPiece == null || this.board.getInboardPlayerPieces(1-player) == 0) {
                this.changeTurn();
            } else {
                this.canMove = false;
                for(Client client : this.clients)
                    client.killPiece(this.playerOfTurn);
            }
        }
    }

    public void removePiece(int player, int[] pos) {
        int enemy = this.board.getPiece(remPos[0], remPos[1]);
        if(enemy != 0 && player != (enemy-1)) {
            this.board.removePiece(remPos[0], remPos[1]);
            for(Client client : this.clients)
                client.removePiece(player, pos);

            int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
            if(pos != null && this.board.getInboardPlayerPieces(1-player) > 0 && this.board.canKillOnDirection(player, pos[0], pos[1], null)) {
                for(Client client : this.clients)
                    client.multkillPiece(player);
            } else {
                if(winState != -1) {
                    this.resetGameState();
                    for(Client client : this.clients)
                        client.playerWin(winState);
                } else {
                    for(Client client : this.clients)
                        client.changeTurn(player);
                }
            }
        } else {
            System.out.println("Jogada Inválida");
        }
    }

    public void changeTurn() {
        this.canMove = true;
        this.playerOfTurn = 1 - this.playerOfTurn;
        for(Client client : this.clients)
            client.changeTurn(this.playerOfTurn);
    }

    public void giveUpGame(int player) {
        this.resetGameState();
        for(Client client : this.clients)
            client.giveUpGame(player);
    }

    public void restartGame(int player) {
        System.out.println("Reset");
        this.resetGameState();
        for(Client client : this.clients)
            client.restartGame(player);
        this.changeTurn();
    }

    public void finishGame(int player) {
        System.out.println("Restartando ok");
        this.board.printBoard();
        this.playerOfTurn = player;
        for(Client client : this.clients)
            client.gameOver(this.playerOfTurn);
    }

    public void updateChat(String msg) {
        for(Client client : this.clients)
            client.updateChat(msg);
    }

    public void registerClient(String playerName, int id) {

    }
    //////////////////// TÉRMINO ////////////////////

	public static void main(String args[]) {
        Server server = new Server();
	}
}

class ServerClientListener extends Thread {
    private int player;
    private Server server;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public ServerClientListener(Socket socket, DataInputStream input, DataOutputStream output, int playerNum, Server server) {
        this.player = playerNum;
        this.server = server;
        this.socket = socket;
        this.input = input;
        this.output = output;

        try{
            this.output.writeUTF("p:"+player+",a:c");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            this.handleClient();
        } catch (EOFException e) {
            System.out.println("Closed connection!");
            this.closeClient();
        } catch (IOException e) {
            e.printStackTrace();
            this.closeClient();
        }
    }

    public void handleClient() throws IOException {
        boolean flag = true;
        try {
            while(this.socket != null && this.socket.isConnected() && !this.socket.isClosed() && flag) {
                String msg = this.input.readUTF();
                if(msg.equals(".close.")) {
                    flag = false;
                } else {
                    //System.out.println("MSG: " + msg + "\nINDEX S: " + msg.indexOf("s:"));
                    if(msg.indexOf("s:") >= 0) {
                        this.server.sendChatUpdate(msg);
                    } else if((this.server.getPlayerOfTurn() == this.player && msg.indexOf("s:") < 0) || msg.indexOf("t:c") >= 0) {
                        this.server.receivedMessage(this.player, msg);
                    }
                }
            }
        } finally {
            this.closeClient();
        }
    }

    public void closeClient() {
        try {
            if(this.socket != null && this.socket.isConnected() && !socket.isClosed()) {
                if(this.input  != null) this.input.close();
                if(this.output != null) this.output.close();
                if(this.socket != null) this.socket.close();
            }
            this.input = null;
            this.output = null;
            this.socket = null;
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}

class ServerBoard {
    private int[][] boardMatrix = new int[5][6];

    public ServerBoard() {
        for(int i=0; i < 5; i++) {
            for(int j=0; j < 6; j++) {
                this.boardMatrix[i][j] = 0;
            }
        }
    }

    public void resetBoard() {
        for(int i=0; i < 5; i++) {
            for(int j=0; j < 6; j++) {
                this.boardMatrix[i][j] = 0;
            }
        }
    }

    public void printBoard(){
        System.out.println("Matriz: ");
        for(int i=0; i < 5; i++) {
            for(int j=0; j < 6; j++) {
                System.out.print(this.boardMatrix[i][j] + " ");
            }
            System.out.println("");
        }
    }

    public void updateBoard(int[] oldPos, int[] newPos, int[] killedPos) {
        int aux = this.boardMatrix[oldPos[0]][oldPos[1]];
        this.boardMatrix[newPos[0]][newPos[1]] = aux;
        this.boardMatrix[oldPos[0]][oldPos[1]] = 0;

        if(killedPos != null) {
            this.boardMatrix[killedPos[0]][killedPos[1]] = 0;
        }
    }

    public void insertPieceOnBoard(int piece, int[] pos) {
        this.boardMatrix[pos[0]][pos[1]] = piece;
    }

    public int getPiece(int i, int j) {
        return this.boardMatrix[i][j];
    }

    public void removePiece(int i, int j) {
        this.boardMatrix[i][j] = 0;
    }

    public int getInboardPlayerPieces(int player) {
        // int enemy = player == 0 ? (1+1) : (0+1);
        int number = 0;
        for(int i=0; i < 5; i++) {
            for(int j=0; j < 6; j++) {
                if(this.boardMatrix[i][j] == player+1) number++;
            }
        }
        return number;
    }

	public boolean canMoveOnDirection(int i, int j, String dir) {
		boolean u = i > 0 && this.posIsEmpty(i-1, j);
		boolean d = i < 4 && this.posIsEmpty(i+1, j);
		boolean l = j > 0 && this.posIsEmpty(i, j-1);
		boolean r = j < 5 && this.posIsEmpty(i, j+1);

		if(dir == null || dir.equals("")) return (u || d || l || r);
        switch(dir) {
		    case "u": return u;
		    case "d": return d;
		    case "l": return l;
			case "r": return r;
		    default: return false;
        }
    }

    public boolean canKillOnDirection(int player, int i, int j, String dir) {
        int enemy = player == 0 ? 1+1 : 0+1;
        boolean u =  i > 1 && this.getPiece(i-1, j) == enemy && this.posIsEmpty(i-2, j);
        boolean d =  i < 3 && this.getPiece(i+1, j) == enemy && this.posIsEmpty(i+2, j);
        boolean l =  j > 1 && this.getPiece(i, j-1) == enemy && this.posIsEmpty(i, j-2);
        boolean r =  j < 4 && this.getPiece(i, j+1) == enemy && this.posIsEmpty(i, j+2);
        if(dir == null || dir.equals("")) return (u || d || l || r);

        switch(dir) {
		    case "u": return u;
		    case "d": return d;
		    case "l": return l;
			case "r": return r;
		    default: return false;
        }
    }

    public boolean canMoveOrKillAny(int player, int i, int j) {
    	return this.canMoveOnDirection(i, j, null) || this.canKillOnDirection(player, i, j, null);
    }

	public boolean canPlayerMakeAnyKill(int player) {
    	for(int i=0; i<5; i++) {
    		for(int j=0; j<6; j++) {
    			int piece = this.boardMatrix[i][j];
    			if(piece == (player+1) && this.canKillOnDirection(player, i, j, null)) return true;
    		}
    	}
    	return false;
    }

    public boolean canPlayerMakeAnyMove(int player) {
    	for(int i=0; i<5; i++) {
    		for(int j=0; j<6; j++) {
    			int piece = this.boardMatrix[i][j];
    			if(piece == (player+1) && this.canMoveOrKillAny(player, i, j)) return true;
    		}
    	}
    	return false;
    }

    public boolean posIsEmpty(int i, int j) {
        return this.getPiece(i, j) == 0 ? true : false;
    }

    public boolean areDifferentPieces(int[] p1, int[] p2) {
        System.out.println("P1: " + this.boardMatrix[p1[0]][p1[1]] + " P2: " + this.boardMatrix[p2[0]][p2[1]]);
        return this.boardMatrix[p1[0]][p1[1]] != this.boardMatrix[p2[0]][p2[1]];
    }

    public int detectGameEnd(int piecesP1, int piecesP2) {
    	int boardP1 = this.getInboardPlayerPieces(0);
    	int boardP2 = this.getInboardPlayerPieces(1);

    	if(piecesP1 == 0 && (boardP1 == 0 || !this.canPlayerMakeAnyMove(0))) {
    		System.out.println("Winner 2");
    		return 1;
    	} else if(piecesP2 == 0 && (boardP2 == 0 || !this.canPlayerMakeAnyMove(1))) {
    		System.out.println("Winner 1");
    		return 0;
    	} else {
    		if(piecesP1 == 0 && piecesP2 == 0 && boardP1 <= 3 && boardP2 <= 3 && !this.canPlayerMakeAnyKill(0) && !this.canPlayerMakeAnyKill(1)) {
    			System.out.println("DRAW");
    			this.printBoard();
    			System.out.println("BP1: " + boardP1 + " BP2: " + boardP2);
    			return 2;
    		}
    	}

    	System.out.println("Goes on...");
    	return -1;
    }
}
