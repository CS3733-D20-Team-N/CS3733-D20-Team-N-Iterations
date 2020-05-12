package edu.wpi.N.games;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

class Ex9 extends JFrame {
  GamePanel p;

  Ex9() {
    this.setTitle("Move the Snake!");
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    p = new GamePanel();
    this.setLocationRelativeTo(null);
    this.setSize(400, 400);
    this.setVisible(true);
    this.add(p);
    p.requestFocus();
    Thread s = new Thread(p);
    s.start();
  }

  class GamePanel extends JPanel implements Runnable {
    static final int LEFT = 0;
    static final int RIGHT = 1;
    static final int UP = 2;
    static final int DOWN = 3;
    static final int DELAY = 200;
    int direction;
    SnakeBody snakebody;

    GamePanel() {
      this.setLayout(null);
      snakebody = new SnakeBody();
      snakebody.addIn(this);
      direction = LEFT;
      this.addKeyListener(new MyKeyListener());
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      ImageIcon icon = new ImageIcon("bg.jpg");
      Image img = icon.getImage();
      g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
    }

    class MyKeyListener implements KeyListener {
      @Override
      public void keyTyped(KeyEvent ke) {}

      @Override
      public void keyPressed(KeyEvent ke) {
        switch (ke.getKeyCode()) {
          case KeyEvent.VK_LEFT:
            direction = LEFT;
            break;
          case KeyEvent.VK_RIGHT:
            direction = RIGHT;
            break;
          case KeyEvent.VK_UP:
            direction = UP;
            break;
          case KeyEvent.VK_DOWN:
            direction = DOWN;
            break;
        }
      }

      @Override
      public void keyReleased(KeyEvent ke) {}
    }

    public void run() {
      while (true) {
        try {
          Thread.sleep(DELAY);
          snakebody.move(direction);
        } catch (Exception e) {
          return;
        }
      }
    }
  }

  class SnakeBody {
    Vector<JLabel> v = new Vector<JLabel>();

    public SnakeBody() {

      ImageIcon head = new ImageIcon("head.jpg");
      JLabel la = new JLabel(head);
      la.setSize(head.getIconWidth(), head.getIconHeight());
      la.setLocation(100, 100);
      v.add(la);

      ImageIcon body = new ImageIcon("body.jpg");
      for (int i = 1; i < 10; i++) {
        la = new JLabel(body);
        la.setSize(body.getIconWidth(), body.getIconHeight());
        la.setLocation(100 + i * 20, 100);
        v.add(la);
      }
    }

    public void addIn(JPanel panel) {
      for (int i = 0; i < v.size(); i++) panel.add(v.get(i));
    }

    public void move(int direction) {
      for (int i = v.size() - 1; i > 0; i--) {
        JLabel b = v.get(i);
        JLabel a = v.get(i - 1);
        b.setLocation(a.getX(), a.getY());
      }

      JLabel head = v.get(0);
      switch (direction) {
        case GamePanel.LEFT:
          head.setLocation(head.getX() - 20, head.getY());
          break;
        case GamePanel.RIGHT:
          head.setLocation(head.getX() + 20, head.getY());
          break;
        case GamePanel.UP:
          head.setLocation(head.getX(), head.getY() - 20);
          break;
        case GamePanel.DOWN:
          head.setLocation(head.getX(), head.getY() + 20);
          break;
      }
    }
  }
}

public class JavaAppSnake {
  public static void main(String[] args) {
    new Ex9();
  }
}
