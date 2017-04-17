import java.io.*;
import java.net.*;

// FTP Client
// Initiates the control connection to the server on port 21.
// Sends the server commands.
// FTPClient writes lines from the data connection (from server) to a file when the "RETR" command is used.
// FTPClient writes lines from a file to a data connection (to server) when the "STOR" command is used.
public class FTPClient {

    static String fileSeparator = System.getProperty("file.separator"); 
    static File FTPClientRoot = new File(System.getProperty("user.dir") + fileSeparator + "ClientFiles");
    static String homeDir = System.getProperty("user.dir") + fileSeparator + "ClientFiles";
    
    public static void main(String args[]) throws Exception {
		//
        // Control Connection -  
        // The communication path between the USER and SERVER for the exchange of commands and replies.
        //
        Socket controlSocket;
        BufferedReader inFromUser, inFromServer; //Input stream, provides the input to the attached socket.
        DataOutputStream outToServer; //Output stream, provides the process output to the attached socket.
        String command, reply;
        int userIDNum = -1;
		//
        // Data Connection
        // Connection over which data is transferred between the USER and SERVER.
        //
        Socket dataSocket;
        int dataPort = 20;
        ServerSocket welcomeDataSocket;
        BufferedReader inFromData; //Input stream, provides the input to the attached socket.
        DataOutputStream outToData; //Output stream, provides the process output to the attached socket.
        if(!FTPClientRoot.exists())
            FTPClientRoot.mkdir(); // Makes the root directory "ClientFiles"
        
        while (true) {
            //Create a controlSocket on port 21.
            controlSocket = new Socket("localhost", 21);
            if (userIDNum == -1) {
                outToServer = new DataOutputStream(controlSocket.getOutputStream());
                outToServer.writeBytes("REQUESTID\n");
                inFromServer = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
                userIDNum = inFromServer.read();
                continue;
            }
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            command = inFromUser.readLine(); //A line typed by the user until a carriage return is typed into the command string.
            if(command.split(" ")[0].equalsIgnoreCase("PORT")){
                dataPort = Integer.parseInt(command.split(" ")[1]);
            }
            outToServer = new DataOutputStream(controlSocket.getOutputStream());
            outToServer.writeBytes(command + " " + userIDNum + '\n'); //Sends the command to the outToServer stream.		

            inFromServer = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            reply = inFromServer.readLine();

			//
            // If the reply is "125 Data connection already open; transfer starting.", then the 
            // client needs to listen for the server initiated a data connection on port 20.
            //
            if (reply.equals("125 Data connection already open; transfer starting.")) {
                System.out.println(reply);

                //Listens for the server initializing the data connection on port 20.
                welcomeDataSocket = new ServerSocket(dataPort);
                dataSocket = welcomeDataSocket.accept();

                outToData = new DataOutputStream(dataSocket.getOutputStream());
                inFromData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                String data;

                switch (command.split(" ")[0].toUpperCase()) {

				//
                    // Read lines from the dataConnection Socket.
                    // Write lines to a new file in the client's local folder. 
                    //
                    case "RETR":
                        data = inFromData.readLine();

                        if (!data.equals("THROW")) {

                            String fileName = command.split(" ")[1];
                            System.out.println("Storing to path: "+homeDir + fileSeparator + fileName);
                            String location = homeDir + fileSeparator + fileName;
                            File newFile = new File(location);

                            BufferedWriter writeToFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)));

                            while (data != null) {
                                writeToFile.write(data);
                                writeToFile.newLine();
                                data = inFromData.readLine();
                            }
                            writeToFile.close();
                        }
                        break;

					//
                    // Read lines from a file in a local folder.
                    // Send the lines to the server via the dataConnection socket.
                    //
                    case "STOR":
                        String fileName = command.split(" ")[1];
                        File pathname = new File(homeDir + fileSeparator + fileName);

                        try {
                            FileInputStream fis = new FileInputStream(pathname);
                            //Construct BufferedReader from InputStreamReader
                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                            String line = br.readLine();
                            //outToData.writeByte(userIDNum);
                            while (line != null) {
                                outToData.writeBytes(line + "\n");
                                line = br.readLine();
                            }

                            br.close();
                        } catch (FileNotFoundException ex) {
                            outToData.writeBytes("THROW" + "\n");
                        }
                        break;

					//
                    // If the command was neither "STOR" or "RETR", just read the data sent from the server.
                    //
                    default:
                        while ((data = inFromData.readLine()) != null) {
                            System.out.println(data);
                        }
                }

				//
                //Close the socket to avoid "JVM_Bind" Error.
                //
                outToData.close();
                dataSocket.close();
                welcomeDataSocket.close();

				//
                // Confirmation of a successful or a failed transfer
                //
                reply = inFromServer.readLine();
                System.out.println(reply);

            } else {
                if (reply.equals("200 Command PORT okay.")) {
                    System.out.println(reply);
                } else {

				//
                    // If a data connection is not initiated, simply read the response from the server. 
                    //
                    while ((reply) != null) {
                        System.out.println(reply);
                        reply = inFromServer.readLine();
                    }
                }

			//
                // If the command is "QUIT", close the control connection.
                // End the TCP connection.
                //
                if (command.equalsIgnoreCase("QUIT")) {
                    controlSocket.close();
                    break;
                }
            }
        }
    }
}
