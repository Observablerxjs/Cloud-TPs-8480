package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.File;

public interface ServerInterface extends Remote {
	void execute(File file) throws RemoteException;
}
