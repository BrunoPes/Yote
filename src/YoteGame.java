import java.util.*;
import java.awt.event.*;
import javafx.scene.image.ImageView;

class YoteGame implements ActionListener {
    private Piece selected;
    private Board board = new Board();

    public void YoteGame() {

    }

    public void movePiece(Piece piece, String move) {

    }

    @Override
    void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        String param = e.paramString();
        if(action == ""){
            if(param == "up") {
                int pos[] = this.selected.getPosition();
                Piece nearPiece = (pos[0] > 0) ? this.board.getPiece(pos[0]-1, pos[1]) : null;
                Piece farPiece  = (pos[0] > 1) ? this.board.getPiece(pos[0]-2, pos[1]) : null;
                if(pos[0] > 1) {
                    if(nearPiece == null) {
                        movePiece(, "up");
                    } else if(!(nearPiece.player.equals(this.selected.player)) && farPiece == null) {

                    }
                } else if(pos[0] > 0) {

                } else {
                    System.out.println("Jogada Invalida");
                }
            } else if(param == "down") {

            } else if(param == "left") {

            } else if(param == "right") {

            }
        }
    }
}

private class Board {
    private List<List<Piece>> boardMatrix = new ArrayList<List<Piece>>();
    private ImageView boardImg;

    public void Board() {
        for(int i=0; i<5; i++) {
            boardMatrix.add(new ArrayList<Piece>());
            for(int j=0; j<6; j++) {
                boardMatrix.get(i).add(null);
            }
        }
    }

    public void updateBoard() {

    }

    public Piece getPiece(int i, int j) {
        return this.boardMatrix.get(i).get(j);
    }

    public int[] getPiecePosition(Piece piece) {
        for(int i=0; i<5; i++) {
            for(int j=0; j<6; j++) {
                Piece auxPiece = this.boardMatrix.get(i).get(j);
                if(piece == auxPiece)
                    return {i, j};
            }
        }
        return {-1, -1};
    }
}

private class Piece {
    private ImageView pieceImg;
    private int i;
    private int j;
    private String player;

    public void Piece(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public int[] getPosition() {
        return {this.i, this.j};
    }

    public void updateI(int i) {
        this.i = i;
    }

    public void updateJ(int j) {
        this.j = j;
    }

    public void updatePosition(int i, int j) {
        this.i = i;
        this.j = j;
    }
}
