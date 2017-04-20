package client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class ServerReaderThread extends Thread {	

}

class ServerThread extends Thread {
	static int port = 9090;
	private DataInputStream input  = null;		
	private DataOutputStream output = null;		
	private String message = "";
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	
	public void run() {
		try {
			while(true){
				this.message = this.input.readUTF();
				System.out.println("Mensagem recebida: " + message);
				
				output.writeUTF("REMOTE SERVER REPLY: Mensagem recebida com sucesso");
		  		output.flush();
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
  
	ServerThread() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Aguardando conexão...");
			socket = serverSocket.accept();
		  	System.out.println("Conexão Estabelecida.");
		  				
			this.input = new DataInputStream(this.socket.getInputStream());
		  	this.output = new DataOutputStream(this.socket.getOutputStream());
		  	this.start();
		} catch(Exception e){
			System.out.println(e);
		}
	}

	public static void main(String args[]) {
		new ServerThread();
	}
}
