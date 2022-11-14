package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    //Sends a message to all users
    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Ошибка. Сообщение не отправлено");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        //Sends a name request to a new user and receives name from user until a correct name is sent
        //If the name is correct, adds this user to "connectionMap" and sends greeting message
        //Returns username
        private String serverHandshake(Connection connection) {
            String clientName;
            try {
                while(true) {
                    connection.send(new Message(MessageType.NAME_REQUEST, "Введите имя..."));
                    Message nameMessage = connection.receive();
                    clientName = nameMessage.getData();

                    if (nameMessage.getType() == MessageType.USER_NAME
                            && clientName.length() > 0
                            && !connectionMap.containsKey(clientName)) {

                        connectionMap.put(clientName, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED, "Добро пожаловать в чат, " + clientName + "!"));
                        break;
                    }
                }
                return clientName;

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }


        }

        //Sends a list of existing users to a new user
        private void notifyUsers(Connection connection, String userName) throws IOException {

            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                String name = entry.getKey();
                if (!name.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, name));
                }
            }
        }

        //Receives message from some user and broadcasts it to all users in a loop (if the message has a "TEXT" MessageType)
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {

            while (true) {
                Message receivedMessage = connection.receive();

                if (receivedMessage.getType() == MessageType.TEXT) {
                    Message messageToSend = new Message(MessageType.TEXT,userName + ": " + receivedMessage.getData());
                    sendBroadcastMessage(messageToSend);
                } else {
                    ConsoleHelper.writeMessage("Ошибка. Сообщение не является текстом");
                }
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с " + socket.getRemoteSocketAddress());
            String userName = null;

            try (Connection connection = new Connection(socket)){

                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }

            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }

            ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");

        }
    }



    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите номер порта для сервера:");
        //reading a port number from console
        int port = ConsoleHelper.readInt();
        //opening server socket
        try (ServerSocket serverSocket = new ServerSocket(port)) {

           ConsoleHelper.writeMessage("Сервер запущен");

           while (true) {
               //opening a new client socket for each user via serverSocket
               Socket clientSocket = serverSocket.accept();
               Thread handler = new Handler(clientSocket);
               handler.start();
           }

        } catch (IOException e) {
           e.printStackTrace();
        }

    }
}
