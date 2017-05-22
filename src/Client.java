import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class Client extends UnicastRemoteObject implements ClientRMI {
	private static final long serialVersionUID = 1L;
	private String playerName;
	private String hostServer;
	private ClientUI clientGame;
	private ServerRMI server;

	public Client(ClientUI game, String hostName, String playerName) throws RemoteException {
		this.clientGame = game;
		this.playerName = playerName;

		try {
            Naming.rebind("//localhost/" +this.playerName, this);
            System.out.println("Cliente //localhost/"+ this.playerName +" Registrado!");
            this.lookupServer(hostName);
        } catch(Exception e) {
            e.printStackTrace();
        }

        
	}

	public void lookupServer(String host) {
		this.hostServer = host.length() == 0 ? "//localhost/YoteServer" : "//localhost/" + host;

		try {
			this.server = (ServerRMI) Naming.lookup(this.hostServer);
        	System.out.println("Objeto Localizado!");
        	this.server.registerClient(this.playerName);
			this.clientGame.requestFocus();
		} catch(RemoteException e) {
			this.updateChat("ERRO DE CONEXÃO: Verifique se o nome servidor está correto e se ele está online");
			e.printStackTrace();
		} catch(ClassCastException e) {
			this.updateChat("ERRO DE CONEXÃO: Servidor não encontrado");
			e.printStackTrace();
		} catch(MalformedURLException e) {
			this.updateChat("ERRO DE CONEXÃO: Servidor não encontrado");
			e.printStackTrace();
		} catch(NotBoundException e) {
			this.updateChat("ERRO DE CONEXÃO: Servidor não encontrado");
			e.printStackTrace();
		}
	}
	
	public void closeConnection(int player) {
		try {
			if(player >= 0) {
				this.server.resetClients(player);
			}
		} catch(RemoteException e){
			e.printStackTrace();
		}
		this.server = null;	
		this.hostServer = "";
	}

	public ServerRMI getServer() {
		return this.server;
	}
	
	public String getName() {
		return this.playerName;
	}

	public void updateChat(String msg) {
		if(msg != null) this.clientGame.updateChat(msg);
	}

	public void insertPiece(int player, int[] pos) {
		this.clientGame.insertPiece(player, pos);
	}

	public void killPiece(int player) {
		this.clientGame.killPiece(player);
	}

	public void multkillPiece(int player) {
		this.clientGame.multkillPiece(player);
	}

	public void removePiece(int player, int[] pos) {
		this.clientGame.removePiece(player, pos);
	}

	public void movePiece(int player, String move, int[] oldPos, int[] killedPiece) {
		this.clientGame.movePiece(player, move, oldPos, killedPiece);
	}

	public void changeTurn(int player) {
		this.clientGame.changeTurn(player);
	}

	public void connected(int player) {
		this.clientGame.connected(player);
	}

	public void giveUpGame(int player) {
		this.clientGame.finishGame(-1, player, true);
	}

	public void restartGame(int player) {
		this.clientGame.restartGame(player);
	}

	public void playerWin(int player) {
		System.out.println("Player WON!:" + player);
		this.clientGame.playerWin(player);
	}

	public void gameOver(int player) {
		this.clientGame.gameOver(player);
	}
}