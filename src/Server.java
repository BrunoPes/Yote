import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class Server {
	static int port = 9090;
	private ServerSocket serverSocket = null;
	private ArrayList<DataOutputStream> outputs = new ArrayList<DataOutputStream>();
	private ServerBoard board = new ServerBoard();
	private int[] playerPieces = {12,12};
	private int playerOfTurn = -1;

	public Server() {
		try {
			serverSocket = new ServerSocket(port);
			for(int i=0; i<2; i++) {
				System.out.println("Aguardando conexão...");
				this.acceptClient(i);
			  	System.out.println("Conexão Estabelecida.");
			}
			
			this.playerOfTurn = 0;
			this.sendGameUpdate(this.playerOfTurn, "t", null, null);
		} catch(Exception e){
			System.out.println(e);
		}
	}
	
	public int getPlayerOfTurn() {
		return playerOfTurn;
	}

	public void acceptClient(int id) {
		try{
			Socket newSocket = this.serverSocket.accept();
			DataInputStream input = new DataInputStream(newSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(newSocket.getOutputStream());
			ServerClientListener client = new ServerClientListener(newSocket, input, output, id, this);
			client.start();
			this.outputs.add(output);			
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public void broadcastReceivedMessageResponse(int clientId) {
		for(DataOutputStream output : this.outputs) {
			try{
				output.writeUTF("REMOTE SERVER REPLY: O jogador id = " + clientId + " enviou uma msg!");
				output.flush();
			} catch (Exception e) {
				System.out.println(e);
			}

		}
	}
	
	public void receivedMessage(int player, String json) {
		//System.out.println("Player " + player + " -- " + json);
		MessageHelper jsonHelper = new MessageHelper(json);
		if(jsonHelper.getAction().equals("i")){
			this.testAndInsertPiece(player, jsonHelper.getMovedPos());
		} else {
			this.testAndMovePiece(jsonHelper.getAction(), jsonHelper.getMovedPos(), player);			
		}
	}

	public void testAndInsertPiece(int player, int[] pos) {
		if(this.playerPieces[player] > 0 && this.board.posIsEmpty(pos[0], pos[1])) {
	        this.playerPieces[player]--;
	        this.board.insertPieceOnBoard(player+1, pos);
	        this.sendGameUpdate(player, "i", pos, null);
	        // this.board.printBoard();
	        this.sendNextTurn(player);
	    } else {
	    	System.out.println("Jogada Invalida");
	    }
	}
	
    public void testAndMovePiece(String move, int[] selected, int player) {
    	int[] nearPiece = null;
    	int[] farPiece = null;
    	int i = selected[0];
    	int j = selected[1];
    	int axis = (move.equals("l") || move.equals("r")) ? j : i;
    	if(move.equals("l")) {
            nearPiece = j > 0 ? new int[]{i, j-1} : null;
            farPiece  = j > 1 ? new int[]{i, j-2} : null;
        } else if(move.equals("r")) {
            nearPiece = j < 5 ? new int[]{i, j+1} : null;
            farPiece  = j < 4 ? new int[]{i, j+2} : null;
        }else if(move.equals("u")) {
            nearPiece = i > 0 ? new int[]{i-1, j} : null;
            farPiece  = i > 1 ? new int[]{i-2, j} : null;
        } else if(move.equals("d")) {
            nearPiece = i < 4 ? new int[]{i+1, j} : null;
            farPiece  = i < 3 ? new int[]{i+2, j} : null;
        }
        if(nearPiece != null && !(this.board.posIsEmpty(nearPiece[0], nearPiece[1])) && 
          ((axis>1 && (move.equals("u") || move.equals("l"))) || (axis<4 && move.equals("r")) || (axis<3 && move.equals("d")))) {        	
            if(this.board.areDifferentPieces(selected, nearPiece) && this.board.posIsEmpty(farPiece[0], farPiece[1])) {
                this.movePiece(move, selected, nearPiece, player);
            } else {
                System.out.println("Jogada Invalida COMER");
            }
        } else if((axis>0 && (move.equals("u") || move.equals("l"))) || (axis<5 && move.equals("r")) || (axis<4 && move.equals("d"))) {
            if(this.board.posIsEmpty(nearPiece[0], nearPiece[1])) {
            	this.movePiece(move, selected, null, player);
            } else {
                System.out.println("Jogada Invalida ANDAR");
            }
        } else {
            System.out.println("Jogada Invalida UNKNOWN");
        }
    }

    public void movePiece(String move, int[] movedPiece, int[] killedPiece, int player) {
    	int[] oldPos = {movedPiece[0], movedPiece[1]};    	
        if(killedPiece != null) {
            if(move.equals("u")) movedPiece[0] += -2;
            else if(move.equals("d")) movedPiece[0] += 2;
            else if(move.equals("l")) movedPiece[1] += -2;
            else if(move.equals("r")) movedPiece[1] += 2;
        } else {
            if(move.equals("u")) movedPiece[0] += -1;
            else if(move.equals("d")) movedPiece[0] += 1;
            else if(move.equals("l")) movedPiece[1] += -1;
            else if(move.equals("r")) movedPiece[1] += 1;
        }       
        
        this.board.updateBoard(oldPos, movedPiece, killedPiece);
        // this.board.printBoard();
        this.sendGameUpdate(player, move, oldPos, killedPiece);
        this.sendNextTurn(player);
    }
	
    public void sendNextTurn(int nowPlayer) {
    	this.playerOfTurn = nowPlayer == 0 ? 1 : 0;
    	this.sendGameUpdate(this.playerOfTurn, "t", null, null);
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
    
	public static void main(String args[]) {
		new Server();
	}
}

class ServerClientListener extends Thread {
	private int player;	
	private Server server;
	private DataInputStream input;
	private DataOutputStream output;

	public ServerClientListener(Socket socket, DataInputStream input, DataOutputStream output, int playerNum, Server server) {
		this.player = playerNum;
		this.server = server;
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
			while(true) {				
				String msg = this.input.readUTF();
				if(this.server.getPlayerOfTurn() == this.player) {
					this.server.receivedMessage(this.player, msg);
				}				
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}

class ServerBoard {
    private int[][] boardMatrix = new int[5][6];

    public ServerBoard() {
        for(int i=0; i < 5; i++) {
            for(int j=0; j < 6; j++) {
                boardMatrix[i][j] = 0;
            }
        }
    }
    
    public void printBoard(){
    	System.out.println("Matriz: ");
    	for(int i=0; i < 5; i++) {    		
            for(int j=0; j < 6; j++) {
            	System.out.print(boardMatrix[i][j] + " ");
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

    public int getInboardPlayerPieces(int player) {
    	int number = 0;
    	for(int i=0; i < 5; i++) {
            for(int j=0; j < 6; j++) {
            	if(this.boardMatrix[i][j] == player) number++;
            }
        }
    	return number;
    }
    
    public boolean posIsEmpty(int i, int j) {
        return this.getPiece(i, j) == 0 ? true : false;
    }

    public boolean areDifferentPieces(int[] p1, int[] p2) {
    	System.out.println("P1: " + this.boardMatrix[p1[0]][p1[1]] + " P2: " + this.boardMatrix[p2[0]][p2[1]]);
    	return this.boardMatrix[p1[0]][p1[1]] != this.boardMatrix[p2[0]][p2[1]];
    }
}