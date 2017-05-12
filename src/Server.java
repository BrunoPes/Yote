import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class Server {
    static int port = 9090;
    private ServerSocket serverSocket = null;
    private ArrayList<DataOutputStream> outputs = new ArrayList<DataOutputStream>();
    private ArrayList<ServerClientListener> clientListeners = new ArrayList<ServerClientListener>();
    private ServerBoard board = new ServerBoard();
    private int playerOfTurn = -1;
    private int[] playerPieces = {12,12};
    private boolean canMove = true;

    public Server() {
        try{
            this.serverSocket = new ServerSocket(port);
        } catch(IOException e) {
            e.printStackTrace();
        }

        this.waitClients();
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

    // public void acceptClient(int id) {
    //     try{
    //         Socket newSocket = this.serverSocket.accept();
    //         DataInputStream input = new DataInputStream(newSocket.getInputStream());
    //         DataOutputStream output = new DataOutputStream(newSocket.getOutputStream());
    //         ServerClientListener client = new ServerClientListener(newSocket, input, output, id, this);
    //         this.outputs.add(output);
    //         this.clientListeners.add(client);
    //         client.start();
    //     } catch(Exception e) {
    //         System.out.println(e);
    //     }
    // }

    public void receivedMessage(int player, String json) {
        MessageHelper jsonHelper = new MessageHelper(json);
        String action = jsonHelper.getAction();
        if(action.equals("i")){
            this.testAndInsertPiece(player, jsonHelper.getMovedPos());
        } else if(action.equals("k")) {
            this.sendRemovePiece(player, jsonHelper.getMovedPos(), jsonHelper.getKilledPos());
        } else if(action.equals("t")) {
            this.sendNextTurn(player);
        } else if(action.equals("g")) {
            this.sendFinishGame(player, "g");
        } else if(json.indexOf("a:rg,") >= 0) {
            System.out.println("Reset");
            this.sendFinishGame(player, "rg");
            this.sendNextTurn(player);
        } else if(json.indexOf("a:wr,") >= 0) {
        	System.out.println("Restartando ok");
        	this.board.printBoard();
        	this.playerOfTurn = player;
        	this.sendGameUpdate(this.playerOfTurn, "wr", null, null);
        } else {
            this.testAndMovePiece(jsonHelper.getAction(), jsonHelper.getMovedPos(), player);
        }
    }

    public void testAndInsertPiece(int player, int[] pos) {
        if(this.playerPieces[player] > 0 && this.board.posIsEmpty(pos[0], pos[1])) {
            this.playerPieces[player]--;
            this.board.insertPieceOnBoard(player+1, pos);

            int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
            this.sendGameUpdate(player, "i", pos, null);
            if(winState != -1) {
            	this.sendFinishGame(winState, "w");
            } else {
            	this.sendNextTurn(player);
            }
        } else {
            System.out.println("Jogada Invalida");
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
        this.sendGameUpdate(player, move, oldPos, killedPiece);
        int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
        if(winState != -1) {
        	this.sendFinishGame(winState, "w");
        } else {
        	if(killedPiece == null || this.board.getInboardPlayerPieces(1-player) == 0) {
                this.sendNextTurn(player);
            } else {
                this.sendKillPiece(player);
            }
        }
    }

    public void sendFinishGame(int player, String type) {
        this.resetGameState();
        this.sendGameUpdate(player, type, null, null);
    }

    public void sendNextTurn(int nowPlayer) {
        this.canMove = true;
        this.playerOfTurn = nowPlayer == 0 ? 1 : 0;
        this.sendGameUpdate(this.playerOfTurn, "t", null, null);
    }

    public void sendKillPiece(int player) {
        this.canMove = false;
        this.sendGameUpdate(this.playerOfTurn, "k", null, null);
    }

    public void sendRemovePiece(int player, int[] pos, int[] remPos) {
        int enemy = this.board.getPiece(remPos[0], remPos[1]);
        if(enemy != 0 && player != (enemy-1)) {
            this.board.removePiece(remPos[0], remPos[1]);
            this.sendGameUpdate(player, "e", remPos, null);

            int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
            if(pos != null && this.board.getInboardPlayerPieces(1-player) > 0 && this.board.canKillOnDirection(player, pos[0], pos[1], null)) {
            	this.sendGameUpdate(player, "m", null, null);
            } else {
            	if(winState != -1) this.sendFinishGame(winState, "w");
            	else this.sendNextTurn(player);
            }
        } else {
            System.out.println("Jogada Invalida");
        }
    }

    public void sendGameUpdate(int player, String action, int[] movedPiece, int[] killedPiece) {
        for(DataOutputStream output : outputs) {
            try{
                String moveMsg = movedPiece != null ? new String(",m:"+movedPiece[0]+""+movedPiece[1]) : "";
                String killMsg = killedPiece != null ? new String(",k:"+killedPiece[0]+""+killedPiece[1]) : "";
                output.writeUTF("p:"+player + ",a:"+ action + moveMsg + killMsg);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendChatUpdate(String msg) {
        for(DataOutputStream output : outputs) {
            try{
                output.writeUTF(msg);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        new Server();
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
