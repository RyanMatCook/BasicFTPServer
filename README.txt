
File Transfer Protocol (FTP) in compliance with RFC 959

_______________________________________________________________________________
TABLE OF CONTENTS:

1. Introduction
2. FTP Commands
3. FTP Replies
4. Classes Implemented with their Methods
5. Instructions

_______________________________________________________________________________
1. INTRODUCTION

The FTP is used mainly for sharing of files across a data reliable transfer connection. The FTP requires two connections to be established between the client and the server, the control connection and the data connection.

1.1 Control Connection
The communication path between the client and the server is for the exchange of commands and replies. The control connection is initiated by the client on port 21. This connection is a persistent connection, implying that it will only close when instructed to do so by the user.


1.2 Data Connection
The connection over which data is transferred between the client and the server. The server will initiate the data connection on port 20 upon receiving the data transfer request. This connection is a non-persistent connection, it will close after each file transfer attempt.

_______________________________________________________________________________
2. FTP COMMANDS

2.1 USER <SP> <username> <CRLF>

Explanation:
- Used to identify the user and keep the user state on the FTP Server.

2.2 PASS <SP> <password> <CRLF>

Explanation:
- Associated password used to log in.

2.3  CWD  <SP> <pathname> <CRLF>

CWD .

Explanation:
- Change working directory.

CWD ..

Explanation:
- Change working directory by traversing one level up.

CWD filename

Explanation:
- Change working directory by traversing to the filename.

2.4 PWD  <CRLF>

Explanation:
- Prints the working directory.

2.5 LIST <SP> <pathname> <CRLF>

Explanation:
- Lists the files and folders in the current directory.

2.6 RETR <SP> <pathname> <CRLF>

Explanation:
- Retrieve the specifed file from the FTP Server and save it to the client's local folder.

2.7 STOR <SP> <pathname> <CRLF>

Explanation:
- Store the specified file in the client's local folder to the FTP Server in the current directory.

2.8  DELE <SP> <pathname> <CRLF>

Explanation:
- Delete the specified file in the current directory. 

2.9 MKD  <SP> <pathname> <CRLF>

Explanation:
- Creates the directory specified in the pathname as a directory.

2.10 RMD  <SP> <pathname> <CRLF>

Explanation:
- Removes the directory specified in the pathname.

2.11 PORT  <SP> <port number> <CRLF>

Explanation:
- Changes the port number for the data connection from its default value (port number: 20) to the value specified. 

2.12 HELP

Explanation:
- The server sends helpful information regarding FTP commands over the control connection to the user.
- The user does not have to be logged in. 

2.13 QUIT <CRLF>

Explanation:
- Terminates a USER and the server closes the control connection.


_______________________________________________________________________________
3. FTP REPLIES (in numerical order)

3.1 125 Data connection already open; transfer starting.

3.2 200 Command PORT okay.

3.3 220 Service ready for new user.

3.4 221 Service closing control connection. Logged out if appropriate.

3.5 226 Closing data connection. Requested file action successful.

3.6 230 User logged in, proceed.

3.7 

250 The current directory has been changed to PATHNAME 
250 Requested file action okay, completed.
250 Directory created: PATHNAME

3.8 257 PATHNAME 

3.9 331 Username OK, password required for USER

3.10 450 Requested file action not taken. File unavailable.

3.11 500 Syntax error, command unrecognized.

3.12 501 Syntax error in parameters or arguments.

3.13

530 Login incorrect, please re-enter your username
530 Login attempt by USER rejected
530 Not logged in.

3.14

550 Requested action not taken. File unavailable.
550 No permission
550 Already exists.

_______________________________________________________________________________
4. Classes implemented:

4.1 FTPServer
Listens for clients (FTPClient) to initiate a control connection to the server on port 21. An instance of the FTPSession is created by the FTPServer to handle each new user.

4.2 FTPClient
Initiates the control connection to the server on port 21. The FTPClient sends commands to the server which the server has to respond to.
The FTPClient writes lines from a file to the data connection when the "STOR" command is used. 
When the "RETR" command is used, the FTPClient writes lines from the data connection to a file on the client's local folder. 

4.3 FTPSession
The commands received from the FTPClient and the responses that need to be generated are handled in this class. 
A data connection is initiated by the server when a file transfer is required.

4.4 FTPState
Keeps the current status of the user, specifically, the current directory the user is in.
Controls log-in authorization and keeps a record of usernames and passwords.

_______________________________________________________________________________
5. Instructions

5.1 

RUNNING THE PROGRAM FROM JAR EXECUTABLES:
**This was tested using Windows 7 operating system**

AUTOMATICALLY RUN THE CLIENT AND SERVER APPLICATION

To save yourself typing commands to start the client-server application,
simply run the runServer.bat file and then run the runClient.bat file.

MANUALLY RUN THE FTP SERVER AND CLIENT APPLICATION

Open command prompt (start menu > search cmd > open cmd)

Execute the following command:
java -jar "<FULL PATH TO FTPServerText.jar>"
Now open a new terminal and execute the following:
java -jar "<FULL PATH TO FTPClientText.jar>"

The client may now execute the following commands:

LOG IN:
1. Type "USER username" via the keyboard into the FTPClient console. 
For purpose of demonstrating the program, "username" will be your first name, e.g. "Asad", (case-sensitive). 

2. Type "PASS password" via the keyboard into the FTPClient console once the "username" has been accepted.
Again, for the purpose of demonstrating the program, the "password" will be your surname, e.g. "Mahmood",  (case-sensitive).

EXECUTING COMMANDS:
The user may only execute commands once they have been authorized. 
See section "2. FTP Commands" for a list of commands that can be executed. 
The FTPServer will respond to the user's command with a reply (see section "3. FTP Replies"). 

LOG OUT:
Simply enter "QUIT" to log out of the FTPServer. The user can only be changed once the user has logged out.  

HELP:
The "HELP" command can be requested at any time, regardless if the user is logged in.

5.2
RUNNING THE PROGRAM FROM SOURCE CODE:
Compile the FTPServer.java
Compile the FTPClient.java

Run the FTPServer code. The server will start and be ready for connections. 

Run the FTPClient code. The client will connect to the server which must be hosted on a local host. 

The client may now execute the following commands:

LOG IN:
1. Type "USER username" via the keyboard into the FTPClient console. 
For purpose of demonstrating the program, "username" will be your first name, e.g. "Asad", (case-sensitive). 

2. Type "PASS password" via the keyboard into the FTPClient console once the "username" has been accepted.
Again, for the purpose of demonstrating the program, the "password" will be your surname, e.g. "Mahmood",  (case-sensitive).

EXECUTING COMMANDS:
The user may only execute commands once they have been authorized. 
See section "2. FTP Commands" for a list of commands that can be executed. 
The FTPServer will respond to the user's command with a reply (see section "3. FTP Replies").

LOG OUT:
Simply enter "QUIT" to log out of the FTPServer. The user can only be changed once the user has logged out.  

HELP:
The "HELP" command can be requested at any time, regardless if the user is logged in.

**Please note: the "Help.txt" file needs to be included in the JAVA project folder created by Eclipse in the workspace. ** 

 





