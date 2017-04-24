import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

class YoteGame extends JFrame implements MouseListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private int id;
    private int[] selected;
    private boolean isMyTurn = false;
    private JPanel gameBoard;
    private Client client;
    private Color playerCol;
    private Color otherCol;

    public YoteGame(int width, int height, String[] args) {
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
        this.gameBoard = new JPanel(new GridLayout(5, 6, 0, 0));
        this.gameBoard.setBounds(left.getWidth()+left.getX(),0,width-400, height-28);
        this.gameBoard.setName("board");
        JPanel right = new JPanel();
        right.setBounds(this.gameBoard.getWidth()+this.gameBoard.getX(), 1, 200, height-30);
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        for(int i=0; i<5; i++){
            for(int j=0; j<6; j++){
                JPanel space = new JPanel();
                space.setBackground(new Color((float)(255/255.0), (float)(240/255.0), (float)(192/255.0)));
                space.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                space.addMouseListener(this);
                space.setName(i+""+j);
                this.gameBoard.add(space);
            }
        }

        this.getContentPane().add(left);
        this.getContentPane().add(this.gameBoard);
        this.getContentPane().add(right);
        this.setVisible(true);
        this.client = new Client(args, this);
    }

    public JPanel getFieldInPos(int i, int j) {
        for(Component comp : this.gameBoard.getComponents()) {
            if(comp instanceof JComponent) {
                if(((JPanel)comp).getName().equals(i+""+j))
                    return ((JPanel)comp);
            }
        }
        return null;
    }
    
    public void updateGame(int player, String action, int[] movedPiece, int[] killedPiece) {
        if(action.equals("c")) {
            this.id = player;
            this.playerCol = this.id == 0 ? Color.BLACK : Color.WHITE;
            this.otherCol = this.id == 0 ? Color.WHITE : Color.BLACK;
        } else if(action.equals("t")) {
            this.isMyTurn = (player == this.id) ? true : false;
            //if(player == this.id) System.out.println("MY TURN!!");
        } else if(action.equals("i")) {
            Piece newPiece = new Piece(player == this.id ? this.playerCol : this.otherCol);
            JPanel field = this.getFieldInPos(movedPiece[0], movedPiece[1]);
            field.setLayout(null);
            field.add(newPiece);
            field.updateUI();
        } else if(action.equals("u") || action.equals("d") || action.equals("l") || action.equals("r")) {
            boolean anyKill = (killedPiece != null) ? true : false;
            JPanel oldField = this.getFieldInPos(movedPiece[0], movedPiece[1]);
            JPanel killField = anyKill ? this.getFieldInPos(killedPiece[0], killedPiece[1]) : null;
            JPanel newField;
            
            if(action.equals("u")) {
                newField = this.getFieldInPos(movedPiece[0] + (anyKill ? -2 : -1), movedPiece[1]);          
            } else if(action.equals("d")) {
                newField = this.getFieldInPos(movedPiece[0] + (anyKill ? 2 : 1), movedPiece[1]);                
            } else if(action.equals("r")) {
                newField = this.getFieldInPos(movedPiece[0], movedPiece[1] + (anyKill ? 2 : 1));
            } else {
                newField = this.getFieldInPos(movedPiece[0], movedPiece[1] + (anyKill ? -2 : -1));
            }           
            
            newField.setLayout(null);
            newField.add(new Piece(player == this.id ? this.playerCol : this.otherCol));
            oldField.removeAll();
            if(killField != null) {
                killField.removeAll();
            }
            this.selected = null;
            this.gameBoard.updateUI();
        }
    }
    
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(this.selected != null && this.isMyTurn) {
            if(keyCode == 37) {
                this.client.sendMovement("l", this.selected);
            } else if(keyCode == 39) {
                this.client.sendMovement("r", this.selected);
            }else if(keyCode == 38) {
                this.client.sendMovement("u", this.selected);
            } else if(keyCode == 40) {
                this.client.sendMovement("d", this.selected);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if(this.isMyTurn) {
            JPanel clickedField = ((JPanel)e.getSource());
            String strPos = clickedField.getName();
            int[] pos = {new Integer(strPos.substring(0, 1)), new Integer(strPos.substring(1))};
            
            if(selected == null && clickedField.getComponentCount() == 0) {
                this.client.sendMovement("i", pos);
            } else if(selected == null) {
                Piece selectedPiece = (Piece)clickedField.getComponent(0);
                if(selectedPiece.getPlayerPiece() == this.id) {
                    selectedPiece.updateColor(Color.BLUE);
                    this.selected = new int[]{pos[0], pos[1]};
                } else {
                    //System.out.println("JOGADA INVALIDA");
                }
            } else if(pos[0] == this.selected[0] && pos[1] == this.selected[1]) {
                Piece selectedPiece = (Piece)clickedField.getComponent(0);
                if(selectedPiece.getPlayerPiece() == this.id) {
                    selectedPiece.returnPreviousColor();
                    this.selected = null;
                } else {
                    //System.out.println("JOGADA INVALIDA SELECT");
                }
            } else {
                //System.out.println("JOGADA INVALIDA UNKNOWN");
            }
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    
    public static void main(String[] args) {
        new YoteGame(1000, 550, args);
    }    
}

class Piece extends JPanel {
    private static final long serialVersionUID = 1L;
    private Color realCol;

    public Piece(Color color) {
        this.realCol = color;
        this.setBackground(color);
        this.setBounds(30, 32, 40, 40);
        this.setVisible(true);
    }
    
    public int getPlayerPiece() {
        return this.realCol == Color.BLACK ? 0 : 1;
    }

    public void returnPreviousColor() {
        this.setBackground(this.realCol);
    }
    
    public void updateColor(Color newColor) {
        this.setBackground(newColor);
    }
}