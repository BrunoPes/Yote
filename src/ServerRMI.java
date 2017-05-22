import java.rmi.Remote;
import java.rmi.RemoteException;

interface ServerRMI extends Remote {
    public void insertPiece(int player, int[] pos) throws RemoteException;
    public void removePiece(int player, int[] pos, int[] remPos) throws RemoteException;
    public void testAndMovePiece(String move, int[] selected, int player) throws RemoteException;
    public void changeTurn() throws RemoteException;

    public void giveUpGame(int player) throws RemoteException;
    public void restartGame(int player) throws RemoteException;
    public void finishGame(int player) throws RemoteException;

    public void resetClients(int player) throws RemoteException;
    public void registerClient(String playerName) throws RemoteException;
    public void updateChat(String msg) throws RemoteException;
}
