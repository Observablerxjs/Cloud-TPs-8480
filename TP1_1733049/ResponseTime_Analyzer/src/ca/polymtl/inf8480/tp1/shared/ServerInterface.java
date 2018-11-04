package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

public interface ServerInterface extends Remote {
	boolean create(String filename, String login, String password) throws RemoteException, IOException, Exception;
	ArrayList<String> list(String login, String pass) throws RemoteException;
	ArrayList<File> syncLocalDirectory(String login, String pass) throws RemoteException;
	File get(String filename, String checksum, String login, String password) throws RemoteException,IOException, Exception;
	File lock(String filename, String checksum, String login, String password) throws RemoteException,IOException, Exception;
	String push(String filename, File file, String login, String password) throws RemoteException,IOException, Exception;		
}
