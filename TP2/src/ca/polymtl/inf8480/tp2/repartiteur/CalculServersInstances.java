package ca.polymtl.inf8480.tp2.repartiteur;

import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;

/* Classe representant une instance d'un serveur de calcul*/
public class CalculServersInstances{

    private CalculServerInterface stub = null;
    private String IpAddr = null;
    private int port = 0;
    private int capacity = 0;

    public CalculServersInstances(CalculServerInterface stub, String IpAddr, int port, int capacity) {
        this.stub = stub;
        this.IpAddr = IpAddr;
        this.port = port;
        this.capacity = capacity;
    }

    public CalculServerInterface getStub(){
        return this.stub;
    }

    public String getIpAddr(){
        return this.IpAddr;
    }

    public int getPort(){
        return this.port;
    }

    public int getCapacity(){
        return this.capacity;
    }

  }
