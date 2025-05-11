import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;
import javax.sound.sampled.*;

public class Server {

  private int port;
  private List<User> clients;
  private ServerSocket server;

  Clip clip;

  public static void main(String[] args) throws IOException {
    new Server(12345).run();
  }

  public Server(int port) {
    this.port = port;
    this.clients = new ArrayList<User>();

    try {//PLAY SOUND
      AudioInputStream audioStream = AudioSystem.getAudioInputStream(
              getClass().getResourceAsStream("SOUND.wav"));
      clip = AudioSystem.getClip();
      clip.open(audioStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void run() throws IOException {
    server = new ServerSocket(port) {
      protected void finalize() throws IOException {
        this.close();
      }
    };
    System.out.println("Port 12345 is now open.");

    while (true) {
      // accepts a new client
      Socket client = server.accept();

      // get nickname of newUser
      String nickname = (new Scanner ( client.getInputStream() )).nextLine();
      nickname = nickname.replace(",", ""); //  ',' use for serialisation
      nickname = nickname.replace(" ", "_");
      System.out.println("New Client: \"" + nickname + "\"\n\t     Host:" + client.getInetAddress().getHostAddress());

      // create new User
      User newUser = new User(client, nickname);

      // add newUser message to list
      this.clients.add(newUser);

      // Welcome msg
      newUser.getOutStream().println(
          "<img src='https://media.tenor.com/cyORI7kwShQAAAAi/shigure-ui-dance.gif' height='65' width='65'>"
          + "<b>Welcome</b> " + newUser.toString() +
          "<img src='https://media.tenor.com/cyORI7kwShQAAAAi/shigure-ui-dance.gif' height='65' width='65'>"
          );
//https://www.kizoa.fr/img/e8nZC.gif
      // create a new thread for newUser incoming messages handling
      new Thread(new UserHandler(this, newUser)).start();
    }
  }

  // delete a user from the list
  public void removeUser(User user){
    this.clients.remove(user);
  }

  // send incoming msg to all Users
  public void broadcastMessages(String msg, User userSender) {
    for (User client : this.clients) {
      client.getOutStream().println(
          userSender.toString() + "<span>: " + msg+"</span>");
          clip.loop(1);
    }
  }

  // send list of clients to all Users
  public void broadcastAllUsers(){
    for (User client : this.clients) {
      client.getOutStream().println(this.clients);
    }
  }

  // send message to a User (String)
  public void sendMessageToUser(String msg, User userSender, String user){
    boolean find = false;
    for (User client : this.clients) {
      if (client.getNickname().equals(user) && client != userSender) {
        find = true;
        userSender.getOutStream().println(userSender.toString() + " -> " + client.toString() +": " + msg);
        client.getOutStream().println(
            "(<b>Private</b>)" + userSender.toString() + "<span>: " + msg+"</span>");
      }
    }
    if (!find) {
      userSender.getOutStream().println(userSender.toString() + " -> (<b>no one!</b>): " + msg);
    }
  }
}

class UserHandler implements Runnable {

  private Server server;
  private User user;

  public UserHandler(Server server, User user) {
    this.server = server;
    this.user = user;
    this.server.broadcastAllUsers();
  }

  public void run() {
    String message;

    // when there is a new message, broadcast to all
    Scanner sc = new Scanner(this.user.getInputStream());
    while (sc.hasNextLine()) {
      message = sc.nextLine();

      // emojies
      message = message.replace(":)", "<img src='https://media.tenor.com/Wq-MvpjAJksAAAAM/emojilaugh-emoji.gif' height='42' width='42'>");
      message = message.replace(":D", "<img src='https://media.tenor.com/qe1w6kOoCyYAAAAM/allu-arjun-allu.gif' height ='130' width='130'>");
      message = message.replace(":d", "<img src='https://media.tenor.com/qe1w6kOoCyYAAAAM/allu-arjun-allu.gif' height ='130' width='130'>");
      message = message.replace(":(", "<img src='https://media.tenor.com/pF7xS2OpqJMAAAAC/embarrassed-sad.gif' height ='150' width = '200'>");
      message = message.replace("-_-", "<img src='https://media.tenor.com/dzy-pFp4GhoAAAAC/seriously-animation.gif' height ='150' width = '230'>");
      message = message.replace(";)", "<img src='https://media.tenor.com/kS5FOYj7eFoAAAAC/mr-bean-making-faces.gif' height ='150' width = '230' >");
      message = message.replace("XD", "<img src='https://media.tenor.com/zWuFgEM7wTcAAAAM/risitas-laugh.gif'height ='150' width = '200'>");
      message = message.replace("xd", "<img src='https://media.tenor.com/yPTb4CQcumsAAAAM/xd-meme.gif' height ='150' width = '200'>");
      message = message.replace(":-o", "<img src='https://media.tenor.com/ZV33Wcs7QMMAAAAC/pikachu-meme.gif' height ='200' width = '180'>");
      message = message.replace(":-O", "<img src='https://media.tenor.com/ZV33Wcs7QMMAAAAC/pikachu-meme.gif' height ='200' width = '180'>");
      message = message.replace("<3","<img src = 'https://media.tenor.com/s--312__jnoAAAAC/kermit-kermit-love.gif' height='200' width = '200'>");
      message = message.replace("B-)","<img src = 'https://media.tenor.com/MWy1tjpTzBUAAAAd/doge-emote.gif' height='200' width = '200'>");
      message = message.replace("x-x","<img src = 'https://media.tenor.com/Yp59JRWk6csAAAAC/chats-dead.gif' height ='220' width = '200'>");
      message = message.replace(":>","<img src = 'https://media.tenor.com/QpBGkEm5MAAAAAAd/ronaldo-joker-smile-ronaldo.gif'width = '200' height = '200' >");
      message = message.replace(":STFU)","<img src = 'https://media.tenor.com/jmEeIVgFhjQAAAAC/stfu-shut-up.gif' height = '300' width = '100'>");
      message = message.replace(":OOF)","<img src = 'https://media.tenor.com/YLdYhGE5SsYAAAAd/wasted-gta.gif' height = '200' width = '200'>");
      message = message.replace(":SUS)","<img src = 'https://media.tenor.com/GBdIH5sL4XQAAAAC/the-rock-rock.gif' height='200' width = '200'>");
      //http://4.bp.blogspot.com/-ZgtYQpXq0Yo/UZEDl_PJLhI/AAAAAAAADnk/2pgkDG-nlGs/s1600/facebook-smiley-face-for-comments.png
      //'http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png
      //'http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png
      //'http://2.bp.blogspot.com/-rnfZUujszZI/UZEFYJ269-I/AAAAAAAADnw/BbB-v_QWo1w/s1600/facebook-frown-emoticon.png
      //'http://3.bp.blogspot.com/-wn2wPLAukW8/U1vy7Ol5aEI/AAAAAAAAGq0/f7C6-otIDY0/s1600/squinting-emoticon.png
      //'http://1.bp.blogspot.com/-lX5leyrnSb4/Tv5TjIVEKfI/AAAAAAAAAi0/GR6QxObL5kM/s400/wink%2Bemoticon.png
      //'http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png
      //'http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png




      // send message in private
      if (message.charAt(0) == '@'){
        if(message.contains(" ")){
          System.out.println("private msg : " + message);
          int firstSpace = message.indexOf(" ");
          String userPrivate= message.substring(1, firstSpace);
          server.sendMessageToUser(
              message.substring(
                firstSpace+1, message.length()
                ), user, userPrivate
              );
        }

      // change colour need to use colour code
      }else if (message.charAt(0) == '#'){
        user.changeColor(message);
        // update color for all other users
        this.server.broadcastAllUsers();
      }else{
        // update user list
        server.broadcastMessages(message, user);
      }
    }
    // end of Thread
    server.removeUser(user);
    this.server.broadcastAllUsers();
    sc.close();
  }
}

class User {
  private static int nbUser = 0;
  private int userId;
  private PrintStream streamOut;
  private InputStream streamIn;
  private String nickname;
  private Socket client;
  private String color;

  // constructor
  public User(Socket client, String name) throws IOException {
    this.streamOut = new PrintStream(client.getOutputStream());
    this.streamIn = client.getInputStream();
    this.client = client;
    this.nickname = name;
    this.userId = nbUser;
    this.color = ColorInt.getColor(this.userId);
    nbUser += 1;
  }

  // change color user
  public void changeColor(String hexColor){
    // check if it's a valid hexColor
    Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
    Matcher m = colorPattern.matcher(hexColor);
    if (m.matches()){
      Color c = Color.decode(hexColor);
      // if the Color is too Bright don't change
      double luma = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue();
      if (luma > 160) {
        this.getOutStream().println("<b>Color Too Bright</b>");
        return;
      }
      this.color = hexColor;
      this.getOutStream().println("<b>Color changed successfully</b> " + this.toString());
      return;
    }
    this.getOutStream().println("<b>Failed to change color</b>");
  }


  public PrintStream getOutStream(){
    return this.streamOut;
  }

  public InputStream getInputStream(){
    return this.streamIn;
  }

  public String getNickname(){
    return this.nickname;
  }

  // print user with his color
  public String toString(){

    return "<u><span style='color:"+ this.color
      +"'>" + this.getNickname() + "</span></u>";

  }
}

class ColorInt {
    public static String[] mColors = {
            "#3079ab", // dark blue
            "#e15258", // red
            "#f9845b", // orange
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#f092b0", // pink
            "#e8d174", // yellow
            "#e39e54", // orange
            "#d64d4d", // red
            "#4d7358", // green
    };

    public static String getColor(int i) {
        return mColors[i % mColors.length];
    }
}
