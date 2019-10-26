/**
 * Chat Client
 *
 * @author Shreya Roy
 * @version 11/27/18
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private static ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("No server found. Start the server before the client.");
            System.exit(0);
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            System.out.println("");
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) throws IOException {
        // Get proper arguments and override defaults
        String username = "Sahana";
        String serverAd = "localhost";
        int port = 1500;
        if (args != null && args.length != 0)
            username = args[0];
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (args.length == 3)
            serverAd = args[2];
        // Create your client and start it
        ChatClient client = new ChatClient(serverAd, port, username);
        client.start();
        // Send an empty message to the server
        Scanner scan = new Scanner(System.in);
        while (true) {
            String message = scan.nextLine();
            if (message.equalsIgnoreCase("/logout")) {
                client.sInput.close();
                client.sOutput.close();
                client.socket.close();
                client.sendMessage(new ChatMessage(message, 1));
                break;
            } else if (message.contains("/msg")) {
                String [] sm = message.split(" ", 3);
                if (!username.equals(sm[1]))
                    client.sendMessage(new ChatMessage(sm[2], 2, sm[1]));
            } else if (message.equals("/list")) {
                client.sendMessage(new ChatMessage(message, 3));
            } else
                client.sendMessage(new ChatMessage(message, 0));
        }
    }
    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            try {
                while (true) {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.print("Server has closed the connection.");
            }
        }
    }
}
