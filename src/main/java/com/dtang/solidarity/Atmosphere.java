package com.dtang.solidarity;

public class Atmosphere {
    private int CO2;
    private int Methane;
    private int N2O;


    public Atmosphere(){
        CO2 = 10000;//naturally occurring from almost all life
        Methane = 1000;
        N2O = 100;//Natural occurence from lightning strikes
    }

    //Modify CO2 in atmosphere by this amount
    public void modifyCO2(int amount){
        CO2 += amount;
    }

    //Modify CO2 in atmosphere by this amount
    public void modifyMethane(int amount){
        Methane += amount;
    }

    //Modify CO2 in atmosphere by this amount
    public void modifyN2O(int amount){
        N2O += amount;
    }
}
