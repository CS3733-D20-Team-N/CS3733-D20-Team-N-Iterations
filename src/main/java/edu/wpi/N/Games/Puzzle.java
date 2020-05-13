package edu.wpi.N.Games;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Puzzle extends JPanel implements MouseListener {

  int count = 0, game[];
  int row = 2, col = 2;
  Image org;
  BufferedImage img[];
  int width, height;
  int clickCount, oldNum;

  public Puzzle() {
    MediaTracker tracker = new MediaTracker(this);
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}
}
