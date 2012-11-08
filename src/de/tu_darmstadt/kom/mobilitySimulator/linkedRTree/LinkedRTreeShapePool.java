package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

import java.util.Stack;

import de.tu_darmstadt.kom.linkedRTree.Circle;
import de.tu_darmstadt.kom.linkedRTree.Point;
import de.tu_darmstadt.kom.linkedRTree.Rectangle;
import de.tu_darmstadt.kom.mobilitySimulator.core.ShapePoolInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.Watchdog;
import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.WatchdogStateListener;

public class LinkedRTreeShapePool implements WatchdogStateListener,
		ShapePoolInterface {

	private Stack<Circle> circles;
	private int circlesCount;

	private Stack<Rectangle> rectangles;
	private int rectanglesCount;

	private Stack<Point> points;
	private int pointsCount;

	private static LinkedRTreeShapePool instance;

	private LinkedRTreeShapePool() {
		circles = new Stack<Circle>();
		circlesCount = 0;
		rectangles = new Stack<Rectangle>();
		rectanglesCount = 0;
		points = new Stack<Point>();
		pointsCount = 0;
		Watchdog.getInstance().register(this);
	}

	public static LinkedRTreeShapePool getInstance_() {
		if (instance == null)
			instance = new LinkedRTreeShapePool();
		return instance;
	}

	@Override
	public void memoryStateChanged(short state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportToWatchdog() {
		Watchdog.getInstance().report(this,
				"Circles: " + circles.size() + " of " + circlesCount);
	}

	@Override
	public String getName() {
		return "LinkedRTree Shape Pool";
	}

	@Override
	public Circle getCircle(int x, int y, int r) {
		synchronized (circles) {
			if (circles.size() == 0) {
				circlesCount++;
				return new Circle(x, y, r);
			} else {
				Circle c = circles.pop();
				c.setCenter(x, y);
				c.setRadius(r);
				return c;
			}
		}
	}

	@Override
	public Point getPoint(int x, int y) {
		synchronized (points) {
			if (points.size() == 0) {
				pointsCount++;
				return new Point(x, y);
			} else {
				Point p = points.pop();
				p.setCenter(x, y);
				return p;
			}
		}
	}

	@Override
	public Rectangle getRectangle(int x, int y, int x2, int y2) {
		synchronized (points) {
			if (rectangles.size() == 0) {
				rectanglesCount++;
				return new Rectangle(x, y, x2, y2);
			} else {
				Rectangle r = rectangles.pop();
				r.setDimensions(x, y, x2 - x, y2 - y);
				return r;
			}
		}
	}

	@Override
	public Object getPolygon(int[] cornerPoints) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recycleShape(Object shape) {
		if (shape instanceof Point)
			points.add((Point) shape);
		else if (shape instanceof Circle)
			circles.add((Circle) shape);
		else if (shape instanceof Rectangle)
			rectangles.add((Rectangle) shape);
	}

	@Override
	public void unregisterFromWatchdog() {
		Watchdog.getInstance().unregister(this);
	}
}
