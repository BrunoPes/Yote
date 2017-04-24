import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

class YoteGame extends JFrame implements MouseListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private int id;
    private int[] selected;
    private Client client;
    private Color playerCol;
    private Color otherCol;
    private boolean shouldKill = false;
    private boolean isMyTurn = false;
    private JPanel gameBoard;
    private JTextField input;
    private JTextArea chat;
    private int countMsgs = 0;

    public YoteGame(int width, int height, String[] args) {
        super("Joguinho do YOTE!");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, width, height);
        this.getContentPane().setLayout(null);
        this.setResizable(false);

        this.gameBoard = new JPanel(new GridLayout(5, 6, 0, 0));
        this.gameBoard.setBounds(0,0,600, height-28);
        this.gameBoard.setName("board");
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBounds(this.gameBoard.getWidth(), 1, 400, height-30);
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

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMaximumSize(new Dimension(393, 260));
        scrollPane.setLocation(10, 10);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        chat = new JTextArea();
        chat.setEditable(false);
        chat.setText("");
        chat.setLineWrap(true);
        chat.setMaximumSize(new Dimension(393, 240));
        ((DefaultCaret)chat.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JLabel logLabel = new JLabel("Converse no YOTÉ Chat!");
        logLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
        logLabel.setBackground(Color.BLACK);
        logLabel.setForeground(Color.BLUE);

        this.input = new JTextField("");
        this.input.setMaximumSize(new Dimension(393, 28));
        this.input.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.input.addKeyListener(this);
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        chatPanel.setMaximumSize(new Dimension(393, 300));
        chatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatPanel.add(scrollPane);
        chatPanel.add(this.input);

        right.add(chatPanel);
        scrollPane.setViewportView(chat);
        scrollPane.setColumnHeaderView(logLabel);
        chatPanel.addKeyListener(null);

        this.addKeyListener(this);
        this.getContentPane().add(this.gameBoard);
        this.getContentPane().add(right);
        this.setVisible(true);
        this.requestFocus();
        this.client = new Client(args, this);
    }

    public void updateChat(String msg) {
        this.countMsgs++;
        this.chat.setText(this.chat.getText() + msg + "\n");
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
            System.out.println("MY ID IS = " + this.id);
            this.playerCol = this.id == 0 ? Color.BLACK : Color.WHITE;
            this.otherCol = this.id == 0 ? Color.WHITE : Color.BLACK;
        } else if(action.equals("t")) {
            this.shouldKill = false;
            this.isMyTurn = (player == this.id) ? true : false;
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
        } else if(action.equals("k")) {
//          System.out.println("SHOULD KILL!!!");
            if(this.id == player) this.shouldKill = true;
        } else if(action.equals("e")) {
            System.out.println("REMOVING!!!");
            if(this.id == player) this.shouldKill = false;
            JPanel removedPieceField = this.getFieldInPos(movedPiece[0], movedPiece[1]);
            removedPieceField.removeAll();
            this.gameBoard.updateUI();
        } else if(action.equals("m")) {

        }
    }

    public void keyPressed(KeyEvent e) {
        System.out.println("CAPTURING AS COISAS AQUI!!!!!");
        int keyCode = e.getKeyCode();
        if(this.selected != null && this.isMyTurn && !shouldKill) {
            if(keyCode == 37) {
                this.client.sendMovement("l", this.selected);
            } else if(keyCode == 39) {
                this.client.sendMovement("r", this.selected);
            }else if(keyCode == 38) {
                this.client.sendMovement("u", this.selected);
            } else if(keyCode == 40) {
                this.client.sendMovement("d", this.selected);
            }
        } else if(keyCode == 10 ) {
            String msg = this.input.getText();
            if(msg.length() > 0) {
                this.input.setText("");
                System.out.println("ID = " + this.id);
                this.client.sendChatMsg("Player "+(this.id+1)+": "+msg);
                this.requestFocus();
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if(this.isMyTurn) {
            JPanel clickedField = ((JPanel)e.getSource());
            String strPos = clickedField.getName();
            int[] pos = {new Integer(strPos.substring(0, 1)), new Integer(strPos.substring(1))};

            if(!this.shouldKill) {
                if(selected == null && clickedField.getComponentCount() == 0) {
                    this.client.sendMovement("i", pos);
                } else if(selected == null) {
                    Piece selectedPiece = (Piece)clickedField.getComponent(0);
                    if(selectedPiece.getPlayerPiece() == this.id) {
                        selectedPiece.updateColor(Color.BLUE);
                        this.selected = new int[]{pos[0], pos[1]};
                    } else {
                        System.out.println("JOGADA INVALIDA");
                    }
                } else if(pos[0] == this.selected[0] && pos[1] == this.selected[1]) {
                    Piece selectedPiece = (Piece)clickedField.getComponent(0);
                    if(selectedPiece.getPlayerPiece() == this.id) {
                        selectedPiece.returnPreviousColor();
                        this.selected = null;
                    } else {
                        System.out.println("JOGADA INVALIDA SELECT");
                    }
                } else {
                    System.out.println("Jogada Invalida");
                }
            } else {
                if(selected == null && clickedField.getComponentCount() > 0) {
                    Piece selectedPiece = (Piece)clickedField.getComponent(0);
                    if(selectedPiece.getPlayerPiece() != this.id) {
                        this.client.sendMovement("k", pos);
                    } else {
                        System.out.println("Jogada Invalida: Remova uma peça inimiga");
                    }
                } else {
                    System.out.println("Jogada Invalida: Remova uma peça inimiga");
                }
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
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
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
