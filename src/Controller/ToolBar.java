package Controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import View.DrawFrame;

public class ToolBar implements ActionListener {
	private JToolBar toolBar;
	private JButton pencil;
	private JButton line;
	private JButton rectangle;
	private JButton square;
	private JButton triangle;
	private JButton circle;
	private JButton ellipse;
	private JButton text;
	private JButton imageBtn;
	private JButton move;
	private JButton erase;
	private JButton fill;
	private JButton undo;
	private JButton redo;
	private JButton delete;
	private JButton clear;
	private JButton rotate;
	private JButton select;
	private JButton copy;
	private JButton paste;
	private JButton colorPick;
	private Dimension newDimensions = new Dimension(700, 500);
	private JButton save;
	private JButton open;
	private JButton newFile;
	private JButton close;
	private JFileChooser fc;
	private JComboBox<String> lineWidth;
	private File f;
	private DrawFrame frame;

    private static final String ICON_DIR = "/icons/";
    private static final String ICON_INDEX = ICON_DIR + "index.txt";
    private List<String> iconResourceNames = null;
    private int currentIconIndex = 0;
    
	public ToolBar(DrawFrame frame) {
		this.frame = frame;
		
		this.iconResourceNames = loadIconIndex();
        if (iconResourceNames.isEmpty()) {
            throw new IllegalArgumentException("No icon entries found in resource: " + ICON_INDEX);
        }
		
		fc = new JFileChooser(new File("."));
		fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png"));
		this.initializeToolBar();
		delete.addActionListener(this);
		clear.addActionListener(this);
		rectangle.addActionListener(this);
		square.addActionListener(this);
		triangle.addActionListener(this);
		line.addActionListener(this);
		circle.addActionListener(this);
		ellipse.addActionListener(this);
		erase.addActionListener(this);
		pencil.addActionListener(this);
		lineWidth.addActionListener(this);
		undo.addActionListener(this);
		redo.addActionListener(this);
		text.addActionListener(this);
		imageBtn.addActionListener(this);
		move.addActionListener(this);
		fill.addActionListener(this);
		save.addActionListener(this);
		open.addActionListener(this);
		newFile.addActionListener(this);
		close.addActionListener(this);
		rotate.addActionListener(this);
		select.addActionListener(this);
		copy.addActionListener(this);
		paste.addActionListener(this);
		colorPick.addActionListener(this);
	}
	
