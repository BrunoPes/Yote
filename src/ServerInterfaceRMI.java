import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterfaceRMI extends Remote {
    public void movePiece(int[] oldPos, int[] newPos) throws RemoteException;
    public void insertPiece(String player, int[] pos) throws RemoteException;
    public void killPiece(String player, int[] pos) throws RemoteException;
    public void registerClient(String playerName, int id) throws RemoteException;
}
