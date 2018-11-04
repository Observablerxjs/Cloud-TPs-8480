package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CalculServerInterface extends Remote{
    /*public boolean New(String login, String password) throws RemoteException;
    public boolean verify(String login, String password) throws RemoteException;*/
    public void test() throws RemoteException;
}
