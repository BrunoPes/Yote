import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JOptionPane;

class Client extends UnicastRemoteObject implements ClientRMI {
	private static final long serialVersionUID = 1L;
	private String hostName;
	private String hostServer;
	private ClientUI clientGame;
	private ServerRMI server;

	public Client(ClientUI game, String playerName) throws RemoteException {
		this.clientGame = game;
		this.hostName = "//localhost/" + playerName;

		try {
            Naming.rebind(this.hostName, this);
            System.out.println("Cliente "+ this.hostName +" Registrado!");
        } catch(Exception e) {
            e.printStackTrace();
        }

        this.lookupServer("");
	}

	public void lookupServer(String host) {
		this.hostServer = host.length() == 0 ? "//localhost/YoteServer" : host;

		try {
			this.server = (ServerRMI) Naming.lookup(this.hostServer);
        	System.out.println("Objeto Localizado!");
        	this.server.registerClient(this.hostName);
			this.clientGame.requestFocus();
		} catch(RemoteException e) {
			String errorMsg = "Verifique se o servidor está online e se a URL inserida está correta";
			JOptionPane.showMessageDialog(this.clientGame, errorMsg, "Não foi possí­vel conectar-se ao Servidor", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public ServerRMI getServer() {
		return this.server;
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