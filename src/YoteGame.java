import java.util.*;
import java.awt.event.*;
import javafx.scene.image.ImageView;

class YoteGame implements ActionListener {
    private Piece selected;
    private Board board = new Board();

    public void YoteGame() {

    }

    public void testAndMovePiece(Piece nearPiece, Piece farPiece, int axis, String move) {
        if(nearPiece != null && (axis>1 && (move.equals("u") || move.equals("l"))) ||
          (axis<4 && move.equals("r")) || (axis<3 && move.equals("d"))) {
            if(!(nearPiece.player.equals(this.selected.player)) && farPiece == null) {
                movePiece(move, this.selected, nearPiece);
            } else {
                System.out.println("Jogada Invalida");
            }
        } else if((axis>0 && (move.equals("u") || move.equals("l"))) || (axis<5 && move.equals("r")) || (axis<4 && move.equals("d"))) {
            if(nearPiece == null) {
                movePiece(move, this.selected, null);
            } else {
                System.out.println("Jogada Invalida");
            }
        } else {
            System.out.println("Jogada Invalida");
        }
    }

    public void movePiece(String move, Piece movedPiece, Piece killedPiece) {

    }

    @Override
    void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        String param = e.paramString();
        System.out.println("Action:  " + action + "\nParam:  " + param);
        if(action == ""){
            int i = this.selected.getPosition()[0];
            int j = this.selected.getPosition()[1];
            if(param == "up") {
                Piece nearPiece = i > 0 ? this.board.getPiece(i-1, j) : null;
                Piece farPiece  = i > 1 ? this.board.getPiece(i-2, j) : null;
                testAndMovePiece(nearPiece, farPiece, i, "u");
            } else if(param == "down") {
                Piece nearPiece = i < 4 ? this.board.getPiece(i+1, j) : null;
                Piece farPiece  = i < 3 ? this.board.getPiece(i+2, j) : null;
                testAndMovePiece(nearPiece, farPiece, i, "d");
            } else if(param == "left") {
                Piece nearPiece = j > 0 ? this.board.getPiece(i, j-1) : null;
                Piece farPiece  = j > 1 ? this.board.getPiece(i, j-2) : null;
                testAndMovePiece(nearPiece, farPiece, j, "l");
            } else if(param == "right") {
                Piece nearPiece = j < 5 ? this.board.getPiece(i, j+1) : null;
                Piece farPiece  = j < 4 ? this.board.getPiece(i, j+2) : null;
                testAndMovePiece(nearPiece, farPiece, j, "r");
            }
        }
    }
}

private class Board {
    private List<List<Piece>> boardMatrix = new ArrayList<List<Piece>>();
    private ImageView boardImg;

    public void Board() {
        for(int i=0; i < 5; i++) {
            boardMatrix.add(new ArrayList<Piece>());
            for(int j=0; j < 6; j++) {
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
