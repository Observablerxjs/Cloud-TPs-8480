package ca.polymtl.inf8480.tp2.calculServer;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.Map;

import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;

public class CalculServer implements CalculServerInterface {

	// private Map<String, String>  users = new HashMap<String, String>();

	private int capacity = 0;

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

		int port = 0;
		
		try{
			if (args.length >= 1){
				if(args.length >= 2){	
					capacity = Integer.parseInt(args[2]);
					if(args[1].matches("[0-9]+")){
						if (Integer.parseInt(args[1]) < 5000 || Integer.parseInt(args[1]) > 5050){
							throw new Exception("Invalid Port. Please enter Port between 5000 and 5050");	
						} else {
							port = Integer.parseInt(args[1]);
						}
						CalculServerInterface stub = (CalculServerInterface) UnicastRemoteObject
								.exportObject(this, port);
						Registry registry = LocateRegistry.createRegistry(port);
			
						// Registry registry = LocateRegistry.getRegistry(port);
						registry.rebind("calculServer", stub);
						System.out.println("Server ready.");

					} else {
						throw new Exception("Invalid Port. Please enter Port between 5000 and 5050");
					}
				} else {
					throw new Exception("Missing Capacity");	
				}
			} else {
				throw new Exception("Missing Port and Capacity");	
			}
		}catch (ConnectException e) {
			System.err
					.println("Calcul_Server: Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	public void test(){
		System.out.println("TEST: SUCCESS");
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
