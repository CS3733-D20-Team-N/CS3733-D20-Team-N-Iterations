package edu.wpi.N.games;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

class Games2 extends JFrame {
  Games2() {
    this.setTitle("Shooter Game");
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    GamePane2 p = new GamePane2();
    this.add(p);
    this.setLocationRelativeTo(null);
    this.setSize(300, 300);
    this.setResizable(false);
    this.setVisible(true);
    p.start();
  }
}

class GamePane2 extends JPanel {
  TargetThread targetThread;
  JLabel base = new JLabel();
  JLabel bullet = new JLabel();
  JLabel target;

  GamePane2() {
    this.setLayout(null);
    base.setSize(40, 40);
    base.setOpaque(true);
    base.setBackground(Color.BLUE);

    ImageIcon img = new ImageIcon("resources/edu/wpi/N/images/wong_sun.PNG");
    target = new JLabel(img);
    target.setSize(img.getIconWidth(), img.getIconHeight());

    bullet.setSize(10, 10);
    bullet.setOpaque(true);
    bullet.setBackground(Color.red);
    this.add(base);
    this.add(target);
    this.add(bullet);
    // TODO: add sound
  }

  public void start() {
    base.setLocation(this.getWidth() / 2 - 20, this.getHeight() - 40);
    bullet.setLocation(this.getWidth() / 2 - 5, this.getHeight() - 50);
    target.setLocation(0, 0);

    targetThread = new TargetThread(target);
    targetThread.startup();

    base.requestFocus();
    base.addKeyListener(
        new KeyListener() {
          BulletThread bulletThread = null;

          @Override
          public void keyTyped(KeyEvent e) {}

          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
              if (bulletThread == null || bulletThread.isAlive()) {
                bulletThread = new BulletThread(bullet, target, targetThread);
                bulletThread.startup();
              }
            }
          }

          @Override
          public void keyReleased(KeyEvent e) {}
        });
  }

  class TargetThread extends Thread {
    JLabel target;

    TargetThread(JLabel target) {
      this.target = target;
      target.setLocation(0, 0);
    }

    public void startup() {
      while (true) {
        int x = target.getX() + 5;
        int y = target.getY();

        if (x > GamePane2.this.getWidth()) target.setLocation(0, 0);
        else target.setLocation(x, y);

        try {
          sleep(20);
        } catch (Exception e) {
          target.setLocation(0, 0);
          try {
            sleep(500);
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }
      }
    }
  }

  class BulletThread extends Thread {
    JLabel bullet, target;
    Thread targetThread;

    public BulletThread(JLabel bullet, JLabel target, Thread targetThread) {
      this.bullet = bullet;
      this.target = target;
      this.targetThread = targetThread;
    }

    public void startup() {
      while (true) {
        if (hit()) {
          targetThread.interrupt(); // kills the thread
          bullet.setLocation(bullet.getParent().getWidth() / 2 - 5, bullet.getParent().getHeight());
          return;
        } else {
          int x = bullet.getX();
          int y = bullet.getY();

          if (y < 0) {
            bullet.setLocation(
                bullet.getParent().getWidth() / 2 - 5, bullet.getParent().getHeight());
            return;
          } else bullet.setLocation(x, y);
        }
        try {
          sleep(20);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    private boolean hit() {
      int x = bullet.getX();
      int y = bullet.getY();
      int w = bullet.getWidth();
      int h = bullet.getHeight();

      if (targetContains(x, y)
          || targetContains(x + w - 1, y)
          || targetContains(x + w - 1, y + h - 1)
          || targetContains(x, y + h - 1)) return true;
      else return false;
    }

    private boolean targetContains(int x, int y) {
      if (((target.getX() <= x) && (x < target.getX() + target.getWidth()))
          && ((target.getY() <= y) && (y < target.getY() + target.getHeight()))) return true;
      else return false;
    }
  }
}

public class JavaAppShooter {
  public static void main(String[] args) {
    new Games2();
  }
}
