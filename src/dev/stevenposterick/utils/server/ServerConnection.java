package dev.stevenposterick.utils.server;

import dev.stevenposterick.data.account.ChatUser;
import dev.stevenposterick.data.message.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerConnection implements Runnable {

    private final Socket socket;
    private final Server server;
    private List<String> names;
    private volatile boolean running = true;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private volatile ChatUser chatUser;
    private int iterator = 0;

    public ServerConnection(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public ServerConnection(List<String> names, Socket socket, Server server) {
        this(socket, server);
        this.names = names;
    }

    public boolean sendMessageToClient(String message) {
        try {
            dataOut.writeUTF(message);
            dataOut.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        try {
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            if (names != null){
                // Tell client which users are active.
                names.forEach(s->
                        sendMessageToClient(MessageType.CONNECTED.getMessageStart() +
                                new ChatUser(s).toString())
                );
                names = null;
            }

            runLoop:
            while (isRunning()){
                // Wait 50 ms while there is no message.
                while (dataIn.available() == 0) {
                    // Break to outer loop if not running.
                    if (!isRunning())
                        break runLoop;

                    // Check every second.
                    if (++iterator > 20){
                        if (!sendMessageToClient("Test"))
                            break runLoop;

                        iterator = 0;
                    }
                    Thread.sleep(50);
                }

                // Read the message.
                String messageIn = dataIn.readUTF();
                sendMessageToAllClients(messageIn);
                System.out.println(messageIn);

                if (messageIn.startsWith(MessageType.CONNECTED.getMessageStart())){
                    setChatUser(ChatUser.createFromString(
                            messageIn.replaceFirst(MessageType.CONNECTED.getMessageStart(), "")));
                } else if (messageIn.startsWith(MessageType.DISCONNECTED.getMessageStart())){
                    System.out.println("Disconnected message: " + getChatUser().toString());
                    setChatUser(null);
                    setRunning(false);
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Send disconnect notifications if it did not click the disconnect button.
            if (getChatUser() != null){
                System.out.println("Disconnect: " + getChatUser().toString());
                sendMessageToAllClients(MessageType.DISCONNECTED + getChatUser().toString());
                setChatUser(null);
            }

            // Close all the sockets/data streams.
            close(socket);
            close(dataIn);
            close(dataOut);
        }
    }

    private void close(Closeable closeable){
        if (closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageToAllClients(String message) {
        server.onMessage(this, message);
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean b){
        this.running = b;
    }

    public ChatUser getChatUser() {
        return chatUser;
    }

    public void setChatUser(ChatUser chatUser) {
        this.chatUser = chatUser;
    }
}
