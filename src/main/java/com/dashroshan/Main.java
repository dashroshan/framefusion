package com.dashroshan;

// GUI FatJARs don't work when the main class extends Application
// Hence we use a wrapper class
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}