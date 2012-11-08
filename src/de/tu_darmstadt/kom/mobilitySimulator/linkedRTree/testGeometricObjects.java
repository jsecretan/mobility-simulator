package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;

public class testGeometricObjects {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DiscreteMap.sizeX = 1000;
		DiscreteMap.sizeY = 1000;

		LinkedRTreeFireMapEvent event = new LinkedRTreeFireMapEvent(100, 100, 10, 0.5f);
	}
}
