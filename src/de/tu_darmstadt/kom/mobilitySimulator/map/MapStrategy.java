package de.tu_darmstadt.kom.mobilitySimulator.map;

import java.util.LinkedList;
import java.util.Queue;

import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapStrategyInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.NeighborPosition;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.WorkingMapPool;

public class MapStrategy implements MapStrategyInterface {

	private DiscreteMap discreteMap;

	public MapStrategy() {
		discreteMap = DiscreteMap.getInstance();
	}

	@Override
	public void buildSinkMap(int[][] resultMap, int[] dest, int maxUp,
			int maxDown, boolean withEvents) {

		int[][] overlayMap = WorkingMapPool.getInstance().getWorkingMap();

		// Initialize overlaymap with infinite
		for (int i = 0; i < overlayMap.length; i++) {
			for (int j = 0; j < overlayMap[i].length; j++) {
				overlayMap[i][j] = Integer.MAX_VALUE;
			}
		}

		Queue<int[]> queue = new LinkedList<int[]>();

		initMap(dest, resultMap, overlayMap, queue);

		buildOverlayMapFromQueue(overlayMap, resultMap, queue, maxUp, maxDown);

		WorkingMapPool.getInstance().recicleWorkingMap(overlayMap);

	}

	@Deprecated
	public void buildSinkMap(int[] dest, int[][] infiniteMap) {

		// Queue<int[]> queue = new LinkedList<int[]>();
		//
		// initMap(dest, infiniteMap, queue);
		//
		// buildOverlayMapFromQueue(infiniteMap, queue);

	}

	@Deprecated
	public void buildSinkMap(int[] dest, int[][] infiniteMap,
			int[][] additionalMap) {

		// Queue<int[]> queue = new LinkedList<int[]>();
		//
		// initMap(dest, infiniteMap, queue);
		//
		// buildOverlayMapFromQueue(infiniteMap, queue, additionalMap);

	}

	private void initMap(int[] dest, int[][] resultMap, int[][] overlayMap,
			Queue<int[]> queue) {
		switch (dest.length) {

		case 2:
			// Point destination
			queue.add(dest);
			overlayMap[dest[1]][dest[0]] = 0;
			resultMap[dest[1]][dest[0]] = discreteMap.getObstacles()[dest[1]][dest[0]];
			break;

		case 3: {
			int x = dest[0] - dest[2];
			if (x < 0)
				x = 0;
			if (x >= DiscreteMap.sizeX)
				x = DiscreteMap.sizeX - 1;
			// Iterate from x-r to x+r
			for (; x < dest[0] + dest[2]; x++) {

				int y = dest[1] - dest[2];
				if (y < 0)
					y = 0;
				if (y >= DiscreteMap.sizeY)
					y = DiscreteMap.sizeY - 1;
				// Iterate from y-r to y+r
				for (; y < dest[1] + dest[2]; y++) {
					if (isInCircle(x, dest[0], y, dest[1], dest[2]) && y > 0
							&& y < DiscreteMap.sizeY && x > 0
							&& x < DiscreteMap.sizeX) {
						queue.add(new int[] { x, y });
						overlayMap[y][x] = 0;
						resultMap[y][x] = discreteMap.getObstacles()[y][x];
					}
				}
			}
		}
			break;
		case 4:
			// Iterate from x1 to x2
			for (int x = dest[0]; x < dest[2]; x++) {
				// Iterate from y1 to y2
				for (int y = dest[1]; y < dest[3]; y++) {
					if (y > 0 && y < DiscreteMap.sizeY && x > 0
							&& x < DiscreteMap.sizeX) {
						queue.add(new int[] { x, y });
						overlayMap[y][x] = 0;
						resultMap[y][x] = discreteMap.getObstacles()[y][x];
					}
				}
			}
			break;

		case 1:
			// Error
		default:
			// Polynomial/Spline destination
			Exception e = new Exception(
					"Unsupported amount of destination parameter ("
							+ dest.length + " parameters).");
			e.printStackTrace();
			System.exit(0);
			break;
		}
	}

	/**
	 * Checks if a point lies inside a circle
	 * 
	 * @param x
	 *            X coordinate
	 * @param x_m
	 *            X center
	 * @param y
	 *            Y coordinate
	 * @param y_m
	 *            Y center
	 * @param r
	 *            Radius
	 * @return
	 */
	private boolean isInCircle(int x, int x_m, int y, int y_m, int r) {
		return Math.pow(r, 2) >= Math.pow((x - x_m), 2)
				+ Math.pow((y - y_m), 2);
	}

