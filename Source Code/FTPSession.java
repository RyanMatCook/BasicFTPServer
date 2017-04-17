


//import deliv.FTPState;
import java.io.*;
import java.net.*;

// FTP Session
// An instance of the FTPSession is created by the FTPServer for each new user.
// Commands received from the client and responses generated are handled here.


public class FTPSession extends Thread{
	//
	// Control Connection
	//
	private Socket controlConnection; 
	private DataOutputStream controlOut; //Sending replies to client
	private BufferedReader controlIn; // Receiving commands from client

	//
	// Data Connection
	//
	private Socket dataConnection;
	private DataOutputStream dataOut; //Sending data requested from client
	private BufferedReader inData;
	
	//
	// User state, a new instance is created for a new user.
	//	
	FTPState User;
	
	//
	// Path information
	//
	 //Path of the FTP root. Only files stored inside this directory are accessible to client
	static String fileSeparator = System.getProperty("file.separator"); //The system file separator. For Unix, the file separator is "/". The file separator can also be determined by using the System.getProperty("file.separator") method.
	static File FTPRoot = new File(System.getProperty("user.dir")+fileSeparator+"FTPFiles");
	//
	//
	// FTPSession Constructor
	//
	public FTPSession(Socket socket) throws IOException {	
		controlConnection = socket;
		controlIn = new BufferedReader(new InputStreamReader(controlConnection.getInputStream()));
		controlOut = new DataOutputStream(controlConnection.getOutputStream());
                User = new FTPState();
	}
	
	//
	// The run method...
	// 
	public void run(){
		//
		// Only execute other commands once the user has been authorized (with the exception of the "HELP" command). 
		//
		
		try{
                    String input = controlIn.readLine();
                    //System.out.println("Input: "+input);
                    if(!input.equals("REQUESTID")){
                        String[] inputs = input.split(" ");
                        if(inputs.length == 2){
                            User = FTPServer.FTPUser.get(Integer.parseInt(inputs[1]));
                            input = input.substring(0,input.lastIndexOf(" "));
                        }
                        else if(inputs.length==3){
                            User = FTPServer.FTPUser.get(Integer.parseInt(inputs[2]));
                            input = input.substring(0,input.lastIndexOf(" "));
                        }
                    }
                    if (User.userLoggedIn){
                            executeCommand(input);
                    } else {
                            executeAuthorization(input);
                    }
		}  catch (Exception e) {
                    System.out.println(e);
		}
            FTPServer.FTPUser.set(User.getUserIDNum(), User);
	}

	
	//
	// Method authorizing the user name and password
	//
	private void executeAuthorization(String s) throws Exception{
		String command, arguement = "";
		boolean validInput = false;
		
		// The first part of the string is always the abbreviated command. 
		command = s.split(" ")[0].toUpperCase(); 
		
		// Certain commands require an additional argument such as pathname. If this is not present, the input is not valid.
		if (s.split(" ").length==2){
			arguement = s.split(" ")[1];
			validInput = true;
		}
		
		switch (command){
		// Authorizing the user during log-in. 
                    case "REQUESTID":
                        User.setUserIDNum(FTPServer.FTPUser.size());
                        FTPServer.FTPUser.add(User);
                        controlOut.writeByte(User.getUserIDNum());
                        break;
                    case "USER":
                            if (validInput){
                                    executeUSER(arguement);
                            } else {
                                    controlOut.writeBytes("530 Login incorrect"+"\n");
                            }
                            controlOut.close();
                            break;

                    // Authorizing the user's password during log-in. 	
                    case "PASS":
                            if (validInput){
                                    executePASS(arguement);
                            } else {
                                    controlOut.writeBytes("530 Login incorrect, please re-enter your username"+"\n");
                            }
                            controlOut.close();
                            break;

                    // The server sends helpful information regarding FTP commands over the control connection to the user.
                    case "HELP":
                            executeHELP();
                            controlOut.close();
                            break;

                    // The FTP Server accepts no other commands other than "USER", "PASS" and "HELP" if the user is not logged in. 
                    default:
                            controlOut.writeBytes("530 Not logged in."+"\n");
                            controlOut.close();
                    }
	}


