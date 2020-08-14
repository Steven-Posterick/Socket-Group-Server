package dev.stevenposterick.utils.server;

import dev.stevenposterick.utils.listeners.ClientListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Server extends Thread implements ClientListener {

    private final int port;
    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private List<ServerConnection> serverConnections = new CopyOnWriteArrayList<>();

    /*
    Use ThreadPoolExecutor to stop connection after 24 hours and use a max of 60 threads.
    */
    private ThreadPoolExecutor executor =
            new ThreadPoolExecutor(5, 60, 24, TimeUnit.HOURS, new LinkedBlockingQueue<>());

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            // Claim the server socket
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (isRunning()){
            try {
                Socket socket = serverSocket.accept();

                // Collect all the names.
                List<String> connectionNames = serverConnections
                        .stream()
                        .filter(s-> s.getChatUser() != null)
                        .map(sc -> sc.getChatUser().getName())
                        .collect(Collectors.toList());

                ServerConnection serverConnection =
                        new ServerConnection(connectionNames, socket, this);

                executor.submit(serverConnection);
                serverConnections.add(serverConnection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean b){
        this.running = b;
    }

    @Override
    public void onMessage(ServerConnection origin, String message) {
        // Remove any dead sockets.
        serverConnections.removeIf(s-> !s.isRunning());

        // Send message to all server connections (besides the origin).
        serverConnections
                .stream()
                .filter(s-> s != null && !s.equals(origin))
                .forEach(s-> {
                    s.sendMessageToClient(message);
                });
    }
}
