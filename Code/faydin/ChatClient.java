package faydin;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.awt.*;
import java.nio.file.*;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Der Client. Es prüft nach "Befehlen", bzw. "Anweisungen" vom Server. 
 * Z.b.: wenn der Server "message" schickt, weiß der Client, dass es sich um eine Nachricht handelt
 * und führt entsprechenden Code aus, bzw. zeigt es in der GUI.
 * 
 * @author Fatih Aydin
 * @version 14-06-2018
 */
public class ChatClient {

    private String eigenerName;
    private ArrayList<String> namenListe_String;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private int clientCounter;
    private boolean nameAlreadyRegistered;
    private JFrame frame;
    private JTextArea nachrichtenAnzeigen;
    private JTextField textField;
    private JTextArea nutzerListe;
    private JButton disconnectButton;

    /**
     * Im Konstruktor wird die Frame, sowie die GUI-Elemente gesetzt.
     */
    public ChatClient() {
    	namenListe_String = new ArrayList<String>();
    	namenListe_String.add("");
    	eigenerName = "";
    	clientCounter = 0;
    	
    	textField = new JTextField(50);
    	nachrichtenAnzeigen = new JTextArea(30, 50);
        textField.setEditable(false);
        nachrichtenAnzeigen.setEditable(false);
        disconnectButton = new JButton("Disconnect");
    	
        nutzerListe = new JTextArea(30, 10);
        nutzerListe.setEditable(false);
        nutzerListe.setText("Nutzer:");
        
    	frame = new JFrame("Chat-Programm");
    	frame.setBounds(900, 200, 1000, 600);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(nachrichtenAnzeigen), "Center");
        frame.getContentPane().add(disconnectButton, "South");
        frame.getContentPane().add(nutzerListe, "East");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
        
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	outputStream.println(textField.getText());
                textField.setText("");
            }
        });
        
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	 System.exit(0);
            }
        });
    }
    
    /**
     * Hier wird nach den Anweisungen geprüft und dementschprecht reagiert.
     * Auch die NutzerListe wird hier erweitert.
     * 
     * @throws Exception
     */
    private void serverInteraction() throws Exception {
        Socket serverSocket = new Socket("", 9000); // Der Port wird nicht dynamisch gesetzt. Bzw. ist immer 9000
        inputStream = new BufferedReader(new InputStreamReader(serverSocket.getInputStream())); // Zum Lesen aus dem InputStream.
        outputStream = new PrintWriter(serverSocket.getOutputStream(), true); 
        
        while (true) {  // Hier wird nach den "Anweisungen" geprüft und dementsprechend reagiert.
            String line = inputStream.readLine();
            if (line.startsWith("getName")) { // Bei "getName": der Name wird mittels OutputStream zum Server geschickt.
            	String name = JOptionPane.showInputDialog(frame, "Wähle einen Namen", "Name wählen", JOptionPane.PLAIN_MESSAGE);
            	outputStream.println(name);
                eigenerName = nutzerListe.getText(); // Für die NutzerListe setze ich den eigenen Namen.
                eigenerName = eigenerName + "\n" + name;
                nutzerListe.setText(eigenerName);
            } else if (line.startsWith("gotName")) { // Wenn der Server dem Client mitteilt, dass er den Namen bekommen hat;
                textField.setEditable(true);		 // kann man auf der TextField schreiben.
            } else if (line.startsWith("Message")) { // Bei "Message": es wird in die TextArea hinzugefügt. 
            										 //Außerdem wird der Name vom Client, von dem die Nachricht stammt, in die NutzerListe hinzugefügt.
            	nachrichtenAnzeigen.append(line.substring(8) + "\n");
                String name = "";
                for(int i = 0; i < (line.substring(8) + "\n").length(); i++) {
                	if(nameAlreadyRegistered == true) {
                		break;
                	}
                	char ch = (line.substring(8) + "\n").charAt(i);
                	if(ch == ':') {
                		if(clientCounter == 0) {
                			clientCounter++;
                		}
                		for(int m = 0; m < clientCounter; m++) {
                			if(!(namenListe_String.contains(name))) {
                				if(name == eigenerName) {
                				} else {
                					namenListe_String.add(clientCounter, name);
                				}
                			} else {
                				nameAlreadyRegistered = true;
                				break;
                			}
                		}
                		if(nameAlreadyRegistered == false) {
                			clientCounter++;
                			break;
                		} else {
                			break;
                		}
                	} else {
                		name = name + "" + ch;
                	}
                }
                if(nameAlreadyRegistered == false) {
                	String namenListe = "";
	                if(clientCounter != 0) {
	                	for(int i = 0; i < namenListe_String.size(); i++) {
	                		namenListe = namenListe + namenListe_String.get(i) + "\n";
	                	}
	                }
	                namenListe = "Nutzer:\n" + namenListe;  // "Nutzer" als Überschrift
	                nutzerListe.setText(namenListe);
	                namenListe = "";
                } else {
                	nameAlreadyRegistered = false;
                }
                
                String namenListe = "";
                
                for(int i = 0; i < namenListe_String.size(); i++) {
            		namenListe = namenListe + namenListe_String.get(i) + "\n";
            	}
                namenListe = "Nutzer:" + namenListe;
                nutzerListe.setText(namenListe);
                namenListe = "";
            }
        }
    }
    
    /**
     * Main Methode: startet den Client.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.serverInteraction();
    }
}
