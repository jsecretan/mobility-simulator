package de.tu_darmstadt.kom.mobilitySimulator.core;

public interface ShapePoolInterface {

	/**
	 * Returns a point-shape
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Object getPoint(int x, int y);

	/**
	 * Returns a circular shape
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @return
	 */
	public Object getCircle(int x, int y, int radius);

	/**
	 * Returns a recangular shape
	 * 
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @return
	 */
	public Object getRectangle(int x, int y, int x2, int y2);

	/**
	 * returns a polygon shape
	 * 
	 * @param cornerPoints
	 *            [x1 y1 x2 y2 x3 y3 ...]
	 * @return
	 */
	public Object getPolygon(int[] cornerPoints);

	/**
	 * Recycles a shape, that was created from this repository or somewhere
	 * else.
	 * 
	 * @param shape
	 */
	public void recycleShape(Object shape);

}
