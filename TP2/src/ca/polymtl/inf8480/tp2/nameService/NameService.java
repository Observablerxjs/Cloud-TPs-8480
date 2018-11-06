package ca.polymtl.inf8480.tp2.nameService;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.FileReader;
import java.net.InetAddress;
import java.io.BufferedReader;

import ca.polymtl.inf8480.tp2.shared.*;
import ca.polymtl.inf8480.tp2.shared.NameServiceInterface;
import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;
import ca.polymtl.inf8480.tp2.calculServer.CalculServer;

// import ca.polymtl.inf8480.tp1.shared.AuthServerInterface;

class NameService implements NameServiceInterface {

	private Map<String,String> users = new HashMap<String,String>();
	private ArrayList<CSModel> calculServers = new ArrayList<CSModel>();

	public static void main(String[] args) {
		NameService nameService = new NameService();
		nameService.run();
	}

	public NameService() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader("config/config_nameService"));
			String line;
    		while ((line = br.readLine()) != null) {
			   String[] words = line.split(": ");
			   if(words[0].equals("IPaddress")){
					System.setProperty("java.rmi.server.hostname",words[1]);
			   }
			}
			br.close();


			NameServiceInterface stub = (NameServiceInterface) UnicastRemoteObject
					.exportObject(this, 5000);
			Registry registry = LocateRegistry.createRegistry(5000);
			registry.rebind("nameService", stub);
			System.out.println("Server ready.");
		
		} catch (ConnectException e) {
			System.err.println("Name_Service: Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	public void test(String test) {
		System.out.println("TEST:" + test + " SUCCESS !!!");
		return;
	}

	public boolean verifyUser(String username, String password){
		if (users.containsKey(username)){
			if (users.get(username).equals(password)){
				return true;
			}
		}
		return false;
	}

	public void signInRepartiteur(String username, String password){
		if (!users.containsKey(username)){
			users.put(username,password);
		}
	}

	public void signIn(String IP, int port, int capacity){
		for (int i = 0; i < calculServers.size(); i++){
			if (calculServers.get(i).getIpAddr().equals(IP) && calculServers.get(i).getPort() == port){
				calculServers.remove(i);
			}
		}
		calculServers.add(new CSModel(IP,port,capacity));
	}

	public ArrayList<CSModel> getCalculServers(){
		return calculServers;
	}
}