	private void buildOverlayMapFromQueue(int[][] overlayMap,
			int[][] resultMap, Queue<int[]> queue, int maxUp, int maxDown)
			throws InstantiationError {

		int x, y;

		do {
			x = queue.peek()[0];
			y = queue.poll()[1];

			calculateNeighborOverlay(NeighborPosition.TOP, overlayMap,
					resultMap, queue, maxUp, maxDown, x, y);
			calculateNeighborOverlay(NeighborPosition.RIGHT, overlayMap,
					resultMap, queue, maxUp, maxDown, x, y);
			calculateNeighborOverlay(NeighborPosition.BOTTOM, overlayMap,
					resultMap, queue, maxUp, maxDown, x, y);
			calculateNeighborOverlay(NeighborPosition.LEFT, overlayMap,
					resultMap, queue, maxUp, maxDown, x, y);

		} while (!queue.isEmpty());

	}

	private void buildOverlayMapFromQueue(int[][] overlayMap, Queue<int[]> queue)
			throws InstantiationError {

		int x, y;

		do {
			x = queue.peek()[0];
			y = queue.poll()[1];

			calculateNeighborOverlay(NeighborPosition.TOP, overlayMap, queue,
					x, y);
			calculateNeighborOverlay(NeighborPosition.RIGHT, overlayMap, queue,
					x, y);
			calculateNeighborOverlay(NeighborPosition.BOTTOM, overlayMap,
					queue, x, y);
			calculateNeighborOverlay(NeighborPosition.LEFT, overlayMap, queue,
					x, y);

		} while (!queue.isEmpty());

	}

	private void buildOverlayMapFromQueue(int[][] overlayMap,
			Queue<int[]> queue, int[][] additionalMap)
			throws InstantiationError {

		int x, y;

		do {
			x = queue.peek()[0];
			y = queue.poll()[1];

			calculateNeighborOverlay(NeighborPosition.TOP, overlayMap, queue,
					x, y, additionalMap);
			calculateNeighborOverlay(NeighborPosition.RIGHT, overlayMap, queue,
					x, y, additionalMap);
			calculateNeighborOverlay(NeighborPosition.BOTTOM, overlayMap,
					queue, x, y, additionalMap);
			calculateNeighborOverlay(NeighborPosition.LEFT, overlayMap, queue,
					x, y, additionalMap);

		} while (!queue.isEmpty());

	}

	private void calculateNeighborOverlay(NeighborPosition neighbor,
			int[][] overlayMap, int[][] resultMap, Queue<int[]> queue,
			int maxUp, int maxDown, int x, int y) {

		int[] nPos = neighbourPos(x, y, neighbor);
		if (nPos == null)
			return;

		int nx = nPos[0], ny = nPos[1];

		int slope = Math.abs(discreteMap.getObstacles()[y][x]
				- discreteMap.getObstacles()[ny][nx]);

		if (slope >= -1 * maxUp && slope <= maxDown) {

			if (discreteMap.getObstacles()[y][x] <= discreteMap.getObstacles()[ny][nx]) {

				if (overlayMap[ny][nx] > overlayMap[y][x] + 1) {
					queue.add(nPos);
					overlayMap[ny][nx] = overlayMap[y][x] + 1;
					resultMap[ny][nx] = overlayMap[y][x] + 1
							+ discreteMap.getObstacles()[ny][nx];
				}

			} else {

				int obstDiff = discreteMap.getObstacles()[y][x]
						- discreteMap.getObstacles()[ny][nx];

				if (overlayMap[ny][nx] - obstDiff > overlayMap[y][x] + 1) {

					queue.add(nPos);
					resultMap[ny][nx] = overlayMap[y][x] + 1
							+ discreteMap.getObstacles()[y][x];

					overlayMap[ny][nx] = resultMap[ny][nx]
							- discreteMap.getObstacles()[ny][nx];

				}

			}
		}

		// if (nPos[0] < 0 || nPos[0] >= discreteMap.getSizeX() || nPos[1] < 0
		// || nPos[1] >= discreteMap.getSizeY())
		// return;

		// short neighborObs = discreteMap.getObstacles()[nPos[1]][nPos[0]];
		// int neighborOverl = overlayMap[nPos[1]][nPos[0]];
		//
		// int thisOverlay = overlayMap[y][x];
		// short thisObs = discreteMap.getObstacles()[y][x];
		//
		// // Overlay ist kleiner
		// if (neighborOverl > thisOverlay + 1) {
		//
		// // Overlay neu zuweisen
		// if (thisObs <= neighborObs)
		// overlayMap[nPos[1]][nPos[0]] = (short) (thisOverlay + 1);
		// else
		// overlayMap[nPos[1]][nPos[0]] = (short) (thisOverlay + 1
		// + discreteMap.getObstacles()[y][x] - discreteMap
		// .getObstacles()[nPos[1]][nPos[0]]);
		//
		// // Feld zur Bearbeitungsschlange hinzufügen
		// queue.add(nPos);
		// }
	}

