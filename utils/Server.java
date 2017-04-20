import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class ServerClientListener extends Thread {
	private int id;
	private Server server;
	private Socket clientSocket;
	private DataInputStream input;
//	private DataOutputStream output;

	public ServerClientListener(Socket socket, DataInputStream input, DataOutputStream output, int id, Server server) {
		this.id = id;
		this.server = server;
		this.input = input;
//		this.output = output;
		this.clientSocket = socket;
	}

	public void run() {
		byte[] buffer = new byte[1000];
		try {
			while(true){
				if((this.input.read(buffer)) > 0) {
					String str = new String(buffer);
					System.out.println("Player "+this.id +": "+ str.substring(2));
				}

				//System.out.println("Esperando próxima msg...");
				buffer = new byte[1000];
				this.server.broadcastReceivedMessageResponse(this.id);
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}

class Server {
	static int port = 9090;
	private ServerSocket serverSocket = null;
	private ArrayList<DataOutputStream> outputs = new ArrayList<DataOutputStream>();

	public Server() {
		try {
			serverSocket = new ServerSocket(port);
			for(int i=0; i<2; i++) {
				System.out.println("Aguardando conexão...");
				this.acceptClient(i);
			  	System.out.println("Conexão Estabelecida.");
			}
		} catch(Exception e){
			System.out.println(e);
		}
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

	public void receivedMessage(Socket clientSocket, byte[] message, int clientId) {
		System.out.println(message);
		this.broadcastReceivedMessageResponse(clientId);
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

	public static void main(String args[]) {
		new Server();
	}
}
