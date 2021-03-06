/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pernille
 */
public class ClientHandler extends Thread
{

    Scanner input;
    PrintWriter out;
    Socket socket;
    String userName;
    ChatServer cs;

    public ClientHandler(Socket socket, String userName, ChatServer cs) throws IOException
    {
        input = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
        this.userName = userName;
        this.cs = cs;
        this.socket = socket;

    }

    public void sendMSG(String msg)
    {
        out.println(msg);
    }

    @Override
    public void run()
    {
        String message = input.nextLine(); //IMPORTANT blocking call
        Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ", message));

        while (!message.equals("STOP#"))
        {
            String inputToSplit = message;
            String[] splitInput = inputToSplit.split("#");
            String command = splitInput[0];
            if (command.equals("MSG"))
            {
                String[] receivers = splitInput[1].split(",");
                if (receivers.length == 1 && receivers[0].equals("*"))
                {
                    String msg = splitInput[2];
                    cs.sendToAll(msg, userName);
                } else if (receivers.length == 1)
                {
                    //Vi vil gerne have fat i den clienthandler som navnet i receivers hør sammem
                    //med.
                    String msg = splitInput[2];
                    String receiver = receivers[0];
                    cs.sendToOne(msg, receiver, userName);
                } else
                {
                    String msg = splitInput[2];
                    cs.sendToSome(msg, receivers, userName);
                } 
            }
            else
                {
                    sendMSG("Invalid command");
                }
            Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ", message.toUpperCase()));
            message = input.nextLine(); //IMPORTANT blocking call
        }
        try
        {
            //Send userlist og fjern bruger fra map.
            cs.removeUser(userName);
            String userList= cs.userList();
            cs.sendUserListToAll(cs.userList());
            currentThread().interrupt();
            input.close();
            out.close();
            socket.close();
        } catch (IOException ex)
        {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Closed a Connection");
    }
}
