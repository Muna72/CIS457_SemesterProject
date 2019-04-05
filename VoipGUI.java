import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.swing.table.*;
import java.util.*;
import java.awt.Color;
import java.awt.event.*;
import java.text.DecimalFormat;
import javax.swing.border.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
/**
 *
 * @author Muna Gigowski
 * @version 1.0 (March 2019)
 */
public class VoipGUI extends JFrame implements ActionListener {


    //Declaring instance variables
    private boolean first = true;
    private boolean firstTimeStartPressed;
    DecimalFormat df = new DecimalFormat("#.00");
    ArrayList<Object>availableFileInfo;
    ArrayList<String> stringsToDisplay;

    private JPanel input;
    private JPanel viewArea;
    private JPanel optionsPanel;
    private JPanel chatArea;
    MediaPanel screen;

    //define buttons
    private JButton connect;
    private JButton disconnect;

    //define text fields
    private JTextField serverHostName;
    private JTextField portNum;
    private JTextField userName;
    private JTextField hostName;

    //define chat text areas
    private JTextArea chat;
    public JTextField messageInput;

    //define font
    private Font messageFont = new Font("SansSerif Bold", Font.PLAIN, 16);
    private Font systemFont = new Font("SansSerif Bold", Font.BOLD, 20);
    private Font uiFont = new Font("SansSerif Bold", Font.BOLD, 13);

    //define JLabels
    private JLabel serverHostnameLabel;
    private JLabel portNumLabel;
    private JLabel userNameLabel;
    private JLabel hostNameLabel;

    //define menu items
    private JMenuBar menu;
    JMenu file;
    JMenuItem reset;
    JMenuItem quit;

