import java.io.File;
import java.net.*;
import java.util.ArrayList;

// FTP Server
// Listens for clients to initiate a control connection to the server on port 21.
// Multi-threading is used to handle multiple clients.
public class FTPServer {

    static String fileSeparator = System.getProperty("file.separator"); 
    static File FTPRoot = new File(System.getProperty("user.dir") + fileSeparator + "FTPFiles");
    public static ArrayList<FTPState> FTPUser = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        //Listens for a client on port 21.
        ServerSocket welcomeControlSocket = new ServerSocket(21);
        System.out.println("220 Service ready for new user.");

        //FTPRoot.mkdir(); // Makes the root file "FTPFiles"
        while (true) {
            //Create a new instance for each new client.
            FTPSession client = new FTPSession(welcomeControlSocket.accept());
            client.start();

        }
    }
}
