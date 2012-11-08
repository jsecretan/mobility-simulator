package de.tu_darmstadt.kom.mobilitySimulator.mapEvent;

import java.util.Collection;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.MapEventTypeInterface;

public interface CollapsedBuildingMapEvent extends MapEventTypeInterface {

	public int getWorkerCount();

	public Collection<AbstractAgent> workers();

	public int getMaxWorkerCapacity();

	public int getNormalWorkerCount();

	public int getSpecializedWorkerCount();
}
