package edu.wpi.N.games;

import java.awt.*;
import javax.swing.*;

public class GamePane extends JPanel implements Runnable {
  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(DELAY);
        snake.move(direction);
      } catch (InterruptedException e) {
        e.printStackTrace();
        return;
      }
    }
  }

  static final int LEFT = 0;
  static final int RIGHT = 1;
  static final int UP = 2;
  static final int DOWN = 3;
  static final int DELAY = 200;
  int direction;
  SnakeBody snake;

  GamePane() {
    this.setLayout(null);
    snake = new SnakeBody();
    snake.addIn(this);
    direction = LEFT;
    this.addKeyListener(new MyKeyListner());
  }

  public void paintBackground(Graphics g) {
    super.paintComponent(g);
    ImageIcon icon = new ImageIcon("");
    Image img = icon.getImage();
    g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
  }
}