    private ImageIcon retrieveIcon(int index, int width, int height) {
    	
        if (index < 0 || index >= iconResourceNames.size()) {
            return (ImageIcon) null;
        }

        String resourceName = iconResourceNames.get(index);
        String resourcePath = ICON_DIR + resourceName;

        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

    		ImageIcon icon = new ImageIcon(ImageIO.read(in));
    		Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    		
            currentIconIndex++;
            
            return new ImageIcon(image);

        } catch (IOException ex) {
            System.err.println("Failed to load icon: " + resourceName);
            return (ImageIcon) null;
        }
    }

	private void initializeToolBar() {
		// ----------------
		// create buttons for the tool bar
		// ----------------
		toolBar = new JToolBar(JToolBar.VERTICAL);
		toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
		toolBar.setFloatable(false);
		toolBar.setLayout(new GridLayout(28, 0));

		// toolBar.setBackground( new Color(0, 153, 204));
		
		save = new JButton("Save", retrieveIcon(currentIconIndex, 24, 24));
		open = new JButton("Open", retrieveIcon(currentIconIndex, 28, 28));
		newFile = new JButton("New", retrieveIcon(currentIconIndex, 24, 24));
		close = new JButton("Close", retrieveIcon(currentIconIndex, 28, 28));
		pencil = new JButton("Pencil", retrieveIcon(currentIconIndex, 24, 24));
		line = new JButton("Line", retrieveIcon(currentIconIndex, 24, 24));
		rectangle = new JButton("Rectangle", retrieveIcon(currentIconIndex, 30, 30));
		square = new JButton("Square", retrieveIcon(currentIconIndex, 24, 24));
		triangle = new JButton("Triangle", retrieveIcon(currentIconIndex, 24, 24));
		circle = new JButton("Circle", retrieveIcon(currentIconIndex, 24, 24));
		ellipse = new JButton("Ellipse", retrieveIcon(currentIconIndex, 30, 30));
		text = new JButton("Text", retrieveIcon(currentIconIndex, 24, 24));
		imageBtn = new JButton("Image", retrieveIcon(currentIconIndex, 24, 24));
		move = new JButton("Move", retrieveIcon(currentIconIndex, 24, 24));
		fill = new JButton("Fill", retrieveIcon(currentIconIndex, 24, 24));
		erase = new JButton("Erase", retrieveIcon(currentIconIndex, 24, 24));
		undo = new JButton("Undo", retrieveIcon(currentIconIndex, 24, 24));
		redo = new JButton("Redo", retrieveIcon(currentIconIndex, 24, 24));
		clear = new JButton("Clear", retrieveIcon(currentIconIndex, 24, 24));
		delete = new JButton("Delete", retrieveIcon(currentIconIndex, 24, 24));
		rotate = new JButton("Rotate", retrieveIcon(currentIconIndex, 28, 28));
		select = new JButton("Select", retrieveIcon(currentIconIndex, 34, 34));
		copy = new JButton("Copy", retrieveIcon(currentIconIndex, 34, 34));
		paste = new JButton("Paste", retrieveIcon(currentIconIndex, 30, 30));
		colorPick = new JButton("Pick Color", retrieveIcon(currentIconIndex, 28, 28));

		String[] items = { "Line Width", "1", "2", "3", "4", "5", "6", "7", "8" };

		lineWidth = new JComboBox<String>(items);
		lineWidth.setMaximumSize(new Dimension(100, 25));

		// ----------------
		// add buttons to the tool bar
		// ----------------
		toolBar.add(newFile);
		toolBar.add(open);
		toolBar.add(save);
		toolBar.add(close);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(pencil);
		toolBar.add(line);
		toolBar.add(rectangle);
		toolBar.add(square);
		toolBar.add(triangle);
		toolBar.add(circle);
		toolBar.add(ellipse);
		toolBar.add(lineWidth);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(fill);
		toolBar.add(erase);
		toolBar.add(delete);
		toolBar.add(clear);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(text);
		toolBar.add(imageBtn);
		toolBar.add(rotate);
		toolBar.add(colorPick);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(move);
		toolBar.add(select);
		toolBar.add(copy);
		toolBar.add(paste);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(undo);
		toolBar.add(redo);
		toolBar.addSeparator();
	}

	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();

		if (source == clear) {
			frame.getInkPanel().clear();
		} else if (source == delete) {
			frame.getInkPanel().setTool(13);
		} else if (source == pencil) {
			frame.getInkPanel().setTool(0);
		} else if (source == line) {
			frame.getInkPanel().setTool(1);
		} else if (source == rectangle) {
			frame.getInkPanel().setTool(2);
		} else if (source == square) {
			frame.getInkPanel().setTool(11);
		} else if (source == triangle) {
			frame.getInkPanel().setTool(9);
		} else if (source == circle) {
			frame.getInkPanel().setTool(3);
		} else if (source == ellipse) {
			frame.getInkPanel().setTool(8);
		} else if (source == text) {
			frame.getInkPanel().setTool(5);
		} else if (source == imageBtn) {
			frame.getInkPanel().setTool(12);
		} else if (source == move) {
			frame.getInkPanel().setTool(10);
		} else if (source == erase) {
			frame.getInkPanel().setTool(6);
		} else if (source == fill) {
			frame.getInkPanel().setTool(7);
		} else if (source == rotate) {
			frame.getInkPanel().setTool(14);
		} else if (source == select) {
			frame.getInkPanel().setTool(15);
		} else if (source == copy) {
			frame.getInkPanel().copySelection();
		} else if (source == paste) {
			frame.getInkPanel().setTool(16);
		} else if (source == colorPick) {
			frame.getInkPanel().setTool(17);
		} else if (source == undo) {
			frame.getInkPanel().undo();
		} else if (source == redo) {
			frame.getInkPanel().redo();
		} else if (source == lineWidth) {
			try {
				JComboBox combo = (JComboBox) ae.getSource();
				String current = (String) combo.getSelectedItem();
				frame.getInkPanel().setThickness(Float.valueOf(current));
			} catch (NumberFormatException e) {

			}
		} else if (source == open) {
			if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				f = fc.getSelectedFile();
				openFile(f);
			}
		} else if (source == save) {
			// open file saver
			if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				f = new File(fc.getSelectedFile() + ".png");
				try {
					saveFile(f);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (source == newFile) {

			newFile();
		} else if (source == close) {
			if (frame.getInkPanel().hasOpenDocument()) {
				frame.getInkPanel().closeCurrentDocument();
				newDimensions = new Dimension(700, 500);
				setDimensions(newDimensions.width, newDimensions.height);
			}
		} else {
			JButton b = (JButton) source;
			frame.getInkPanel().setColor(b.getBackground());
		}
	}

	public JToolBar getToolBar() {
		return this.toolBar;
	}

	private void setDimensions(int width, int height) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		if (height > dim.height - 160 && width > dim.width - 150) {
			frame.getScrollPane().setSize(dim.width - 150, dim.height - 160);
		} else if (width > dim.width - 150) {
			frame.getScrollPane().setSize(dim.width - 150, height);
		} else if (height > dim.height - 160) {
			frame.getScrollPane().setSize(width, dim.height - 160);
		} else {
			frame.getScrollPane().setSize(width, height);
		}
	}
	
	private String getNextIcon() {
		
		String iconResourceName = iconResourceNames.get(currentIconIndex);
		currentIconIndex++;
		
		return iconResourceName;	
	}
	
    private List<String> loadIconIndex() {
        List<String> frames = new ArrayList<>();

        try (InputStream in = getClass().getResourceAsStream(ICON_INDEX)) {
            if (in == null) {
                throw new IllegalArgumentException("Missing required resource: " + ICON_INDEX);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    if (trimmed.startsWith("#")) {
                        continue;
                    }
                    frames.add(trimmed);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read icon index: " + ICON_INDEX, e);
        }

        return frames;
    }

	private void newFile() {
		java.awt.Rectangle activeSelection = frame.getInkPanel().getSelectionRectangle();
		int suggestedWidth = (activeSelection != null && activeSelection.width > 0) ? activeSelection.width : newDimensions.width;
		int suggestedHeight = (activeSelection != null && activeSelection.height > 0) ? activeSelection.height : newDimensions.height;

		JFrame newFileFrame = new JFrame();
		newFileFrame.setTitle("New");
		newFileFrame.setBackground(Color.GRAY);
		newFileFrame.setSize(400, 200);
		newFileFrame.setPreferredSize(new Dimension(400, 200));
		newFileFrame.setLayout(null);
		newFileFrame.setResizable(false);
		newFileFrame.pack();

		// put the frame in the middle of the display
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		newFileFrame.setLocation(dim.width / 2 - newFileFrame.getSize().width / 2,
				dim.height / 2 - newFileFrame.getSize().height / 2);

		newFileFrame.setVisible(true);

		JTextField width = new JTextField(String.valueOf(suggestedWidth));
		width.setSize(100, 25);
		width.setLocation(100, 25);

		JLabel widthLabel = new JLabel("Width (px):");
		widthLabel.setSize(75, 25);
		widthLabel.setLocation(25, 25);

		JLabel heightLabel = new JLabel("Height (px):");
		heightLabel.setSize(75, 25);
		heightLabel.setLocation(25, 75);

		JTextField height = new JTextField(String.valueOf(suggestedHeight));
		height.setLocation(100, 75);
		height.setSize(100, 25);

		JButton okay = new JButton("OK");
		okay.setLocation(250, 25);
		okay.setSize(75, 25);
		okay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					newDimensions = new Dimension(Integer.parseInt(width.getText()),
							Integer.parseInt(height.getText()));
					System.out.println(newDimensions);
					frame.getDrawFrame().setDocumentVisible(true);
					frame.getInkPanel().setInkPanel(newDimensions.width, newDimensions.height);
					frame.getInkPanel().clearOpenedImageFlag();
					// Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					setDimensions(newDimensions.width, newDimensions.height);
					newFileFrame.dispose();
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null,
							"Invalid numeric entry. A proper integer is required.",
							"New",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.setSize(75, 25);
		cancel.setLocation(250, 75);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newFileFrame.dispose();
			}
		});

		newFileFrame.add(heightLabel);
		newFileFrame.add(widthLabel);
		newFileFrame.add(width);
		newFileFrame.add(height);
		newFileFrame.add(okay);
		newFileFrame.add(cancel);
	}

	private void openFile(File f) {

		// ----------------
		// update the contents of the jlabel to be the image from the selected file
		// ----------------

		// Image image = Toolkit.getDefaultToolkit().getImage(f.getPath());
		try {
			frame.getInkPanel().setImage(ImageIO.read(f));
			newDimensions = new Dimension(ImageIO.read(f).getWidth(), ImageIO.read(f).getHeight());
			setDimensions(newDimensions.width, newDimensions.height);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void saveFile(File f) throws IOException {

		// ----------------
		// Take all the contents of the jpanel and save them to a png
		// destination is the file they selected via the filechooser
		// ----------------
		BufferedImage im = makePanel(frame.getInkPanel());
		ImageIO.write(im, "png", f);
	}

	private BufferedImage makePanel(JPanel panel) {
		int w = panel.getWidth();
		int h = panel.getHeight();
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		panel.print(g);
		return bi;
	}
}