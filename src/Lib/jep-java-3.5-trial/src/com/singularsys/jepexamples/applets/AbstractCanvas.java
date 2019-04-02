/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 23 Jul 2008 - Richard Morris
 */
package com.singularsys.jepexamples.applets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * An abstract base class providing scaling and double buffering support for
 * Canvas components. Scaling is such that the position of the origin on the
 * screen does not change. Has methods for clearing background, painting axis,
 * grid, scales and elapse time.
 * 
 * @author Richard Morris
 *
 */
public abstract class AbstractCanvas extends JPanel
	implements MouseListener, ComponentListener, MouseWheelListener, MouseMotionListener, KeyListener {
    private static final long serialVersionUID = 330L;
    /** Color for background */
    protected Color bgColor = Color.white;
    /** Color for labels */
    protected Color labelColor = Color.gray;
    /** Color for axis */
    protected Color axisColor = Color.darkGray;
    /** Color for grid */
    protected Color gridColor = Color.lightGray;

    protected Font scaleFont = new Font("SansSerif", Font.PLAIN, 8);
    protected Font labelFont = new Font("SansSerif", Font.PLAIN, 10);

    protected double cenX = 0.0;
    protected double cenY = 0.0;
    /** Scaling of the graph in x and y directions */
    protected double scaleX;
    /** Scaling of the graph in x and y directions */
    protected double scaleY;
    /** Offset of the graph in absolute coords */
    protected int offX;
    /** Offset of the graph in absolute coords */
    protected int offY;

    /** Default scale values, set in constructor used in reset */
    protected double defaultScaleX, defaultScaleY;

    protected int defaultOffX, defaultOffY;
    /** Dimensions of the canvas */
    protected Dimension dimensions;

    /** BufferStratergy to use */
    protected BufferStrategy strategy = null;

    /** A popup menu */
    protected JPopupMenu popup = new JPopupMenu();
    /** position of popup */
    int popupX = 0, popupY = 0;

    /** Whether to show scales */
    protected boolean showScale = true;

    /** Whether to show calculation times */
    protected boolean showGrid = false;

    /** Whether to show calculation times */
    protected boolean showTime = false;

    /**
     * Constructor using 1 for scaling and 0 for offset.
     */
    public AbstractCanvas() {
	this(1, 1, 0, 0);
    }

    /**
     * Constructor specifying the default values for scaling and offset.
     * 
     * @param sx
     * @param sy
     * @param offx
     * @param offy
     */
    public AbstractCanvas(double sx, double sy, int offx, int offy) {
	super();
	scaleX = sx;
	scaleY = sy;
	offX = offx;
	offY = offy;
	defaultScaleX = sx;
	defaultScaleY = sy;
	defaultOffX = offx;
	defaultOffY = offy;
	dimensions = getSize();

	add(popup);

	JMenuItem center = new JMenuItem("Center");
	popup.add(center);
	center.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		center();
	    }
	});

	JMenuItem resetBut = new JMenuItem("Reset");
	popup.add(resetBut);
	resetBut.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		resetCanvas();
	    }
	});

	JCheckBoxMenuItem scales = new JCheckBoxMenuItem("Show scale", showScale);
	popup.add(scales);
	scales.addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		int state = e.getStateChange();
		if (state == ItemEvent.SELECTED)
		    showScale = true;
		if (state == ItemEvent.DESELECTED)
		    showScale = false;
	    }
	});

	JCheckBoxMenuItem grid = new JCheckBoxMenuItem("Show grid", showGrid);
	popup.add(grid);
	grid.addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		int state = e.getStateChange();
		if (state == ItemEvent.SELECTED)
		    showGrid = true;
		if (state == ItemEvent.DESELECTED)
		    showGrid = false;
	    }
	});

	JCheckBoxMenuItem times = new JCheckBoxMenuItem("Show times", showTime);
	popup.add(times);
	times.addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		int state = e.getStateChange();
		if (state == ItemEvent.SELECTED)
		    showTime = true;
		if (state == ItemEvent.DESELECTED)
		    showTime = false;
	    }
	});

	JMenuItem dump = new JMenuItem("Dump");
	popup.add(dump);
	dump.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		dump();
	    }
	});

	this.setFocusable(true);
	this.addMouseListener(this);
	this.addComponentListener(this);
	this.addMouseWheelListener(this);
	this.addMouseMotionListener(this);
	this.addKeyListener(this);
    }

    // ************************************** geometrical

    protected void center() {
	// System.out.println("Center: "+popupX+" "+popupY);
	int xi = popupX;
	int yi = popupY;
	double xd = this.xRelative(xi);
	double yd = this.yRelative(yi);
	this.offX += (xd - cenX) * scaleX;
	this.offY -= (yd - cenY) * scaleY;
	this.cenX = xd;
	this.cenY = yd;
    }

    /** Dump textual representation to System.out */
    public abstract void dump();

    /**
     * Called when scale, or offset changed Default action is to call repaint
     */
    protected void rescaled() {
	repaint();
    }

    /**
     * Called when canvas sized changes Default action is to call repaint
     */
    protected void resized() {
	repaint();
    }

    /**
     * Clip screen X values to lie within screen
     */
    protected int clipX(int xAbsolute) {
	int res = xAbsolute;
	if (res > dimensions.width)
	    res = dimensions.width;
	if (res < -1)
	    res = -1;
	return res;
    }

    /**
     * Clip screen Y values to lie within screen
     */
    protected int clipY(int yAbsolute) {
	int res = yAbsolute;
	if (res > dimensions.height)
	    res = dimensions.height;
	if (res < -1)
	    res = -1;
	return res;
    }

    /**
     * Translates from scaled coordinates to screen coordinates.
     * 
     */
    protected int xAbsolute(double xRelative) {
	int xAbsolute = (int) ((xRelative - cenX) * scaleX + dimensions.width / 2 + offX + 0.5);
	return xAbsolute;
    }

    /**
     * Translates from scaled coordinates to screen coordinates.
     */
    protected int yAbsolute(double yRelative) {
	int yAbsolute = (int) (-(yRelative - cenY) * scaleY + dimensions.height / 2 + offY + 0.5);
	return yAbsolute;
    }

    /**
     * Translates from screen coordinates to scaled coordinates.
     */
    protected double xRelative(int xAbsolute) {
	double xRelative = cenX + ((double) xAbsolute - dimensions.width / 2 - offX) / scaleX;
	return xRelative;
    }

    /**
     * Translates from screen coordinates to scaled coordinates.
     */
    protected double yRelative(int yAbsolute) {
	double yRelative = cenY + ((double) -yAbsolute + dimensions.height / 2 + offY) / scaleY;
	return yRelative;
    }

    public double getScaleX() {
	return scaleX;
    }

    public double getScaleY() {
	return scaleY;
    }

    public void setScale(double scX, double scY) {
	scaleX = scX;
	scaleY = scY;
	rescaled();
    }

    public int getOffX() {
	return offX;
    }

    public int getOffY() {
	return offY;
    }

    public void setOffset(int offX, int offY) {
	this.offX = offX;
	this.offY = offY;
	rescaled();
    }

    public void reset() {
	scaleX = defaultScaleX;
	scaleY = defaultScaleY;
	offX = defaultOffX;
	offY = defaultOffY;
	cenX = 0.0;
	cenY = 0.0;
	rescaled();
    }

    protected Point lastpos;

    public void resetCanvas() {
	this.reset();
	this.rescaled();
    }

    /**
     * Zoom the canvas. The x, y scales are multiplied by 1+amount/10.
     * 
     * @param amount
     */
    public void zoomCanvas(double amount) {
	double scx = this.getScaleX();
	double scy = this.getScaleY();
	double mul = 1.0 + amount / 50.0;
	if (mul < 0.1)
	    mul = 0.1;
	scx *= mul;
	scy *= mul;
	this.setScale(scx, scy);
	// System.out.println("mul "+mul+" scx "+scx+" scy "+scy);
	this.rescaled();
    }

    public void shiftCanvas(int xdiff, int ydiff) {
	int xoff = this.getOffX();
	int yoff = this.getOffY();
	xoff += xdiff;
	yoff += ydiff;
	this.setOffset(xoff, yoff);
	this.rescaled();
    }

    /**
     * A pop-up menu which subclasses can add items to.
     * 
     * @return the menu
     */
    public JPopupMenu getPopup() {
	return popup;
    }

    /****************** Painting methods ********************************/

    /**
     * Just calls paint. Eliminates background flicker problems with
     * Canvas.update.
     */
    @Override
    public void update(Graphics g) {
	// System.out.println("Update");
	// super.update(g);
	paint(g);
    }

    /**
     * Provides double buffering support and calls paintCanvas. Subclasses
     * should not override this method unless they want a different buffering
     * strategy. Instead they should implement paintCanvas.
     * 
     * @see #paintCanvas(Graphics)
     */
    @Override
    public void paint(Graphics g) {
	// System.out.println("Paint");
	// if(strategy==null) {
	// createBufferStrategy(2);
	// strategy = getBufferStrategy();
	// }
	// Graphics g2 = strategy.getDrawGraphics();
	// System.out.println(g.getClipBounds());

	paintCanvas(g);

	// strategy.show();
    }

    /**
     * Paint the canvas. Calls paintWhite(g); paintAxes(g); paintScale(g);
     * paintGrid(g); paintCurve(g); and paintTime(g,t1,t2);
     * 
     * @param g
     *            the graphics object
     */
    protected void paintCanvas(Graphics g) {
	if (g instanceof Graphics2D)
	    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	paintWhite(g);
	paintAxes(g);
	paintScale(g);
	paintGrid(g);
	long t1 = System.nanoTime();
	paintCurve(g);
	long t2 = System.nanoTime();
	paintTime(g, t1, t2);
    }

    /**
     * Paint the curve
     * 
     * @param g
     */
    protected abstract void paintCurve(Graphics g);

    /**
     * Paints the elapse time
     * 
     * @param g
     * @param t1
     *            start time in nano-secs.
     * @param t2
     *            end time in nano-secs.
     */
    protected void paintTime(Graphics g, long t1, long t2) {
	if (!showTime)
	    return;
	g.setFont(labelFont);
	g.setColor(labelColor);
	FontMetrics fm = g.getFontMetrics();

	long tdiff = t2 - t1;
	// long nPts = dimensions.width * dimensions.height;
	long nPts = getNumPts(); // dimensions.getWidth() *
				 // dimensions.getHeight();

	long frac = tdiff / nPts;
	double secs = tdiff / 1000000000.0;
	String msg = String.format("  %4.3fs %,dpts %4dns/pt  ", secs, nPts, frac);
	int wid = fm.stringWidth(msg);
	int high = fm.getHeight();
	g.setColor(bgColor);
	g.fillRect(dimensions.width - wid, 0, wid, high);
	g.setColor(labelColor);
	g.drawString(msg, dimensions.width - wid, fm.getAscent());

    }

    /**
     * The number of points which are calculated
     * 
     * @return the number of points
     */
    protected abstract long getNumPts();

    /**
     * Fills the background with white.
     */
    protected void paintWhite(Graphics g) {
	g.setColor(bgColor);
	g.fillRect(0, 0, dimensions.width, dimensions.height);
    }

    /**
     * Paints the axes for the graph.
     */
    protected void paintAxes(Graphics g) {
	g.setColor(axisColor);
	int yZero = this.yAbsolute(0.0);
	int xZero = this.xAbsolute(0.0);
	g.drawLine(0, yZero, dimensions.width - 1, yZero);
	g.drawLine(xZero, 0, xZero, dimensions.height - 1);
    }

    protected void paintScale(Graphics g) {
	if (!showScale)
	    return;
	g.setColor(labelColor);
	g.setFont(scaleFont);
	FontMetrics fm = g.getFontMetrics();
	int yZero = this.yAbsolute(0.0);
	int xZero = this.xAbsolute(0.0);
	if (yZero >= dimensions.height - 6)
	    yZero = dimensions.height - 6;
	if (yZero < 0)
	    yZero = 0;
	if (xZero >= dimensions.width)
	    xZero = dimensions.width - 1;
	if (xZero < 5)
	    xZero = 5;

	double xlow = xRelative(0);
	double xhigh = xRelative(dimensions.width - 1);
	double xrange = xhigh - xlow;
	double xlog = Math.floor(Math.log10(xrange) - 1);

	double yhigh = yRelative(0);
	double ylow = yRelative(dimensions.height - 1);
	double yrange = yhigh - ylow;
	double ylog = Math.floor(Math.log10(yrange) - 1);

	if (scaleX == scaleY) {
	    double min = xlog < ylog ? xlog : ylog;
	    xlog = min;
	    ylog = min;
	}
	double xinc = Math.pow(10, xlog);
	double yinc = Math.pow(10, ylog);

	String xformat = xlog > 0 ? "%-1.0f" : "%-" + (int) (2 - xlog) + "." + (int) (-xlog) + "f";
	String yformat = ylog > 0 ? "%-1.0f" : "%-" + (int) (2 - ylog) + "." + (int) (-ylog) + "f";

	double xleft = xlow - (xlow % xinc);
	double xright = xhigh - (xhigh % xinc);
	double xleft5 = xlow - (xlow % (xinc * 5));
	double xright5 = xhigh - (xhigh % (xinc * 5));

	double yleft = ylow - (ylow % yinc);
	double yright = yhigh - (yhigh % yinc);
	double yleft5 = ylow - (ylow % (yinc * 5));
	double yright5 = yhigh - (yhigh % (yinc * 5));

	for (double x = xleft; x <= xright; x += xinc) {
	    int xAbs = xAbsolute(x);
	    g.drawLine(xAbs, yZero, xAbs, yZero + 5);
	}

	int ypos = yZero + 5 + fm.getHeight();
	if (ypos < 5)
	    ypos = 5;
	if (ypos > dimensions.height - 10)
	    ypos = dimensions.height - 10;

	for (double x = xleft5; x <= xright5; x += xinc * 5) {
	    int xAbs = xAbsolute(x);
	    String out = String.format(xformat, x);
	    int xpos = xAbs - fm.stringWidth(out) / 2;
	    g.drawString(out, xpos, ypos);
	}
	for (double y = yleft; y <= yright; y += yinc) {
	    int yAbs = yAbsolute(y);
	    g.drawLine(xZero, yAbs, xZero - 5, yAbs);
	}
	for (double y = yleft5; y <= yright5; y += yinc * 5) {
	    int yAbs = yAbsolute(y);
	    String out = String.format(yformat, y);
	    int wid = fm.stringWidth(out);
	    int xpos = xZero - wid - 10;
	    if (xpos < 10)
		xpos = 10;
	    g.drawString(out, xpos, yAbs + fm.getAscent() / 2);
	}

    }

    protected void paintGrid(Graphics g) {
	if (!showGrid)
	    return;
	g.setFont(scaleFont);
	g.setColor(gridColor);

	int yZero = this.yAbsolute(0.0);
	int xZero = this.xAbsolute(0.0);
	if (yZero >= dimensions.height - 6)
	    yZero = dimensions.height - 6;
	if (yZero < 0)
	    yZero = 0;
	if (xZero >= dimensions.width)
	    xZero = dimensions.width - 1;
	if (xZero < 5)
	    xZero = 5;

	double xlow = xRelative(0);
	double xhigh = xRelative(dimensions.width - 1);
	double xrange = xhigh - xlow;
	double xlog = Math.floor(Math.log10(xrange));
	double xinc = Math.pow(10, xlog);

	double yhigh = yRelative(0);
	double ylow = yRelative(dimensions.height - 1);
	double yrange = yhigh - ylow;
	double ylog = Math.floor(Math.log10(yrange));
	double yinc = Math.pow(10, ylog);

	if (scaleX == scaleY) {
	    double min = xinc < yinc ? xinc : yinc;
	    xinc = min;
	    yinc = min;
	}

	double xleft = xlow - (xlow % xinc);
	double xright = xhigh - (xhigh % xinc);

	double yleft = ylow - (ylow % yinc);
	double yright = yhigh - (yhigh % yinc);

	for (double x = xleft; x <= xright; x += xinc) {
	    int xAbs = xAbsolute(x);
	    g.drawLine(xAbs, 0, xAbs, dimensions.height - 1);
	}
	for (double y = yleft; y <= yright; y += yinc) {
	    int yAbs = yAbsolute(y);
	    g.drawLine(0, yAbs, dimensions.width - 1, yAbs);
	}
    }

    // ***** events *****************/

    @Override
    public void componentHidden(ComponentEvent e) {
	/* ignored */ }

    @Override
    public void componentMoved(ComponentEvent e) {
	/* ignored */ }

    @Override
    public void componentShown(ComponentEvent e) {
	/* ignored */ }

    @Override
    public void componentResized(ComponentEvent e) {
	dimensions = getSize();
	resized();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
	double clicks = e.getWheelRotation();
	zoomCanvas(-clicks);
    }

    @Override
    public void mousePressed(MouseEvent e) {
	requestFocusInWindow();
	// System.out.println(e);
	if (e.isPopupTrigger()) {
	    // System.out.println("MousePressed "+e.getX()+" "+e.getY());
	    popupX = e.getX();
	    popupY = e.getY();
	    popup.show(e.getComponent(), e.getX(), e.getY());
	    return;
	}
	lastpos = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
	// System.out.println(e);
	if (e.isPopupTrigger()) {
	    // System.out.println("MouseReleased "+e.getX()+" "+e.getY());
	    popupX = e.getX();
	    popupY = e.getY();
	    popup.show(e.getComponent(), e.getX(), e.getY());
	    return;
	}
	Point relp = e.getPoint();
	if (lastpos == null)
	    return;
	int xdiff = relp.x - lastpos.x;
	int ydiff = relp.y - lastpos.y;
	lastpos = null;
	shiftCanvas(xdiff, ydiff);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
	requestFocusInWindow();
	// System.out.println(e);
	Point relp = e.getPoint();
	if (lastpos == null) {
	    lastpos = relp;
	    return;
	}
	int xdiff = relp.x - lastpos.x;
	int ydiff = relp.y - lastpos.y;
	lastpos = relp;
	shiftCanvas(xdiff, ydiff);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
	// System.out.println(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
	requestFocusInWindow();
	// System.out.println(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
	// System.out.println(e);
	lastpos = null;
    }

    @Override
    public void mouseExited(MouseEvent e) {
	lastpos = null;
    }

    @Override
    public void keyPressed(KeyEvent e) {
	int code = e.getKeyCode();
	boolean control = (e.getModifiers() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK;
	int val = control ? 10 : 1;
	if (code == KeyEvent.VK_LEFT)
	    shiftCanvas(-val, 0);
	if (code == KeyEvent.VK_RIGHT)
	    shiftCanvas(val, 0);
	if (code == KeyEvent.VK_UP)
	    shiftCanvas(0, -val);
	if (code == KeyEvent.VK_DOWN)
	    shiftCanvas(0, val);
	if (code == KeyEvent.VK_HOME)
	    resetCanvas();
	if (code == KeyEvent.VK_PAGE_UP)
	    zoomCanvas(5);
	if (code == KeyEvent.VK_PAGE_DOWN)
	    zoomCanvas(-5);
    }

    @Override
    public void keyReleased(KeyEvent e) {
	/* do nothing */ }

    @Override
    public void keyTyped(KeyEvent e) {
	char c = e.getKeyChar();
	if (c == '+')
	    zoomCanvas(1);
	else if (c == '-')
	    zoomCanvas(-1);
    }
}