	//
	// Method executing the command from the FTPClient. 
	// Each command is handled in a different method.
	//
	private void executeCommand(String s) throws Exception{
		String command, arguement = "", confirmTransfer ;
		boolean validInput = false;
			
		// The first part of the string is always the abbreviated command. 
		command = s.split(" ")[0].toUpperCase(); 
		
		// Certain commands require an additional argument such as pathname. If this is not present, the input is not valid.
		if (s.split(" ").length==2){
			arguement = s.split(" ")[1];
			validInput = true;
		}
		switch (command){                    
                case "PORT":
                    try{
                        User.dataPort = Integer.parseInt(arguement);
                        //System.out.println("User, " + User.username + ", data connection port set to: " + User.dataPort); 
                        // how else will we know it worked other than printing it
                        controlOut.writeBytes("200 Command PORT okay." +"\n");
                        controlOut.close();
                        break;
                    }catch(NumberFormatException e){
                        controlOut.writeBytes("501 Syntax error in parameters or arguments." +"\n");
                    }
		//List files in the current directory
		case "LIST": 
			controlOut.writeBytes("125 Data connection already open; transfer starting."+"\n");			
			confirmTransfer = executeLIST();		
			controlOut.writeBytes(confirmTransfer+"\n");		
			break;
			
		//Print working directory	
		case "PWD": 
			controlOut.writeBytes("257 "+User.CurrentDirectory.getCanonicalPath()+"\n");	
			controlOut.close();
			break;
			
		//Change working directory
		case "CWD": 
			if (validInput){
				executeCWD(arguement);	
			} else {
				controlOut.writeBytes("501 Syntax error in parameters or arguments." +"\n");
			}
			controlOut.close();
			break;
			
		//Delete the specified file in the current directory.
		case "DELE":  
			if (validInput){
				executeDELE(arguement);	
			} else {
				controlOut.writeBytes("501 Syntax error in parameters or arguments." +"\n");
			}
			controlOut.close();
			break;
			
		//Creates the directory specified in the pathname as a directory
		case "MKD": 
			if (validInput){
				executeMKD(arguement);	
			} else {
				controlOut.writeBytes("501 Syntax error in parameters or arguments." +"\n");
			}
			controlOut.close();
			break;
			
		//Removes the directory specified in the pathname
		case "RMD": 
			if (validInput){
				executeDELE(arguement);	
			} else {
				controlOut.writeBytes("501 Syntax error in parameters or arguments." +"\n");
			}
			controlOut.close();
			break;
			
		//Retrieve a file specified by the pathname from the SERVER.
		case "RETR": 
			if (validInput){
				controlOut.writeBytes("125 Data connection already open; transfer starting."+"\n");			
				confirmTransfer = executeRETR(arguement); 
				controlOut.writeBytes(confirmTransfer+"\n");	
				
			} else {
				controlOut.writeBytes("450 Requested file action not taken. File unavailable." +"\n");
			}
			controlOut.close();
			break;
			
		//Sends a file specified by the pathname to the SERVER.
		case "STOR": 
			if (validInput){
				controlOut.writeBytes("125 Data connection already open; transfer starting."+"\n");			
				confirmTransfer = executeSTOR(arguement); 
				controlOut.writeBytes(confirmTransfer+"\n");	
			} else {
				controlOut.writeBytes("450 Requested file action not taken. File unavailable." +"\n");
			}
			controlOut.close();
			break;
		
		//Close the TCP connection between the CLIENT and SERVER.
		case "QUIT":
			controlOut.writeBytes("221 Service closing control connection. Logged out if appropriate."+"\n");
			controlConnection.close();
			break;
		
		//The server sends helpful information regarding FTP commands over the control connection to the user.
		case "HELP":
			executeHELP();
			controlOut.close();
			break;
		
		//If the user entered a command not recognized by this FTP Server:
		default:
			controlOut.writeBytes("500 Syntax error, command unrecognized."+"\n");
			controlOut.close();
			break;
		}
	}
	 
