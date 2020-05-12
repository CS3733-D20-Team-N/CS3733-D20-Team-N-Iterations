package edu.wpi.N.games;

import static edu.wpi.N.games.GamePane.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MyKeyListner implements KeyListener {

  GamePane p;

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:
        p.direction = LEFT;
        break;
      case KeyEvent.VK_RIGHT:
        p.direction = RIGHT;
        break;
      case KeyEvent.VK_UP:
        p.direction = UP;
        break;
      case KeyEvent.VK_DOWN:
        p.direction = DOWN;
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {}
}
