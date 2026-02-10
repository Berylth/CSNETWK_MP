# CSNETWK_MP: File Exchange System
CSNETWK_MP: Simple File Exchange System written in Java using sockets and multithreading.

## How to run:
1) Download the repository via git:  
    ```
    git clone https://github.com/Berylth/NSCOM1_MCO2.git
    ```

2) Move to src folder via the command:
    ```
    cd src
    ```
    
3) Run the Client via the command:
    ```
    java com/client/Client
    ```

4) Run the Server via the command:
    ```
    java com/server/Server
    ```

5) Connect to the server by typing the command:
    ```
    /join <server_ip> <server_port>
    ```

6) Other commands for the client can be displayed using the command below:
    ```
    /?
    ```

## Command list
Below is the list of commands for the client application:

1) /join <server_ip> <server_port>
    - Command to connect to the server specified its ip and port.
2) /?
    - Display all commands.
3) /register <username>
    - Register user to the server. Required to access the server's filesystem.
4) /store <filename>
    - Sends and stores the file to the server given the filename stored in the ClientFiles directory.
5) /get <filename>
    - Feches file soted in the server given the filename.
6) /dir
    - Displays the list of files stored in the server.
7) /leave
    - Disconnects from the server.
8) /exit
    - Exits program.

## Members:
- Sacdalan, Justin Morrie
- Magura, Bryle Jhone
