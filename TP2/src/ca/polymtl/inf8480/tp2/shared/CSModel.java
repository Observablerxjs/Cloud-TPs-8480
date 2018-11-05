package ca.polymtl.inf8480.tp2.shared;

import java.io.Serializable;


public class CSModel implements Serializable {

    private String ipaddr = null;
    private int port = 0;
    private int capacity = 0;

    public CSModel(String ipaddr, int port, int capacity) {
        this.ipaddr = ipaddr;
        this.port = port;
        this.capacity = capacity;
    }

    public String getIpAddr(){
        return this.ipaddr;
    }

    public int getPort(){
        return this.port;
    }

    public int getCapacity(){
        return this.capacity;
    }

    @Override
    public String toString() {
        return "CalculServer [IP address=" + ipaddr + ", port=" + port
                + ", capacity= " + capacity +"]";
    }

  }
