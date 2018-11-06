package ca.polymtl.inf8480.tp2.calculServer;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;
import ca.polymtl.inf8480.tp2.shared.NameServiceInterface;
import ca.polymtl.inf8480.tp2.shared.Command;
import ca.polymtl.inf8480.tp2.calculServer.Operations;

public class CalculServer implements CalculServerInterface {

	// private Map<String, String>  users = new HashMap<String, String>();

	private int capacity = 0;
	private int malice = 0;
	private NameServiceInterface nameServiceInterface = null;

    public static void main(String[] args) {
		CalculServer calculServer = new CalculServer();
		calculServer.run(args);
    }
    
    public CalculServer() {
		super();
	}

	private void run(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		String ip = null;
		int port = 0;
		
		try{

			if (args.length >= 1){

				final DatagramSocket socket = new DatagramSocket();
				socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
				ip = socket.getLocalAddress().getHostAddress();
				System.setProperty("java.rmi.server.hostname",ip);
				String nameServiceIP = null;

				// TODO: add Malice parameter

				if(args[0].matches("[0-9]+")){
					capacity = Integer.parseInt(args[0]); 
				} else {
					throw new Exception("Invalid Capacity. Please enter a number > 0");
				}

				BufferedReader br = new BufferedReader(new FileReader("config/config_calculServer"));
				String line;
				while ((line = br.readLine()) != null) {
				String[] words = line.split(": ");
				if(words[0].equals("nameServiceIP")){
					nameServiceIP = words[1];
				} else if (words[0].equals("port")){
					port = Integer.parseInt(words[1]);
				}
				}
				br.close();

				if (port < 5000 || port > 5050){
					throw new Exception("Invalid Port in configFile. Please modify Port between 5000 and 5050 and start again the server");
				}

				CalculServerInterface stub = (CalculServerInterface) UnicastRemoteObject
									.exportObject(this, port);
				Registry registry = LocateRegistry.createRegistry(port);
				registry.rebind("calculServer", stub);
				nameServiceInterface = loadNameServiceStub(nameServiceIP);
				nameServiceInterface.signIn(ip,port,capacity);
				System.out.println("Server ready.");

			} else {
				throw new Exception("Missing Capacity value");
			}

		}catch (ConnectException e) {
			System.err
					.println("Calcul_Server: Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (UnknownHostException e){
			System.err.println("(UnknownHostException) Erreur: " + e.getMessage());
		} catch (SocketException e){
			System.err.println("(SocketException) Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		} 

	}

	public Integer execute(ArrayList<Command> commands){
		// T sera toujours = 0 car on a choisi d'envoyer uniquement le nombre de commanques que le serveur peut supporter
		int T = ((commands.size()-capacity)/4*capacity)*100;
		System.out.println("ThreadID: " + Thread.currentThread().getId() + " T = " + T);
		int result = 0;
		for (int i = 0; i < commands.size(); i++){
			String operand = commands.get(i).getOperand();
			if (operand.equals("pell")){
				result += Operations.pell(commands.get(i).getValue());
			} else if(operand.equals("prime")){
				result += Operations.prime(commands.get(i).getValue());
			}
			result = result % 4000;
		}
		return result;
	}

	private NameServiceInterface loadNameServiceStub(String nameServiceIP) {

		NameServiceInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(nameServiceIP, 5000);
			stub = (NameServiceInterface) registry.lookup("nameService");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

	/*
	// Fonction pour enregistrer le client.

	public boolean New(String login, String password) throws RemoteException {
		if(!users.containsKey(login)){
			users.put(login,password);
			return true;		
		}
		return false;
	}
	
	// On verifie si le mot de passe du user correspond au username

	public boolean verify(String login, String password) throws RemoteException {
		String user = users.get(login);
		if(user != null){
			return users.get(login).equals(password);
		} else {
			return false;
		}
	}
    */
}

