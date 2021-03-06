package ca.polymtl.inf8480.tp2.calculServer;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Random;

import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;
import ca.polymtl.inf8480.tp2.shared.NameServiceInterface;
import ca.polymtl.inf8480.tp2.shared.Command;
import ca.polymtl.inf8480.tp2.calculServer.Operations;

public class CalculServer implements CalculServerInterface {

	private int capacity = 0;
	private double malice = 0.0;
	private NameServiceInterface nameService = null;
	private String ip = null;
	private  int port = 0;

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
		
		try{

			/* On commence par initialiser les attributs du serveur de calcul avec les parametres communiques en ligne de commande*/

			if (args.length >= 1){
				if (args.length >=2){
					malice = (Integer.parseInt(args[1])/100.0);
					System.out.println("malice = " + malice);
				}

				// Ensuite on recupere l'adresse IP du serveur pour eviter de demander a l'utilisateur de l'ecrire en ligne de commande

				final DatagramSocket socket = new DatagramSocket();
				socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
				ip = socket.getLocalAddress().getHostAddress();
				// On associe cette adresse IP au java.rmi.server.hostname pour que les autres processus peuvent communiquer avec le server
				System.setProperty("java.rmi.server.hostname",ip);
				String nameServiceIP = null;

				if(args[0].matches("[0-9]+")){
					capacity = Integer.parseInt(args[0]); 
				} else {
					throw new Exception("Invalid Capacity. Please enter a number > 0");
				}

				/*On initialise encore les attributs du serveur cette fois ci avec les parametres des fichiers de config*/

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

				// verification du numero de port communique
				
				if (port < 5000 || port > 5050){
					throw new Exception("Invalid Port in configFile. Please modify Port between 5000 and 5050 and start again the server");
				}

				// Creation du stub
				CalculServerInterface stub = (CalculServerInterface) UnicastRemoteObject
									.exportObject(this, port);
				Registry registry = LocateRegistry.createRegistry(port);
				registry.rebind("calculServer", stub);

				nameService = loadNameServiceStub(nameServiceIP);
				// Authentification aupres du service de noms.
				nameService.signIn(ip,port,capacity);
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

	public Integer execute(String username, String password, ArrayList<Command> commands){
		// T sera toujours = 0 car on a choisi d'envoyer uniquement le nombre de commanques que le serveur peut supporter
		int T = ((commands.size()-capacity)/4*capacity)*100;
		int result = 0;

		// Calcul du resultat

		for (int i = 0; i < commands.size(); i++){
			String operand = commands.get(i).getOperand();
			if (operand.equals("pell")){
				result += Operations.pell(commands.get(i).getValue());
			} else if(operand.equals("prime")){
				result += Operations.prime(commands.get(i).getValue());
			}
			result = result % 4000;
		}

		// Si le serveur est malicieux nous allons rajouter une valeur arbitraire au resultat pour le fausser (en l'occurence 15)

		if (malice != 0){
			Random rand = new Random();
			double  n = rand.nextDouble();
			if (n <= malice){
				result += 15;
			}
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

	// On verifie si le mot de passe du user correspond au username

	public boolean verify(String username, String password) throws RemoteException {
		return nameService.verifyUser(username, password);
	}
}

