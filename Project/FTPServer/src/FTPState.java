


import java.io.*;

// FTP State
// Keeps the state of the user.
// Handles log-in authorization - keeps a record of user names and passwords.

public class FTPState {
	static String[][] Login = 
		{{"Ryan","Cook"},
		{"Brendan","Bell"},
		{"Candice","Swarts"},
		{"Asad","Mahmood"}};
	
	// User's name
	public String username;
	private int userIDNum;
	// Is the current user logged in?
	public boolean userLoggedIn = false;
	public int dataPort = 20;
	// Records the current directory of the user.
	static File CurrentDirectory;
	
	//
	// FTPState constructor
	//
	public FTPState() {
		
	}
	
	//
	// Method authorizing the user's name. 
	// The current directory is set to the FTP root if the user is valid.
	//
	public boolean Username(){
		boolean userCorrect = false;
			
		for (int i = 0; i<Login.length; i++){
			if (username.equals(Login[i][0])){
				userCorrect = true;
                                CurrentDirectory = FTPSession.FTPRoot;
				break;
			} 
		}
			
	return userCorrect;
	}
	
	//
	// Method authorizing the user's password. 
	// 
	public boolean Password(String s){
		boolean passCorrect = false;
		String password = s;
		
		for (int i = 0; i<Login.length; i++){
			if (username.equals(Login[i][0]) && password.equals(Login[i][1])){
				passCorrect = true;
				userLoggedIn = true;
				break;
			} 
		}
					
		return passCorrect;
	}
        
        public int getUserIDNum(){
            return userIDNum;
        }
        public void setUserIDNum(int userIDNum){
            this.userIDNum = userIDNum;
        }

}
