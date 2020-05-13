package edu.wpi.N.Games;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class game150 {

  public static void main(String[] ar) {

    MainFrame mf = new MainFrame();
  }
}

class MainFrame extends JFrame implements MouseListener, Runnable {

  private JLabel lb_title = new JLabel();

  private JLabel lb_time = new JLabel();

  private JButton bt_start = new JButton("START");

  private JButton bt_reset = new JButton("RESET");

  SimpleDateFormat time_format;

  String show_time;

  long start_time, current_time, actual_time;

  boolean time_run = false;

  Thread th;

  ImagePanel sc;

  MainFrame() {

    init();

    start();

    setTitle("1 to 50 Game");

    setSize(600, 600);

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    int xpos = (int) (screen.getWidth() / 2 - getWidth() / 2);

    int ypos = (int) (screen.getHeight() / 2 - getHeight() / 2);

    setLocation(xpos, ypos);

    setResizable(false);

    setVisible(true);
  }

  public void init() {

    Container con = this.getContentPane();

    con.setLayout(null);

    lb_title.setBounds(250, 10, 300, 30);

    lb_title.setFont(new Font("Default", Font.BOLD, 20));

    con.add(lb_title);

    lb_time.setBounds(400, 50, 150, 30);

    lb_time.setFont(new Font("Default", Font.BOLD, 20));

    con.add(lb_time);

    bt_start.setBounds(100, 520, 100, 30);

    bt_start.setFont(new Font("Default", Font.BOLD, 20));

    con.add(bt_start);

    bt_reset.setBounds(400, 520, 100, 30);

    bt_reset.setFont(new Font("Default", Font.BOLD, 20));

    con.add(bt_reset);

    sc = new ImagePanel();

    sc.setBounds(100, 100, 410, 410);

    con.add(sc);
  }

  public void start() {

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this.addMouseListener(this);

    bt_start.addMouseListener(this);

    bt_reset.addMouseListener(this);

    th = new Thread(this);

    th.start();

    time_format = new SimpleDateFormat("HH:mm:ss.SSS");

    lb_time.setText("00:00:00.000");

    lb_title.setText("1 to 50 Game");

    sc.gameStart(false);
  }

  public void run() {

    while (true) {

      try {

        repaint();

        TimeCheck();

        Thread.sleep(15);

      } catch (Exception e) {

      }
    }
  }

  public void TimeCheck() {

    if (time_run) {

      current_time = System.currentTimeMillis();

      actual_time = current_time - start_time;

      sc.countDown((int) actual_time / 1000);

      if (!sc.game_start && sc.check <= 50) {

        show_time = time_format.format(actual_time - 32403000);

        lb_time.setText(show_time);
      }
    }

    if (sc.check > 50) {

      sc.ClearTime(lb_time.getText());
    }
  }

  public void mouseClicked(MouseEvent e) {

    if (e.getSource() == bt_start) {

      if (!time_run && !sc.game_start) {

        start_time = System.currentTimeMillis();

        sc.rect_select.clear();

        time_run = true;

        sc.gameStart(true);
      }

    } else if (e.getSource() == bt_reset) {

      start_time = 0;

      lb_time.setText("00:00:00.000");

      sc.rect_select.clear();

      sc.countDown(0);

      time_run = false;

      sc.gameStart(false);

      sc.check = 0;
    }
  }

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}

class ImagePanel extends JPanel implements MouseListener {

  int count = 3;

  int x, y;

  int check;

  String time;

  boolean game_start = false;

  Random ran_num = new Random();

  Vector rect_select = new Vector();

  SelectRect sr;

  ImagePanel() {

    this.addMouseListener(this);
  }

  public void countDown(int count) {

    switch (count) {
      case 0:
        this.count = 3;

        break;

      case 1:
        this.count = 2;

        break;

      case 2:
        this.count = 1;

        break;

      case 3:
        this.count = 0;

        game_start = false;

        break;
    }
  }

  public void gameStart(boolean game_start) {

    this.game_start = game_start;

    if (this.game_start) {

      check = 1;

      for (int i = 0; i < 5; ++i) {

        for (int j = 0; j < 5; ++j) {

          int num = 0;

          int xx = 5 + i * 80;

          int yy = 5 + j * 80;

          num = ran_num.nextInt(25) + 1;

          for (int k = 0; k < rect_select.size(); ++k) {

            sr = (SelectRect) rect_select.get(k);

            if (sr.number == num) {

              num = ran_num.nextInt(25) + 1;

              k = -1;
            }
          }

          sr = new SelectRect(xx, yy, num);

          rect_select.add(sr);
        }
      }
    }
  }

  public void paint(Graphics g) {

    g.drawRect(0, 0, 400, 400);

    if (game_start) {
      g.setFont(new Font("Default", Font.BOLD, 50));

      g.drawString("CountDown", 70, 150);

      g.setFont(new Font("Default", Font.BOLD, 100));

      g.drawString("" + count, 170, 250);

    } else if (!game_start && count == 0) {

      for (int i = 0; i < rect_select.size(); ++i) {

        sr = (SelectRect) rect_select.get(i);

        g.drawRect(sr.x, sr.y, 70, 70);

        g.setFont(new Font("Default", Font.BOLD, 30));

        g.drawString("" + sr.number, sr.x + 22, sr.y + 45);
      }

      g.setColor(Color.red);

      g.drawRect(x * 80 + 5, y * 80 + 5, 70, 70);
    }

    if (check > 50) {

      g.setColor(Color.blue);

      g.setFont(new Font("Default", Font.BOLD, 50));

      g.drawString("GAME CLEAR!", 40, 150);

      g.setColor(Color.red);

      g.setFont(new Font("Default", Font.BOLD, 40));

      g.drawString("" + time, 90, 250);
    }
  }

  public void ClearTime(String time) {

    this.time = time;
  }

  public void mouseClicked(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {

    x = e.getX() / 80;

    y = e.getY() / 80;

    if (count == 0) {

      for (int i = 0; i < rect_select.size(); ++i) {

        sr = (SelectRect) rect_select.get(i);

        if (x == sr.x / 80 && y == sr.y / 80) {

          int xx = sr.x;

          int yy = sr.y;

          if (sr.number - check == 0) {

            check++;

            rect_select.remove(i);

            if (check < 27) {

              int num = 0;

              num = ran_num.nextInt(25) + 26;

              for (int k = 0; k < rect_select.size(); ++k) {

                sr = (SelectRect) rect_select.get(k);

                if (sr.number == num) {

                  num = ran_num.nextInt(25) + 26;

                  k = -1;
                }
              }

              sr = new SelectRect(xx, yy, num);

              rect_select.add(sr);
            }
          }
        }
      }
    }
  }

  public void mouseReleased(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}

class SelectRect {

  int x, y;

  int number;

  SelectRect(int x, int y, int number) {

    this.x = x;

    this.y = y;

    this.number = number;
  }
}
