import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

class Server extends UnicastRemoteObject implements ServerRMI {
    
    private static final long serialVersionUID = 1L;
    private ArrayList<ClientRMI> clients = new ArrayList<ClientRMI>();
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

    public void registerClient(String clientName) {
        try {
            this.clients.add((ClientRMI)Naming.lookup(clientName));
            this.clients.get(this.clients.size()-1).connected(this.clients.size()-1);
            if(this.clients.size() == 2) {
                this.playerOfTurn = 0;
                for(int i=0; i<2; i++) {
                    this.clients.get(i).changeTurn(this.playerOfTurn);
                }
            }   
        } catch(RemoteException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // public void waitClients(){
    //     try {
    //         while(true) {
    //             if(this.clientListeners.size() < 2) {
    //                 for(int i=0; i<2; i++) {
    //                     System.out.println("Aguardando conexão...");
    //                     this.acceptClient(i);
    //                     System.out.println("Conexão Estabelecida.");
    //                 }

    //                 this.playerOfTurn = 0;
    //                 this.sendGameUpdate(this.playerOfTurn, "t", null, null);
    //             } else {
    //                 int remove = 0;
    //                 for(ServerClientListener client : this.clientListeners) {
    //                     if(client == null || !client.isAlive()) {
    //                         remove++;
    //                     }
    //                 }
    //                 if(remove == 2) {
    //                     this.resetClientSockets();
    //                 }
    //             }
    //         }
    //     } catch(Exception e){
    //         System.out.println(e);
    //     }
    // }

    public void resetGameState() {
        this.playerOfTurn = -1;
        this.playerPieces = new int[]{12,12};
        this.canMove = true;
        this.board.resetBoard();
    }

    public void resetClientSockets() {

    }

    public int getPlayerOfTurn() {
        return playerOfTurn;
    }

    public void insertPiece(int player, int[] pos) {
        try {
            if(this.playerPieces[player] > 0 && this.board.posIsEmpty(pos[0], pos[1])) {
                this.playerPieces[player]--;
                this.board.insertPieceOnBoard(player+1, pos);

                for(ClientRMI client : this.clients)
                    client.insertPiece(player, pos);

                int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
                if(winState != -1) {
                    this.resetGameState();
                    for(ClientRMI client : this.clients)
                        client.playerWin(winState);
                } else {
                    this.changeTurn();
                }
            } else {
                System.out.println("Jogada Inválida");
            }
        } catch(RemoteException e) {
            e.printStackTrace();
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
        try {
            int[] oldPos = {movedPiece[0], movedPiece[1]};

            switch(move) {
                case "u": movedPiece[0] += killedPiece != null ? -2 : -1; break;
                case "d": movedPiece[0] += killedPiece != null ?  2 :  1; break;
                case "l": movedPiece[1] += killedPiece != null ? -2 : -1; break;
                case "r": movedPiece[1] += killedPiece != null ?  2 :  1; break;
                default: break;
            }

            this.board.updateBoard(oldPos, movedPiece, killedPiece);
            for(ClientRMI client : this.clients)
                client.movePiece(player, move, oldPos, killedPiece);

            int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
            if(winState != -1) {
                this.resetGameState();
                for(ClientRMI client : this.clients)
                    client.playerWin(winState);
            } else {
                if(killedPiece == null || this.board.getInboardPlayerPieces(1-player) == 0) {
                    this.changeTurn();
                } else {
                    this.canMove = false;
                    for(ClientRMI client : this.clients)
                        client.killPiece(this.playerOfTurn);
                }
            }
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    public void removePiece(int player, int[] pos, int[] remPos) {
        try {
            int enemy = this.board.getPiece(remPos[0], remPos[1]);
            if(enemy != 0 && player != (enemy-1)) {
                this.board.removePiece(remPos[0], remPos[1]);
                for(ClientRMI client : this.clients)
                    client.removePiece(player, remPos);

                int winState = this.board.detectGameEnd(this.playerPieces[0], this.playerPieces[1]);
                if(pos != null && this.board.getInboardPlayerPieces(1-player) > 0 && this.board.canKillOnDirection(player, pos[0], pos[1], null)) {
                    System.out.println("MULTKILL DO PLAYER: " + player);
                    for(ClientRMI client : this.clients)
                        client.multkillPiece(player);
                } else {
                    if(winState != -1) {
                        this.resetGameState();
                        for(ClientRMI client : this.clients)
                            client.playerWin(winState);
                    } else {
                        this.changeTurn();
                    }
                }
            } else {
                System.out.println("Jogada Inválida");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void changeTurn() {
        try {
            this.canMove = true;
            this.playerOfTurn = 1 - this.playerOfTurn;
            for(ClientRMI client : this.clients)
                client.changeTurn(this.playerOfTurn);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    public void giveUpGame(int player) {
        try{
            this.resetGameState();
            for(ClientRMI client : this.clients)
                client.giveUpGame(player);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    public void restartGame(int player) {
        try{
            System.out.println("Reset");
            this.resetGameState();
            this.playerOfTurn = 1 - player;
            for(ClientRMI client : this.clients)
                client.restartGame(this.playerOfTurn);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    public void finishGame(int player) {
        try{
            System.out.println("Restartando ok");
            this.board.printBoard();
            this.playerOfTurn = player;
            for(ClientRMI client : this.clients)
                client.gameOver(this.playerOfTurn);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateChat(String msg) {
        try{
            for(ClientRMI client : this.clients)
                client.updateChat(msg);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String args[]) {
        try{
            new Server();
        } catch(RemoteException e) {
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
