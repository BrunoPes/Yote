import java.rmi.Remote;
import java.rmi.RemoteException;

interface ClientRMI extends Remote {
    public void insertPiece(int player, int[] pos) throws RemoteException;
    public void killPiece(int player) throws RemoteException;
    public void multkillPiece(int player, int[] pos) throws RemoteException;
    public void removePiece(int player, int[] pos) throws RemoteException;
    public void movePiece(int[] oldPos, int[] newPos) throws RemoteException;
    public void changeTurn(int player) throws RemoteException;

    public void connected(int player) throws RemoteException;
    public void giveUpGame(int player) throws RemoteException;
    public void restartGame(int player) throws RemoteException;
    public void playerWin(int player) throws RemoteException;
    public void gameOver() throws RemoteException;

    public void updateChat(String msg) throws RemoteException;
    public void registerClient(String playerName, int id) throws RemoteException;
}