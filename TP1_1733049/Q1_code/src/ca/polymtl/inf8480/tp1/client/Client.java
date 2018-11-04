package ca.polymtl.inf8480.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;


public class Client {

private static final String FILENAME = "/usagers3/yabent/Desktop/file.txt";

	public static void main(String[] args) {
		String distantHostname = null;
		BufferedReader br = null;
		FileReader fr = null;
		File file = null;

		if (args.length > 0) {

			distantHostname = args[0];
			
			if(args.length > 1){

				try {

					file = new java.io.File(args[1]);
					System.out.println("TestFile Size: " + file.length() + " octets");

					fr = new FileReader(args[1]);
					br = new BufferedReader(fr);
					

					String sCurrentLine;

					while ((sCurrentLine = br.readLine()) != null) {
						System.out.println(sCurrentLine);
					}


				} catch (IOException e) {

					e.printStackTrace();

				} finally {

					try {

						if (br != null)
							br.close();

						if (fr != null)
							fr.close();

					} catch (IOException ex) {

						ex.printStackTrace();

					}

				}
			}
		}

		Client client = new Client(distantHostname);
		client.run(file);
	}

	FakeServer localServer = null; // Pour tester la latence d'un appel de
									// fonction normal.
	private ServerInterface localServerStub = null;
	private ServerInterface distantServerStub = null;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		localServer = new FakeServer();
		localServerStub = loadServerStub("127.0.0.1");

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
		}
	}

	private void run(File file) {
		appelNormal(file);

		if (localServerStub != null) {
			appelRMILocal(file);
		}

		if (distantServerStub != null) {
			appelRMIDistant(file);
		}
	}

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
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

	private void appelNormal(File file) {
		long start = System.nanoTime();
		localServer.execute(file);
		long end = System.nanoTime();

		System.out.println("Temps écoulé appel normal: " + (end - start)
				+ " ns");
		// System.out.println("Résultat appel normal: " + result);
	}

	private void appelRMILocal(File file) {
		try {
			long start = System.nanoTime();
			localServerStub.execute(file);
			long end = System.nanoTime();

			System.out.println("Temps écoulé appel RMI local: " + (end - start)
					+ " ns");
			// System.out.println("Résultat appel RMI local: " + result);
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private void appelRMIDistant(File file) {
		try {
			long start = System.nanoTime();
			distantServerStub.execute(file);
			long end = System.nanoTime();

			System.out.println("Temps écoulé appel RMI distant: "
					+ (end - start) + " ns");
			// System.out.println("Résultat appel RMI distant: " + result);
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}
}
