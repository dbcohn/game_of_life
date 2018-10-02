/*
 * Conway's Game of Life programmed in Java
 *
 * Derek Bradley Cohn
 * 2014
 */

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.util.Random;

@SuppressWarnings("serial")
public class GameOfLife extends JComponent implements ActionListener, ItemListener
{
	private int WIN_SIZE = 600;	// WIN_SIZE must be evenly
	private int BLK_SIZE = 10;	// divisible by BLK_SIZE
	private int CEL_SIZE = WIN_SIZE / BLK_SIZE;

	private Random rand = new Random();

	private boolean[][] cells = new boolean[CEL_SIZE][CEL_SIZE];

	private Color bg = Color.WHITE;
	private Color fg = Color.BLACK;

	private long ticks = 1;
	boolean inGame = false;

	private JFrame window;
	private JMenuBar menuBar;
	private JMenu menuGame;
	private JMenu menuColors;
	private JMenu menuHelp;
	private JMenuItem menuItemNewGame;
	private JMenuItem menuItemRandom;
	private JMenuItem menuItemColorFG;
	private JMenuItem menuItemColorBG;
	private JMenuItem menuItemAbout;
	private JMenuItem menuItemRules;
	private JMenuItem menuItemExit;

	private JPanel panel;
	private JLabel label = new JLabel("Generations: " + ticks);
	private JButton button = new JButton("Start");

	private Timer tick;

	public static void main(String[] args) {

		new GameOfLife();

	}