    /**
     * Main method
     * @param args
     */
    public static void main(String[] args) {
        try {
            VoipGUI gui = new VoipGUI();
            gui.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            gui.setTitle("FTP Client");
            gui.setPreferredSize(new Dimension(1800, 1000));
            gui.pack();
            gui.setVisible(true);
            gui.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {

                    int dialogResult = JOptionPane.showConfirmDialog(gui,
                            "Closing window while client is running" +
                                    " will cause you to lose all data. Proceed in closing?", "Close Window?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Class constructor initializes instance variables
     */
    public VoipGUI() {

        firstTimeStartPressed = true;
        availableFileInfo = new ArrayList<Object>();
        stringsToDisplay = new ArrayList<String>();

        setLayout(new GridBagLayout());
        GridBagConstraints position = new GridBagConstraints();
        TitledBorder border;

        //Adding all panels to JFrame
        input = new JPanel(new GridBagLayout());
        input.setPreferredSize(new Dimension(1000, 200));
        //input.setBorder(new EmptyBorder(15, 0, 30, 20));
        border = new TitledBorder("Connection Input Information");
        border.setBorder(new LineBorder(Color.BLACK, 3));
        border.setTitleFont(new Font("Arial", Font.BOLD, 20));
        border.setTitleJustification(TitledBorder.CENTER);
        border.setTitlePosition(TitledBorder.TOP);
        input.setBorder(border);
        position = makeConstraints(10, 0, 1, 3, GridBagConstraints.LINE_END);
        position.insets =  new Insets(-70, 0, 0, -280);
        add(input,position);

        viewArea = new JPanel(new GridBagLayout());
        viewArea.setPreferredSize(new Dimension(1000, 500));
        viewArea.setBorder(new LineBorder(Color.BLACK, 3));
        position = makeConstraints(10, 4, 1, 3, GridBagConstraints.LINE_END);
        position.insets =  new Insets(0, 0, 0, -280);
        add(viewArea,position);

        screen = new MediaPanel();
        screen.setPreferredSize(new Dimension(970, 480));
        screen.setBorder(new LineBorder(Color.BLACK, 3));
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(0, 0, 0, 0);
        viewArea.add(screen,position);

        //Adding all panels to JFrame
        optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setPreferredSize(new Dimension(1000, 100));
        optionsPanel.setBackground(Color.RED);
        position = makeConstraints(10, 6, 1, 3, GridBagConstraints.LINE_END);
        position.insets =  new Insets(0, 0, 0, -280);
        add(optionsPanel,position);

        chatArea = new JPanel(new GridBagLayout());
        chatArea.setPreferredSize(new Dimension(400, 790));
        chatArea.setBackground(Color.BLUE);
        position = makeConstraints(3, 6, 1, 2, GridBagConstraints.LINE_END);
        position.insets =  new Insets(-690, -300, 0, 2);
        add(chatArea,position);

        //Adding text chat input and output areas
        chat = new JTextArea();
        chat.setEditable(false);
        chat.setFont(systemFont);
        chat.setPreferredSize(new Dimension(380, 500));
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(-200, 0, 0, 0);
        chatArea.add((new JScrollPane(chat)), position);

        messageInput = new JTextField("Type a message...");
        messageInput.setPreferredSize(new Dimension(350, 30));
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(380, 0, 0, 0);
        chatArea.add(messageInput, position);

        //Adding stats to searchArea JPanel
        serverHostnameLabel = new JLabel("Server Hostname:");
        position = makeConstraints(1, 1, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(30, 10, 0, 0);
        serverHostnameLabel.setBorder(new EmptyBorder(10, 0, 30, 0));
        input.add(serverHostnameLabel, position);

        portNumLabel = new JLabel("Port:");
        portNumLabel.setFont(uiFont);
        position = makeConstraints(3, 1, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(7, 20, 0, 0);
        input.add(portNumLabel, position);

        userNameLabel = new JLabel("Username:");
        userNameLabel.setFont(uiFont);
        position = makeConstraints(1, 2, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(10, 15, 20, 20);
        input.add(userNameLabel, position);

        hostNameLabel = new JLabel("Hostname:");
        hostNameLabel.setFont(uiFont);
        position = makeConstraints(3, 2, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(15, 20, 0, 0);
        input.add(hostNameLabel, position);

        //Place the textfields
        serverHostName = new JTextField("", 20);
        position = makeConstraints(2, 1, 1, 1, GridBagConstraints.LINE_START);
        //serverHostName.setMinimumSize(serverHostName.getPreferredSize());
        position.insets =  new Insets(15, 15, 0, 20);
        input.add(serverHostName, position);

        portNum = new JTextField("", 10);
        position = makeConstraints(3, 1, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(12, 60, 0, 0);
        input.add(portNum, position);

        userName = new JTextField("", 15);
        position = makeConstraints(2, 2, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(0, -20, 0, 80);
        input.add(userName, position);

        hostName = new JTextField("", 20);
        position = makeConstraints(4, 2, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(15, -80, 0, 0);
        input.add(hostName, position);

        //place each button
        connect = new JButton( "Connect" );
        connect.setForeground(Color.BLACK);
        position = makeConstraints(10, 1, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(40, 100, 0, 20);
        input.add(connect, position);

        disconnect = new JButton();
        try {
            Image img = ImageIO.read(getClass().getResource("images/hangup.png"));
            //Image img = ImageIO.read(new File("images/hangup.png"));
            //Image img = icon.getImage() ;
            Image newimg = img.getScaledInstance(70, 70,  java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon( newimg );
            disconnect.setIcon(icon);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(10, 0, 0, 20);
        optionsPanel.add(disconnect, position);

        //create and add menu items
        menu = new JMenuBar();
        file = new JMenu("File");
        quit = new JMenuItem("Quit");
        reset = new JMenuItem("Clear");
        menu.add(file);
        file.add(quit);
        file.add(reset);
        setJMenuBar(menu);

        //add all action listeners
        messageInput.addActionListener(this);
        messageInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(messageInput.getText().equals("")) {
                    messageInput.setText("Type a message...");
                }
            }
        });
        messageInput.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(messageInput.getText().equals("Type a message...")) {
                    messageInput.setText("");
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        connect.addActionListener(this);
        serverHostName.addActionListener(this);
        portNum.addActionListener(this);
        userName.addActionListener(this);
        hostName.addActionListener(this);
        file.addActionListener(this);
        quit.addActionListener(this);
        reset.addActionListener(this);

        //disable buttons by default
        disconnect.setEnabled(false);
    }

    /**
     * Action performed method
     * @param e
     */
    public void actionPerformed(ActionEvent e) {


        //exit application if QUIT menu item
        if (e.getSource() == quit) {
            System.exit(1);
        }

        if (e.getSource() == messageInput) {
            chat.append("You just typed in: " + messageInput.getText() + " \n");
            messageInput.setText("");
        }

        //set running variable to false if STOP button
        if (e.getSource() == connect) {
            chat.append("Hello, " + userName.getText() + "! \n");
            chat.setFont(messageFont);
           /* if(!serverHostName.getText().equals("") && !portNum.getText().equals("") &&
                    !userName.getText().equals("") && !hostName.getText().equals("")) {
                try {


                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Error setting up connection");
                }
            } else {
                JOptionPane.showMessageDialog(null, "All connection setup fields must have values");
            } */
        }

        if (e.getSource() == disconnect) {
                //TODO do not close out program, only terminate connection
        }
        updateCommandLine();
        //cmdLine.repaint();
    }

    //Method to update command line JPanel area
    public void updateCommandLine() {

        //cmdLine.removeAll();
        //cmdLine.revalidate();
        //cmdLine.repaint();

        GridBagConstraints position = new GridBagConstraints();
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.FIRST_LINE_START);
        position.insets =  new Insets(-100, -240, 0, 0);

        JList list = new JList(stringsToDisplay.toArray());
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(10);
        list.setFont(new Font("Arial", Font.PLAIN, 16));
        //cmdLine.add(list, position);
    }


    //Method to set the width of all table columns
    public void setColunmWidth(JTable table) {

        TableColumnModel tcm = table.getColumnModel();

        for (int i = 0; i < (tcm.getColumnCount()); i++) {
            tcm.getColumn(i).setPreferredWidth(90);
        }
    }

    /**
     * Method to set contraints for gridbag layout
     * @param x
     * @param y
     * @param h
     * @param w
     * @param align
     * @return
     */
    private GridBagConstraints makeConstraints(int x, int y, int h, int w, int align) {
        GridBagConstraints rtn = new GridBagConstraints();
        rtn.gridx = x;
        rtn.gridy = y;
        rtn.gridheight = h;
        rtn.gridwidth = w;

        rtn.anchor = align;
        return rtn;
    }
}

