import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

class ClientUI extends JFrame implements MouseListener, KeyListener, WindowListener {
    private static final long serialVersionUID = 1L;
    private Client client;

    private int id;
    private int countMsgs = 0;
    private int[] selected;
    private int[] playersQnt = new int[]{12, 12};
    private boolean isMyTurn = false;
    private boolean canRestart = false;
    private boolean shouldKill = false;
    private boolean shouldContinue = false;

    private Color playerCol;
    private Color otherCol;
    private JPanel gameBoard;
    private JTextArea chat;
    private JTextField input;
    private JTextField clientInput;
    private JTextField serverInput;
    private JButton connect;
    private JButton forfeit;
    private JButton restart;
    private JLabel helpLabel;
    private JLabel turnLabel;
    private JLabel player1Label;
    private JLabel player1Num;
    private JLabel player2Label;
    private JLabel player2Num;
    private JPanel stateLabels;

    public ClientUI(int width, int height) {
        super("Joguinho do YOTE!");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, width, height);
        this.getContentPane().setLayout(null);
        this.setResizable(false);
        this.addWindowListener(this);

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

        JLabel labelClient = new JLabel("Nome do jogador");
        labelClient.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.clientInput = new JTextField();
        this.clientInput.setMaximumSize(new Dimension(292, 20));
        this.clientInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelServer = new JLabel("Nome do servidor");
        labelServer.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.serverInput = new JTextField();
        this.serverInput.setMaximumSize(new Dimension(292, 20));
        this.serverInput.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.connect = new JButton("Conectar");
        this.forfeit = new JButton("Desistir");
        this.restart = new JButton("Reiniciar");
        this.connect.addMouseListener(this);
        this.connect.setBounds(0, 0, 98, 20);
        this.restart.addMouseListener(this);
        this.restart.setBounds(100, 0, 98, 20);
        this.restart.setEnabled(false);
        this.forfeit.addMouseListener(this);
        this.forfeit.setBounds(200, 0, 98, 20);
        this.forfeit.setEnabled(false);
        JPanel buttons = new JPanel(null);
        buttons.setSize(new Dimension(300, 30));
        buttons.add(this.connect);
        buttons.add(this.restart);
        buttons.add(this.forfeit);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setMaximumSize(new Dimension(292, 100));
        buttonsPanel.add(labelClient);
        buttonsPanel.add(this.clientInput);
        buttonsPanel.add(labelServer);
        buttonsPanel.add(this.serverInput);
        buttonsPanel.add(buttons);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.turnLabel = new JLabel("Turno: ");
        this.helpLabel = new JLabel("Esperando o início de uma partida");
        this.player1Label = new JLabel("Player 1: ");
        this.player1Num = new JLabel("Peças Restantes: ");
        this.player2Label = new JLabel("Player 2: ");
        this.player2Num = new JLabel("Peças Restantes: ");
        this.turnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.helpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.player1Label.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.player1Num.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.player2Label.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.player2Num.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.stateLabels = new JPanel();
        this.stateLabels.setLayout(new BoxLayout(stateLabels, BoxLayout.Y_AXIS));
        this.stateLabels.setMaximumSize(new Dimension(292, 100));
        this.stateLabels.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.stateLabels.add(this.turnLabel);
        this.stateLabels.add(this.helpLabel);
        this.stateLabels.add(this.player1Label);
        this.stateLabels.add(this.player1Num);
        this.stateLabels.add(this.player2Label);
        this.stateLabels.add(this.player2Num);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMaximumSize(new Dimension(293, 200));
        scrollPane.setLocation(10, 10);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        chat = new JTextArea();
        chat.setEditable(false);
        chat.setText("");
        chat.setLineWrap(true);
        chat.setMaximumSize(new Dimension(293, 180));
        ((DefaultCaret)chat.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JLabel logLabel = new JLabel("Converse no YOTE Chat!");
        logLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
        logLabel.setBackground(Color.BLACK);
        logLabel.setForeground(Color.BLUE);
        scrollPane.setViewportView(chat);
        scrollPane.setColumnHeaderView(logLabel);

        this.input = new JTextField("");
        this.input.setMaximumSize(new Dimension(293, 28));
        this.input.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.input.addKeyListener(this);
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        chatPanel.setMaximumSize(new Dimension(293, 232));
        chatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatPanel.add(scrollPane);
        chatPanel.add(this.input);

        right.add(buttonsPanel);
        right.add(chatPanel);
        right.add(this.stateLabels);

        this.addKeyListener(this);
        this.getContentPane().add(this.gameBoard);
        this.getContentPane().add(right);
        this.setVisible(true);
        this.requestFocus();
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

    public void resetStateAndUI() {
        this.countMsgs = 0;
        this.isMyTurn = false;
        this.shouldKill = false;
        this.canRestart = false;
        this.shouldContinue = false;
        this.selected = null;
        this.playersQnt = new int[]{12,12};

        //this.chat.setText("");
        this.input.setText("");
        for(Component comp : this.gameBoard.getComponents()) {
            if(comp instanceof JPanel) {
                ((JPanel)comp).removeAll();
            }
        }
        this.gameBoard.updateUI();
        this.updateLabels(true);
    }

    public void updateLabels(boolean start) {
        if(start) {
            this.turnLabel.setText("Turno: " + (this.id == 0 ? "Você" : "Oponente"));
            this.helpLabel.setText(this.id == 0 ? "Insira ou movimente alguma peça" : "Aguarde a sua vez...");
            this.player1Label.setText("Player 1: Pretas " + (this.id == 0 ? "(Você)" : "(Oponente)"));
            this.player1Num.setText("Peças para inserir: " + playersQnt[0]);
            this.player2Label.setText("Player 2: Brancas " + (this.id == 1 ? "(Você)" : "(Oponente)"));
            this.player2Num.setText("Peças para inserir: " + playersQnt[1]);
        } else {
            this.turnLabel.setText("Turno: " + (this.isMyTurn  ? "Você" : "Oponente"));
            this.helpLabel.setText(this.isMyTurn ? "Insira ou movimente alguma peça" : "Aguarde a sua vez...");
        }
        this.stateLabels.updateUI();
    }

    public void updateButtons(boolean connectEnabled) {
        this.clientInput.setEnabled(connectEnabled);
        this.serverInput.setEnabled(connectEnabled);
        this.forfeit.setEnabled(!connectEnabled);
        this.restart.setEnabled(!connectEnabled);
        this.connect.setEnabled(connectEnabled);
        this.connect.setText(connectEnabled ? "Conectar" : "Conectado");
        this.connect.updateUI();
        if(!connectEnabled) this.requestFocus();
    }

    public void finishGame(int winPlayer, int giveUpPlayer, boolean shouldResetUI) {
        if(shouldResetUI) this.resetStateAndUI();

        String msg = "";
        if(giveUpPlayer >= 0) {
            msg = this.id == giveUpPlayer ? "Você perdeu por desistência" : "Parabéns, você venceu! Seu oponente desistiu!";
            this.updateButtons(true);
            this.client.closeConnection(-1);
        } else if(winPlayer < 2) {
            msg = this.id == winPlayer ? "Parabéns, você venceu!" : "Você perdeu :(";
        } else {
            msg = "Empate! Nesse jogo não haverá vencedores.";
        }

        this.updateChat(msg);
        this.requestFocus();
    }

    public void updateChat(String msg) {
        this.countMsgs++;
        this.chat.setText(this.chat.getText() + msg + "\n");

        if(countMsgs > 400) this.chat.setText("");
    }

    public void connected(int player) {
        this.id = player;
        this.playerCol = this.id == 0 ? Color.BLACK : Color.WHITE;
        this.otherCol = this.id == 0 ? Color.WHITE : Color.BLACK;
        this.updateButtons(false);
        this.updateLabels(true);
    }

    public void insertPiece(int player, int[] pos) {
        Piece newPiece = new Piece(player == this.id ? this.playerCol : this.otherCol);
        JPanel field = this.getFieldInPos(pos[0], pos[1]);
        field.setLayout(null);
        field.add(newPiece);

        if(player == 0) this.player1Num.setText("Peças para inserir: " + (--this.playersQnt[player]));
        else this.player2Num.setText("Peças para inserir: " + (--this.playersQnt[player]));
        this.stateLabels.updateUI();
        field.updateUI();
    }

    public void movePiece(int player, String move, int[] movedPiece, int[] killedPiece) {
        boolean anyKill = (killedPiece != null) ? true : false;
        JPanel oldField = this.getFieldInPos(movedPiece[0], movedPiece[1]);
        JPanel killField = anyKill ? this.getFieldInPos(killedPiece[0], killedPiece[1]) : null;
        int[] newPos;

        if(move.equals("u")) {
            newPos = new int[]{movedPiece[0] + (anyKill ? -2 : -1), movedPiece[1]};
        } else if(move.equals("d")) {
            newPos = new int[]{movedPiece[0] + (anyKill ? 2 : 1), movedPiece[1]};
        } else if(move.equals("r")) {
            newPos = new int[]{movedPiece[0], movedPiece[1] + (anyKill ? 2 : 1)};
        } else {
            newPos = new int[]{movedPiece[0], movedPiece[1] + (anyKill ? -2 : -1)};
        }

        JPanel newField = this.getFieldInPos(newPos[0], newPos[1]);
        newField.setLayout(null);
        newField.add(new Piece(player == this.id ? this.playerCol : this.otherCol));
        oldField.removeAll();
        this.selected = null;
        if(killField != null) {
            killField.removeAll();
            if(this.id == player) this.selected = new int[]{newPos[0], newPos[1]};
        }
        this.gameBoard.updateUI();
    }

    public void killPiece(int player) {
        if(this.id == player) {
            this.shouldKill = true;
            this.helpLabel.setText("Remova uma peça do oponente");
            this.helpLabel.updateUI();
        }
    }

    public void multkillPiece(int player) {
        if(this.id == player) {
            System.out.println("Pode multikill");
            Component moved = this.getFieldInPos(this.selected[0], this.selected[1]).getComponent(0);

            if(moved != null && moved instanceof Piece) {
                this.shouldKill = false;
                this.shouldContinue = true;
                ((Piece)moved).updateColor(Color.BLUE);
                this.helpLabel.setText("Faça uma captura múltipla");
                this.helpLabel.updateUI();
            } else {
                try{
                    this.client.getServer().changeTurn();
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removePiece(int player, int[] pos) {
        if(this.id == player) this.shouldKill = false;
        JPanel removedPieceField = this.getFieldInPos(pos[0], pos[1]);
        removedPieceField.removeAll();
        this.gameBoard.updateUI();
    }

    public void changeTurn(int player) {
        this.selected = null;
        this.shouldKill = false;
        this.shouldContinue = false;
        this.isMyTurn = (player == this.id) ? true : false;
        this.updateLabels(false);
        this.turnLabel.setText("Turno: " + (this.id == player ? "Você" : "Oponente"));
        System.out.println("Changed turn : " + player);
    }

    public void restartGame(int player) {
        System.out.println("Player: " + player);
        this.finishGame(player, -1, true);
        this.isMyTurn = (this.id == player);
        this.changeTurn(player);
    }

    public void playerWin(int player) {
        this.finishGame(player, -1, false);
        this.canRestart = true;
    }

    public void gameOver(int player) {
        this.resetStateAndUI();
        this.isMyTurn = (player == this.id) ? true : false;
        this.updateLabels(false);
    }

    public void keyPressed(KeyEvent key) {
        try {
            int keyCode = key.getKeyCode();
            if(((this.selected != null && this.isMyTurn && !this.shouldKill) || this.shouldContinue) && keyCode != 10) {
                if(keyCode == 37) {
                    this.client.getServer().testAndMovePiece("l", this.selected, this.id);
                } else if(keyCode == 39) {
                    this.client.getServer().testAndMovePiece("r", this.selected, this.id);
                }else if(keyCode == 38) {
                    this.client.getServer().testAndMovePiece("u", this.selected, this.id);
                } else if(keyCode == 40) {
                    this.client.getServer().testAndMovePiece("d", this.selected, this.id);
                }
            } else if(keyCode == 10) {
                String msg = this.input.getText();
                if(msg.length() > 0) {
                    this.client.getServer().updateChat(this.client.getName() + ": " + msg);
                    this.input.setText("");
                    this.requestFocus();
                }
            }

        } catch(RemoteException e) {
            e.printStackTrace();
        }
}

    public void mousePressed(MouseEvent e) {
        try {
            if(e.getSource() instanceof JButton) {
                JButton button = (JButton)e.getSource();
                switch(button.getText()) {
                    case "Conectar":
                        String client = this.clientInput.getText();
                        String host = this.serverInput.getText();
                        if(host.length() > 0 && client.length() > 0) {
                            host = host.replace(" ", "").toLowerCase();
                            client = client.replace(" ", "");
                            this.clientInput.setText(client);
                            this.serverInput.setText(host);

                            this.client = new Client(this, host, client);
                        }
                        break;
                    case "Desistir":
                        this.client.getServer().giveUpGame(this.id);
                        break;
                    case "Reiniciar":
                        if(canRestart) {
                            this.client.getServer().finishGame(this.id);
                        } else {
                            this.client.getServer().restartGame(this.id);
                        }
                        break;
                    default: break;
                }
            } else if(this.isMyTurn && !this.canRestart) {
                JPanel clickedField = ((JPanel)e.getSource());
                Piece selectedPiece = clickedField.getComponentCount() > 0 ? (Piece)clickedField.getComponent(0) : null;
                String strPos = clickedField.getName();
                int[] pos = {new Integer(strPos.substring(0, 1)), new Integer(strPos.substring(1))};

                if(!this.shouldKill && !this.shouldContinue) {
                    if(this.selected == null && clickedField.getComponentCount() == 0) {
                        this.client.getServer().insertPiece(this.id, pos);
                    } else if(this.selected == null && selectedPiece.getPlayerPiece() == this.id) {
                        selectedPiece.updateColor(Color.BLUE);
                        this.selected = new int[]{pos[0], pos[1]};
                    } else if(this.selected != null && selectedPiece.getPlayerPiece() == this.id) {
                        if(pos[0] == this.selected[0] && pos[1] == this.selected[1]) {
                            selectedPiece.returnPreviousColor();
                            this.selected = null;
                        } else {
                            Piece previous = (Piece)this.getFieldInPos(this.selected[0], this.selected[1]).getComponent(0);
                            previous.returnPreviousColor();
                            selectedPiece.updateColor(Color.BLUE);
                            this.selected = new int[]{pos[0], pos[1]};
                        }
                    }
                } else if(this.shouldKill && clickedField.getComponentCount() > 0) {
                    if(selectedPiece.getPlayerPiece() != this.id)
                        this.client.getServer().removePiece(this.id, this.selected, pos);
                }
            }
        } catch(RemoteException exc) {
            exc.printStackTrace();
        }
    }

    public void windowClosing(WindowEvent e) {
        if(!this.connect.isEnabled()) {
            System.out.println("Closing Window");
            this.client.closeConnection(this.id);
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}

    public static void main(String[] args) {
        new ClientUI(900, 550);
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