	public GameOfLife()
	{
		super();

		window = new JFrame("Derek's Game of Life");

		/*
		 * Setup the menu.
		 */
		menuBar = new JMenuBar();

		menuGame = new JMenu("Game");
		menuGame.setMnemonic(KeyEvent.VK_G);
		menuGame.getAccessibleContext().setAccessibleDescription(
		        "The only menu in this program that has menu items");
		menuBar.add(menuGame);

		menuItemNewGame = new JMenuItem("New Game", KeyEvent.VK_N);
		menuItemNewGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItemNewGame.addActionListener(this);
		menuGame.add(menuItemNewGame);

		menuItemRandom = new JMenuItem("Generate Random Pattern", KeyEvent.VK_G);
		menuItemRandom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItemRandom.addActionListener(this);
		menuGame.add(menuItemRandom);

		menuGame.addSeparator();

		menuItemExit = new JMenuItem("Exit", KeyEvent.VK_E);
		menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
		menuItemExit.addActionListener(this);
		menuGame.add(menuItemExit);

		menuColors = new JMenu("Colors");
		menuColors.setMnemonic(KeyEvent.VK_C);
		menuColors.getAccessibleContext().setAccessibleDescription(
		        "The only menu in this program that has menu items");
		menuBar.add(menuColors);

		menuItemColorFG = new JMenuItem("Set Cell Color", KeyEvent.VK_C);
		menuItemColorFG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
		menuItemColorFG.addActionListener(this);
		menuColors.add(menuItemColorFG);

		menuItemColorBG = new JMenuItem("Set Background Color", KeyEvent.VK_B);
		menuItemColorBG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.ALT_MASK));
		menuItemColorBG.addActionListener(this);
		menuColors.add(menuItemColorBG);

		menuHelp = new JMenu("Help");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuHelp.getAccessibleContext().setAccessibleDescription(
		        "The only menu in this program that has menu items");
		menuBar.add(menuHelp);

		menuItemRules = new JMenuItem("Rules", KeyEvent.VK_R);
		menuItemRules.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, ActionEvent.ALT_MASK));
		menuItemRules.addActionListener(this);
		menuHelp.add(menuItemRules);

		menuItemAbout = new JMenuItem("About", KeyEvent.VK_A);
		menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, ActionEvent.ALT_MASK));
		menuItemAbout.addActionListener(this);
		menuHelp.add(menuItemAbout);

		/*
		 * Setup the panel and button.
		 */
		panel = new JPanel(new GridBagLayout());

		button.addActionListener(this);

		panel.addMouseListener(new MouseListener());

		panel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		button.setPreferredSize(new Dimension(90, 30));

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;		// COLUMN
		c.gridy = 0;		// ROW
		c.gridwidth = 4;
		c.gridheight = 1;

		panel.add(this, c);
		
		label.setForeground(Color.BLACK);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_START;

		panel.add(label, c);

		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_END;

		panel.add(button, c);

		window.add(panel);
		window.setJMenuBar(menuBar);

		window.pack();
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window.setVisible(true);

		tick = new Timer(100, this);

		newGame();
	}

	private void newGame()
	{
		inGame = false;
		ticks = 1;
		tick.stop();

		for (int i = 0; i < cells.length; i++)
			for (int k = 0; k < cells[i].length; k++)
				cells[i][k] = false;

		repaint();

		label.setText("Generations: " + ticks);
		button.setText("Start");
		menuItemRandom.setEnabled(true);
		panel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	private void startGame()
	{
		menuItemRandom.setEnabled(false);

		if (inGame) {
			tick.stop();
			inGame = false;
			panel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			button.setText("Resume");
		} else {
			tick.start();
			inGame = true;
			panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			button.setText("Pause");
		}
	}

	private void quitGame()
	{
		window.setVisible(false);
		window.dispose();
		System.exit(0);	
	}

	private void addBlock(int x, int y)
	{
		int x_cel = x / BLK_SIZE;
		int y_cel = y / BLK_SIZE;

		if (x_cel == CEL_SIZE)
			x_cel--;

		if (y_cel == CEL_SIZE)
			y_cel--;

		if (cells[x_cel][y_cel])
			cells[x_cel][y_cel] = false;
		else
			cells[x_cel][y_cel] = true;

		repaint();
	}

	private void genRandomPattern()
	{
		for (int i = 0; i < cells.length; i++) {

			for (int k = 0; k < cells[i].length; k++) {

				int r = rand.nextInt(5);

				if (r == 1)
					cells[i][k] = true;
				else
					cells[i][k] = false;

			}
		}

		repaint();
	}

	private boolean[][] processCells()
	{
		boolean[][] newcells = new boolean[WIN_SIZE/BLK_SIZE][WIN_SIZE/BLK_SIZE];

		ticks++;  // used by the "Generations: ###" label

		for (int x = 0; x < cells.length; x++) {

			for (int y = 0; y < cells[x].length; y++) {

				int neighbors = 0;

				/*
				 * determine how many neighbors each cell currently has
				 */

				if (y > 0)
					if (cells[x][y-1])
						neighbors++;

				if (y < (cells[x].length)-1)
					if (cells[x][y+1])
						neighbors++;

				if (x > 0) {

					if (cells[x-1][y])
						neighbors++;

					if (y > 0)
						if (cells[x-1][y-1])
							neighbors++;

					if (y < (cells[x].length)-1)
						if (cells[x-1][y+1])
							neighbors++;
				}

				if (x < (cells[x].length)-1) {

					if (cells[x+1][y])
						neighbors++;

					if (y > 0)
						if (cells[x+1][y-1])
							neighbors++;

					if (y < (cells[x].length)-1)
						if (cells[x+1][y+1])
							neighbors++;

				}

				/*
				 * implement game of life rules, create the array for the next game tick
				 */

				switch (neighbors) {

					case 0:
					case 1:					// live cell with 0 or 1 neighbors dies
						newcells[x][y] = false;
						break;
					case 2:					// live cell with 2 neighbors stays alive
						newcells[x][y] = cells[x][y];	// OR dead cell with 2 neighbors stays dead
						break;
					case 3:					// live cell with 3 neighbors stays alive
						newcells[x][y] = true;		// OR dead cell with 3 neighbors becomes a live cell
						break;
					default:				// live cell with 4 or more neighbors dies
						newcells[x][y] = false;
						break;

				}
			}
		}

		return newcells;	// return reference to array containing next game tick
	}

	private void showAbout()
	{
		JPanel panel = new JPanel(new BorderLayout());

		JLabel label = new JLabel("<html>Conway's Game of Life<br><br>Programmed by Derek Cohn<br><br>Last modification: June 3, 2014</html>",
				SwingConstants.CENTER);

		panel.add(label, BorderLayout.CENTER);

		JOptionPane.showMessageDialog(window,
		    panel,
		    "About",
		    JOptionPane.INFORMATION_MESSAGE);
	}

	private void showRules()
	{
		JPanel panel = new JPanel(new BorderLayout());

		JLabel label = new JLabel("<html>"
				+ "Any live cell with fewer than two live neighbors dies, as if caused by under-population.<br>"
				+ "Any live cell with two or three live neighbors lives on to the next generation.<br>"
				+ "Any live cell with more than three live neighbors dies, as if by overcrowding.<br>"
				+ "Any dead cell with exactly three live neighbors becomes a live cell, as if by reproduction.<br>",
				SwingConstants.CENTER);

		panel.add(label, BorderLayout.CENTER);

		JOptionPane.showMessageDialog(window,
		    panel,
		    "Game of Life Rules",
		    JOptionPane.INFORMATION_MESSAGE);
	}

	private Color chooseColors(Color current)
	{
		Color selectedColor = JColorChooser.showDialog(window, "Pick a Color", Color.GREEN);

		if (selectedColor == null)
			return current;
		else
			return selectedColor;
	}

	protected void paintComponent(Graphics g)
	{
	    Graphics2D g2 = (Graphics2D)g;

	    RenderingHints rh = new RenderingHints(
	    		RenderingHints.KEY_RENDERING,
	    		RenderingHints.VALUE_RENDER_QUALITY);

	    g2.setRenderingHints(rh);
	    g2.setColor(bg);
	    g2.fillRect(0, 0, WIN_SIZE, WIN_SIZE);

		for (int i = 0; i < cells.length; i++) {

			for (int k = 0; k < cells[i].length; k++) {

				if (cells[i][k]) {

					g2.setColor(fg);
					g2.fillRect(i*BLK_SIZE, k*BLK_SIZE, BLK_SIZE, BLK_SIZE);
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == tick) {

			cells = processCells();

			label.setText("Generations: " + ticks);
			repaint();

		} else if (e.getSource() == button) {

			startGame();

		} else if (e.getSource() == menuItemNewGame) {

			newGame();

		} else if (e.getSource() == menuItemRandom) {

			genRandomPattern();

		} else if (e.getSource() == menuItemAbout) {

			showAbout();

		} else if (e.getSource() == menuItemRules) {

			showRules();

		}  else if (e.getSource() == menuItemExit) {

			quitGame();

		} else if (e.getSource() == menuItemColorFG) {

			fg = chooseColors(fg);

			repaint();

		} else if (e.getSource() == menuItemColorBG) {

			bg = chooseColors(bg);

			repaint();
		}
	}

	public void itemStateChanged(ItemEvent e) {

	}

	public Dimension getPreferredSize()
	{
		return new Dimension(WIN_SIZE, WIN_SIZE);
	}

	class MouseListener extends MouseAdapter
	{
		 public void mouseClicked(MouseEvent e)
		 {
			 int x = e.getX();
			 int y = e.getY();

			 if (x <= WIN_SIZE && y <= WIN_SIZE) {

				 if (!inGame) {
					 addBlock(x, y);
				 }
			 }
		 }
	}
}
