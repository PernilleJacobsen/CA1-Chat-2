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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
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

    private BufferedReader inA;
    private BufferedReader inB;
    private BufferedReader inC;
    private PrintWriter outA;
    private PrintWriter outB;
    private PrintWriter outC;
    private Socket socketA;
    private Socket socketB;
    private Socket socketC;
    private final String userNameA = "USER#A";
    private final String userNameB = "USER#B";
    private final String userNameC = "USER#C";
    private String msg;
    private String stop = "STOP#";

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
        socketA = new Socket("localhost", 9090);
        inA = new BufferedReader(new InputStreamReader(socketA.getInputStream()));
        outA = new PrintWriter(socketA.getOutputStream(), true);

        outA.println(userNameA);
        msg = inA.readLine();
        assertEquals("USERLIST#A, ", msg);
        outA.println(stop);

    }

    public void sendMSGtoAll() throws IOException
    {
        //Vi test at vi kan sende til alle 
        msg = "MSG#*#JUnitTest";

        socketA = new Socket("localhost", 9090);
        inA = new BufferedReader(new InputStreamReader(socketA.getInputStream()));
        outA = new PrintWriter(socketA.getOutputStream(), true);

        socketB = new Socket("localhost", 9090);
        inB = new BufferedReader(new InputStreamReader(socketB.getInputStream()));
        outB = new PrintWriter(socketB.getOutputStream(), true);

        socketC = new Socket("localhost", 9090);
        inC = new BufferedReader(new InputStreamReader(socketC.getInputStream()));
        outC = new PrintWriter(socketC.getOutputStream(), true);

        outA.println(userNameA);
        outB.println(userNameB);
        outC.println(userNameC);

        outA.println(msg);
        assertEquals("MSG#A#JUnitTest", inA);
        assertEquals("MSG#A#JUnitTest", inB);
        assertEquals("MSG#A#JUnitTest", inC);

        outA.println(stop);
        outB.println(stop);
        outC.println(stop);

    }

    public void sendMSGtoTwo() throws IOException
    {
        //Vi test at vi kan sende til to 
        msg = "MSG#B,C#JUnitTest";

        socketA = new Socket("localhost", 9090);
        inA = new BufferedReader(new InputStreamReader(socketA.getInputStream()));
        outA = new PrintWriter(socketA.getOutputStream(), true);

        socketB = new Socket("localhost", 9090);
        inB = new BufferedReader(new InputStreamReader(socketB.getInputStream()));
        outB = new PrintWriter(socketB.getOutputStream(), true);

        socketC = new Socket("localhost", 9090);
        inC = new BufferedReader(new InputStreamReader(socketC.getInputStream()));
        outC = new PrintWriter(socketC.getOutputStream(), true);

        outA.println(userNameA);
        outB.println(userNameB);
        outC.println(userNameC);

        outA.println(msg);
        assertEquals("MSG#A#JUnitTest", inB);
        assertEquals("MSG#A#JUnitTest", inC);

        outA.println(stop);
        outB.println(stop);
        outC.println(stop);
    }

    public void sendMSGtoOne() throws IOException
    {
        //Vi test at vi kan sende til en 
        msg = "MSG#B#JUnitTest";

        socketA = new Socket("localhost", 9090);
        inA = new BufferedReader(new InputStreamReader(socketA.getInputStream()));
        outA = new PrintWriter(socketA.getOutputStream(), true);

        socketB = new Socket("localhost", 9090);
        inB = new BufferedReader(new InputStreamReader(socketB.getInputStream()));
        outB = new PrintWriter(socketB.getOutputStream(), true);
        outA.println(userNameA);
        outB.println(userNameB);

        outA.println(msg);
        assertEquals("MSG#A#JUnitTest", inB);

        outA.println(stop);
        outB.println(stop);

    }

    public void userListStop() throws IOException
    {
        // Vi tester userlist og stop

        socketA = new Socket("localhost", 9090);
        inA = new BufferedReader(new InputStreamReader(socketA.getInputStream()));
        outA = new PrintWriter(socketA.getOutputStream(), true);

        socketB = new Socket("localhost", 9090);
        inB = new BufferedReader(new InputStreamReader(socketB.getInputStream()));
        outB = new PrintWriter(socketB.getOutputStream(), true);

        outA.println(userNameA);
        outB.println(userNameB);

        outA.println(stop);
        assertEquals("USERLIST#B,", inB);
        outB.println(stop);

    }

}
