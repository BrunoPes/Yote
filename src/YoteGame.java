import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

class YoteGame extends JFrame implements MouseListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private Piece selected;
    private Board board = new Board();
    private Player p1 = new Player(new Color((float)70/255,(float)70/255,(float)70/255), "Player 1");
    private Player p2 = new Player(new Color((float)210/255,(float)210/255,(float)210/255), "Player 2");

    public YoteGame(int width, int height) {
        super("Joguinho do YOTE!");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, width, height);
        this.getContentPane().setLayout(null);
        this.addKeyListener(this);
        this.setResizable(false);

        JPanel left = new JPanel();
        left.setBounds(0,1, 200, height-30);
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        JPanel gameBoard = new JPanel(new GridLayout(5, 6, 0, 0));
        gameBoard.setBounds(left.getWidth()+left.getX(),0,width-400, height-28);
        gameBoard.setName("board");
        JPanel right = new JPanel();
        right.setBounds(gameBoard.getWidth()+gameBoard.getX(), 1, 200, height-30);
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        for(int i=0; i<5; i++){
            for(int j=0; j<6; j++){
                JPanel space = new JPanel();
                space.setBackground(new Color((float)(255/255.0), (float)(240/255.0), (float)(192/255.0)));
                space.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                space.addMouseListener(this);
                space.setName(i+""+j);
                gameBoard.add(space);
            }
        }

        this.getContentPane().add(left);
        this.getContentPane().add(gameBoard);
        this.getContentPane().add(right);
        this.setVisible(true);
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
        new YoteGame(1000, 550);
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
//        System.out.println("Selected : " + selected);
        if(this.selected != null) {
            int i = this.selected.getPosition()[0];
            int j = this.selected.getPosition()[1];
            if(keyCode == 37) {
                Piece nearPiece = j > 0 ? this.board.getPieceOnBoard(i, j-1) : null;
                Piece farPiece  = j > 1 ? this.board.getPieceOnBoard(i, j-2) : null;
                testAndMovePiece(nearPiece, farPiece, j, "l");
            } else if(keyCode == 39) {
                Piece nearPiece = j < 5 ? this.board.getPieceOnBoard(i, j+1) : null;
                Piece farPiece  = j < 4 ? this.board.getPieceOnBoard(i, j+2) : null;
                testAndMovePiece(nearPiece, farPiece, j, "r");
            }else if(keyCode == 38) {
                Piece nearPiece = i > 0 ? this.board.getPieceOnBoard(i-1, j) : null;
                Piece farPiece  = i > 1 ? this.board.getPieceOnBoard(i-2, j) : null;
                testAndMovePiece(nearPiece, farPiece, i, "u");
            } else if(keyCode == 40) {
                Piece nearPiece = i < 4 ? this.board.getPieceOnBoard(i+1, j) : null;
                Piece farPiece  = i < 3 ? this.board.getPieceOnBoard(i+2, j) : null;
                testAndMovePiece(nearPiece, farPiece, i, "d");
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        JPanel clickedPanel = ((JPanel)e.getSource());
        String strPos = clickedPanel.getName();
        int[] pos = {new Integer(strPos.substring(0, 1)), new Integer(strPos.substring(1))};
        System.out.println("i = " + pos[0] + " and j = " + pos[1]);

        if(this.p1.existAnyAvailablePiece() && this.board.boardPositionIsEmpty(pos[0], pos[1])) {
            Piece addPiece = this.p1.getOneAvailablePiece();
            addPiece.updatePosition(pos[0], pos[1]);
            this.p1.updatePiecesVectors(addPiece);
            this.board.insertPieceOnBoard(addPiece);

        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
}

class Board {
    private Piece[][] boardMatrix = new Piece[5][6];
//    private JPanel[][] graphicMatrix = new JPanel[5][6];

    public Board() {
        for(int i=0; i < 5; i++) {
            for(int j=0; j < 6; j++) {
                boardMatrix[i] = null;
            }
        }
    }

    public void updateBoard(Piece movedPiece, Piece killedPiece) {
        int[] oldMovedPos = this.getPieceBoardPosition(movedPiece);
        int[] newMovedPos = movedPiece.getPosition();

        if(oldMovedPos!= null && oldMovedPos[0] != newMovedPos[0] || oldMovedPos[1] != newMovedPos[1]) {
            this.boardMatrix[newMovedPos[0]][newMovedPos[1]] =  movedPiece;
        }

        if(killedPiece != null) {
            int[] killedPos = killedPiece.getPosition();
            this.boardMatrix[killedPos[0]][killedPos[1]] = null;
        }
    }

    public void insertPieceOnBoard(Piece piece) {
        int[] pos = piece.getPosition();
        this.boardMatrix[pos[0]][pos[1]] = piece;
    }

    public Piece getPieceOnBoard(int i, int j) {
        return this.boardMatrix[i][j];
    }

    public boolean boardPositionIsEmpty(int i, int j) {
        return this.getPieceOnBoard(i, j) == null ? true : false;
    }

    public int[] getPieceBoardPosition(Piece piece) {
        for(int i=0; i<5; i++) {
            for(int j=0; j<6; j++) {
                Piece auxPiece = this.boardMatrix[i][j];
                if(piece == auxPiece) return new int[]{i, j};
            }
        }
        return null;
    }
}

class Piece extends JPanel {
    private static final long serialVersionUID = 1L;
    private int i;
    private int j;
    private Color color;

    public Piece(Color color) {
        this(-1,-1, color);
    }

    public Piece(int i, int j, Color color) {
        this.i = i;
        this.j = j;
        this.color = color;
        this.setBackground(color);
        this.setSize(40, 40);
        this.setVisible(false);
//      this.setOpaque(true);
    }

    public Color getColor() {
        return this.color;
    }

    public int[] getPosition() {
        return new int[]{this.i, this.j};
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
