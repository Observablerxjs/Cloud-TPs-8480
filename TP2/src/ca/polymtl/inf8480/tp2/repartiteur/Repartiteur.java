package ca.polymtl.inf8480.tp2.repartiteur;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import ca.polymtl.inf8480.tp2.repartiteur.CalculServersInstances;

import ca.polymtl.inf8480.tp2.shared.CSModel;
import ca.polymtl.inf8480.tp2.shared.Command;
import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;
import ca.polymtl.inf8480.tp2.shared.NameServiceInterface;

public class Repartiteur {

	String username = null;
	String password = null;
	ArrayList<Command> commands = new ArrayList<Command>();

	public static void main(String[] args) {

		String fileName = null;
		boolean unsecureMode = false;

		try{
			if (args.length != 0){
				if (args[0].equals("-us") && args.length >= 2){
					unsecureMode = true;
					fileName = args[1];
				} else if(args.length >= 1){
					fileName = args[0];
				} else {
					throw new Exception("Missing File Path");	
				}
			} else {
				throw new Exception("Missing File Path");
			}
			
			if (fileName != null){
				Repartiteur repartiteur = new Repartiteur();
				repartiteur.run(fileName,unsecureMode);
			}

		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	private CalculServerInterface calculServer = null;
	private NameServiceInterface nameService = null;

	private ArrayList<CalculServersInstances> servers = new ArrayList<CalculServersInstances>(); 

	public Repartiteur() {
		super();
	}

	// fonction pour charger l'instance du Serveur d'Authentification
	private CalculServerInterface loadCalculServer(String hostname,int port) {
		CalculServerInterface stub = null;
		try {
			Registry registry = LocateRegistry.getRegistry(hostname, port);
			stub = (CalculServerInterface) registry.lookup("calculServer");
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

	private NameServiceInterface loadNameServiceStub(String hostname) {
		NameServiceInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname,5000);
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

	private void run(String fileName, boolean unsecureMode) {

		long startTime = System.nanoTime();
		
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(30);

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try{

			String nameServiceIp = initNameServiceIP();

			int nOps = initCommandsAndNops(fileName);

			nameService = loadNameServiceStub(nameServiceIp);

			signInNameService("config/config_repartiteur");

			ArrayList<CSModel> calculServers = nameService.getCalculServers();

			for (int i = 0; i < calculServers.size(); i++){
				calculServer = loadCalculServer(calculServers.get(i).getIpAddr(),calculServers.get(i).getPort());					
				servers.add(new CalculServersInstances(calculServer,calculServers.get(i).getCapacity()));
			}

			// Trier les stubs pour communiquer en premier avec le serveur ayant la meilleur capacite
			servers.sort(Collections.reverseOrder(Comparator.comparing(CalculServersInstances::getCapacity)));
			
			int j = 0;

			List<Future<Integer>> resultList = new ArrayList<>();

			// if systeme securise then:
			if (!unsecureMode){

				while (j < nOps){
					for (int i = 0; i < servers.size(); i++){
						double ci = servers.get(i).getCapacity();
						Callable<Integer> callable = new MultithreadingDemo(username,password,servers.get(i).getStub(), commands , ci ,j);
						Future<Integer>  value = executor.submit(callable);
						if (value.get() == -1){
							servers.remove(i);
							if (servers.size() == 0){
								throw new Exception("No server running");
							}
						}
						else {
							resultList.add(value);
							j += ci;
						}
					}
				}

			} else {
				// if systeme non securisee then:

				Map<Integer, Integer> tempResultList = new HashMap<Integer, Integer>();

				while (j < nOps){
					for (int i = 0; i < servers.size(); i++){
						double ci = servers.get(i).getCapacity();
						Callable<Integer> callable = new MultithreadingDemo(username,password,servers.get(i).getStub(), commands , ci ,j);
						Future<Integer>  value = executor.submit(callable);
						if (value.get() == -1){
							servers.remove(i);
							if (servers.size() < 2){
								throw new Exception("Not enough servers to run in unsecure mode: just 1 server still alive ");
							}
						} else if (tempResultList.containsKey((int)(j+ci))){
							if (tempResultList.get((int)(j+ci)).compareTo(value.get()) == 0){
								resultList.add(value);
								j += ci;
							} else {
								tempResultList.clear();
							}
						} else {
							tempResultList.put((int)(j+ci), value.get());
						}
					}
				}

			}

			Integer finalResult = 0;

			for(Future<Integer> future : resultList){
				try
                {
					finalResult += future.get();
					finalResult = finalResult % 4000; 
                }
                catch (InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }
			}
			
			System.out.println("Final Result: " + finalResult);

			executor.shutdown();

			long endTime = System.nanoTime();
		
			long duration = (endTime - startTime);
			System.out.println("Execution Time: " + duration);

		} catch (RemoteException e){
			System.err.println("(RemoteException) Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	public void signInNameService(String fileName) {
		
		try{
			
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split(": ");
				if(words[0].equals("username")){
					username = words[1];
				} else if (words[0].equals("password")){
					password = words[1];
				}
			}

			br.close();

			nameService.signInRepartiteur(username, password);
		
		} catch (FileNotFoundException e){
			System.err.println("(FileNotFoundException) Erreur: " + e.getMessage());
		} catch (IOException e){
			System.err.println("(IOException) Erreur: " + e.getMessage());
		} 
	}

	public String initNameServiceIP() {
		String nameServiceIp = null;

		try{

			BufferedReader br = new BufferedReader(new FileReader("config/config_nameService"));
			String line;
    		while ((line = br.readLine()) != null) {
			   String[] words = line.split(": ");
			   if(words[0].equals("IPaddress")){
				   nameServiceIp = words[1];
			   }
			}
			br.close();
		
		} catch (FileNotFoundException e){
			System.err.println("(FileNotFoundException) Erreur: " + e.getMessage());
		} catch (IOException e){
			System.err.println("(IOException) Erreur: " + e.getMessage());
		} 
		return nameServiceIp;
	}

	public int initCommandsAndNops(String fileName){
		int nOps = 0;
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split(" ");
				commands.add(new Command(words[0],Integer.parseInt(words[1])));
				nOps++;
			}
			br.close();
		} catch (FileNotFoundException e){
			System.err.println("(FileNotFoundException) Erreur: " + e.getMessage());
		} catch (IOException e){
			System.err.println("(IOException) Erreur: " + e.getMessage());
		}
		return nOps; 
	}
}


class MultithreadingDemo implements Callable<Integer>
{
	private String username;
	private String password;
	private CalculServerInterface stub;
	private ArrayList<Command> commands;
	private double capacity;
	private int index;
	private Integer result;

	MultithreadingDemo(String username, String password,CalculServerInterface stub, ArrayList<Command> commands, double cap ,int j) {
		this.username = username;
		this.password = password;
		this.stub = stub;
		this.capacity = cap;
		this.index = j;
		this.commands = commands;
	}

	@Override
    public Integer call(){
        try
        {

			/* 
			calcul du Taux de refus ( on a decide d'envoyer a chaque serveur autant d'operations
			qu'il peut sans le surcharger)
			C'est donc pour cela qu'on ne verifie pas la valeur de T car on sait qu'elle sera toujours = 0 selon notre
			implementation 
			*/

			int T = (int)(((capacity-capacity)/(4*capacity))*100);
			ArrayList<Command> commandToSend = new ArrayList<Command>();
			for (int k = index; k < (index + (int)capacity); k++){
				if( k < commands.size()){
					commandToSend.add(commands.get(k));
				}
			}
			try{
				result = stub.execute(username, password,commandToSend);
			} catch (RemoteException e){
				return -1;
			}
		} 
        catch (Exception e) 
        { 
            // Throwing an exception 
            System.out.println ("Exception is caught"); 
		} 
		return result;
    }

	public int getResult(){
		return result;
	}
} 