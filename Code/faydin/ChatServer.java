package faydin;

import java.awt.*;
import java.nio.file.*;
import java.net.ServerSocket;
import java.util.Date;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * Ein Chat Programm mit mehreren Threads. Mehrere Clients sind möglich.
 * 
 * @author Fatih Aydin
 * @version 12-06-2018
 */
public class ChatServer {

    private static int serverPort = 9000;
    private static HashSet<String> namen = new HashSet<String>();
    private static HashSet<PrintWriter> schreiber = new HashSet<PrintWriter>();
    // Mit der "schreiber" Liste speichert man die Nachrichten die man aus dem InputStream, bzw. BufferedReader, liest.
    
    /**
     * Erstellt mehrere MessageHandler-Threads mit dem Server Socket.
     * 
     * @param socketListener the ServerSocket
     * @throws Exception
     */
    public static void createMessageHandler(ServerSocket socketListener) throws Exception {
    	try {
            while(true) {
                new MessageHandler(socketListener.accept()).start();
            }
        } catch(Exception e) {
            System.out.println(e);	
        } finally {
        	socketListener.close();
        }
    }
    
    /**
     * Main Methode: erstellt den ServerSocket und ruft createHandler auf.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Der Chat ist gestartet!");
        ServerSocket socketListener = new ServerSocket(serverPort);
        createMessageHandler(socketListener);
    }
    
    /**
     * Handler extendet Thread.
     * Hier wird vom Client Nachrichten erhalten und an alle anderen Clients versendet.
     * 
     * @author Fatih Aydin
     * @version 13-06-2018
     */
    private static class MessageHandler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader inputStream;
        private PrintWriter outputStream;
        
        /**
         * Der Konstruktor von MessageHandler
         * 
         * @param socket Der Server Socket
         */
        public MessageHandler(Socket socket) {
            this.socket = socket;
        }
        
        /**
         * Fragt nach dem Namen des Clients. Bekommt Nachrichten von Clients und verteilt diese.
         */
        public void run() {
            try {
            	inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            	outputStream = new PrintWriter(socket.getOutputStream(), true);
                
                // Hier wird nach dem Namen gefragt.
                while(true) {
                	outputStream.println("getName");		// Wenn der Server "getName" zum Client schreibt, liefert der Client den Namen.
                    // Es wirkt sozusagen wie ein Befehl. 
                    name = inputStream.readLine();
                    if(name == null) {	//kein name = es passiert nichts
                        return;
                    }
                    // ein "Lock" setzen, bzw. keine Race-Condition
                    synchronized(namen) {
                        if (!namen.contains(name)) {	// Falls die HashSet namen den erhaltenen Namen enthällt, wird es nicht hinzugefügt.
                        	namen.add(name);
                            break;
                        } else {
                        	System.out.println("Name nicht mehr verfügbar! Suchen Sie sich einen anderen Namen aus.");
                        }
                    }
                }

                outputStream.println("gotName");
                schreiber.add(outputStream);
                
                // bekommt Nachrichten. Diese Nachrichten werden an alle Clients geschickt.
                while(true) {
                    String input = inputStream.readLine(); // liest die Nachricht aus dem InputStream
                    if(input == null) {
                        return;
                    }
                    for(PrintWriter writer : schreiber) { // hier wird es verschickt.
                        writer.println("Message " + name + ": " + input);
                    }
                }
            } catch(Exception e) {
                System.out.println(e);
            }
        }
    }
}