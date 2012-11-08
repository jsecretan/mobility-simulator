package de.tu_darmstadt.kom.mobilitySimulator.core.map;

import java.util.Stack;
import java.util.Vector;

import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.Watchdog;
import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.WatchdogStateListener;

public class WorkingMapPool implements WatchdogStateListener {

	private static WorkingMapPool instance;

	private short memState;
	private int sizeX, sizeY;
	private Stack<int[][]> cacheStack;

	private int createdWorkingMaps;

	public static WorkingMapPool getInstance() {
		if (instance == null)
			instance = new WorkingMapPool();
		return instance;
	}

	private WorkingMapPool() {
		cacheStack = new Stack<int[][]>();
		// sizeX = 1000;
		// sizeY = 1000;€
		sizeX = DiscreteMap.getInstance().getSizeX();
		sizeY = DiscreteMap.getInstance().getSizeY();
		createdWorkingMaps = 0;
		Watchdog.getInstance().register(this);
		memState = Watchdog.MEMORY_NORMAL;
	}

	@Override
	public String getName() {
		return "WorkingMapFactory";
	}

	@Override
	public void memoryStateChanged(short state) {
		memState = state;
	}

	@Override
	public void reportToWatchdog() {
		Watchdog.getInstance().report(this,
				"Created Working Maps: " + createdWorkingMaps);
	}

	public int[][] getWorkingMap() {
		return getWorkingMap(false);
	}

	public int[][] getWorkingMap(boolean cleared) {
		synchronized (cacheStack) {
			if (cacheStack.size() > 0) {
				if (cleared)
					return clearMap(cacheStack.pop());
				else
					return cacheStack.pop();

			} else {

				createdWorkingMaps++;
				return new int[sizeY][sizeX];
			}
		}

		// if (memState == Watchdog.MEMORY_ALERT)
		// return null;
		// else {
		// synchronized (cacheStack) {
		// if (cacheStack.size() > 0) {
		// synchronized (cacheStack) {
		// if (cleared)
		// return clearMap(cacheStack.pop());
		// else
		// return cacheStack.pop();
		// }
		// } else {
		// if (memState == Watchdog.MEMORY_NORMAL) {
		// createdWorkingMaps++;
		// return new int[sizeY][sizeX];
		// } else {
		// return null;
		// }
		// }
		// }
		// }
	}

	public int[][] clearMap(int[][] map) {
		int y, x;
		for (y = 0; y < map.length; y++) {
			for (x = 0; x < map[y].length; x++) {
				map[y][x] = 0;
			}
		}
		return map;
	}

	public void recicleWorkingMap(int[][] map) {
		// TODO think about this. redo
		if (memState == Watchdog.MEMORY_ALERT) {
			map = null;
			createdWorkingMaps--;
		} else if (map != null && map.length == sizeY && map[0].length == sizeX)
			cacheStack.push(map);
		else
			map = null;
	}

	public static void main(String[] args) {
		WorkingMapPool c = new WorkingMapPool();
		Watchdog.getInstance().register(c);
		Vector<int[][]> v = new Vector<int[][]>();

		for (int i = 0; i < 1000; i++) {
			v.add(c.getWorkingMap(false));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void unregisterFromWatchdog() {
		Watchdog.getInstance().unregister(this);
	}
}
