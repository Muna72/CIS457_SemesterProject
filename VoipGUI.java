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
    ArrayList<JLabel>availableFileInfo;


    private JPanel input;
    private JPanel viewArea;
    private JPanel optionsPanel;
    private JPanel chatArea;
    private JPanel screen;

    //define instances of client and server
    Client me;
    AudioCapture myCapture;

    //define buttons
    public static JButton connect;
    public static JButton disconnect;
    public static JButton stop;
    public static JButton record;
    public static JButton sendAudio;
    private JButton retreive;

    //define text fields
    private JTextField serverHostName;
    private JTextField userName;

    //define chat text areas
    public static JTextArea chat;
    public static JTextField messageInput;

    //define font
    private Font messageFont = new Font("SansSerif Bold", Font.PLAIN, 16);
    private Font systemFont = new Font("SansSerif Bold", Font.BOLD, 20);
    private Font uiFont = new Font("SansSerif Bold", Font.BOLD, 14);

    //define JLabels
    private JLabel serverHostnameLabel;
    private JLabel userNameLabel;

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
        availableFileInfo = new ArrayList<JLabel>();
        myCapture = new AudioCapture();

        setLayout(new GridBagLayout());
        GridBagConstraints position = new GridBagConstraints();
        TitledBorder border;

        //Adding all panels to JFrame
        input = new JPanel(new GridBagLayout());
        input.setPreferredSize(new Dimension(1000, 200));
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

        screen = new JPanel();
        screen.setPreferredSize(new Dimension(400, 380));
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(50, -100, 0, 0);
        viewArea.add(screen,position);

        //Adding all panels to JFrame
        optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setPreferredSize(new Dimension(1000, 100));
        position = makeConstraints(10, 6, 1, 3, GridBagConstraints.LINE_END);
        position.insets =  new Insets(0, 0, 0, -280);
        add(optionsPanel,position);

        chatArea = new JPanel(new GridBagLayout());
        chatArea.setPreferredSize(new Dimension(400, 790));
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
        serverHostnameLabel.setFont(uiFont);
        position = makeConstraints(1, 1, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(30, 10, 0, 0);
        serverHostnameLabel.setBorder(new EmptyBorder(10, 0, 30, 0));
        input.add(serverHostnameLabel, position);

        userNameLabel = new JLabel("Username:");
        userNameLabel.setFont(uiFont);
        position = makeConstraints(1, 2, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(10, 15, 20, 20);
        input.add(userNameLabel, position);

        //Place the textfields
        serverHostName = new JTextField("", 20);
        position = makeConstraints(2, 1, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(15, 15, 0, 20);
        input.add(serverHostName, position);

        userName = new JTextField("", 15);
        position = makeConstraints(2, 2, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(0, -20, 0, 80);
        input.add(userName, position);

        //place each button
        connect = new JButton( "Connect" );
        connect.setForeground(Color.BLACK);
        position = makeConstraints(10, 1, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(40, 100, 0, 20);
        input.add(connect, position);

        retreive = new JButton( "Retreive Audio Files" );
        retreive.setForeground(Color.BLACK);
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(-400, 25, 0, 0);
        viewArea.add(retreive, position);

        disconnect = new JButton();
        try {
            Image img = ImageIO.read(getClass().getResource("images/hangup.png"));
            Image newimg = img.getScaledInstance(70, 70,  java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon( newimg );
            disconnect.setIcon(icon);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(10, -300, 0, 0);
       // optionsPanel.add(disconnect, position);

        stop = new JButton();
        try {
            Image img = ImageIO.read(getClass().getResource("images/stop.png"));
            Image newimg = img.getScaledInstance(70, 70,  java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon( newimg );
            stop.setIcon(icon);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        position = makeConstraints(0, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(10, 0, 0, 0);
        optionsPanel.add(stop, position);

        record = new JButton();
        try {
            Image img = ImageIO.read(getClass().getResource("images/record.png"));
            Image newimg = img.getScaledInstance(70, 70,  java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon( newimg );
            record.setIcon(icon);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        position = makeConstraints(1, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(10, 150, 0, 0);
        optionsPanel.add(record, position);

        sendAudio = new JButton();
        try {
            Image img = ImageIO.read(getClass().getResource("images/send.png"));
            Image newimg = img.getScaledInstance(70, 70,  java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon( newimg );
            sendAudio.setIcon(icon);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        position = makeConstraints(2, 0, 1, 1, GridBagConstraints.LINE_START);
        position.insets =  new Insets(10, 150, 0, 0);
        optionsPanel.add(sendAudio, position);

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
        userName.addActionListener(this);
        file.addActionListener(this);
        quit.addActionListener(this);
        reset.addActionListener(this);
        retreive.addActionListener(this);

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

        //set running variable to false if STOP button
        if (e.getSource() == connect) {
            chat.append("Hello, " + userName.getText() + "! \n");
            chat.setFont(messageFont);
           if(!serverHostName.getText().equals("") && !userName.getText().equals("")) {
                try {
                    me = new Client(serverHostName.getText(), userName.getText(),false);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Error setting up connection");
                }
            } else {
                JOptionPane.showMessageDialog(null, "All connection setup fields must have values");
            }
        }

        if (e.getSource() == retreive) {
                createFileLabels();
        }
    }

    public void createFileLabels() {

        File folder = new File("audioFiles/");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                String name = listOfFiles[i].getName();
                JLabel newLabel = new JLabel(listOfFiles[i].getName());
                newLabel.setBorder(new LineBorder(Color.BLACK, 1));
                newLabel.setFont(uiFont);
                newLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println("Action listener added");
                        myCapture.play("audioFiles/" + name);
                    }
                });
                availableFileInfo.add(newLabel);
            }
        }
        displayAudioFiles();
    }

    //Method to update command line JPanel area
    public void displayAudioFiles() {

        GridBagConstraints position = new GridBagConstraints();
        int insetFromTop = 0;

        for(int i = 0; i < availableFileInfo.size(); ++i) {
            position = makeConstraints(0, 0, 1, 1, GridBagConstraints.FIRST_LINE_START);
            position.insets =  new Insets(insetFromTop, 0, 0, 0);
            screen.add(availableFileInfo.get(i), position);
            insetFromTop = insetFromTop + 30;
        }
        screen.validate();
        screen.repaint();
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
