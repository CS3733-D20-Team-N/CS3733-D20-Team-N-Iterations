package edu.wpi.N.games;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

class HelpDialog extends JDialog {
  JTextField tf;

  public HelpDialog(JFrame f, String title, boolean modal) {
    super(f, title, modal);
    setLayout(new FlowLayout());
    tf = new JTextField(10);
    add(tf);
    JButton okBtn = new JButton("OK");
    add(okBtn);
    okBtn.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setVisible(false);
          }
        });
    setSize(150, 100);
  }

  int getValue() {
    String s = tf.getText();
    if (s.length() == 0) return -1;
    else {
      try {
        return Integer.parseInt(s);
      } catch (NumberFormatException e) {
        return -1;
      }
    }
  }
}

public class JavaAppSnake extends JFrame {
  Thread snakeThread;
  GroundPanel p;

  public JavaAppSnake() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Container c = getContentPane();
    p = new GroundPanel();
    c.add(p, BorderLayout.CENTER);
    setSize(400, 400);
    createToolBar();
    createMenu();
    setVisible(true);
    p.requestFocus();
    snakeThread = new Thread(p);
    snakeThread.start();

    ToolTipManager tt = ToolTipManager.sharedInstance();
    tt.setInitialDelay(5);
    tt.setDismissDelay(2000);
  }

  public void createToolBar() {

    JToolBar tb = new JToolBar("Snake Controllar");
    tb.setFloatable(false);

    JButton suspendButton = new JButton("Suspend");
    suspendButton.setToolTipText("Stopping.");
    tb.add(suspendButton);

    JButton resumeButton = new JButton("Resume");
    resumeButton.setToolTipText("Continue");
    tb.add(resumeButton);

    tb.addSeparator(new Dimension(100, 50));
    JButton plusButton = new JButton("Speed : +");

    tb.add(plusButton);

    JButton minusButton = new JButton("Speed : -");
    tb.add(minusButton);

    JButton helpButton = new JButton("HELP");
    tb.add(helpButton);

    getContentPane().add(tb, BorderLayout.NORTH);

    suspendButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            p.suspend();
            p.requestFocus();
          }
        });

    resumeButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            p.resume();
            p.requestFocus();
          }
        });

    plusButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            p.speedUp();
            p.requestFocus();
          }
        });

    minusButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            p.speedDown();
            p.requestFocus();
          }
        });

    helpButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {

            String s = JOptionPane.showInputDialog("Enter delay value");
            int n = Integer.parseInt(s);
            if (n > 0) p.setDelay(n);
            p.requestFocus();
          }
        });
  }

  public void createMenu() {
    JMenuBar mb = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem item1 = new JMenuItem("Thread Resume");
    JMenuItem item2 = new JMenuItem("Thread Suspend");
    JMenuItem item3 = new JMenuItem("Thread Kill");
    JMenuItem item4 = new JMenuItem("Change Background");

    mb.add(fileMenu);

    fileMenu.add(item1);
    fileMenu.add(item2);
    fileMenu.addSeparator();
    fileMenu.add(item3);
    fileMenu.add(item4);
    this.setJMenuBar(mb);

    item1.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            p.resume();
          }
        });

    item2.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            p.suspend();
          }
        });

    item3.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            int res =
                JOptionPane.showConfirmDialog(null, "You sure?", "END?", JOptionPane.YES_NO_OPTION);

            if (res == JOptionPane.CLOSED_OPTION || res == JOptionPane.NO_OPTION) return;
            else snakeThread.interrupt();
          }
        });

    item4.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            p.suspend();

            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & GIF", "gif", "jpg");
            chooser.setFileFilter(filter);
            chooser.showOpenDialog(null);
            String path = chooser.getSelectedFile().getPath();
            p.setImage(new ImageIcon(path).getImage());

            p.resume();
          }
        });
  }

  class GroundPanel extends JPanel implements Runnable {
    private boolean suspendFlag = false; // true:suspend

    static final int LEFT = 0;
    static final int RIGHT = 1;
    static final int UP = 2;
    static final int DOWN = 3;

    int delay = 200;

    int direction;

    Image img;

    SnakeBody snakeBody;

    public GroundPanel() {
      setLayout(null);
      snakeBody = new SnakeBody();
      snakeBody.addin(this);

      direction = LEFT;
      this.addKeyListener(new MyKeyListener());
      ImageIcon icon = new ImageIcon("edu/wpi/N/images/map/Floor1Reclor.png");
      img = icon.getImage();
    }

    public void setImage(Image img) {
      this.img = img;
      repaint();
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
    }

    public void run() {

      while (true) {
        try {
          checkSuspend();
          Thread.sleep(delay);
          snakeBody.move(direction);
        } catch (InterruptedException e) {
          return;
        }
      }
    }

    synchronized void checkSuspend() {
      if (suspendFlag == false) {
        return;
      } else {
        try {
          wait();
        } catch (InterruptedException e) {
          return;
        }
      }
    }

    void setDelay(int n) {
      delay = n;
    }

    void speedUp() {
      if (delay < 10) return;
      delay = delay / 2;
    }

    void speedDown() {
      delay = delay * 2;
    }

    void suspend() {
      suspendFlag = true;
    }

    synchronized void resume() {
      suspendFlag = false;
      notify();
    }

    class MyKeyListener extends KeyAdapter {

      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
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
    }
  }

  class SnakeBody {
    Vector<JLabel> v = new Vector<JLabel>();
    ImageIcon icon = new ImageIcon("edu/wpi/N/images/map/Floor1Reclor.png");

    public SnakeBody() {
      for (int i = 0; i < 10; i++) {
        JLabel la = new JLabel(icon);
        la.setSize(20, 20);
        la.setToolTipText("I'm a snake");
        la.setLocation(100 + i * 20, 100);
        v.add(la);
      }
    }

    public void addin(JPanel p) {
      for (int i = 0; i < v.size(); i++) {
        p.add(v.get(i));
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
        case GroundPanel.LEFT:
          head.setLocation(head.getX() - 10, head.getY());
          break;

        case GroundPanel.RIGHT:
          head.setLocation(head.getX() + 10, head.getY());
          break;

        case GroundPanel.UP:
          head.setLocation(head.getX(), head.getY() - 10);
          break;

        case GroundPanel.DOWN:
          head.setLocation(head.getX(), head.getY() + 10);
          break;
      }
    }
  }

  public static void main(String args[]) {
    new JavaAppSnake();
  }
}
