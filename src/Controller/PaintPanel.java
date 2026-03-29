package Controller;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Stack;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Model.*;
import View.DrawFrame;

public class PaintPanel extends JPanel implements MouseListener, MouseMotionListener {
	private final int PENCIL_TOOL = 0;
	private final int LINE_TOOL = 1;
	private final int RECTANGLE_TOOL = 2;
	private final int SQUARE_TOOL = 11;
	private final int CIRCLE_TOOL = 3;
	private final int TEXT_TOOL = 5;
	private final int IMAGE_TOOL = 12;
	private final int MOVE_TOOL = 10;
	private final int ERASER_TOOL = 6;
	private final int FILL_TOOL = 7;
	private final int ELLIPSE_TOOL = 8;
	private final int TRIANGLE_TOOL = 9;
	private final int DELETE_TOOL = 13;
	private final int ROTATE_TOOL = 14;
	private final int SELECT_TOOL = 15;
	private final int PASTE_TOOL = 16;
	private final int IMAGE_COLOR_PICKER_TOOL = 17;

	private TextDialog td;
	private ImageDialog imgd;
	private BasicStroke stroke = new BasicStroke((float) 2);
	BufferedImage canvas;
	Graphics2D graphics2D;
	private int activeTool = 0;
	private DrawFrame frame;

	private Stack<MyElement> shapes;
	private Stack<MyElement> removed;
	private Stack<MyElement> preview;

	private Stack<OperationWrapper> operations;
	private Stack<OperationWrapper> undoneOperations;

	private MyElement selectedShape;
	private int selectedShapeGroup;
	private Point initialMousePosition;
	private Point beforeMovePosition;
	private java.awt.Rectangle selectionRectangle;
	private ImageShape selectionSourceImage;
	private BufferedImage clipboardImage;
	private boolean selectionInProgress;

	private int grouped;

	int x1, y1, x2, y2;

	private boolean dragged = false;
	private Color currentColor;
	private Color fillColor;
	private boolean transparent;

	private int inkPanelWidth;
	private int inkPanelHeight;
	private double viewScale = 1.0;
	private boolean openedImageFromFile = false;

