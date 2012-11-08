package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.kom.linkedRTree.Circle;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;

public class testLinkedRTree {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DiscreteMap.sizeX = 10;
		DiscreteMap.sizeY = 10;

		LinkedRTreeMapEventRepository eventRep = new LinkedRTreeMapEventRepository(
				10, 10);

		LinkedRTreeFireMapEvent event1 = new LinkedRTreeFireMapEvent(
				new Circle(3, 3, 2), 0.5f);

		System.out.println(event1.getId() + " created!");

		eventRep.put(event1.getId(), event1);
		eventRep.executePut();

		Set<LinkedRTreeFireMapEvent> container = new HashSet<LinkedRTreeFireMapEvent>();

		Circle c2 = new Circle(5, 5, 1);

		System.out.println(event1.getImpactArea().intersects(c2));

//		eventRep.findIntersectingEvents(c2, container);

		for (LinkedRTreeFireMapEvent staticRTreeMapEvent : container) {
			System.out.println(staticRTreeMapEvent.getId() + "");
		}
	}
}
