package edu.wpi.N.Games;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class PlaneShooter {
  public static void main(String[] ar) {
    game_Frame fms = new game_Frame();
  }
}

class game_Frame extends JFrame implements KeyListener, Runnable {

  int f_width;
  int f_height;

  int x, y;

  boolean KeyUp = false;
  boolean KeyDown = false;
  boolean KeyLeft = false;
  boolean KeyRight = false;
  boolean KeySpace = false;

  Thread th;

  Toolkit tk = Toolkit.getDefaultToolkit();
  Image me_img;
  Image Missile_img;

  ArrayList Missile_List = new ArrayList();

  Image buffImage;
  Graphics buffg;

  Missile ms;

  game_Frame() {
    init();
    start();

    setTitle("Shooting Game");
    setSize(f_width, f_height);

    Dimension screen = tk.getScreenSize();

    int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
    int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);

    setLocation(f_xpos, f_ypos);
    setResizable(false);
    setVisible(true);
  }

  public void init() {
    x = 100;
    y = 100;
    f_width = 800;
    f_height = 600;

    me_img = tk.getImage("/edu/wpi/N/images/mikeswag.png");

    Missile_img = tk.getImage("/edu/wpi/N/images/wong_sun.png");
  }

  public void start() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    addKeyListener(this);
    th = new Thread(this);
    System.out.println("Game Started");
    th.start();
  }

  public void run() {
    System.out.println("System running");
    try {
      while (true) {
        KeyProcess();

        MissileProcess();

        repaint();
        Thread.sleep(20);
      }
    } catch (Exception e) {
    }
  }

  public void MissileProcess() {
    if (KeySpace) {
      ms = new Missile(x, y);
      Missile_List.add(ms);
    }
  }

  public void paint(Graphics g) {
    buffImage = createImage(f_width, f_height);
    buffg = buffImage.getGraphics();
    System.out.println("Paint working");

    update(g);
  }

  public void update(Graphics g) {
    Draw_Char();

    Draw_Missile();

    g.drawImage(buffImage, 0, 0, this);
  }

  public void Draw_Char() {
    buffg.clearRect(0, 0, f_width, f_height);
    buffg.drawImage(me_img, x, y, this);
  }

  public void Draw_Missile() {
    for (int i = 0; i < Missile_List.size(); ++i) {

      ms = (Missile) (Missile_List.get(i));

      buffg.drawImage(Missile_img, ms.pos.x + 150, ms.pos.y + 30, this);

      ms.move();

      if (ms.pos.x > f_width) {
        Missile_List.remove(i);
      }
    }
  }

  public void keyPressed(KeyEvent e) {

    switch (e.getKeyCode()) {
      case KeyEvent.VK_UP:
        KeyUp = true;
        break;
      case KeyEvent.VK_DOWN:
        KeyDown = true;
        break;
      case KeyEvent.VK_LEFT:
        KeyLeft = true;
        break;
      case KeyEvent.VK_RIGHT:
        KeyRight = true;
        break;

      case KeyEvent.VK_SPACE:
        KeySpace = true;
        break;
    }
  }

  public void keyReleased(KeyEvent e) {

    switch (e.getKeyCode()) {
      case KeyEvent.VK_UP:
        KeyUp = false;
        break;
      case KeyEvent.VK_DOWN:
        KeyDown = false;
        break;
      case KeyEvent.VK_LEFT:
        KeyLeft = false;
        break;
      case KeyEvent.VK_RIGHT:
        KeyRight = false;
        break;

      case KeyEvent.VK_SPACE:
        KeySpace = false;
        break;
    }
  }

  public void keyTyped(KeyEvent e) {}

  public void KeyProcess() {

    if (KeyUp == true) y -= 5;
    if (KeyDown == true) y += 5;
    if (KeyLeft == true) x -= 5;
    if (KeyRight == true) x += 5;
  }
}

class Missile {

  Point pos;

  Missile(int x, int y) {
    pos = new Point(x, y);
  }

  public void move() {
    pos.x += 10;
  }
}
