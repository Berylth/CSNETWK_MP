package com.server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Server {
    private static int Port = 12345;
    private static HashMap<String, String> clientMap = new HashMap<>();
    private static int nConnections = 0;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(Port);
            System.out.println("Server started on port " + Port + ". . . . \n\n");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected from " + socket.getInetAddress() + "\n");

                new Thread(new ServerThread(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println();
        }
    }

    private static class ServerThread extends Thread {
        private Socket socket;
        private DataInputStream input;
        private DataOutputStream output;
        private String clientName;

        private ServerThread(Socket socket) {
            this.socket = socket;
            this.clientName = "Client" + nConnections;
            clientMap.put(this.clientName, String.valueOf(socket.getInetAddress()));
            nConnections++;
        }

        @Override
        public void run() {
            String command = "";

            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                output.writeUTF(this.clientName);
            } catch (IOException io) {
                io.printStackTrace();
            }

            do {
                try {
                    command = input.readUTF();

                    switch (command) {
                        case "Register":
                            register();
                            break;
                        case "Store":
                            storeFile();
                            break;
                        case "Get":
                            getFile();
                            break;
                        case "List":
                            listFiles();
                            break;
                        case "Leave":
                            input.close();
                            output.close();
                            socket.close();
                            System.out.println("Client: " + this.clientName + " disconnected\n");
                            clientMap.remove(this.clientName);
                            nConnections--;
                            break;
                        default:
                            output.writeUTF("Invalid command\n");
                            break;
                    }
                    output.flush();
                } catch (IOException io) {
                    io.printStackTrace();
                    System.out.println();
                }
            } while (!command.equals("Leave"));
        }

        private void register() throws IOException {
            String newName = input.readUTF();
            boolean isRepeating = false;

            for (HashMap.Entry<String, String> m : clientMap.entrySet()) {
                if (m.getKey().equals(newName)) {
                    isRepeating = true;
                    break;
                }
            }

            if (isRepeating) {
                output.writeUTF("ERROR");
            } else {
                output.writeUTF("VALID");
                clientMap.remove(this.clientName);
                clientMap.put(newName, String.valueOf(socket.getInetAddress()));
                this.clientName = newName;
            }
        }

        private void storeFile() throws IOException {
            String fileName = input.readUTF();
            long fileSize = input.readLong();

            Path p = Paths.get(fileName);
            fileName = p.getFileName().toString();

            LocalDateTime time = LocalDateTime.now();
            DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedtime = time.format(timeformat);

            File file = new File("com/server/ServerFiles/" + fileName);
            file.getParentFile().mkdirs();

            System.out.println("Storing file: " + fileName + " (" + fileSize + " bytes)\n");

            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while (fileSize > 0 && (bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fos.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }

            fos.close();

            output.writeUTF(this.clientName + "<" + formattedtime + ">: Uploaded " + fileName);
            System.out.println(this.clientName + "<" + formattedtime + ">: Uploaded " + fileName);
        }

        private void getFile() throws IOException {
            String fileName = input.readUTF();
            File file = new File("com/server/ServerFiles/" + fileName);

            if (file.exists() && file.canRead()) {
                output.writeUTF("TRUE");
                output.writeLong(file.length());
                FileInputStream fis = new FileInputStream(file);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                output.writeUTF("File received from Server: " + fileName);
                System.out.println("File sent to " + clientName + ": " + fileName);
                fis.close();
            } else {
                output.writeUTF("FALSE");
                output.writeUTF("Error: File does not exists from the Server");
            }

            output.flush();
        }

        private void listFiles() throws IOException {
            File folder = new File("com/server/ServerFiles");
            File[] files = folder.listFiles();

            output.writeUTF(String.valueOf(files.length));
            output.writeUTF("Server Directory");

            if (files.length != 0) {
                for (File f : files) {
                    output.writeUTF(f.getName());
                }
            }
        }
    }
}