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
            int clientsSize = 0;

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
                    clients.put(userName, ch = new ClientHandler(socket, userName, cs));
                    out.println("Welcome: " + userName);
                    ch.start();
                    
                    cs.sendUserListToAll(cs.userList());
                }else
                {
                    out.println("Remember to use the format: USER#brugernavn");
                    //fejl opstår - bruger får ikke lov at komme på selv efter korrekt indtastning
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
            receiver.sendMSG("MSG#"+userName+"#"+msg);
        }
    }
    public void sendUserListToAll(String msg)
    {
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
                receiver.sendMSG("MSG#"+userName+"#"+msg);
            }
        }
    }

    public void sendToOne(String msg, String receivers, String userName)
    {
        ClientHandler receiver = clients.get(receivers);
        receiver.sendMSG("MSG#"+userName+"#"+msg);
    }

    public void removeUser(String userName)
    {
        clients.remove(userName);
    }

    public String userList()
    {
        String temp;
        String msg = "USERLIST#";
        ClientHandler receiver = null;
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet())
        {
            temp = entry.getKey() ;
            receiver = entry.getValue();
            msg += temp + ", " ;
            
        }
      return msg;
    }
}
