package dev.stevenposterick.utils.listeners;


import dev.stevenposterick.utils.server.ServerConnection;

public interface ClientListener {

    void onMessage(ServerConnection origin, String message);

}