	//
	// Method executing the "HELP" command. 
	// The contents of the "Help.txt" file are written out line by line via the controlConnection to the FTPClient.
	// The "Help.txt" file is located at the client's local folder. 
	//
	private void executeHELP() throws Exception {
		File file = new File("./Help.txt");
		
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader fileReader = new BufferedReader(new InputStreamReader(fis));
		 
			String line;
			while ((line = fileReader.readLine()) != null) {
				controlOut.writeBytes(line+"\n");
			}
			
			fileReader.close();
		} catch (FileNotFoundException ex) {
			controlOut.writeBytes("550 Requested action not taken. File unavailable."+"\n");
		}
	}
	
	
	//
	// Method executing the "MKD" command. 
	// If the directory specified by a pathname does not exist, the directory is created.
	//
	private void executeMKD(String folderName) throws Exception{
		File directory = new File(User.CurrentDirectory.getCanonicalPath()+fileSeparator+folderName); 

		if (!directory.exists()) {
		    try{
		    	directory.mkdir();
		    	controlOut.writeBytes("250 Directory created: " + directory+"\n");
		     } catch(SecurityException se){
		    	controlOut.writeBytes("550 No permission"+"\n");
		     }        
		  } else {
				controlOut.writeBytes("550 Already exists."+"\n");
		  }

	}
	
	//
	// Method executing the "DELE" and "RMD" command. 
	// If the directory or file specified by a pathname exists, that file or directory is deleted.
	//
	private void executeDELE(String fileName) throws Exception{
		File file = new File(User.CurrentDirectory.getCanonicalPath()+fileSeparator+fileName); 
    	
		if (file.exists()){
    		file.delete();
    		controlOut.writeBytes("250 "+file.getCanonicalPath()+ " has been deleted."+"\n");
    	} else {
    		controlOut.writeBytes("550 Requested action not taken. File unavailable."+"\n");
    	}
		
	}

	//
	// Method executing the "STOR" command. 
	// The server initiates the data connection on port 20. 
	// Lines are read from the dataConnnection socket (sent from FTPClient).
	// Lines are written to a file in the current directory.
	// The name of the new file is specifed by a filename.
	// The data connection is closed once the file is transfered.
	//
	private String executeSTOR(String fileName) throws Exception{
                InetAddress ip = controlConnection.getInetAddress(); // gets the IP address of the host connected to the connection socket
		dataConnection = new Socket(ip,User.dataPort);
		
		inData = new BufferedReader(new InputStreamReader(dataConnection.getInputStream()));
		String data = inData.readLine();
		
		if (!data.equals("THROW")){
			
			File location = new File(User.CurrentDirectory.getCanonicalPath()+fileSeparator+fileName);
			File newFile = location;
			
			BufferedWriter writeToFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)));
			
			while (data != null){
				writeToFile.write(data);
				writeToFile.newLine();
                                data=inData.readLine();
			}
			writeToFile.close();
		} else {
			return "550 Requested action not taken. File unavailable.";
		}
		
		dataConnection.close();
			
		return "250 Requested file action okay, completed. " + fileName + " stored in "+User.CurrentDirectory;
	}

	//
	// Method executing the "RETR" command. 
	// The server initiates the data connection on port 20. 
	// Lines are written from a specifed file to the dataConnnection socket (to FTPClient).
	// The name of the new file is specifed by a filename.
	// The data connection is closed once the file is transfered.
	//	
	private String executeRETR(String fileName) throws Exception{
            InetAddress ip = controlConnection.getInetAddress();
		dataConnection = new Socket(ip,User.dataPort);
		
		dataOut = new DataOutputStream(dataConnection.getOutputStream());
		
		File pathname = new File(User.CurrentDirectory.getCanonicalPath()+fileSeparator+fileName);
	
		try {
			FileInputStream fis = new FileInputStream(pathname);
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        //User = FTPServer.FTPUser.get(br.read());
			String line = br.readLine();
			while (line != null) {
				dataOut.writeBytes(line+"\n");
                                line = br.readLine();
			}
			
			br.close();
		} catch (FileNotFoundException ex) {
			dataOut.writeBytes("THROW"+"\n");
			return "550 Requested action not taken. File unavailable.";
		}
		
		dataConnection.close();
		
		return "250 Requested file action okay, completed." + fileName;
	}

	//
	// Method executing the "CWD" command. 
	// If the argument is "..", the directory needs to be traversed one level upwards.
	// If the argument is ".", the directory remains the same. 
	// If the argument is a "filename", append the filename to the current directory.
	// Note: the directory can not be traversed a level above the FTP root.
	//		
	private void executeCWD(String argument)  throws Exception{
    	String filename = User.CurrentDirectory.getCanonicalPath();
 
    	if (argument.equals("..")){
    		int ind = filename.lastIndexOf(fileSeparator);
    		if (ind > 0){
    			filename = filename.substring(0, ind);
    		}
    	} else if (!argument.equals(".")){
    		filename = filename + fileSeparator + argument;
    	}

    	//
    	// Confirm that the specified directory exists and is not the FTP root's parent directory.
    	//
    	File f = new File(filename);

    	if ((filename.length()>= FTPRoot.getCanonicalPath().length())){
    		if (f.exists()){
    			User.CurrentDirectory = f;
    			controlOut.writeBytes("250 The current directory has been changed to " + User.CurrentDirectory.getCanonicalPath()+"\n");
    		} else {
    		controlOut.writeBytes("550 Requested action not taken. File unavailable."+"\n");
    		}
    	}else {
    		controlOut.writeBytes("550 Requested action not taken. File unaccessible by user - FTP root reached."+"\n");
    	}
    			
    }

	//
	// Method executing the "PASS" command. 
	// The password is authorized using the FTPState instance of the user.
	//
	private void executePASS(String pass) throws Exception{
    	if (User.Password(pass)){
			controlOut.writeBytes("230 User logged in, proceed."+"\n");
		} else {
			controlOut.writeBytes("530 Login attempt by "+User.username+" rejected"+"\n");
		}
		
	}

	//
	// Method executing the "USER" command. 
	// The user name is authorized by the FTPState.
	// If the user name is valid, a new instance of the FTPState is created to keep the state of the user.
	//
	private void executeUSER(String name) throws Exception{		
		User.username = name;
		
		if (User.Username()){
			controlOut.writeBytes("331 Username OK, password required for "+name+"\n");
		} else {
			controlOut.writeBytes("530 Login attempt by "+name+" rejected"+"\n");
		}
		//controlOut.close();
		
	}
		
	//
	// Method executing the "LIST" command. 
	// The server initiates the data connection on port 20.
	// The method writes all the files in the current directory to the dataConnection Socket (to the FTPClient).
	// The data connection is closed once the file is transfered.
	//	
	private String executeLIST() throws IOException {
		InetAddress ip = controlConnection.getInetAddress(); 
		dataConnection = new Socket(ip,User.dataPort);
			
		inData = new BufferedReader(new InputStreamReader(dataConnection.getInputStream()));
		dataOut = new DataOutputStream(dataConnection.getOutputStream());
		
		File f = User.CurrentDirectory; 

		File[] files = f.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				dataOut.writeBytes("directory: "+file.getCanonicalPath()+ '\n');
			} else {
				dataOut.writeBytes("     file: "+file.getCanonicalPath()+ '\n');
			}
		}
		
		dataConnection.close();	
		
		return "226 Closing data connection. Requested file action successful.";
	}

}

