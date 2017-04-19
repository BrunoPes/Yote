import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;

class YoteGame implements ActionListener, KeyListener {
    private Piece selected;
    private Board board = new Board();

    public YoteGame(int width, int height) {
        JFrame window = new JFrame("Time to play Yoté Game!");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.addKeyListener(this);
        window.setLayout(new GridLayout(5, 6));
//      window.setSize(width, height);

        for(int i=0; i<30; i++){
            window.add(new JButton("vazia"));
        }

        window.setVisible(true);
    }

    public void testAndMovePiece(Piece nearPiece, Piece farPiece, int axis, String move) {
        if(nearPiece != null && (axis>1 && (move.equals("u") || move.equals("l"))) ||
          (axis<4 && move.equals("r")) || (axis<3 && move.equals("d"))) {
            if(!(nearPiece.getColor().equals(this.selected.getColor())) && farPiece == null) {
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
        if(killedPiece != null) {
            if(move == "u") movedPiece.incrementI(-2);
            else if(move == "d") movedPiece.incrementI(2);
            else if(move == "l") movedPiece.incrementJ(-2);
            else if(move == "r") movedPiece.incrementJ(2);
        } else {
            if(move == "u") movedPiece.incrementI(-1);
            else if(move == "d") movedPiece.incrementI(1);
            else if(move == "l") movedPiece.incrementJ(-1);
            else if(move == "r") movedPiece.incrementJ(1);
        }
        this.board.updateBoard(movedPiece, killedPiece);
    }

    public static void main(String[] args) {
        new YoteGame(800, 550);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
//        String param = e.paramString();
        System.out.println("Action:  " + action);
    }

    public void keyPressed(KeyEvent e) {
        System.out.println("PRESSED!! "+e.getKeyCode());
        int keyCode = e.getKeyCode();
        System.out.println("SelectED : " + selected);
        if(this.selected != null) {
            int i = this.selected.getPosition()[0];
            int j = this.selected.getPosition()[1];
            if(keyCode == 37) {
                Piece nearPiece = j > 0 ? this.board.getPiece(i, j-1) : null;
                Piece farPiece  = j > 1 ? this.board.getPiece(i, j-2) : null;
                testAndMovePiece(nearPiece, farPiece, j, "l");
            } else if(keyCode == 39) {
                Piece nearPiece = j < 5 ? this.board.getPiece(i, j+1) : null;
                Piece farPiece  = j < 4 ? this.board.getPiece(i, j+2) : null;
                testAndMovePiece(nearPiece, farPiece, j, "r");
            }else if(keyCode == 38) {
                Piece nearPiece = i > 0 ? this.board.getPiece(i-1, j) : null;
                Piece farPiece  = i > 1 ? this.board.getPiece(i-2, j) : null;
                testAndMovePiece(nearPiece, farPiece, i, "u");
            } else if(keyCode == 40) {
                Piece nearPiece = i < 4 ? this.board.getPiece(i+1, j) : null;
                Piece farPiece  = i < 3 ? this.board.getPiece(i+2, j) : null;
                testAndMovePiece(nearPiece, farPiece, i, "d");
            }
        }
    }
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}

class Board {
    private List<List<Piece>> boardMatrix = new ArrayList<List<Piece>>();

    public Board() {
        for(int i=0; i < 5; i++) {
            boardMatrix.add(new ArrayList<Piece>());
            for(int j=0; j < 6; j++) {
                boardMatrix.get(i).add(null);
            }
        }
    }

    public void updateBoard(Piece movedPiece, Piece killedPiece) {
        int[] oldMovedPos = this.getPieceBoardPosition(movedPiece);
        int[] newMovedPos = movedPiece.getPosition();

        if(oldMovedPos!= null && oldMovedPos[0] != newMovedPos[0] || oldMovedPos[1] != newMovedPos[1]) {
            this.boardMatrix.get(newMovedPos[0]).set(newMovedPos[1], movedPiece);
        }

        if(killedPiece != null) {
            int[] killedPos = killedPiece.getPosition();
            this.boardMatrix.get(killedPos[0]).set(killedPos[1], null);
        }
    }

    public Piece getPiece(int i, int j) {
        return this.boardMatrix.get(i).get(j);
    }

    public int[] getPieceBoardPosition(Piece piece) {
        for(int i=0; i<5; i++) {
            for(int j=0; j<6; j++) {
                Piece auxPiece = this.boardMatrix.get(i).get(j);
                if(piece == auxPiece) {
                    int[] pos = {i, j};
                    return pos;
                }
            }
        }
        return null;
    }
}

class Piece {
    private int i;
    private int j;
    private String color;

    public Piece(String color) {
        this.i = -1;
        this.j = -1;
        this.color = color;
    }

    public Piece(int i, int j, String color) {
        this.i = i;
        this.j = j;
        this.color = color;
    }

    public String getColor() {
        return this.color;
    }

    public int[] getPosition() {
        int[] pos = {this.i, this.j};
        return pos;
    }

    public void updateI(int i) {
        this.i = i;
    }

    public void updateJ(int j) {
        this.j = j;
    }

    public void incrementI(int i) {
        this.i += i;
    }

    public void incrementJ(int j) {
        this.j += j;
    }

    public void updatePosition(int i, int j) {
        this.i = i;
        this.j = j;
    }
}