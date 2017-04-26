import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import javax.swing.JOptionPane;

class Client {
	private int port;
	private String host;
	private Socket socket = null;
	private DataOutputStream output = null;
	private DataInputStream input = null;
	private YoteGame clientGame;

	public Client(YoteGame game) {
		this.clientGame = game;
	}

	public void initSocket(String host, int port) {
		this.port = port;
		this.host = host.length() == 0 ? "localhost" : host;
		try{
			this.socket = new Socket(this.host, this.port);
			this.input = new DataInputStream(this.socket.getInputStream());
			System.out.println("Conectado....");
			new ClientServerListener(this.input, this);
			this.output = new DataOutputStream(this.socket.getOutputStream());
			this.clientGame.requestFocus();
		} catch(ConnectException e) {
			String errorMsg = "Verifique se o servidor está online e se o IP inserido está correto";
			//new JOptionPane(errorMsg, JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(this.clientGame, errorMsg, "Não foi possível conectar-se ao Servidor", JOptionPane.ERROR_MESSAGE);
			System.out.println("Connection refused?");
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void closeSocketClient() {
		this.sendCloseMsg();
		long now = System.currentTimeMillis();
		while(System.currentTimeMillis() - now < 1000);
		try {
			if(this.socket != null && this.socket.isConnected() && !this.socket.isClosed()) {
				this.input.close();
				this.output.close();
				this.socket.close();
			}
			this.input = null;
			this.output = null;
			this.socket = null;
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		this.clientGame.updateButtons(true);
	}

	public Socket getSocket() {
		return this.socket;
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

	public void sendCloseMsg() {
		try {
			this.output.writeUTF(".close.");
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void sendControlMsg(String action, String msg) {
		try {
			msg = msg != null ? msg : "";
			this.output.writeUTF("a:"+action+",t:c,"+msg);
		} catch(Exception e){
			e.printStackTrace();
		}
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

class ClientServerListener extends Thread {
	private DataInputStream input = null;
	private String readMsg = "";
	private Client client;

	public ClientServerListener(DataInputStream input, Client client) {
		this.input = input;
		this.client = client;

		try {
			this.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		Socket sock = this.client.getSocket();
		try {
			while(sock != null && this.input != null && sock.isConnected() && !sock.isClosed()) {
				if(!sock.isClosed()) {
					this.readMsg = this.input.readUTF();
					if(readMsg.indexOf("s:") >= 0) {
						this.client.chatMessageUpdate(this.readMsg);
					} else {
						this.client.receivedMovement(this.readMsg);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
