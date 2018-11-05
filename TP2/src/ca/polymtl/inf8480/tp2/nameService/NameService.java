package ca.polymtl.inf8480.tp2.nameService;

import java.rmi.AccessException;
import java.rmi.NotBoundException;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap.SimpleEntry;
import java.io.FileReader;
import java.io.BufferedReader;

import ca.polymtl.inf8480.tp2.shared.*;
import ca.polymtl.inf8480.tp2.shared.NameServiceInterface;
import ca.polymtl.inf8480.tp2.calculServer.CalculServer;

// import ca.polymtl.inf8480.tp1.shared.AuthServerInterface;

class NameService implements NameServiceInterface {

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



	/*private AuthServerInterface loadServerStub(String hostname) {
		AuthServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (AuthServerInterface) registry.lookup("authServer");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}*/

/*	public ArrayList<String> list(String login, String pass) throws RemoteException {
		ArrayList<String> files = new ArrayList<String>();

		if(authServer.verify(login,pass)){
			File dir = new File("Server_Files");
			if (dir.exists()){

				// Si le dossier existe, on recuper touts les fichiers qui ne sont pas cache (exemple ._DS_Store)

				File[] listOfFiles = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return !file.isHidden();
					}
				});

				// On intere a travers les fichiers pour remplir le tableau de String qu'on va retourner au client
				for (int i = 0; i < listOfFiles.length; i++) {
					
					String fileStatus = "";
					if (listOfFiles[i].isFile()) {
						fileStatus += "* " + listOfFiles[i].getName();
						if (listOfFiles[i].canWrite()){
							fileStatus += " non verrouillé";
						} else {
							if(!lockedFiles.get(listOfFiles[i].getName()).isEmpty()){
								fileStatus += " verrouillé par " + lockedFiles.get(listOfFiles[i].getName());
							}
						}
					}
					files.add(fileStatus);
				}
				files.add(listOfFiles.length + " fichier(s)");
			} else {

				// Si le fichier n'existe pas, on notifie le client qu'il y a 0 fichiers sur le serveur

				files.add("0 fichier(s)");
			}
		} else {
			throw new RemoteException("AuthServer: " + login + " not signed in");
		}

		return files;
	}

	public ArrayList<File> syncLocalDirectory(String login, String pass) throws RemoteException {
		File[] files = null;
		ArrayList<File> Absolutefiles = new ArrayList<File>();

		if(authServer.verify(login,pass)){
			File dir = new File("Server_Files");
			if (dir.exists()){
				files = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return !file.isHidden();
					}
				});

				for (File element : files){
					
					// On recupere le AbsoluteFile pour pouvoir avoir 2+ clients qui communiquent avec le serveur

					Absolutefiles.add(element.getAbsoluteFile());
				}
			}
		}else {
			throw new RemoteException("AuthServer: " + login + " not signed in");
		}
			
		return Absolutefiles;
	}

	public boolean create(String filename, String login, String password) throws RemoteException,IOException,Exception {
		if(authServer.verify(login,password)){
			File dir = new File("Server_Files");
			if(!dir.exists()){
				dir.mkdir();
			}
			File file = new File("Server_Files/" + filename + ".txt");
			if(!file.exists()){
				file.createNewFile();
				return true;
			} else {
				return false;
			}
		} else {
			throw new RemoteException("AuthServer: " + login + " not signed in");
		}
	}

	public File get(String filename, String clientChecksum, String login, String password) throws RemoteException,IOException,Exception {

		if(authServer.verify(login,password)){
			File file = new File("Server_Files/" + filename + ".txt");
			if(file.exists()){

				// Si le checksum envoye par le client est null on force le serveur a lui envoyer le fichier

				if (clientChecksum.equals("")){
					return file.getAbsoluteFile();
				} else {

					// On recupere le checksum du fichier

					String serverChecksum = getMD5Checksum(file);

					// On envoit le fichier au client uniquement si les 2 checksums sont differents.
					
					if(serverChecksum.equals(clientChecksum)){
						throw new Exception("File already up to date");
					} else {
						return file.getAbsoluteFile();
					}
				}
			} else {
				throw new RemoteException("Files Server: File Not found");	
			}				
		} else {
			throw new RemoteException("AuthServer: " + login + " not signed in");
		}			
	}

	// fonction pour recuperer le checksum MD5 d'un fichier 

	private String getMD5Checksum(File file) throws IOException, Exception{
		InputStream fis = new FileInputStream(file);				
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] buffer = new byte[1024];
		int numRead;				

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				md.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();

		byte[] digest = md.digest();
		String checksum = "";
		for (int i=0; i < digest.length; i++) {
			checksum += Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return checksum;
	}

	public File lock(String filename, String clientChecksum, String login, String password) throws RemoteException,IOException, Exception{
		
		if(authServer.verify(login,password)){
			File file = new File("Server_Files/" + filename + ".txt");
			if(file.exists()){

				// si le fichier est unlocked (canWrite == true)

				if(file.canWrite()){

					// Alors on change l'etat d'ecriture du fichier.

					file.setWritable(false);

					// On store ensuite le nom du fichier et le nom du client

					lockedFiles.put(filename+".txt", login);

					// calcul du checksum

					String serverChecksum = getMD5Checksum(file);
					
					if(serverChecksum.equals(clientChecksum)){
						throw new Exception(filename + " verrouillé");
					} else {
						return file.getAbsoluteFile(
							*/