	// Now for the constructors
	public PaintPanel(int f, DrawFrame frame, int width, int height) {
		// this.setPreferredSize(new Dimension(300,300));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		inkPanelWidth = dim.width - 150;
		inkPanelHeight = dim.height - 160;
		this.setSize(inkPanelWidth, inkPanelHeight);
		this.setPreferredSize(new Dimension(inkPanelWidth, inkPanelHeight));
		this.setLayout(null);
		setDoubleBuffered(true);
		setLocation(10, 10);
		setBackground(Color.WHITE);
		currentColor = Color.BLACK;
		this.fillColor = Color.white;
		setFocusable(true);
		requestFocus();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.frame = frame;
		this.printPaintPanelSize(inkPanelWidth, inkPanelHeight);
		this.shapes = new Stack<MyElement>();
		this.removed = new Stack<MyElement>();
		this.operations = new Stack<OperationWrapper>();
		this.undoneOperations = new Stack<OperationWrapper>();
		this.grouped = 1;
		this.preview = new Stack<MyElement>();
		this.transparent = true;
		td = new TextDialog(frame);
		imgd = new ImageDialog(frame);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (canvas == null) {
			canvas = new BufferedImage(inkPanelWidth, inkPanelHeight, BufferedImage.TYPE_INT_ARGB);
			graphics2D = canvas.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			clear();
		}
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.scale(viewScale, viewScale);
		g2.drawImage(canvas, 0, 0, null);

		for (MyElement s : shapes) {
			s.draw(g2);
		}
		if (selectionRectangle != null) {
			Color previousColor = g2.getColor();
			java.awt.Stroke previousStroke = g2.getStroke();
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 5f, 5f }, 0f));
			g2.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
			g2.setStroke(previousStroke);
			g2.setColor(previousColor);
		}
		if (!preview.isEmpty()) {
			MyElement s = preview.peek();
			s.draw(g2);
		}
		g2.dispose();

	}

	private int toModelX(int viewX) {
		return Math.max(0, (int) Math.round(viewX / viewScale));
	}

	private int toModelY(int viewY) {
		return Math.max(0, (int) Math.round(viewY / viewScale));
	}

	public void setViewScale(double viewScale) {
		if (viewScale < 0.1) {
			viewScale = 0.1;
		} else if (viewScale > 4.0) {
			viewScale = 4.0;
		}
		this.viewScale = viewScale;
		updateScaledSize();
		revalidate();
		repaint();
	}

	public double getViewScale() {
		return viewScale;
	}

	private void updateScaledSize() {
		int scaledWidth = Math.max(1, (int) Math.round(inkPanelWidth * viewScale));
		int scaledHeight = Math.max(1, (int) Math.round(inkPanelHeight * viewScale));
		setPreferredSize(new Dimension(scaledWidth, scaledHeight));
		setSize(scaledWidth, scaledHeight);
		adjustScrollPaneViewport(scaledWidth, scaledHeight);
	}

	public void setTool(int tool) {
		this.activeTool = tool;
	}

	public void setImage(BufferedImage image) {
		if (image == null) {
			return;
		}

		frame.setDocumentVisible(true);
		this.setInkPanel(image.getWidth(), image.getHeight());
		shapes.push(new ImageShape(0, 0, image, image.getWidth(), image.getHeight()));
		operations.push(new OperationWrapper(OperationType.DRAW));
		openedImageFromFile = true;
		clearSelectionState();
		repaint();
	}

	public void clear() {
		graphics2D.setPaint(Color.white);
		graphics2D.fillRect(0, 0, getSize().width, getSize().height);
		shapes.removeAllElements();
		removed.removeAllElements();
		repaint();
		graphics2D.setColor(currentColor);
	}

	public void clearRedoStack() {
		removed.removeAllElements();
		undoneOperations.removeAllElements();
	}

	public void undo() {
		if (operations.isEmpty()) {
			return;
		}

		OperationWrapper lastOperation = operations.pop();
		if (lastOperation.getType() == OperationType.DRAW) {
			if (shapes.size() > 0 && shapes.peek().getGroup() == 0) {
				removed.push(shapes.pop());
				repaint();
			} else if (shapes.size() > 0 && shapes.peek().getGroup() != 0) {
				MyElement lastRemoved = shapes.pop();
				removed.push(lastRemoved);

				while (!shapes.isEmpty() && shapes.peek().getGroup() == lastRemoved.getGroup()) {
					removed.push(shapes.pop());
					repaint();
				}
			}
		} else if (lastOperation.getType() == OperationType.FILL) {
			ClosedShape temp = (ClosedShape) lastOperation.getShape();
			temp.fill(lastOperation.getFromColor());
		} else if (lastOperation.getType() == OperationType.MOVE) {
			MoveableElement toMove = (MoveableElement) lastOperation.getShape();

			if (toMove.getGroup() == 0) {
				MoveableElement temp = (MoveableElement) lastOperation.getShape();
				temp.displace(-lastOperation.getDeltaX(), -lastOperation.getDeltaY());
			} else {
				Iterator<MyElement> itr = shapes.iterator();

				while (itr.hasNext()) {
					MoveableElement nextShape = (MoveableElement) itr.next();
					if (nextShape.getGroup() == toMove.getGroup()) {
						nextShape.displace(-lastOperation.getDeltaX(), -lastOperation.getDeltaY());
					}

				}
			}
		} else if (lastOperation.getType() == OperationType.ROTATE) {
			ImageShape image = (ImageShape) lastOperation.getShape();
			image.rotateCounterClockwise90();
			syncCanvasToImageFrame(image);
		} else if (lastOperation.getType() == OperationType.IMAGE_EDIT) {
			ImageShape image = (ImageShape) lastOperation.getShape();
			image.replaceImage(lastOperation.getBeforeImage());
			syncCanvasToImageFrame(image);
		}

		undoneOperations.add(lastOperation);
		repaint();
	}

	public void redo() {
		if (undoneOperations.isEmpty()) {
			return;
		}

		OperationWrapper lastOperation = undoneOperations.pop();
		if (lastOperation.getType() == OperationType.DRAW) {
			if (removed.size() > 0 && removed.peek().getGroup() == 0) {
				shapes.push(removed.pop());
				repaint();
			} else if (removed.size() > 0 && removed.peek().getGroup() != 0) {
				MyElement lastRemoved = removed.pop();
				shapes.push(lastRemoved);

				while (removed.isEmpty() == false && removed.peek().getGroup() == lastRemoved.getGroup()) {
					shapes.push(removed.pop());
					repaint();
				}
			}
		} else if (lastOperation.getType() == OperationType.FILL) {
			ClosedShape temp = (ClosedShape) lastOperation.getShape();
			temp.fill(lastOperation.getToColor());
		} else if (lastOperation.getType() == OperationType.MOVE) {
			MoveableElement toMove = (MoveableElement) lastOperation.getShape();

			if (toMove.getGroup() == 0) {
				MoveableElement temp = (MoveableElement) lastOperation.getShape();
				temp.displace(lastOperation.getDeltaX(), lastOperation.getDeltaY());
			} else {
				Iterator<MyElement> itr = shapes.iterator();

				while (itr.hasNext()) {
					MoveableElement nextShape = (MoveableElement) itr.next();
					if (nextShape.getGroup() == toMove.getGroup()) {
						nextShape.displace(lastOperation.getDeltaX(), lastOperation.getDeltaY());
					}
				}
			}
		} else if (lastOperation.getType() == OperationType.ROTATE) {
			ImageShape image = (ImageShape) lastOperation.getShape();
			image.rotateClockwise90();
			syncCanvasToImageFrame(image);
		} else if (lastOperation.getType() == OperationType.IMAGE_EDIT) {
			ImageShape image = (ImageShape) lastOperation.getShape();
			image.replaceImage(lastOperation.getAfterImage());
			syncCanvasToImageFrame(image);
		}
		operations.add(lastOperation);
		repaint();
	}

	public void setColor(Color c) {
		currentColor = c;
		graphics2D.setColor(c);

	}

	public void setFillColor(Color c) {
		this.fillColor = c;
	}

	public void setThickness(float f) {
		stroke = new BasicStroke(f);
		graphics2D.setStroke(stroke);
	}

	public void setTransparency(Boolean b) {
		this.transparent = b;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Color primary = currentColor;
		Color secondary = fillColor;

		if (SwingUtilities.isRightMouseButton(e)) {
			primary = secondary;
			secondary = currentColor;
		}

		printCoords(e);
		x2 = toModelX(e.getX());
		y2 = toModelY(e.getY());

		dragged = true;

		if (activeTool == LINE_TOOL || activeTool == RECTANGLE_TOOL || activeTool == SQUARE_TOOL
				|| activeTool == TRIANGLE_TOOL || activeTool == CIRCLE_TOOL || activeTool == ELLIPSE_TOOL) {
			preview.clear();
		}

		if (activeTool == ERASER_TOOL) {
			shapes.push(new EraserTool(x1, y1, x2, y2, Color.white, stroke, grouped));
			operations.push(new OperationWrapper(OperationType.DRAW));
			repaint();
			x1 = x2;
			y1 = y2;
		} else if (activeTool == PENCIL_TOOL) {
			shapes.push(new PencilTool(x1, y1, x2, y2, primary, stroke, grouped));
			operations.push(new OperationWrapper(OperationType.DRAW));
			repaint();
			x1 = x2;
			y1 = y2;
		} else if (activeTool == LINE_TOOL) {
			preview.push(new Line(x1, y1, x2, y2, primary, stroke));
			repaint();
		} else if (activeTool == RECTANGLE_TOOL) {
			if (x1 < x2 && y1 < y2) {
				preview.push(new Model.Rectangle(x1, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				preview.push(new Model.Rectangle(x2, y1, x1, y2, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				preview.push(new Model.Rectangle(x1, y2, x2, y1, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				preview.push(new Model.Rectangle(x2, y2, x1, y1, primary, stroke, secondary, transparent));
			}
			repaint();
		} else if (activeTool == SQUARE_TOOL) {
			int side = Math.min(
					Math.abs(x2 - x1),
					Math.abs(y2 - y1));
			if (x1 < x2 && y1 < y2) {
				preview.push(new Square(x1, y1, side, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				preview.push(new Square(x2, y1, side, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				preview.push(new Square(x1, y2, side, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				preview.push(new Square(x2, y2, side, primary, stroke, secondary, transparent));
			}
			repaint();
		} else if (activeTool == TRIANGLE_TOOL) {
			if (x1 < x2 && y1 < y2) {
				preview.push(
						new Triangle(x1, y2, (int) (x1 + x2) / 2, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				preview.push(
						new Triangle(x1, y2, (int) (x1 + x2) / 2, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				preview.push(
						new Triangle(x1, y2, (int) (x1 + x2) / 2, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				preview.push(
						new Triangle(x1, y2, (int) (x1 + x2) / 2, y1, x2, y2, primary, stroke, secondary, transparent));
			}
			repaint();
		} else if (activeTool == CIRCLE_TOOL) {
			int radius;
			if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
				radius = (int) (y2 - y1) / 2;
				radius = Math.abs(radius);
			} else {
				radius = (int) (x2 - x1) / 2;
				radius = Math.abs(radius);
			}

			if (x1 < x2 && y1 < y2) {
				preview.push(new Circle(x1, y1, radius, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				preview.push(new Circle(x2, y1, radius, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				preview.push(new Circle(x1, y2, radius, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				preview.push(new Circle(x2, y2, radius, primary, stroke, secondary, transparent));
			}
			repaint();
		} else if (activeTool == ELLIPSE_TOOL) {
			if (x1 < x2 && y1 < y2) {
				preview.push(new Ellipse(x1, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				preview.push(new Ellipse(x2, y1, x1, y2, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				preview.push(new Ellipse(x1, y2, x2, y1, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				preview.push(new Ellipse(x2, y2, x1, y1, primary, stroke, secondary, transparent));
			}
			repaint();
		} else if (activeTool == SELECT_TOOL && selectionInProgress && selectionSourceImage != null) {
			selectionRectangle = buildSelectionRectangle(x1, y1, x2, y2, selectionSourceImage);
			repaint();
		} else if (activeTool == MOVE_TOOL && selectedShape != null) {
			int deltaX = x2 - initialMousePosition.x;
			int deltaY = y2 - initialMousePosition.y;

			initialMousePosition.x = x2;
			initialMousePosition.y = y2;

			// Update the shape's position based on the mouse movement
			if (selectedShapeGroup != 0) {
				Iterator<MyElement> iterator = shapes.iterator();
				while (iterator.hasNext()) {
					MyElement shape = iterator.next();
					if (shape.getGroup() == selectedShapeGroup) {
						MoveableElement temp = (MoveableElement) shape;
						temp.displace(deltaX, deltaY);
					}
				}
			} else {
				MoveableElement temp = (MoveableElement) selectedShape;
				temp.displace(deltaX, deltaY);
			}

			repaint();
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		printCoords(e);
		// not using
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// not using

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// not using
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// not using
	}

	@Override
	public void mousePressed(MouseEvent e) {
		x1 = toModelX(e.getX());
		y1 = toModelY(e.getY());

		if (activeTool == SELECT_TOOL) {
			selectionInProgress = true;
			selectionSourceImage = findTopmostImageAt(x1, y1);
			selectionRectangle = null;
			repaint();
			return;
		}

		if (activeTool == MOVE_TOOL) {
			Stack<MyElement> temp = new Stack<MyElement>();

			while (!shapes.isEmpty()) {
				MyElement shape = shapes.pop();

				if (shape instanceof EraserTool) {
					temp.push(shape);
					continue;
				}

				MoveableElement shapeMv = (MoveableElement) shape;
				if (shapeMv.isPointInside(x1, y1)) {
					selectedShape = shapeMv;
					selectedShapeGroup = shapeMv.getGroup();
					initialMousePosition = new Point(x1, y1);
					beforeMovePosition = new Point(x1, y1);
					break;
				} else {
					temp.push(shapeMv);
				}
			}

			while (!temp.isEmpty()) {
				shapes.push(temp.pop());
			}
			if (selectedShape != null) {
				shapes.push(selectedShape);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		x2 = toModelX(e.getX());
		y2 = toModelY(e.getY());
		grouped++;

		Color primary = currentColor;
		Color secondary = fillColor;
		if (SwingUtilities.isRightMouseButton(e)) {
			primary = secondary;
			secondary = currentColor;
		}

		if (activeTool == LINE_TOOL && dragged) {
			shapes.push(new Line(x1, y1, x2, y2, primary, stroke));
			operations.push(new OperationWrapper(OperationType.DRAW));
			repaint();
		} else if (activeTool == RECTANGLE_TOOL && dragged) {
			if (x1 < x2 && y1 < y2) {
				shapes.push(new Model.Rectangle(x1, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				shapes.push(new Model.Rectangle(x2, y1, x1, y2, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				shapes.push(new Model.Rectangle(x1, y2, x2, y1, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				shapes.push(new Model.Rectangle(x2, y2, x1, y1, primary, stroke, secondary, transparent));
			}
			operations.push(new OperationWrapper(OperationType.DRAW));
			repaint();
		} else if (activeTool == SQUARE_TOOL && dragged) {
			int side = Math.min(
					Math.abs(x2 - x1),
					Math.abs(y2 - y1));

			if (x1 < x2 && y1 < y2) {
				shapes.push(new Square(x1, y1, side, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				shapes.push(new Square(x2, y1, side, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				shapes.push(new Square(x1, y2, side, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				shapes.push(new Square(x2, y2, side, primary, stroke, secondary, transparent));
			}
			operations.push(new OperationWrapper(OperationType.DRAW));
			repaint();
		} else if (activeTool == TRIANGLE_TOOL) {
			if (x1 < x2 && y1 < y2) {
				shapes.push(
						new Triangle(x1, y2, (int) (x1 + x2) / 2, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				shapes.push(
						new Triangle(x1, y2, (int) (x1 + x2) / 2, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				shapes.push(
						new Triangle(x1, y2, (int) (x1 + x2) / 2, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				shapes.push(
						new Triangle(x1, y2, (int) (x1 + x2) / 2, y1, x2, y2, primary, stroke, secondary, transparent));
			}
			operations.push(new OperationWrapper(OperationType.DRAW));
			repaint();
		} else if (activeTool == CIRCLE_TOOL && dragged) {
			int radius;
			if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
				radius = (int) (y2 - y1) / 2;
				radius = Math.abs(radius);
			} else {
				radius = (int) (x2 - x1) / 2;
				radius = Math.abs(radius);
			}

			if (x1 < x2 && y1 < y2) {
				shapes.push(new Circle(x1, y1, radius, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				shapes.push(new Circle(x2, y1, radius, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				shapes.push(new Circle(x1, y2, radius, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				shapes.push(new Circle(x2, y2, radius, primary, stroke, secondary, transparent));
			}
			operations.push(new OperationWrapper(OperationType.DRAW));
			repaint();
		} else if (activeTool == ELLIPSE_TOOL && dragged) {
			if (x1 < x2 && y1 < y2) {
				shapes.push(new Ellipse(x1, y1, x2, y2, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y1 < y2) {
				shapes.push(new Ellipse(x2, y1, x1, y2, primary, stroke, secondary, transparent));
			} else if (x1 < x2 && y2 < y1) {
				shapes.push(new Ellipse(x1, y2, x2, y1, primary, stroke, secondary, transparent));
			} else if (x2 < x1 && y2 < y1) {
				shapes.push(new Ellipse(x2, y2, x1, y1, primary, stroke, secondary, transparent));
			}
			operations.push(new OperationWrapper(OperationType.DRAW));
			repaint();
		} else if (activeTool == DELETE_TOOL) {
			Stack<MyElement> temp = new Stack<MyElement>();

			while (!shapes.isEmpty()) {
				MyElement shape = shapes.pop();

				if (shape instanceof EraserTool) {
					temp.push(shape);
					continue;
				}

				MoveableElement shapeMv = (MoveableElement) shape;
				if (shapeMv.isPointInside(x1, y1)) {
					if (shapeMv.getGroup() != 0) {
						/////////////////////////////////////////////
						while (!temp.isEmpty()) {
							shapes.push(temp.pop());
						}

						while (!shapes.isEmpty()) {
							MyElement shape2 = shapes.pop();
							if (shape2.getGroup() != shape.getGroup()) {
								temp.push(shape2);
							}
						}
						/////////////////////////////////////////////
					}
					break;
				} else {
					temp.push(shape);
				}
			}

			while (!temp.isEmpty()) {
				shapes.push(temp.pop());
			}
		} else if (activeTool == TEXT_TOOL) {
			int i = td.showCustomDialog(frame);
			if (i == TextDialog.APPLY_OPTION) {
				shapes.push(new Text(x1, y1, td.getInputSize(), td.getFont(), primary, stroke, td.getText()));
				operations.push(new OperationWrapper(OperationType.DRAW));
			}

		} else if (activeTool == IMAGE_TOOL) {
			int i = imgd.showCustomDialog(frame);
			if (i == ImageDialog.APPLY_OPTION) {
				int width = imgd.getWidthValue();
				int height = imgd.getHeightValue();
				File selectedFile = imgd.getSelectedFile();
				if (selectedFile != null) {
					shapes.push(new ImageShape(x1, y1, selectedFile, width, height));
					operations.push(new OperationWrapper(OperationType.DRAW));
				}
			}
		} else if (activeTool == SELECT_TOOL && selectionInProgress && selectionSourceImage != null) {
			selectionRectangle = buildSelectionRectangle(x1, y1, x2, y2, selectionSourceImage);
			repaint();
		} else if (activeTool == MOVE_TOOL && selectedShape != null) {
			int deltaX = x2 - beforeMovePosition.x;
			int deltaY = y2 - beforeMovePosition.y;

			operations.push(new OperationWrapper(OperationType.MOVE, selectedShape, deltaX, deltaY));

			selectedShape = null;
		} else if (activeTool == SELECT_TOOL) {
			selectionInProgress = false;
			selectionRectangle = buildSelectionRectangle(x1, y1, x2, y2, selectionSourceImage);
			repaint();
		} else if (activeTool == ROTATE_TOOL) {
			ImageShape image = findTopmostImageAt(x2, y2);
			if (image != null) {
				flattenOverlayElementsIntoImage(image);
				image.rotateClockwise90();
				syncCanvasToImageFrame(image);
				operations.push(new OperationWrapper(OperationType.ROTATE, image));
				repaint();
			}
		} else if (activeTool == PASTE_TOOL) {
			ImageShape targetImage = findOrCreatePasteTargetImage(x2, y2);
			if (clipboardImage != null && targetImage != null) {
				BufferedImage beforePaste = targetImage.copyCurrentImage();
				if (targetImage.pasteImage(clipboardImage, x2, y2)) {
					BufferedImage afterPaste = targetImage.copyCurrentImage();
					operations.push(new OperationWrapper(OperationType.IMAGE_EDIT, targetImage, beforePaste, afterPaste));
					syncCanvasToImageFrame(targetImage);
					repaint();
				}
			}
		} else if (activeTool == IMAGE_COLOR_PICKER_TOOL) {
			pickPrimaryColorFromImageAt(x2, y2);
		} else if (activeTool == FILL_TOOL) {
			Stack<MyElement> temp = new Stack<MyElement>();

			while (!shapes.isEmpty()) {
				MyElement shape = shapes.pop();

				if (!(shape instanceof ClosedShape)) {
					temp.push(shape);
					continue;
				}

				ClosedShape shapeCl = (ClosedShape) shape;
				if (shapeCl.isPointInside(x2, y2) && shapeCl.getGroup() == 0) {
					Color fromColor = shapeCl.getFillColor();
					Color toColor = fillColor;

					operations.push(new OperationWrapper(OperationType.FILL, shapeCl, fromColor, toColor));

					shapeCl.fill(fillColor);
					shapes.push(shapeCl);
					break;
				} else {
					temp.push(shapeCl);
				}
			}

			while (!temp.isEmpty()) {
				shapes.push(temp.pop());
			}
		}

		preview.clear();
		dragged = false;
		removed.removeAllElements();
		repaint();
	}

	public void printCoords(MouseEvent e) {
		String posX = String.valueOf(toModelX((int) e.getPoint().getX()));
		String posY = String.valueOf(toModelY((int) e.getPoint().getY()));
		frame.getCoordinateBar().getCoordinates().setText(posX + ",  " + posY + " px");
	}

	public void printPaintPanelSize(int width, int height) {
		frame.getCoordinateBar().getFrameSize().setText(width + ",  " + height + " px");
	}

	public void setInkPanelWidth(int width) {
		this.inkPanelWidth = width;
	}

	public void setInkPanelHeight(int height) {
		this.inkPanelHeight = height;
	}

	public void setInkPanel(int width, int height) {
		inkPanelWidth = width;
		inkPanelHeight = height;
		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		graphics2D = canvas.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.printPaintPanelSize(width, height);
		updateScaledSize();
		clear();
		revalidate();

	}

	private void adjustScrollPaneViewport(int width, int height) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int maxWidth = dim.width - 150;
		int maxHeight = dim.height - 160;
		int viewportWidth = Math.min(width, maxWidth);
		int viewportHeight = Math.min(height, maxHeight);
		frame.getScrollPane().setSize(viewportWidth, viewportHeight);
		frame.getScrollPane().revalidate();
	}

	private void syncCanvasToImageFrame(ImageShape image) {
		if (image == null) {
			return;
		}

		boolean anchoredAtOrigin = image.getX1() == 0 && image.getY1() == 0;
		if (!anchoredAtOrigin) {
			return;
		}

		int imageWidth = image.getX2() - image.getX1();
		int imageHeight = image.getY2() - image.getY1();
		boolean canvasMatchesImage = canvas != null
				&& canvas.getWidth() == imageWidth
				&& canvas.getHeight() == imageHeight;

		if (shapes.size() == 1 || canvasMatchesImage) {
			setInkPanel(imageWidth, imageHeight);
			shapes.clear();
			shapes.push(image);
		}
	}

	private void flattenOverlayElementsIntoImage(ImageShape image) {
		if (image == null) {
			return;
		}

		int imageIndex = shapes.indexOf(image);
		if (imageIndex < 0 || imageIndex >= shapes.size() - 1) {
			return;
		}

		BufferedImage flattened = image.copyCurrentImage();
		if (flattened == null) {
			return;
		}

		Graphics2D g2 = flattened.createGraphics();
		g2.setComposite(AlphaComposite.SrcOver);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.translate(-image.getX1(), -image.getY1());
		g2.setClip(image.getX1(), image.getY1(), image.getX2() - image.getX1(), image.getY2() - image.getY1());

		boolean flattenedAny = false;
		for (int i = imageIndex + 1; i < shapes.size(); i++) {
			MyElement element = shapes.get(i);
			if (element == null || element instanceof ImageShape) {
				continue;
			}
			element.draw(g2);
			flattenedAny = true;
		}
		g2.dispose();

		if (!flattenedAny) {
			return;
		}

		image.replaceImage(flattened);
		for (int i = shapes.size() - 1; i > imageIndex; i--) {
			MyElement element = shapes.get(i);
			if (!(element instanceof ImageShape)) {
				shapes.remove(i);
			}
		}
	}


	private void pickPrimaryColorFromImageAt(int x, int y) {
		ImageShape image = findTopmostImageAt(x, y);
		if (image == null) {
			return;
		}

		Color pickedColor = image.getPixelColorAt(x, y);
		if (pickedColor == null) {
			return;
		}

		frame.getColorChooserController().setPrimaryColor(pickedColor);
		setColor(pickedColor);
		repaint();
	}

	private ImageShape findTopmostImageAt(int x, int y) {
		for (int i = shapes.size() - 1; i >= 0; i--) {
			MyElement shape = shapes.get(i);
			if (shape instanceof ImageShape) {
				ImageShape image = (ImageShape) shape;
				if (image.isPointInside(x, y)) {
					return image;
				}
			}
		}
		return null;
	}

	// Enum to represent types of operations
	private enum OperationType {
		DRAW,
		MOVE,
		FILL,
		ROTATE,
		IMAGE_EDIT
	}

	// Inner class to wrap both draw and move operations
	private class OperationWrapper {
		private OperationType type;
		private MyElement shape;
		private int deltaX;
		private int deltaY;
		private Color fromColor;
		private Color toColor;
		private BufferedImage beforeImage;
		private BufferedImage afterImage;

		public OperationWrapper(OperationType type) {
			this.type = type;
			clearRedoStack();
		}

		public OperationWrapper(OperationType type, MyElement shape, int deltaX, int deltaY) {
			this.type = type;
			this.shape = shape;
			this.deltaX = deltaX;
			this.deltaY = deltaY;
			clearRedoStack();
		}

		public OperationWrapper(OperationType type, MyElement shape) {
			this.type = type;
			this.shape = shape;
			clearRedoStack();
		}

		public OperationWrapper(OperationType type, Shape shape, Color fromColor, Color toColor) {
			this.type = type;
			this.shape = shape;
			this.fromColor = fromColor;
			this.toColor = toColor;
			clearRedoStack();
		}

		public OperationWrapper(OperationType type, MyElement shape, BufferedImage beforeImage, BufferedImage afterImage) {
			this.type = type;
			this.shape = shape;
			this.beforeImage = beforeImage;
			this.afterImage = afterImage;
			clearRedoStack();
		}

		public OperationType getType() {
			return type;
		}

		public MyElement getShape() {
			return shape;
		}

		public int getDeltaX() {
			return deltaX;
		}

		public int getDeltaY() {
			return deltaY;
		}

		public Color getFromColor() {
			return fromColor;
		}

		public Color getToColor() {
			return toColor;
		}

		public BufferedImage getBeforeImage() {
			return beforeImage;
		}

		public BufferedImage getAfterImage() {
			return afterImage;
		}
	}


	public boolean hasOpenedImageFromFile() {
		return openedImageFromFile;
	}

	public boolean hasOpenDocument() {
		return frame != null && frame.getScrollPane() != null && frame.getScrollPane().isVisible();
	}

	public void clearOpenedImageFlag() {
		openedImageFromFile = false;
	}

	public void closeOpenedImage() {
		closeCurrentDocument();
	}

	public void closeCurrentDocument() {
		if (!hasOpenDocument()) {
			return;
		}
		clear();
		clearSelectionState();
		operations.clear();
		undoneOperations.clear();
		preview.clear();
		selectedShape = null;
		selectedShapeGroup = 0;
		initialMousePosition = null;
		beforeMovePosition = null;
		openedImageFromFile = false;
		frame.setDocumentVisible(false);
		revalidate();
		repaint();
	}

	private void clearSelectionState() {
		selectionRectangle = null;
		selectionSourceImage = null;
		selectionInProgress = false;
		clipboardImage = null;
	}


	public java.awt.Rectangle getSelectionRectangle() {
		if (selectionRectangle == null) {
			return null;
		}
		return new java.awt.Rectangle(selectionRectangle);
	}

	public boolean hasActiveSelection() {
		return selectionRectangle != null && selectionRectangle.width > 0 && selectionRectangle.height > 0;
	}

	public void copySelection() {
		if (selectionRectangle == null || selectionSourceImage == null) {
			return;
		}
		clipboardImage = renderSelectionComposite(selectionRectangle, selectionSourceImage);
	}

	private BufferedImage renderSelectionComposite(java.awt.Rectangle selection, ImageShape sourceImage) {
		if (selection == null || sourceImage == null || selection.width <= 0 || selection.height <= 0) {
			return null;
		}

		BufferedImage composite = sourceImage.copyRegion(selection.x, selection.y, selection.width, selection.height);
		if (composite == null) {
			return null;
		}

		Graphics2D g2 = composite.createGraphics();
		g2.setComposite(AlphaComposite.SrcOver);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.translate(-selection.x, -selection.y);
		g2.setClip(selection.x, selection.y, selection.width, selection.height);

		int sourceIndex = shapes.indexOf(sourceImage);
		if (sourceIndex < 0) {
			sourceIndex = -1;
		}

		for (int i = sourceIndex + 1; i < shapes.size(); i++) {
			MyElement element = shapes.get(i);
			if (element == null || element == sourceImage) {
				continue;
			}
			element.draw(g2);
		}

		g2.dispose();
		return composite;
	}


	private ImageShape findOrCreatePasteTargetImage(int x, int y) {
		ImageShape targetImage = findTopmostImageAt(x, y);
		if (targetImage != null) {
			return targetImage;
		}

		if (inkPanelWidth <= 0 || inkPanelHeight <= 0) {
			return null;
		}

		BufferedImage blankImage = new BufferedImage(inkPanelWidth, inkPanelHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = blankImage.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, inkPanelWidth, inkPanelHeight);
		g2.dispose();

		ImageShape baseImage = new ImageShape(0, 0, blankImage, inkPanelWidth, inkPanelHeight);
		shapes.push(baseImage);
		operations.push(new OperationWrapper(OperationType.DRAW));
		return baseImage;
	}

	private java.awt.Rectangle buildSelectionRectangle(int startX, int startY, int endX, int endY, ImageShape sourceImage) {
		if (sourceImage == null) {
			return null;
		}

		int left = Math.min(startX, endX);
		int top = Math.min(startY, endY);
		int right = Math.max(startX, endX);
		int bottom = Math.max(startY, endY);

		left = Math.max(left, sourceImage.getX1());
		top = Math.max(top, sourceImage.getY1());
		right = Math.min(right, sourceImage.getX2());
		bottom = Math.min(bottom, sourceImage.getY2());

		int width = right - left;
		int height = bottom - top;
		if (width <= 0 || height <= 0) {
			return null;
		}

		return new java.awt.Rectangle(left, top, width, height);
	}
}