import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

class ClientThreadReader extends Thread {
	private DataInputStream input = null;
	private Socket socket = null;
	private String readMsg = "";
	private Client client;

	public ClientThreadReader(Socket socket, Client client) {
		this.socket = socket;
		this.client = client;

		try {
			this.input = new DataInputStream(this.socket.getInputStream());
			this.start();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public void run() {
		while (true) {
			try {
				this.readMsg = this.input.readUTF();
				System.out.println(this.readMsg);
				if(readMsg.indexOf("s:") >= 0) {
					this.client.chatUpdateMessage(this.readMsg);
				} else {
					this.client.receivedMovement(this.readMsg);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getLastRcvdMessage() {
		return this.readMsg;
	}
}

class Client {
	static String host = "";
	static int port = 9090;
	private Socket socket = null;
	private DataOutputStream output = null;
	private String sendMsg = "";
	private YoteGame clientGame;

	public Client(String[] args, YoteGame game) {
		this.initSocket(args);
		this.clientGame = game;
	}

	public void initSocket(String[] args) {
		host = args.length == 0 ? "localhost" : args[0];
		try{
			this.socket = new Socket(host, port);
			System.out.println("Conectado....");
			new ClientThreadReader(this.socket, this);
			this.output = new DataOutputStream(this.socket.getOutputStream());
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public String getLastSentMessage() {
		return this.sendMsg;
	}

	public void sendChatMsg(String msg) {
		try {
			this.output.writeUTF("s:"+msg);
		} catch(Exception e){
			System.out.println(e);
		}
	}

	public void chatUpdateMessage(String msg) {
		String chatMsg = (new MessageHelper(msg)).getChatMessage();
		if(chatMsg != null) this.clientGame.updateChat(chatMsg);
	}

	public void receivedMovement(String json) {
		MessageHelper jsonObj = new MessageHelper(json);
		int player = jsonObj.getPlayer();
		String move = jsonObj.getAction();
		int[] movedPos = jsonObj.getMovedPos();
		int[] killedPos = jsonObj.getKilledPos();

		this.clientGame.updateGame(player, move, movedPos, killedPos);
	}

	public void sendMovement(String move, int[] movedPiece) {
		try {
			this.output.writeUTF("a:"+move+",m:"+movedPiece[0]+""+movedPiece[1]);
		} catch(Exception e){
			System.out.println(e);
		}
	}
}
