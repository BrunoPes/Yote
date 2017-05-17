import java.io.IOException;
import java.net.ConnectException;
import java.rmi.*;

import javax.swing.JOptionPane;

class Client extends UnicastRemoteObject implements ClientRMI {
	private String hostName;
	private String hostServer;
	private YoteGame clientGame;
	private ServerRMI server;

	public Client(YoteGame game, String playerName) {
		this.clientGame = game;
		this.hostName = playerName;

		try {
            Naming.rebind("//localhost/" + this.hostName, this);
            System.out.println("Servidor Registrado!");
        } catch(Exception e) {
            e.printStackTrace();
        }
	}

	public void getServerRMI(String host) {
		this.hostServer = host.length() == 0 ? "//localhost/YoteServer" : host;
		
		try {
			this.server = (ServerRMI) Naming.lookup(this.hostServer);
        	System.out.println("Objeto Localizado!");
			this.clientGame.requestFocus();
		} catch(ConnectException e) {
			String errorMsg = "Verifique se o servidor está online e se a URL inserida está correta";
			JOptionPane.showMessageDialog(this.clientGame, errorMsg, "Não foi possível conectar-se ao Servidor", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void chatMessageUpdate(String msg) {
		String chatMsg = (new MessageHelper(msg)).getChatMessage();
		if(chatMsg != null) this.clientGame.updateChat(chatMsg);
	}

	public void receivedMovement(String json) {
		MessageHelper jsonObj = new MessageHelper(json);
		String substr = json.indexOf("a:rg") >= 0 ? "rg" : (json.indexOf("a:wr") >= 0 ? "wr" : null);
		
		String move = substr != null ? substr : jsonObj.getAction();
		int player = jsonObj.getPlayer();
		int[] movedPos = jsonObj.getMovedPos();
		int[] killedPos = jsonObj.getKilledPos();

		this.clientGame.updateGame(player, move, movedPos, killedPos);
	}

	public void sendChatMsg(String msg) {
		try {
			this.output.writeUTF("s:"+msg);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void sendMovement(String move, int[] movedPiece, int[] lastMoved) {
		try {
			String last = lastMoved != null ? ",k:"+lastMoved[0]+""+lastMoved[1] : "";
			String moved = movedPiece != null ? ",m:"+movedPiece[0]+""+movedPiece[1] : "";
			this.output.writeUTF("a:"+move+moved+last);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
