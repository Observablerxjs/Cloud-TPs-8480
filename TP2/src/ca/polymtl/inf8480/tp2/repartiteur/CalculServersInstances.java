package ca.polymtl.inf8480.tp2.repartiteur;

import ca.polymtl.inf8480.tp2.shared.CalculServerInterface;

public class CalculServersInstances{

    private CalculServerInterface stub = null;
    private int capacity = 0;

    public CalculServersInstances(CalculServerInterface stub, int capacity) {
        this.stub = stub;
        this.capacity = capacity;
    }

    public CalculServerInterface getStub(){
        return this.stub;
    }

    public int getCapacity(){
        return this.capacity;
    }

  }
