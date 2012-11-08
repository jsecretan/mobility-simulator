package de.tu_darmstadt.kom.mobilitySimulator.core.map;

public interface MapStrategyInterface {

	/**
	 * macht eine combined-map aus ner leeren map
	 * 
	 * @param dest
	 * @param infiniteMap
	 */
	public abstract void buildSinkMap(int[][] resultMap, int[] dest, int maxUp,
			int maxDown, boolean withEvents);

}