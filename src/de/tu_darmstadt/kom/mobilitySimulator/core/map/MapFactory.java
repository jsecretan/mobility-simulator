package de.tu_darmstadt.kom.mobilitySimulator.core.map;

public abstract class MapFactory {

	protected static MapFactory instance;

	public void setAsMapFactory() {
		instance = this;
	}

	public static void clearInstance() {
		if (instance != null)
			instance = null;
	}

	public static MapFactory getInstance() {
		if (instance == null) {
			Exception e = new Exception(
					"No map factory was set. Create an instance of MapFactory and call its setAsMapFactory method.");
			e.printStackTrace();
			System.exit(0);
		}
		return instance;
	}

	public abstract int[][] getMap(int[] dest, int maxUp, int maxDown,
			boolean withEvents);

	public abstract int[][] getMapIncludingEvents(int[] dest, int maxUp,
			int maxDown);

}