package ca.polymtl.inf8480.tp2.shared;

import java.io.Serializable;


public class Command implements Serializable {

    private String operand = null;
    private int value = 0;

    public Command(String operand, int value) {
        this.operand = operand;
        this.value = value;
    }

    public String getOperand(){
        return this.operand;
    }

    public int getValue(){
        return this.value;
    }

    @Override
    public String toString() {
        return "Command [operand=" + operand + ", value=" + value +"]";
    }

  }
