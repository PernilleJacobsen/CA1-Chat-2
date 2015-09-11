package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Utils;

/**
 *
 * @author Pernille
 */
public class ChatServer
{

    private ServerSocket serverSocket;
    private static final Properties properties = Utils.initProperties("server.properties");
    private BufferedReader in;
    private PrintWriter out;
    private static boolean keepRunning = true;
    private ClientHandler ch;
    private Socket socket;
    private final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap();
    String[] splitInput = new String[100];

    public static void stopServer()
    {
        keepRunning = false;
    }

    private void runServer(ChatServer cs)
    {
        int port = Integer.parseInt(properties.getProperty("port"));
        String ip = properties.getProperty("serverIp");

        Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Sever started. Listening on: " + port + ", bound to: " + ip);
        try
        {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));

            while (keepRunning)
            {
                socket = serverSocket.accept(); //Important Blocking call
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Connected to a client");
                
                String inputToSplit = in.readLine();
                splitInput = inputToSplit.split("#");
                String command = splitInput[0];
                if (command.equals("USER"))
                {
                    String userName = splitInput[1];
                    ch = new ClientHandler(socket, userName, cs);
                    clients.put(userName, ch);
                    ch.start();
                    cs.sendUserListToAll(cs.userList());
                }
            }
        } catch (IOException ex)
        {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args)
    {
        String logFile = properties.getProperty("logFile");
        Utils.setLogFile(logFile, ChatServer.class.getName());

        try
        {
            ChatServer cs = new ChatServer();
            cs.runServer(cs);
        } finally
        {
            Utils.closeLogger(ChatServer.class.getName());
        }
    }

    public void sendToAll(String msg, String userName)
    {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet())
        {
            ClientHandler receiver = entry.getValue();
            try
            {
                receiver.sendMSG("MSG#" + userName + "#" + msg);
            } catch (NullPointerException ex)
            {
                receiver = clients.get(userName);
                receiver.sendMSG("MSG#" + userName + "#User doesn't exsist");
                Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, ex.getMessage());
            }
        }
    }

    public void sendUserListToAll(String msg)
    {
        if (clients.isEmpty())
        {
            Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "No users left");
        }
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet())
        {
            ClientHandler receiver = entry.getValue();
            receiver.sendMSG(msg);
        }
    }

    public void sendToSome(String msg, String[] receivers, String userName)
    {
        for (String receiver1 : receivers)
        {
            String temp = receiver1;
            if (clients.containsKey(receiver1))
            {
                ClientHandler receiver = clients.get(temp);
                try
                {
                    receiver.sendMSG("MSG#" + userName + "#" + msg);
                } catch (NullPointerException ex)
                {
                    receiver = clients.get(userName);
                    receiver.sendMSG("MSG#" + userName + "#User doesn't exsist");
                    Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, ex.getMessage());
                }
            }
        }
    }

    public void sendToOne(String msg, String receivers, String userName)
    {
        ClientHandler receiver = clients.get(receivers);
        try
        {
            receiver.sendMSG("MSG#" + userName + "#" + msg);
        } catch (NullPointerException ex)
        {
            receiver = clients.get(userName);
            receiver.sendMSG("MSG#" + userName + "#User doesn't exsist");
            Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, ex.getMessage());
        }
    }

    public void removeUser(String userName)
    {
        clients.remove(userName);
        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, "User removed: " + userName);
    }

    public String userList()
    {
        String temp;
        String msg = "USERLIST#";
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet())
        {
            temp = entry.getKey();
            msg += temp + ", ";
        }
        return msg;
    }
}
