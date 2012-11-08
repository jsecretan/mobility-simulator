package de.tu_darmstadt.kom.mobilitySimulator.core.map;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import de.tu_darmstadt.kom.gui.MapDrawPanel;

@Deprecated
public class PotentialFieldRepository {

	private Map<String, float[][]> overlayCache;

	private static final int CACHESIZE = 200;
	private MapDrawPanel mapGui;

	// private int GUISleepTime;

	public PotentialFieldRepository(MapDrawPanel mapGui) {
		overlayCache = new LinkedHashMap<String, float[][]>(CACHESIZE + 2, 1,
				true) {
			protected boolean removeEldestEntry(Map.Entry eldest) {
				// System.out.println("Size: "+size()+" - eldest: "+eldest.getKey().toString());
				return size() > CACHESIZE;
			}
		};
		this.mapGui = mapGui;
	}

	public PotentialFieldRepository() {
		overlayCache = new LinkedHashMap<String, float[][]>(CACHESIZE + 2, 1,
				true) {
			protected boolean removeEldestEntry(Map.Entry eldest) {
				// System.out.println("Size: "+size()+" - eldest: "+eldest.getKey().toString());
				return size() > CACHESIZE;
			}
		};
		this.mapGui = null;
	}

	public float[][] getPlan(int[] dest) {
		if (overlayCache.containsKey(Arrays.toString(dest))) {
			return overlayCache.get(Arrays.toString(dest));
		} else {

			// Maximum Size of overlay Cache
			// if (overlayCache.size() > CACHESIZE)
			// overlayCache.clear();

			// TODO Create Plan from Map

			// Initialize Overlay map with infinite
			float[][] overlayMap = initOverlayMapWithInfinit();

			if (mapGui != null)
				mapGui.setOverlayMap(overlayMap);

			Queue<int[]> queue = new LinkedList<int[]>();

			queue.add(dest);
			overlayMap[dest[1]][dest[0]] = 0;

			buildOverlayMapFromQueue(overlayMap, queue);

			// System.out.println("Insert: "+Arrays.toString(dest));
			overlayCache.put(Arrays.toString(dest), overlayMap);

			// System.err.println(overlayCache.size());

			// System.out
			// .println(DiscreteMap.getInstance().getObstacles()[20][20]);
			// System.out.println(overlayMap[20][20]);
			// System.exit(0);

			return overlayMap;
		}
	}

	public float[][] getPlan(int[] destTopLeft, int[] destBottomRight) {

		if (overlayCache.containsKey(Arrays.toString(destTopLeft) + "-"
				+ Arrays.toString(destBottomRight))) {
			return overlayCache.get(Arrays.toString(destTopLeft) + "-"
					+ Arrays.toString(destBottomRight));
		} else {
			// TODO Create Plan from Map

			// Initialize Overlay map with infinite
			float[][] overlayMap = initOverlayMapWithInfinit();

			if (mapGui != null)
				mapGui.setOverlayMap(overlayMap);

			int[] dest = new int[2];
			Queue<int[]> queue = new LinkedList<int[]>();

			for (int i = destTopLeft[1]; i <= destBottomRight[1]; i++) {
				for (int j = destTopLeft[0]; j <= destBottomRight[0]; j++) {

					dest[0] = i;
					dest[1] = j;
					queue.add(dest);
					overlayMap[dest[1]][dest[0]] = 0;
				}
			}

			buildOverlayMapFromQueue(overlayMap, queue);

			overlayCache.put(
					Arrays.toString(destTopLeft) + "-"
							+ Arrays.toString(destBottomRight), overlayMap);

			// System.err.println(overlayCache.size());

			// System.out
			// .println(DiscreteMap.getInstance().getObstacles()[20][20]);
			// System.out.println(overlayMap[20][20]);
			// System.exit(0);

			return overlayMap;
		}

	}

	public void setMapGui(MapDrawPanel mapGui) {
		this.mapGui = mapGui;
	}

	private float[][] initOverlayMapWithInfinit() {
		float[][] overlayMap = new float[DiscreteMap.getInstance().getSizeY()][DiscreteMap
				.getInstance().getSizeX()];
		for (int i = 0; i < overlayMap.length; i++) {
			for (int j = 0; j < overlayMap[i].length; j++) {
				overlayMap[i][j] = Integer.MAX_VALUE
						- DiscreteMap.getInstance().getObstacles()[i][j];
			}
		}
		return overlayMap;
	}

	private void buildOverlayMapFromQueue(float[][] overlayMap,
			Queue<int[]> queue) throws InstantiationError {

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

		if (mapGui != null) {
			float maxVal = 0;
			for (int i = 0; i < overlayMap.length; i++) {
				for (int j = 0; j < overlayMap[i].length; j++) {
					if (overlayMap[i][j]
							+ DiscreteMap.getInstance().getObstacles()[i][j] > maxVal)
						maxVal = overlayMap[i][j]
								+ DiscreteMap.getInstance().getObstacles()[i][j];
				}
			}
			mapGui.setHeatMapMaxValue(maxVal);
		}
	}

	private void calculateNeighborOverlay(NeighborPosition neighbor,
			float[][] overlayMap, Queue<int[]> queue, int x, int y) {

		int[] nPos = DiscreteMap.neighbourPos(x, y, neighbor);

		if (nPos[0] < 0 || nPos[0] >= DiscreteMap.getInstance().getSizeX()
				|| nPos[1] < 0
				|| nPos[1] >= DiscreteMap.getInstance().getSizeY())
			return;

		int neighborObs = DiscreteMap.getInstance().getObstacles()[nPos[1]][nPos[0]];
		float neighborOverl = overlayMap[nPos[1]][nPos[0]];

		float thisOverlay = overlayMap[y][x];
		int thisObs = DiscreteMap.getInstance().getObstacles()[y][x];

		// Overlay ist kleiner
		if (neighborOverl > thisOverlay + 1) {

			// Overlay neu zuweisen
			if (thisObs <= neighborObs)
				overlayMap[nPos[1]][nPos[0]] = thisOverlay + 1;
			else
				overlayMap[nPos[1]][nPos[0]] = thisOverlay
						+ 1
						+ DiscreteMap.getInstance().getObstacles()[y][x]
						- DiscreteMap.getInstance().getObstacles()[nPos[1]][nPos[0]];

			// Feld zur Bearbeitungsschlange hinzufügen
			queue.add(new int[] { nPos[0], nPos[1] });
		}

		// if (mapGui != null) {
		// mapGui.repaint();
		//
		// try {
		//
		// Thread.sleep(0);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		//
		// }
		// }
	}
}
