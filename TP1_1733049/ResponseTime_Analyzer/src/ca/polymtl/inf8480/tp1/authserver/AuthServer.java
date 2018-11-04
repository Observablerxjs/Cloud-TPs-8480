package ca.polymtl.inf8480.tp1.authserver;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.Map;

import ca.polymtl.inf8480.tp1.shared.AuthServerInterface;

public class AuthServer implements AuthServerInterface {

	private Map<String, String>  users = new HashMap<String, String>();

    public static void main(String[] args) {
		AuthServer authServer = new AuthServer();

		authServer.run();
    }
    
    public AuthServer() {
		super();
	}

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

    private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			AuthServerInterface stub = (AuthServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("authServer", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("AUTH_SERVER: Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
}