	private void calculateNeighborOverlay(NeighborPosition neighbor,
			int[][] overlayMap, Queue<int[]> queue, int x, int y) {

		int[] nPos = neighbourPos(x, y, neighbor);
		if (nPos == null)
			return;

		if (nPos[0] < 0 || nPos[0] >= discreteMap.getSizeX() || nPos[1] < 0
				|| nPos[1] >= discreteMap.getSizeY())
			return;

		short neighborObs = discreteMap.getObstacles()[nPos[1]][nPos[0]];
		int neighborOverl = overlayMap[nPos[1]][nPos[0]];

		int thisOverlay = overlayMap[y][x];
		short thisObs = discreteMap.getObstacles()[y][x];

		// Overlay ist kleiner
		if (neighborOverl > thisOverlay + 1) {

			// Overlay neu zuweisen
			if (thisObs <= neighborObs)
				overlayMap[nPos[1]][nPos[0]] = (short) (thisOverlay + 1);
			else
				overlayMap[nPos[1]][nPos[0]] = (short) (thisOverlay + 1
						+ discreteMap.getObstacles()[y][x] - discreteMap
						.getObstacles()[nPos[1]][nPos[0]]);

			// Feld zur Bearbeitungsschlange hinzufügen
			queue.add(nPos);
		}
	}

	private void calculateNeighborOverlay(NeighborPosition neighbor,
			int[][] overlayMap, Queue<int[]> queue, int x, int y,
			int[][] additionalMap) {

		int[] nPos = neighbourPos(x, y, neighbor);
		if (nPos == null)
			return;

		if (nPos[0] < 0 || nPos[0] >= discreteMap.getSizeX() || nPos[1] < 0
				|| nPos[1] >= discreteMap.getSizeY())
			return;

		short neighborObs = discreteMap.getObstacles()[nPos[1]][nPos[0]];
		int neighborOverl = overlayMap[nPos[1]][nPos[0]];
		int neighborAdditional = additionalMap[nPos[1]][nPos[0]];

		int thisOverlay = overlayMap[y][x];
		short thisObs = discreteMap.getObstacles()[y][x];
		int thisAdditional = additionalMap[y][x];

		// Overlay ist kleiner
		if (neighborOverl > thisOverlay + 1) {

			// Overlay neu zuweisen
			if (Math.max(thisObs, thisAdditional) <= Math.max(neighborObs,
					neighborAdditional))
				overlayMap[nPos[1]][nPos[0]] = (short) (thisOverlay + 1);
			else
				overlayMap[nPos[1]][nPos[0]] = (short) (thisOverlay + 1
						+ Math.max(thisObs, thisAdditional) - Math.max(
						neighborObs, neighborAdditional));

			// Feld zur Bearbeitungsschlange hinzufügen
			queue.add(nPos);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tu_darmstadt.kom.mobilitySimulator.map.MapStrategyInterface#
	 * makeInfinitieMap(int[][])
	 */
	@Deprecated
	public void makeInfinitieMap(int[][] map) {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = Integer.MAX_VALUE
						- discreteMap.getObstacles()[i][j];
			}
		}
	}

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
	public int[] neighbourPos(int x, int y, NeighborPosition neighbor) {
		int[] returnValue = new int[2];
		switch (neighbor) {
		case TOP:
			if (y - 1 < 0)
				return null;
			returnValue[0] = x;
			returnValue[1] = y - 1;
			break;
		case RIGHT:
			if (x + 1 >= DiscreteMap.sizeX)
				return null;
			returnValue[0] = x + 1;
			returnValue[1] = y;
			break;
		case BOTTOM:
			if (y + 1 >= DiscreteMap.sizeY)
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
