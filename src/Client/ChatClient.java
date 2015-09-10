/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.currentThread;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Observable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.ChatServer;
import server.ClientHandler;

/**
 *
 * @author Pernille
 */
public class ChatClient extends Observable
{

    Socket socket;
    private int port;
    private InetAddress serverAddress;
    private Scanner input;
    private PrintWriter output;

    public void connect(String address, int port) throws IOException
    {
        this.port = port;
        serverAddress = InetAddress.getByName(address);
        socket = new Socket(serverAddress, port);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    String msg = input.nextLine();
                    setChanged();
                    notifyObservers(msg);
                    if (msg.equals("STOP#"))
                    {
                        try
                        {
                            socket.close();
                        } catch (IOException ex)
                        {
                            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }).start();

    }

    public void send(String msg)
    {
        output.println(msg);
    }
}
