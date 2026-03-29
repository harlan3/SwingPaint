package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import Controller.ColorChooser;
import Controller.CoordinateBar;
import Controller.PaintPanel;
import Controller.ToolBar;

public class DrawFrame extends JFrame {

	// fields
	private JPanel contentPane;
	private PaintPanel inkPanel;
	private JToolBar toolBar;
	private JToolBar colorChooser;
	private ColorChooser colorChooserController;
	private CoordinateBar coordinateBar;
	private JScrollPane scrollPane;

	private final int CONTENT_PANE_WIDTH = 1300;
	private final int CONTENT_PANE_HEIGHT = 700;

	private int inkPanelWidth;
	private int inkPanelHeight;
	private final Color background = Color.GRAY;

	public DrawFrame() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		inkPanelWidth = dim.width - 150;
		inkPanelHeight = dim.height - 160;
		// construct our layout manager.
		contentPane = new JPanel();
		contentPane.setLayout(null);

		// create a tool bar
		toolBar = (new ToolBar(this)).getToolBar();

		// create coordinate bar at the bottom
		coordinateBar = new CoordinateBar();

		// create color chooser
		colorChooserController = new ColorChooser(this);
		colorChooser = colorChooserController.getToolBar();

		// construct the panels needed. (INKPANEL COMES LAST)
		inkPanel = new PaintPanel(0, this, inkPanelWidth, inkPanelHeight);

		// configure components and add them to the frame.
		this.add(colorChooser, BorderLayout.PAGE_START);
		scrollPane = new JScrollPane();
		scrollPane.setLocation(10, 10);
		scrollPane.setViewportView(inkPanel);
		scrollPane.setSize(inkPanelWidth, inkPanelHeight);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVisible(false);
		contentPane.add(scrollPane);
		contentPane.setBackground(background);

		// add listeners to buttons
		this.addWindowListener(new WindowCloser());

		// set components into the contentPane
		this.add(coordinateBar, BorderLayout.PAGE_END);
		this.add(toolBar, BorderLayout.WEST);
		this.add(contentPane);

		setLocation(0, 0);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		setVisible(true);
	}

	private class WindowCloser extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent event) {
			System.exit(0);
		}
	}

	public CoordinateBar getCoordinateBar() {
		return this.coordinateBar;
	}

	public PaintPanel getInkPanel() {
		return this.inkPanel;
	}

	public DrawFrame getDrawFrame() {
		return this;
	}

	public JScrollPane getScrollPane() {
		return this.scrollPane;
	}

	public ColorChooser getColorChooserController() {
		return this.colorChooserController;
	}

	public void setDocumentVisible(boolean visible) {
		scrollPane.setVisible(visible);
		scrollPane.revalidate();
		scrollPane.repaint();
		contentPane.revalidate();
		contentPane.repaint();
	}
	
	public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
        	new DrawFrame();
        });
	}

}
