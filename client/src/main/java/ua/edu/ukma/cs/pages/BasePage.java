package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.app.PingPongClient;

import javax.swing.*;

public abstract class BasePage extends JPanel {

    protected final PingPongClient app;

    public BasePage(PingPongClient app) {
        this.app = app;
    }

    public void init() {}
} 