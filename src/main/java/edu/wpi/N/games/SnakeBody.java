package edu.wpi.N.games;

import java.util.Vector;
import javax.swing.*;

public class SnakeBody {
  Vector<JLabel> v = new Vector<JLabel>();

  public SnakeBody() {
    ImageIcon head = new ImageIcon("");
    JLabel la = new JLabel(head);
    la.setSize(head.getIconWidth(), head.getIconHeight());
    la.setLocation(100, 100);
    v.add(la);

    ImageIcon body = new ImageIcon("");
    for (int i = 0; i < 10; i++) {
      la = new JLabel(body);
      la.setSize(body.getIconWidth(), body.getIconHeight());
      la.setLocation(100 + i * 20, 100);
      v.add(la);
    }
  }

  public void addIn(JPanel panel) {
    for (int i = 0; i < v.size(); i++) {
      panel.add(v.get(i));
    }
  }

  public void move(int direction) {
    for (int i = v.size() - 1; i > 0; i--) {
      JLabel b = v.get(i);
      JLabel a = v.get(i - 1);
      b.setLocation(a.getX(), a.getY());
    }

    JLabel head = v.get(0);
    switch (direction) {
      case GamePane.LEFT:
        head.setLocation(head.getX() - 20, head.getY());
        break;
      case GamePane.RIGHT:
        head.setLocation(head.getX() + 20, head.getY());
        break;
      case GamePane.UP:
        head.setLocation(head.getX(), head.getY() - 20);
        break;
      case GamePane.DOWN:
        head.setLocation(head.getX(), head.getY() + 20);
        break;
    }
  }
}
