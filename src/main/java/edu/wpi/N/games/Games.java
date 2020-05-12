package edu.wpi.N.games;

import javax.swing.*;

public class Games extends JFrame {
  GamePane p;

  Games() {
    this.setTitle("Moving Snake");
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    p = new GamePane();
    this.setLocationRelativeTo(null);
    this.setSize(800, 800);
    this.setVisible(true);
    this.add(p);
    p.requestFocus();
    Thread t = new Thread(p);
    t.start();
  }
}
