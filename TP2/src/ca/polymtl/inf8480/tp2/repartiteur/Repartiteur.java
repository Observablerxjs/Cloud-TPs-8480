package ca.polymtl.inf8480.tp2.repartiteur;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadPoolExecutor;


import ca.polymtl.inf8480.tp2.repartiteur.CalculServersInstances;

import ca.polymtl.inf8480.tp2.shared.CSModel;
import ca.polymtl.inf8480.tp2.shared.Command;
import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;
import ca.polymtl.inf8480.tp2.shared.NameServiceInterface;

public class Repartiteur {

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
						Callable<Integer> callable = new MultithreadingDemo(servers.get(i).getStub(), commands , ci ,j);
						Future<Integer>  value = executor.submit(callable);
						resultList.add(value);
						j += ci;
					}
				}

			} else {
				// if systeme non securisee then:

				Map<Integer, Integer> tempResultList = new HashMap<Integer, Integer>();

				while (j < nOps){
					for (int i = 0; i < servers.size(); i++){
						double ci = servers.get(i).getCapacity();
						Callable<Integer> callable = new MultithreadingDemo(servers.get(i).getStub(), commands , ci ,j);
						Future<Integer>  value = executor.submit(callable);
						if (tempResultList.containsKey((int)(j+ci))){
							if (tempResultList.get((int)(j+ci)).compareTo(value.get()) == 0){
								resultList.add(value);
								j += ci;
							} else {
								System.out.println("FOUND A MALICIOUS SERVER");
								System.out.println("previous Result: " + tempResultList.get((int)(j+ci))+ " ; new result: " + (value.get()));
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
                    // System.out.println("Future result is - " + " - " + future.get() + "; And Task done is " + future.isDone());
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
	private CalculServerInterface stub;
	private ArrayList<Command> commands;
	private double capacity;
	private int index;
	private Integer result;

	MultithreadingDemo(CalculServerInterface stub, ArrayList<Command> commands, double cap ,int j) {
		this.stub = stub;
		this.capacity = cap;
		this.index = j;
		this.commands = commands;
	}

	@Override
    public Integer call(){
        try
        {
			// calcul du Taux de refus ( on a decide d'envoyer a chaque serveur autant d'operations qu'il peut sans surcharger)
			// T sera donc toujours = 0;
			int T = (int)(((capacity-capacity)/(4*capacity))*100);
			ArrayList<Command> commandToSend = new ArrayList<Command>();
			for (int k = index; k < (index + (int)capacity); k++){
				if( k < commands.size()){
					commandToSend.add(commands.get(k));
				}
			}
			result = stub.execute(commandToSend);
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