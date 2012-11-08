package de.tu_darmstadt.kom.mobilitySimulator.filter;

import de.tu_darmstadt.kom.linkedRTree.FilterInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.mapEvent.CollapsedBuildingMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.mapEvent.FireMapEvent;

public class EventTypeFilter implements FilterInterface {

	private String[] attributes;

	public EventTypeFilter(String attributeString) {
		attributes = attributeString.split(",");
	}

	@Override
	public boolean filter(Object obj) {
		if (obj instanceof AbstractMapEvent) {

			if (attributes[0].equals("fire")) {
				return obj instanceof FireMapEvent;
			}

			if (attributes[0].equals("collapsedBuilding")
					&& attributes.length == 1) {
				return obj instanceof CollapsedBuildingMapEvent;
			}

			if (attributes[0].equals("collapsedBuilding")
					&& attributes[1].equals("freeForNormalAgent")) {
				return obj instanceof CollapsedBuildingMapEvent
						&& ((CollapsedBuildingMapEvent) obj)
								.getMaxWorkerCapacity() > ((CollapsedBuildingMapEvent) obj)
								.getWorkerCount();
			}

			if (attributes[0].equals("collapsedBuilding")
					&& attributes[1].equals("freeForRescueAgent")) {
				return obj instanceof CollapsedBuildingMapEvent
						&& ((CollapsedBuildingMapEvent) obj)
								.getMaxWorkerCapacity() > ((CollapsedBuildingMapEvent) obj)
								.getSpecializedWorkerCount();
			}

			return false;

		}
		return false;
	}
}
