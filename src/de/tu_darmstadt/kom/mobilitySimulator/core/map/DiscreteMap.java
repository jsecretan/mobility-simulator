package de.tu_darmstadt.kom.mobilitySimulator.core.map;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class DiscreteMap {

	private static DiscreteMap instance;

	private int[][] terrain;
	private short[][] obstacles;

	private short[][] additionalCahnnel;

	public static int sizeX;
	public static int sizeY;

	/**
	 * Loads a map and initializes the DiscreteMap.
	 * 
	 * @param mapPath
	 *            The absolute Path to the map-file including the filename and
	 *            ending.
	 * @throws IOException
	 */
	public static void init(String mapPath) throws IOException {
		instance = new DiscreteMap(mapPath);
	}

	/**
	 * Initialize the DiscreteMap before its first use with the init(..) method!
	 * 
	 * @return
	 * @throws InstantiationError
	 *             if the discrete map was not initialized.
	 */
	public static DiscreteMap getInstance() throws InstantiationError {
		if (instance == null)
			throw new InstantiationError(
					"The discrete map was not yet initialized. "
							+ "Initialize the DiscreteMap using the DiscreteMap.init(..) method.");
		return instance;
	}

	private DiscreteMap(String mapPath) throws IOException {

		// read the Mapfile
		File mapFile = new File(mapPath);
		System.out.println("\tLoad map: " + mapFile.getAbsolutePath() + ".");
		BufferedImage a;

		a = ImageIO.read(mapFile);

		sizeX = a.getWidth();
		sizeY = a.getHeight();

		System.out.println("\tMap size: " + sizeX + " x " + sizeY + " = "
				+ (sizeX * sizeY) + ".");

		double memorySize = (sizeX * sizeY * 4);
		System.out.println("\tMap Memory Size: ~"
				+ (memorySize >= 1000000 ? +(memorySize / 1000000d) + " MByte"
						: (memorySize >= 1000 ? (memorySize / 1000d) + " kByte"
								: memorySize + " Byte")));

		terrain = new int[sizeY][sizeX];
		obstacles = new short[sizeY][sizeX];
		additionalCahnnel = new short[sizeY][sizeX];

		for (int i = 0; i < sizeY; i++) { // i = y-value
			for (int j = 0; j < sizeX; j++) { // j = x-value

				Color rgbC = new Color(a.getRGB(j, i));

				// Terrain => B-channel
				terrain[i][j] = rgbC.getBlue() * (sizeX + sizeY) / 200;

				obstacles[i][j] = (short) rgbC.getRed();

				additionalCahnnel[i][j] = (short) rgbC.getGreen();

				// if (terrain[i][j] == 0) {
				// System.out.print("+");
				// } else {
				// System.out.print("#");
				// }

				// // Get HSL-Values if required
				// float[] hsl = new float[3];
				// hsl = Color.RGBtoHSB(rgbC.getRed(), rgbC.getGreen(), rgbC
				// .getBlue(), hsl);
			}
			// System.out.println("");
		}

		// System.out.println(obstacles[0][0]);
		// System.exit(0);
	}

	public Set<int[]> getNeighbors(int[] cellCoordinates) {

		if (cellCoordinates.length == 2) {

			HashSet<int[]> neig = new HashSet<int[]>();

			if (cellCoordinates[0] > 0) {
				neig.add(new int[] { cellCoordinates[0] - 1, cellCoordinates[1] });
			}
			if (cellCoordinates[0] < sizeY - 1) {
				neig.add(new int[] { cellCoordinates[0] + 1, cellCoordinates[1] });
			}
			if (cellCoordinates[1] > 0) {
				neig.add(new int[] { cellCoordinates[0], cellCoordinates[1] - 1 });
			}
			if (cellCoordinates[1] < sizeX - 1) {
				neig.add(new int[] { cellCoordinates[0], cellCoordinates[1] + 1 });
			}
			return neig;
		}

		throw new IndexOutOfBoundsException(
				"Coordinates must have dimension: 2");
	}

	public float getSlope(int[] dest, int[] origin) {
		if (dest.length == 2 && origin.length == 2) {
			return terrain[dest[0]][dest[1]] - terrain[origin[0]][origin[1]];
		}
		throw new IndexOutOfBoundsException(
				"Coordinates must have dimension: 2");
	}

	public int[][] getTerrain() {
		return terrain;
	}

	public short[][] getObstacles() {
		return obstacles;
	}

	public short[][] getAdditionalCahnnel() {
		return additionalCahnnel;
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	/**
	 * Returns the value of the potential field composed of:
	 * <ul>
	 * <li>the obstacle Value of this cell</li>
	 * <li>the overlay value of the overlaymap leading to the <b>point</b> of
	 * destination</li>
	 * </ul>
	 * 
	 * @param pos
	 *            The destination of the Agent (Sink of the potential field)
	 * @param dest
	 *            The destination of the Agent (Sink of the potential field)
	 * @return
	 */
	@Deprecated
	public short getComposedCellValue(int[] pos, int[] dest) {
		return getComposedCellValue(pos[0], pos[1], dest[0], dest[1]);
	}

	/**
	 * Returns the value of the potential field composed of:
	 * <ul>
	 * <li>the obstacle Value of this cell</li>
	 * <li>the overlay value of the overlaymap leading to the <b>point</b> of
	 * destination</li>
	 * </ul>
	 * 
	 * @param pos
	 *            The destination of the Agent (Sink of the potential field)
	 * @param xDest
	 *            The Y-Destination of the Agent (Sink of the potential field)
	 * @param yDest
	 *            The Y-Destination of the Agent (Sink of the potential field)
	 * @return
	 */
	@Deprecated
	public short getComposedCellValue(int[] pos, int xDest, int yDest) {
		return getComposedCellValue(pos[0], pos[1], xDest, yDest);
	}

	// /**
	// * Returns the value of the potential field composed of:
	// * <ul>
	// * <li>the obstacle Value of this cell</li>
	// * <li>the overlay value of the overlaymap leading to the
	// * <b>region</b>(Rectangle) of destination</li>
	// * </ul>
	// *
	// * @param xPos
	// * The X-Position of the agent
	// * @param yPos
	// * The Y-Position of the agent
	// * @param destTopLeft
	// * The top left corner of the destination area of the Agent (Sink
	// * of the potential field)
	// * @param destBottomRight
	// * The bottom right corner of the destination area of the Agent
	// * (Sink of the potential field)
	// * @return
	// */
	// public short getComposedCellValue(int[] pos, int[] destTopLeft,
	// int[] destBottomRight) {
	// return getComposedCellValue(pos[0], pos[1], destTopLeft[0],
	// destTopLeft[1], destBottomRight[0], destBottomRight[1]);
	// }

	// /**
	// * Returns the value of the potential field composed of:
	// * <ul>
	// * <li>the obstacle Value of this cell</li>
	// * <li>the overlay value of the overlaymap leading to the
	// * <b>region</b>(Rectangle) of destination</li>
	// * </ul>
	// *
	// * @param pos
	// * The position of the agent
	// * @param xDestTopLeft
	// * The X-Value of the top left corner of the destination area of
	// * the Agent (Sink of the potential field)
	// * @param yDestTopLeft
	// * The Y-Value of the top left corner of the destination area of
	// * the Agent (Sink of the potential field)
	// * @param xDestBottomRight
	// * The X-Value of the bottom right corner of the destination area
	// * of the Agent (Sink of the potential field)
	// * @param yDestBottomRight
	// * The X-Value of the bottom right corner of the destination area
	// * of the Agent (Sink of the potential field)
	// * @return
	// */
	// public short getComposedCellValue(int[] pos, int xDestTopLeft,
	// int yDestTopLeft, int xDestBottomRight, int yDestBottomRight) {
	// return getComposedCellValue(pos[0], pos[1], xDestTopLeft, yDestTopLeft,
	// xDestBottomRight, yDestBottomRight);
	// }

	/**
	 * Returns the value of the potential field composed of:
	 * <ul>
	 * <li>the obstacle Value of this cell</li>
	 * <li>the overlay value of the overlaymap leading to the <b>point</b> of
	 * destination</li>
	 * </ul>
	 * 
	 * @param xPos
	 *            The X-Position of the agent
	 * @param yPos
	 *            The Y-Position of the agent
	 * @param xDest
	 *            The X-Destination of the Agent (Sink of the potential field)
	 * @param yDest
	 *            The Y-Destination of the Agent (Sink of the potential field)
	 * @return
	 */
	@Deprecated
	public short getComposedCellValue(int xPos, int yPos, int xDest, int yDest) {
		if (xDest < 0 || xDest >= sizeX || yDest < 0 || yDest >= sizeY
				|| xPos < 0 || xPos >= sizeX || yPos < 0 || yPos >= sizeY)
			return Short.MAX_VALUE;
		try {
			// return (short) (MapFactory.getInstance().getMap(
			// new int[] { xDest, yDest })[yPos][xPos] + obstacles[yPos][xPos]);
			return 0;
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.exit(0);
			return Short.MAX_VALUE;
		}
	}

	// /**
	// * Returns the value of the potential field composed of:
	// * <ul>
	// * <li>the obstacle Value of this cell</li>
	// * <li>the overlay value of the overlaymap leading to the
	// * <b>region</b>(Rectangle) of destination</li>
	// * </ul>
	// *
	// * @param xPos
	// * The X-Position of the agent
	// * @param yPos
	// * The Y-Position of the agent
	// * @param xDestTopLeft
	// * The X-Value of the top left corner of the destination area of
	// * the Agent (Sink of the potential field)
	// * @param yDestTopLeft
	// * The Y-Value of the top left corner of the destination area of
	// * the Agent (Sink of the potential field)
	// * @param xDestBottomRight
	// * The X-Value of the bottom right corner of the destination area
	// * of the Agent (Sink of the potential field)
	// * @param yDestBottomRight
	// * The X-Value of the bottom right corner of the destination area
	// * of the Agent (Sink of the potential field)
	// * @return
	// */
	// public short getComposedCellValue(int xPos, int yPos, int xDestTopLeft,
	// int yDestTopLeft, int xDestBottomRight, int yDestBottomRight) {
	// if (xDestTopLeft < 0 || xDestTopLeft >= sizeX || yDestTopLeft < 0
	// || yDestTopLeft >= sizeY || xDestBottomRight < 0
	// || xDestBottomRight >= sizeX || yDestBottomRight < 0
	// || yDestBottomRight >= sizeY || xPos < 0 || xPos >= sizeX
	// || yPos < 0 || yPos >= sizeY)
	// return Float.MAX_VALUE;
	// try {
	// return overlayCache.getPlan(
	// new int[] { xDestTopLeft, yDestTopLeft }, new int[] {
	// xDestBottomRight, yDestBottomRight })[yPos][xPos]
	// + obstacles[yPos][xPos];
	// } catch (ArrayIndexOutOfBoundsException e) {
	// e.printStackTrace();
	// System.exit(0);
	// return Float.MAX_VALUE;
	// }
	// }

	/**
	 * Returns the destination of the requested neighbor cell
	 * 
	 * @param x
	 *            the X position of the cell, the neighbor cell is requested
	 * @param y
	 *            the Y position of the cell, the neighbor cell is requested
	 * @param neighbor
	 *            the requested neighbor cell
	 * @return the neighbor cell position as 2 dimensional array or null if the
	 *         neighbor cell is located outside of the map
	 */
	public static int[] neighbourPos(int x, int y, NeighborPosition neighbor) {
		int[] returnValue = new int[2];
		switch (neighbor) {
		case TOP:
			if ((y - 1) < 0)
				return null;
			returnValue[0] = x;
			returnValue[1] = y - 1;
			break;
		case RIGHT:

			if ((x + 1) >= sizeX)
				return null;
			returnValue[0] = x + 1;
			returnValue[1] = y;
			break;
		case BOTTOM:
			if ((y + 1) >= sizeY)
				return null;
			returnValue[0] = x;
			returnValue[1] = y + 1;
			break;
		case LEFT:
			if (x - 1 < 0)
				return null;
			returnValue[0] = x - 1;
			returnValue[1] = y;
			break;
		default:
			return null;
		}
		return returnValue;
	}
}
