package com.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private String clientName;
    private Scanner sc;

    public Client() {
        this.clientSocket = null;
        this.input = null;
        this.output = null;
        this.clientName = null;
        this.sc = new Scanner(System.in);
        Initialize();
    }

    public static void main(String[] args) {
        new Client();
    }

    private void Initialize() {
        System.out.println("Welcome to Csnetwk file management system program");
        System.out.println("Type \"/?\" to display the list of commands\n");
        String[] command;
        boolean isRegistered = false;
        boolean isConnected = false;

        do {

            System.out.print("Enter command here: ");
            command = (sc.nextLine()).split(" ");

            switch (command[0]) {
                case "/join":
                    if (!isConnected) {
                        if (command.length == 3) {
                            try {
                                JoinNetwork(command[1], command[2]);
                                isConnected = true;
                                System.out.println("Connection to the File Exchange " + //
                                        "Server is successful!");
                            } catch (Exception e) {
                                System.out.println("Error: Connection to the Server " +
                                        "has failed! Please check IP " +
                                        "Address and Port Number.");
                                isConnected = false;
                            }
                        } else {
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        }
                    } else {
                        System.out.println("Error: Client is already connected to server.");
                    }
                    break;
                case "/leave":
                    if (isConnected) {
                        if (command.length == 1) {
                            try {
                                LeaveNetwork();
                                System.out.println("Connection closed. Thank you!");
                                isConnected = false;
                                isRegistered = false;
                            } catch (IOException i) {
                                System.out.println("Error: Disconnection failed. Please " +
                                        "connect to the server first.");
                                isConnected = true;
                            }
                        } else {
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        }
                    } else {
                        System.out.println("Error: Disconnection failed. Please " +
                                "connect to the server first.");
                    }
                    break;
                case "/register":
                    if (!isRegistered) {
                        if (command.length == 2) {
                            try {
                                RegisterAllias(command[1]);
                                isRegistered = true;
                                System.out.println("Welcome " + clientName);
                            } catch (IOException io) {
                                System.out.println("Error: Registration failed. Client " +
                                        "is not yet connected to server.");
                            } catch (Exception e) {
                                System.out.println("Error: Registration failed. Client " +
                                        "is not yet connected to server.");
                            }
                        } else {
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        }
                    } else {
                        System.out.println("Error: Registration failed. Handle " +
                                "or alias already exists or Client is not connected to server.");
                    }
                    break;
                case "/store":
                    if (isConnected && isRegistered) {
                        if (command.length == 2) {
                            try {
                                StoreFile(command[1]);
                            } catch (IOException io) {
                                System.out.println("Error: File not found.");
                            }
                        } else {
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        }
                    } else {
                        System.out.println("Error: Client is not connected to server " +
                                "or is not registered.");
                    }
                    break;
                case "/get":
                    if (isConnected && isRegistered) {
                        if (command.length == 2) {
                            try {
                                GetFile(command[1]);
                            } catch (IOException io) {
                                System.out.println("Error: File not found.");
                            }
                        } else {
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        }
                    } else {
                        System.out.println("Error: Client is not connected to server " +
                                "or is not registered.");
                    }
                    break;
                case "/dir":
                    if (isConnected && isRegistered) {
                        if (command.length == 1) {
                            try {
                                ListFiles();
                            } catch (IOException io) {
                                System.out.println("Error: Cannot get Files from Server");
                            }
                        } else {
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        }
                    } else {
                        System.out.println("Error: Client is not connected to server " +
                                "or is not registered.");
                    }
                    break;
                case "/?":
                    DisplayCommands();
                    break;
                case "/exit":
                    if (!this.clientSocket.isClosed()) {
                        try {
                            this.LeaveNetwork();
                        } catch (IOException e) {

                            System.out.println("Error: Unable to Disconnect from the server.");
                        }
                    }

                    System.out.println("Exiting Program. . .");
                    break;
                default:
                    System.out.println("Error: Command not found.");
            }
            System.out.println("\n\n");
        } while (!command[0].equals("/exit"));
    }

    private void DisplayCommands() {
        System.out.println("\n\nList of commands: ");
        System.out.println("Connect to server:     /join <server_ip_add> <port>");
        System.out.println("Disconnect to server:     /leave");
        System.out.println("Register a username:     /register <username>");
        System.out.println("Send file to server:     /store <filename>");
        System.out.println("Fetch file from server:     /get <filename>");
        System.out.println("Request file list from server:     /dir");
        System.out.println("Display all commands:     /?");
        System.out.println("Exit Program:     /exit");
    }

    private void JoinNetwork(String address, String port) throws Exception {
        int nPort = Integer.parseInt(port);
        this.clientSocket = new Socket(address, nPort);
        this.input = new DataInputStream(clientSocket.getInputStream());
        this.output = new DataOutputStream(clientSocket.getOutputStream());
        this.clientName = input.readUTF();
    }

    private void RegisterAllias(String name) throws IOException, Exception {
        output.writeUTF("Register");
        output.writeUTF(name);

        String msg = input.readUTF();

        if (msg.equals("VALID")) {
            this.clientName = name;
        } else {
            throw new Exception();
        }
    }

    private void LeaveNetwork() throws IOException {
        output.writeUTF("Leave");
        this.clientSocket.close();
        this.clientSocket = null;
        this.input = null;
        this.output = null;
    }

    private void StoreFile(String filename) throws IOException {
        File file = new File("com/client/ClientFiles/" + filename);

        if (file.exists() && file.canRead()) {
            output.writeUTF("Store");
            output.writeUTF(filename);
            output.writeLong(file.length());

            FileInputStream fis = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            fis.close();
            output.flush();

            String response = input.readUTF();
            System.out.println(response);
        } else {
            System.out.println("Error: File not found.");
        }
    }

    private void GetFile(String filename) throws IOException {
        output.writeUTF("Get");
        output.writeUTF(filename);

        String confirmation = input.readUTF();

        if (confirmation.equals("TRUE")) {
            File file = new File("com/client/ClientFiles/" + filename);
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;

            long fileSize = input.readLong();

            while (fileSize > 0 && (bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fos.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }

            fos.close();
        }

        System.out.println(input.readUTF());
    }

    private void ListFiles() throws IOException {
        output.writeUTF("List");

        int max = Integer.parseInt(input.readUTF());
        System.out.println(input.readUTF());
        for (int i = 0; i < max; i++) {
            System.out.println(input.readUTF());
        }

        if (max == 0) {
            System.out.println("None");
        }
    }
}
