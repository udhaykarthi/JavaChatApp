import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;
import javax.sound.sampled.*;

import java.util.ArrayList;
import java.util.Arrays;


public class ClientGui extends Thread{

  final JTextPane jtextFilDiscu = new JTextPane();
  final JTextPane jtextListUsers = new JTextPane();
  final JTextField jtextInputChat = new JTextField();
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name;
  Clip clip;
  BufferedReader input;
  PrintWriter output;
  Socket server;

  public ClientGui() {
    this.serverName = "localhost";
    this.PORT = 12345;
    this.name = "Enter Your Name";

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 20);
    //mainframe
    final JFrame jfr = new JFrame("GroupChat");
    jfr.getContentPane().setLayout(null);
    jfr.setSize(1280, 800);
    jfr.getContentPane().setBackground(new Color(35, 36, 42));
    jfr.setResizable(false);
    jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ImageIcon icon = new ImageIcon("pngs/DALL·E 2023-12-04 21.04.24 - a conversation bubble.png"); //icon
    jfr.setIconImage(icon.getImage());

    //chatbox
    jtextFilDiscu.setBounds(50, 50, 900, 640);
    jtextFilDiscu.setFont(font);
    jtextFilDiscu.setMargin(new Insets(12, 12, 12, 12));
    jtextFilDiscu.setEditable(false);
    JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
    jtextFilDiscuSP.setBounds(50, 50, 800, 500);
    jtextFilDiscu.setOpaque(true);
    jtextFilDiscu.setBackground(new Color(35, 36, 42));
    jtextFilDiscu.setForeground(new Color(255, 255, 255));
    jtextFilDiscu.setContentType("text/html");
    jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    //user box
    jtextListUsers.setBounds(1040, 50, 312, 640);
    jtextListUsers.setFont(font);
    jtextListUsers.setMargin(new Insets(12, 12, 12, 12));
    jtextListUsers.setEditable(false);
    JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
    jsplistuser.setBounds(912, 50, 324, 500);
    jtextListUsers.setContentType("text/html");
    jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Field message user input
    jtextInputChat.setBounds(0, 700, 800, 100);
    jtextInputChat.setFont(font);
    jtextInputChat.setMargin(new Insets(12, 12, 12, 12));
    jtextInputChat.setBorder(new BorderUIResource.LineBorderUIResource(Color.black));
    jtextInputChat.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        jtextInputChat.setBackground(new Color(34, 36, 38));
        jtextInputChat.setForeground(Color.white);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (jtextInputChat.getText().isEmpty()) {
          jtextInputChat.setBackground(new Color(44, 47, 51));
          jtextInputChat.setForeground(Color.white);
        }
      }
    });
    jtextInputChat.setBackground(new Color(44, 47, 51));
    jtextInputChat.setForeground(new Color(255, 255, 255));
    final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
    jtextInputChatSP.setBounds(50, 570, 1184,100);

    //send button
    final JButton jsbtn = new JButton("Send");
    jsbtn.setFont(font);
    jsbtn.setBounds(920, 700, 100, 35);
    jsbtn.setBackground(new Color(88, 101, 242));
    jsbtn.setForeground(new Color(0, 0, 0));
    jsbtn.setBorder(null);


    //Disconnect button
    final JButton jsbtndeco = new JButton("Disconnect");
    jsbtndeco.setFont(font);
    jsbtndeco.setBounds(1100, 700, 130, 35);
    jsbtndeco.setBackground(new Color(88, 101, 242));
    jsbtndeco.setForeground(new Color(0, 0, 0));
    jsbtndeco.setBorder(null);

    jtextInputChat.addKeyListener(new KeyAdapter() {
      // send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });


    // Click on send button
    jsbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    // Connection view
    final JTextField jtfName = new JTextField(this.name);
    final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
    final JTextField jtfAddr = new JTextField(this.serverName);
    final JButton jcbtn = new JButton("Connect");

    // check if those field are not empty
    jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));

    // position of module
    jcbtn.setFont(font);
    jcbtn.setBackground(new Color(88, 101, 242));                    //connect button
    jcbtn.setForeground(new Color(0, 0, 0));
    jcbtn.setBorder(null);
    jcbtn.setBounds(900, 620, 140, 40);


    jtfAddr.setFont(font);
    jtfAddr.setBounds(80, 700, 200, 40);
    jtfAddr.setBackground(Color.DARK_GRAY);
    jtfAddr.setBorder(new BorderUIResource.LineBorderUIResource(Color.black));
    jtfAddr.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        jtfAddr.setBackground(Color.DARK_GRAY);                             ///address of server
        jtfAddr.setForeground(Color.white);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (jtfAddr.getText().isEmpty()) {
          jtfAddr.setBackground(new Color(44, 47, 51));
          jtfAddr.setForeground(Color.white);
        }
      }
    });


    jtfport.setFont(font);
    jtfport.setBounds(80, 640, 200, 40);
    jtfport.setBackground(Color.DARK_GRAY);
    jtfport.setBorder(new BorderUIResource.LineBorderUIResource(Color.black));
    jtfport.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        jtfport.setBackground(Color.DARK_GRAY);                //port number
        jtfport.setForeground(Color.white);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (jtfport.getText().isEmpty()) {
          jtfport.setBackground(new Color(255, 255, 255));
          jtfport.setForeground(Color.white);
        }
      }
    });


    jtfName.setFont(font);
    jtfName.setBounds(300, 680, 250, 40);
    jtfName.setBackground(Color.DARK_GRAY);
    jtfName.setBorder(new BorderUIResource.LineBorderUIResource(Color.black));
    jtfName.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        jtfName.setBackground(Color.DARK_GRAY);                       //NAME
        jtfName.setForeground(Color.white);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (jtfName.getText().isEmpty()) {
          jtfName.setBackground(new Color(0, 0, 0));
          jtfName.setForeground(Color.white);
        }
      }
    });

    // colour
    jtextFilDiscu.setBackground(Color.DARK_GRAY);
    jtextListUsers.setBackground(Color.DARK_GRAY);

    // elements
    jfr.add(jcbtn);
    jfr.add(jtextFilDiscuSP);
    jfr.add(jsplistuser);
    jfr.add(jtfName);
    jfr.add(jtfport);
    jfr.add(jtfAddr);
    jfr.setVisible(true);


    // chatbox msg
    // appendToPane(jtextFilDiscu, "<h4>Enter you message here:</h4>");

    // On connect
    jcbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          name = jtfName.getText();
          String port = jtfport.getText();
          serverName = jtfAddr.getText();
          PORT = Integer.parseInt(port);
          //appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "......</span>");
          appendToPane(jtextFilDiscu, "<span>Connecting......</span>");
          server = new Socket(serverName, PORT);

          appendToPane(jtextFilDiscu, "<span>Connected to " +
                  server.getRemoteSocketAddress() + " </span>");

          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
          output = new PrintWriter(server.getOutputStream(), true);

          // send nickname to server
          output.println(name);

          // create new Read Thread
          read = new Read();
          read.start();
          jfr.remove(jtfName);
          jfr.remove(jtfport);
          jfr.remove(jtfAddr);
          jfr.remove(jcbtn);
          jfr.add(jsbtn);
          jfr.add(jtextInputChatSP);
          jfr.add(jsbtndeco);
          jfr.revalidate();
          jfr.repaint();
          jtextFilDiscu.setBackground(new Color(44, 47, 51));//after connecting
          jtextListUsers.setBackground(new Color(44, 47, 51));
        } catch (Exception ex) {
          appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
          JOptionPane.showMessageDialog(jfr, ex.getMessage());
        }
      }

    });

    // on decoding
    jsbtndeco.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.add(jcbtn);
        jfr.remove(jsbtn);
        jfr.remove(jtextInputChatSP);
        jfr.remove(jsbtndeco);
        jfr.revalidate();
        jfr.repaint();
        read.interrupt();
        jtextListUsers.setText(null);
        jtextFilDiscu.setBackground(Color.DARK_GRAY);//AFTER DISCONETING
        jtextListUsers.setBackground(Color.DARK_GRAY);
        appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
        output.close();
      }
    });
  }



  // check if if all field are not empty
  public class TextListener implements DocumentListener{
    JTextField jtf1;
    JTextField jtf2;
    JTextField jtf3;
    JButton jcbtn;

    public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn){
      this.jtf1 = jtf1;
      this.jtf2 = jtf2;
      this.jtf3 = jtf3;
      this.jcbtn = jcbtn;
    }

    public void changedUpdate(DocumentEvent e) {}

    public void removeUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }
    public void insertUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }

  }

  // on messaging
  public void sendMessage() {
    try {
      String message = jtextInputChat.getText().trim();
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
      output.println(message);
      jtextInputChat.requestFocus();
      jtextInputChat.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    ClientGui client = new ClientGui();
  }

  // read new incoming messages
  class Read extends Thread {
    public void run() {
      String message;
      while(!Thread.currentThread().isInterrupted()){
        try {
          message = input.readLine();
          if(message != null){

            if (message.charAt(0) == '[') {
              message = message.substring(1, message.length()-1);
              ArrayList<String> ListUser = new ArrayList<String>(
                  Arrays.asList(message.split(", "))
                  );
              jtextListUsers.setText(null);
              for (String user : ListUser) {

                appendToPane(jtextListUsers,  "<span>"+"<img src='https://media.tenor.com/e-IagIPq3_cAAAAi/gloorp-dance.gif' height='20' width='20'>" +"--"+ user +" </span>");
              }
            }else{
              appendToPane(jtextFilDiscu, message);
            }
          }
        }
        catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }
  // send html to pane
  private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
      tp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
    } catch(Exception e){
      e.printStackTrace();
    }
  }
}
