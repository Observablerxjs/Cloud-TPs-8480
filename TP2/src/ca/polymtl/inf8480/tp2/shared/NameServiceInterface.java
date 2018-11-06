package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;


public interface NameServiceInterface extends Remote {
	public void test(String test) throws RemoteException;
	public void signInRepartiteur(String username, String password) throws RemoteException;
	public void signIn(String IP, int port, int capacity) throws RemoteException;
	public ArrayList<CSModel> getCalculServers() throws RemoteException;
	public boolean verifyUser(String username, String password) throws RemoteException;
}