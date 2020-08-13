package dev.stevenposterick.core;

import dev.stevenposterick.utils.listeners.ClientListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main implements ClientListener {

    public static void main(String[] args) {
        try {
            new Main();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Main() throws Exception {
        String commandLine = System.getProperty("sun.java.command");
        Map<String, String> commandMap = loadCommandMap(commandLine);

        if (!commandMap.containsKey("host") || commandMap.containsKey("port")){
            throw new Exception("Failed to find host or port argument");
        }

        // Fetch the host and port.
        String host = commandMap.get("host");
        String port = commandMap.get("port");
    }

    private Map<String, String> loadCommandMap(String commandLine) {
        Map<String, String> commandMap = new HashMap<>();
        String[] arguments = commandLine.split(" ");

        Arrays.stream(arguments)
                .filter(s-> s.contains("="))
                .forEach(s-> {
                    String[] argument = s.split("=");
                    commandMap.put(argument[0], argument[1]);
                });
        return commandMap;
    }
}
