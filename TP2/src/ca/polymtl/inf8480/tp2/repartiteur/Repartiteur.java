package ca.polymtl.inf8480.tp2.repartiteur;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;


import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;
import ca.polymtl.inf8480.tp2.shared.NameServiceInterface;

public class Repartiteur {

	public static void main(String[] args) {

		String fileName = null;

		try{
			if (args.length != 0){
				fileName = args[0];
				Repartiteur repartiteur = new Repartiteur();
				repartiteur.run(fileName);
			} else {
				throw new Exception("Missing File Path");
			}
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	private CalculServerInterface calculServer = null;
	// private NameServiceInterface nameServiceInterface = null;

	public Repartiteur() {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		// On charge nos 2 serveurs en local.

		calculServer = loadAuthServerStub("127.0.0.1");
		//filesServerStub = loadServerStub("127.0.0.1");
	}

	// fonction pour charger l'instance du Serveur d'Authentification
	private CalculServerInterface loadAuthServerStub(String hostname) {
		CalculServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(5000);
			stub = (CalculServerInterface) registry.lookup("calculServer");
			stub.test();			
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

	private void run(String fileName) {
		File file = new File(fileName);
		if (file.exists()){
			System.out.println("File Successfully opened");
		} else {
			System.out.println("Error opening the file");
		}
	}


/*
	private void signIn(String login,String password) throws IOException{
		
		if(authServer.New(login,password)){
			File dir = new File("Client_Files");
			if(!dir.exists()){
				dir.mkdir();
			}

			//On cree le fichier credentials pour pouvoir reutiliser le username et le password lors des appels RMI

			File file = new File("Client_Files/credentials.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			String credentials = login + " " + password;
			writer.write(credentials);
			writer.close();
			System.out.println("AuthServer : " + login + " signed in succesfully");				
		} else {
			System.out.println("AuthServer : Username already taken, please choose another one");			
		}

	}

	private void createFile(String filename) throws IOException, Exception{

		/*On verifie avant chaque appel si le fichier credentials existe 

		File file = new File("Client_Files/credentials.txt");
		if(file.exists()){
			ArrayList<String> credentials = new ArrayList<String>();
			Scanner input = new Scanner(file); 
			while (input.hasNext()) {
				String word  = input.next();
				credentials.add(word);
			}
			input.close();
			String login = credentials.get(0);
			String password = credentials.get(1);
			try{
				if(filesServerStub.create(filename,login,password)){
					System.out.println(filename + " ajouté");	
				} else {
					System.out.println(filename + " existe déjà");					
				}
			} catch (RemoteException e){
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			System.out.println("Credentials file not found: make sure you signed in before");
		}
	}

	private void listFiles() throws IOException{
		ArrayList<String> credentials = getCredentials();
		if(credentials.size() > 0){			
			String login = credentials.get(0);
			String password = credentials.get(1);
			ArrayList<String> files = new ArrayList<String>();

			/*Appel RMI de la fonction list qui va nous retourner une liste de tout les fichiers presents
			sur le serveur.
			files = filesServerStub.list(login,password);
			for (String element : files){
				System.out.println(element);
			}
		}
	}

	// Fonction qui va recuperer le nom d'utilisateur et le mot de passe de l'utilisateur
	private ArrayList<String> getCredentials() throws IOException{
		File file = new File("Client_Files/credentials.txt");
		ArrayList<String> credentials = new ArrayList<String>();		
		if(file.exists()){
			Scanner input = new Scanner(file); 
			// On parcours le fichier credentials et connaisant son format on stocke les valeurs dans un tableau de String
			while (input.hasNext()) {
				String word  = input.next();
				credentials.add(word);
			}
			input.close();
		} else {
			System.out.println("Credentials file not found: make sure you signed in before");
		}
		return credentials;
	}

	private void syncLocalDirectory() throws IOException {

		ArrayList<String> credentials = getCredentials();
		if(credentials.size() > 0){
			String login = credentials.get(0);
			String password = credentials.get(1);
			ArrayList<File> files= filesServerStub.syncLocalDirectory(login,password);
			for (File element : files){

				// Pour chaque fichier retourné on ecrase le fichier s'il existait deja en local sinon on le cree

				Path localFile = Paths.get("Client_Files/" + element.getName() + ".txt");		
				InputStream in = new FileInputStream(element);							
				Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);

			}
		}
	}

	// Fonction qui retourne la valeur du checksum MD5
	private String getMD5Checksum(File file) throws IOException, Exception{

		String checksum = "";		

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
		for (int i=0; i < digest.length; i++) {
			checksum += Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
		}

		return checksum;
	}

	private void lock(String filename) throws IOException, Exception {
		Path localFile = Paths.get("Client_Files/" + filename + ".txt");		
		ArrayList<String> credentials = getCredentials();
		if(credentials.size() > 0){
			String login = credentials.get(0);
			String password = credentials.get(1);
			File file = new File("Client_Files/" + filename + ".txt");
			
			// Calcul du checksum			
			String checksum = getMD5Checksum(file);

			try {
				File newFile = filesServerStub.lock(filename,checksum,login,password);	
				InputStream in = new FileInputStream(newFile);							
				Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
				System.out.println(filename + " verrouillé");
			} catch (Exception e){
				System.out.println(e.getMessage());
			}
			
		}
				
	}

	private void get(String filename) throws IOException,Exception {
		
		Path localFile = Paths.get("Client_Files/" + filename + ".txt");		
		ArrayList<String> credentials = getCredentials();

		if(credentials.size() > 0){
			
			String login = credentials.get(0);
			String password = credentials.get(1);
			File file = new File("Client_Files/" + filename + ".txt");
			String checksum = "";			
			
			if(file.exists()){
				// Calcul du checksum
				checksum = getMD5Checksum(file);
			}

			try {
				File newFile = filesServerStub.get(filename,checksum,login,password);
				InputStream in = new FileInputStream(newFile);							
				Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
				System.out.println(filename + " synchronisé");
			} catch (Exception e){
				System.out.println(filename + " already up to date");
			}

		}
	}

	private void push(String filename) throws IOException,Exception {

		ArrayList<String> credentials = getCredentials();
		if(credentials.size() > 0){
			String login = credentials.get(0);
			String password = credentials.get(1);
			File file = new File("Client_Files/" + filename + ".txt");
			if(!file.exists()){
				System.out.println(filename + " Not found.");
			} else {
				System.out.println(filesServerStub.push(filename,file.getAbsoluteFile(),login,password));
			}
		}			
	}

	// fonction pour charger l'instance du Serveur de fichiers
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
	*/
}
