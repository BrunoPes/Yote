package client;
import java.net.*;
import java.io.*;
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

class ClientThreadWriter {
	private DataOutputStream output = null;
	private String sendMsg = "";
	private Socket socket = null;

	public ClientThreadWriter(Socket socket) {
		this.socket = socket;
		Scanner console = new Scanner(System.in);			
		
		try {
			output = new DataOutputStream(this.socket.getOutputStream());
			while(!sendMsg.equals(".")) {
				System.out.println("Envie uma mensagem: ");
				String sendMsg = console.nextLine();
				this.output.writeUTF(sendMsg);
				this.output.flush();
			}
			console.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public String getLastSentMessage() {
		return this.sendMsg;
	}
}

public class ClientThread {
	static String host = "";
	static int port = 9090;
	static Socket socket = null;

	@SuppressWarnings("unused")
	public static void main(String args[]) {
		host = args.length == 0 ? "localhost" : args[0];
		try{
			socket = new Socket(host, port);
			System.out.println("Conectado....");			
			ClientThreadReader reader = new ClientThreadReader(socket);
			ClientThreadWriter writer = new ClientThreadWriter(socket);
		} catch(Exception e) {
			
		}
				
		
	}
}