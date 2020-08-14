package dev.stevenposterick.core;

import dev.stevenposterick.utils.server.Server;

public class Main {

    private final Server server;

    public static void main(String[] args) {
        try {
            new Main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Main(String[] args) throws Exception {
        if (args.length != 1){
            throw new Exception("Incorrect amount of arguments, only port needed");
        }
        // Fetch the host and port.
        String port = args[0];
        this.server = new Server(Integer.parseInt(port));
        this.server.start();
    }
}
