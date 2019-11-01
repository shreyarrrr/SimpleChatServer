/**
 * Chat Server
 *
 * @author Shreya Roy
 * @version 11/27/18
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    public static Object o = new Object();
    private static ChatFilter cf;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private boolean run = true;
    public static boolean unique = true;

    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println(sdf.format(new Date()) + " Server waiting for Clients on port " + port + ".");
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                for (int i = 0; i < clients.size()-1; i++) {
                    if (clients.get(i).username.equals(clients.get(clients.size()-1).username)) {
                        clients.get(clients.size()-1).writeMessage("You must use a unique username.");
                        unique = false;
                    }
                }
                if (!clients.isEmpty() && unique)
                    System.out.println(sdf.format(new Date()) + " " + clients.get(clients.size()-1).username + " just connected.");
                unique = true;
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int port = 1500;
        String file = "badwords.txt";
        if (args != null) {
            try {
                if (args.length >= 1)
                    port = Integer.parseInt(args[0]);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (args.length == 2)
            file = args[1];

        cf = new ChatFilter(file);
        ChatServer server = new ChatServer(port);
        server.start();
        sdf.setTimeZone(TimeZone.getTimeZone("EST"));
    }
    private void broadcast(String msg) {
        System.out.println(sdf.format(new Date()) + " " + msg);
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).writeMessage(sdf.format(new Date()) + " " + msg);
        }
    }
    private void directMessage(String msg, String username) {
        System.out.println(sdf.format(new Date()) + " " + username + " -> " + msg);
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).username.equals(username))
                clients.get(i).writeMessage(sdf.format(new Date()) + " " + username + " -> " + msg);
            if (clients.get(i).username.equals(msg.substring(0,msg.indexOf(':'))))
                clients.get(i).writeMessage(sdf.format(new Date()) + " " + username + " -> " + msg);
        }
    }
    private void list(String username) {
        for (int i = 0; i < clients.size(); i++) {
            if (!clients.get(i).username.equals(username))
                System.out.println(clients.get(i).username);
        }
    }
    private void remove(int id) {
        synchronized (o) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id == id) {
                    System.out.println(sdf.format(new Date()) + " " + clients.get(i).username + " disconnected with a LOGOUT message.");
                    clients.remove(i);
                }
            }
            try {
                if (clients.isEmpty())
                    close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void close() throws IOException {
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).sInput.close();
            clients.get(i).sOutput.close();
            clients.get(i).socket.close();
        }
        System.exit(0);
        //System.out.println("Server has closed the connection");
    }

    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            try {
                while (true) {
                    cm = (ChatMessage) sInput.readObject();
                    if (cm.getType() == 0) {
                        String message = cm.getMessage();
                        message = cf.filter(message);
                        broadcast(username + ": " + message);
                    } else if (cm.getType() == 1) {
                        broadcast(username + ": logout");
                        break;
                    } else if (cm.getType() == 2) {
                        String message = cm.getMessage();
                        message = cf.filter(message);
                        directMessage(cm.getRecipient() + ": " + message, username);
                    } else if (cm.getType() == 3) {
                        list(username);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                remove(id);
            }
        }

        private boolean writeMessage(String msg) {
            if (!socket.isConnected())
                return false;
            else {
                try {
                    sOutput.writeObject(msg + "\n");
                    if (msg.contains("unique")) {
                        for (int i = 0; i < clients.size(); i++) {
                            if (clients.get(i).id == id) {
                                clients.get(i).sInput.close();
                                clients.get(i).sOutput.close();
                                clients.get(i).socket.close();
                                clients.remove(i);
                            }
                        }
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
