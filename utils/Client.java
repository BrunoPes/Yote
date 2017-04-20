import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

class ClientThreadReader extends Thread {
	private DataInputStream input = null;
	private Socket socket = null;
	private String readMsg = "";

	public ClientThreadReader(Socket socket) {
		this.socket = socket;

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
				System.out.println(readMsg);
			} catch(Exception e) {
				System.out.println(e);
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

	public Client(String[] args) {
		this.initSocket(args);
		Scanner console = new Scanner(System.in);
		try {
			output = new DataOutputStream(this.socket.getOutputStream());
			while(!sendMsg.equals(".")) {
				System.out.println("Envie uma mensagem: ");
				String sendMsg = console.nextLine();
				this.output.writeUTF(sendMsg);
				this.output.flush();
			}
			this.output.close();
			console.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public void initSocket(String[] args) {
		host = args.length == 0 ? "localhost" : args[0];
		try{
			this.socket = new Socket(host, port);
			System.out.println("Conectado....");
			new ClientThreadReader(this.socket);
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public String getLastSentMessage() {
		return this.sendMsg;
	}

	public static void main(String[] args) {
		new Client(args);
	}
}
