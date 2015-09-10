/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import Client.ChatClient;
import java.io.BufferedReader;
import server.ChatServer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jeanette
 */
public class TestChatServer
{

    private BufferedReader in;
    private ChatClient client;
    private Socket socket;
    private String userName1 = "USER#A";
    private String userName2 = "USER#B";
    private String userName3 = "USER#C";

    public TestChatServer()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ChatServer.main(null);
            }
        }).start();
    }

    @AfterClass
    public static void tearDownClass()
    {
        ChatServer.stopServer();
    }

    @Before
    public void setUp()
    {
    }

    @Test
    public void user() throws IOException
    {
        //vi tester om vi kan logge en bruger ind.
        client = new ChatClient();
        client.connect("localhost", 9090);
        client.send(userName1);
        client.addObserver(new Observer()
        {
            @Override
            public void update(Observable o, Object arg)
            {
                assertEquals("Welcome: A", arg.toString());
            }
        });

    }
}
