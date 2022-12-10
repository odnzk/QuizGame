package ru.itis.server;

import ru.itis.connection.Connection;
import ru.itis.constants.Properties;
import ru.itis.models.Room;
import ru.itis.protocol.message.ContentMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private final HashMap<Integer, Connection> connections;

    private final ServerSocket serverSocket;

    private final HashMap<Integer, Room> rooms;

    private int userId = 1;

    private int roomId = 1;

    public Server() {
        connections = new HashMap<>();
        rooms = new HashMap<>();
        try {
            serverSocket = new ServerSocket(Properties.port);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void start() {
        System.out.println("Server started");
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                addConnection(socket);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public void addConnection(Socket socket) {
        PlayerConnection connection = new PlayerConnection(socket, userId++);
        connections.put(connection.getId(), connection);
        System.out.println("Connected user Id: " + (connection.getId() + "Nickname: " + connection.getPlayer().getUsername()));
    }

    private void sendToConnection(int connectionId, ContentMessage message) {
        try {
            Connection con = connections.get(connectionId);
            if (con.isConnected()) {
                con.send(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void handMessage(ContentMessage message) {
        switch (message.getType()) {
            case PLAYER_DISCONNECT -> removeConnection(message.getSenderId());
            case ROOM_CREATE -> createRoom((Room) message.getContent());
        }
    }

    public void removeConnection(int id) {
        Connection connection = connections.get(id);
        if (connection == null) {
            return;
        }
        connections.remove(id);
    }
    private int createRoom(Room room){
        room.setId(roomId++);
        rooms.put(roomId,room);
        return room.getId();
    }

    public void removeRoom(int roomId) {
        rooms.remove(roomId);
    }
}
