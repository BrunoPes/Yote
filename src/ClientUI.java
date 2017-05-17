import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;
import java.rmi.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

class ClientUI extends JFrame implements MouseListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private int id;
    private int[] selected;
    private Client client;
    private Color playerCol;
    private Color otherCol;
    private JPanel gameBoard;
    private JTextArea chat;
    private JTextField input;
    private JTextField hostInput;
    private int countMsgs = 0;
    private int[] playersQnt = new int[]{12, 12};
    private boolean isMyTurn = false;
    private boolean canRestart = false;
    private boolean shouldKill = false;
    private boolean shouldContinue = false;
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

    public YoteGame(int width, int height) {
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

        JLabel labelIP = new JLabel("Escreva o IP do servidor");
        labelIP.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.hostInput = new JTextField();
        this.hostInput.setMaximumSize(new Dimension(292, 20));
        this.hostInput.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        buttonsPanel.setMaximumSize(new Dimension(292, 70));
        buttonsPanel.add(labelIP);
        buttonsPanel.add(this.hostInput);
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
        JLabel logLabel = new JLabel("Converse no YOTÉ Chat!");
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
        this.client = new Client(this);
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

        this.chat.setText("");
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
        this.hostInput.setEnabled(connectEnabled);
        this.forfeit.setEnabled(!connectEnabled);
        this.restart.setEnabled(!connectEnabled);
        this.connect.setEnabled(connectEnabled);
        this.connect.setText(connectEnabled ? "Conectar" : "Conectado");
        this.connect.updateUI();
        if(!connectEnabled) this.requestFocus();
    }





    public void updateChat(String msg) {
        this.countMsgs++;
        this.chat.setText(this.chat.getText() + msg + "\n");
        if(countMsgs > 400)
            this.chat.setText("");
    }

    public void updateGame(int player, String action, int[] movedPiece, int[] killedPiece) {
        if(action.equals("c")) {
            this.id = player;
            this.playerCol = this.id == 0 ? Color.BLACK : Color.WHITE;
            this.otherCol = this.id == 0 ? Color.WHITE : Color.BLACK;
            this.updateButtons(false);
            this.updateLabels(true);
        } else if(action.equals("t")) {
            this.selected = null;
            this.shouldKill = false;
            this.shouldContinue = false;
            this.isMyTurn = (player == this.id) ? true : false;
            this.updateLabels(false);
        } else if(action.equals("i")) {
            Piece newPiece = new Piece(player == this.id ? this.playerCol : this.otherCol);
            JPanel field = this.getFieldInPos(movedPiece[0], movedPiece[1]);
            field.setLayout(null);
            field.add(newPiece);

            if(player == 0) this.player1Num.setText("Peças para inserir: " + (--this.playersQnt[player]));
            else this.player2Num.setText("Peças para inserir: " + (--this.playersQnt[player]));
            this.stateLabels.updateUI();
            field.updateUI();
        } else if(action.equals("u") || action.equals("d") || action.equals("l") || action.equals("r")) {
            boolean anyKill = (killedPiece != null) ? true : false;
            JPanel oldField = this.getFieldInPos(movedPiece[0], movedPiece[1]);
            JPanel killField = anyKill ? this.getFieldInPos(killedPiece[0], killedPiece[1]) : null;
            int[] newPos;

            if(action.equals("u")) {
                newPos = new int[]{movedPiece[0] + (anyKill ? -2 : -1), movedPiece[1]};
            } else if(action.equals("d")) {
                newPos = new int[]{movedPiece[0] + (anyKill ? 2 : 1), movedPiece[1]};
            } else if(action.equals("r")) {
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
        } else if(action.equals("k") && this.id == player) {
            this.shouldKill = true;
            this.helpLabel.setText("Remova uma peça do oponente");
            this.helpLabel.updateUI();
        } else if(action.equals("e")) {
            if(this.id == player) this.shouldKill = false;
            JPanel removedPieceField = this.getFieldInPos(movedPiece[0], movedPiece[1]);
            removedPieceField.removeAll();
            this.gameBoard.updateUI();
        } else if(action.equals("m") && this.id == player) {
            Component moved= this.getFieldInPos(this.selected[0], this.selected[1]).getComponent(0);
            if(moved != null && moved instanceof Piece) {
                this.shouldKill = false;
                this.shouldContinue = true;
                ((Piece)moved).updateColor(Color.BLUE);
                this.helpLabel.setText("Faça uma captura múltipla");
                this.helpLabel.updateUI();
            } else {
                this.client.sendMovement("t", null, null);
            }
        } else if(action.equals("g")) {
            this.finishGame(-1, player, true);
        } else if(action.equals("rg")) {
            int winner = this.id == player ? (1-this.id) : this.id;
            this.finishGame(winner, -1, true);
        } else if(action.equals("w")) {
            this.finishGame(player, -1, false);
            this.canRestart = true;
        } else if(action.equals("wr")) {
            this.resetStateAndUI();
            this.isMyTurn = (player == this.id) ? true : false;
            this.updateLabels(false);
        }
    }

    public void finishGame(int winPlayer, int giveUpPlayer, boolean shouldResetUI) {
        if(shouldResetUI) this.resetStateAndUI();
        
        String msg;
        if(giveUpPlayer >= 0) {
            msg = this.id == giveUpPlayer ? "Você perdeu por desistência" : "Parabéns, você venceu! Seu oponente desistiu!";
            this.client.closeSocketClient();
        } else if(winPlayer < 2) {
            msg = this.id == winPlayer ? "Parabéns, você venceu!" : "Você perdeu :(";
        } else {
            msg = "Empate! Nesse jogo não haverá vencedores.";
        }
        
        JOptionPane.showMessageDialog(this, msg, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
        this.requestFocus();
    }







    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(((this.selected != null && this.isMyTurn && !this.shouldKill) || this.shouldContinue) && keyCode != 10) {
            if(keyCode == 37) {
                this.client.sendMovement("l", this.selected, null);
            } else if(keyCode == 39) {
                this.client.sendMovement("r", this.selected, null);
            }else if(keyCode == 38) {
                this.client.sendMovement("u", this.selected, null);
            } else if(keyCode == 40) {
                this.client.sendMovement("d", this.selected, null);
            }
        } else if(keyCode == 10) {
            String msg = this.input.getText();
            if(msg.length() > 0) {
                this.input.setText("");
                this.client.sendChatMsg("Player "+(this.id+1)+": "+msg);
                this.requestFocus();
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if(e.getSource() instanceof JButton) {
            JButton button = (JButton)e.getSource();
            switch(button.getText()) {
                case "Conectar":
                    String host = this.hostInput.getText();
                    if(host.length() > 0 && host.indexOf(":") >= 0) {
                        int portIndex = host.indexOf(":");
                        int hostPort = new Integer(host.substring(portIndex+1));
                        String hostIP = host.substring(0, portIndex);
                        this.client.initSocket(hostIP, hostPort);
                    }
                    break;
                case "Desistir":
                    this.client.sendControlMsg("g", null);
                    break;
                case "Reiniciar":
                    this.client.sendControlMsg(canRestart ? "wr" : "rg", null);
                    break;
                default: break;
            }
        } else if(this.isMyTurn) {
            JPanel clickedField = ((JPanel)e.getSource());
            String strPos = clickedField.getName();
            int[] pos = {new Integer(strPos.substring(0, 1)), new Integer(strPos.substring(1))};

            if(!this.shouldKill && !this.shouldContinue) {
                if(this.selected == null && clickedField.getComponentCount() == 0) {
                    this.client.sendMovement("i", pos, null);
                } else if(this.selected == null) {
                    Piece selectedPiece = (Piece)clickedField.getComponent(0);
                    if(selectedPiece.getPlayerPiece() == this.id) {
                        selectedPiece.updateColor(Color.BLUE);
                        this.selected = new int[]{pos[0], pos[1]};
                    } else {
                        //System.out.println("JOGADA INVALIDA");
                    }
                } else if(this.selected != null) {
                    Piece selectedPiece = (Piece)clickedField.getComponent(0);
                    if(pos[0] == this.selected[0] && pos[1] == this.selected[1]) {
                        if(selectedPiece.getPlayerPiece() == this.id) {
                            selectedPiece.returnPreviousColor();
                            this.selected = null;
                        } else {
                            //System.out.println("JOGADA INVALIDA SELECT");
                        }
                    } else if(selectedPiece.getPlayerPiece() == this.id) {
                        Piece previous = (Piece)this.getFieldInPos(this.selected[0], this.selected[1]).getComponent(0);
                        previous.returnPreviousColor();
                        this.selected = new int[]{pos[0], pos[1]};
                        selectedPiece.updateColor(Color.BLUE);
                    }
                }
            } else if(this.shouldKill) {
                if(clickedField.getComponentCount() > 0) {
                    Piece selectedPiece = (Piece)clickedField.getComponent(0);
                    if(selectedPiece.getPlayerPiece() != this.id) {
                        this.client.sendMovement("k", this.selected, pos);
                    } else {
                        System.out.println("Jogada Invalida: Remova uma peça inimiga");
                    }
                } else {
                    System.out.println("Jogada Invalida: Remova uma peça inimiga");
                }
            }  else {
                System.out.println("Capture uma peça ou termine sua jogada!!!");
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
        new YoteGame(900, 550);
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
