package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface CalculServerInterface extends Remote{
    public Integer execute(String username, String password,ArrayList<Command> test) throws RemoteException;
}
