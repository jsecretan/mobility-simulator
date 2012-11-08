package de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent;

import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;

public abstract class AbstractMapEvent implements MapEventTypeInterface {

	private static int elementCount = 0;
	private int id;

	protected int[][] impactMap;

	/**
	 * Agents that are actually working on this event
	 */
	protected Set<AbstractAgent> workers;

	/**
	 * EventPoints determine the gravity and impact of an event. If 0, the event
	 * has no more impact and is considered as solved
	 */
	protected int eventPoints;

	/**
	 * Allows agents to work on this event to decrease its event points
	 */
	protected boolean enableWorking;

	public AbstractMapEvent() {
		id = ++elementCount;
		enableWorking = true;
		this.workers = new HashSet<AbstractAgent>();
		impactMap = new int[DiscreteMap.sizeY][DiscreteMap.sizeX];
	}

	/**
	 * Calculates the impact Map
	 */
	protected abstract void calculateImpactMap();

	/**
	 * Returns the X coordinate of the event center
	 * 
	 * @return
	 */
	public abstract int getX();

	/**
	 * Returns the Y coordinate of the event center
	 * 
	 * @return
	 */
	public abstract int getY();

	/**
	 * <p>
	 * This function returns the impact area of this event.
	 * </p>
	 * <p>
	 * The damage area of an event is the area where the event is having an
	 * impact on its environment but is not necessarily damaging it. This range
	 * is mostly the range where agents are fleeing from.
	 * </p>
	 * 
	 * @return the shape that represents the impact area
	 */
	public abstract Object getImpactArea();

	/**
	 * <p>
	 * This function returns the damage area of this event.
	 * </p>
	 * <p>
	 * The damage area of an event is the area where the event is damaging its
	 * environment. It is a subarea of the impact area. The damage area is
	 * mostly the area where first responders act.
	 * </p>
	 * 
	 * @return the shape that represents the damage area
	 */
	public abstract Object getDamageArea();

	/**
	 * Returns the coordinates of the impact area
	 * 
	 * @return
	 */
	public abstract int[] getImpactAreaAsCoordinates();

	/**
	 * Returns the coordinates of the damage area
	 * 
	 * @return
	 */
	public abstract int[] getDamageAreaAsCoordinates();

	/**
	 * Returns the coordinates of the working area
	 * 
	 * @return
	 */
	public abstract int[] getWorkingAreaAsCoordinates();

	/**
	 * Returns the Area agents can start to work to decrease the event points of
	 * this event. Mostly this is equal to the damage area.
	 * 
	 * @return the shape that represents the working area
	 */
	abstract public Object getWorkingArea();

	/**
	 * 
	 * @param agent
	 *            The agent to register as worker on this event
	 * @return <ul>
	 *         <li><i>true</i> if the agent was registered</li>
	 *         <li><i>false</i>, if the agent is already registered or the event
	 *         does not allow registering</li>
	 *         </ul>
	 */
	public boolean registerWorker(AbstractAgent agent) {
		if (enableWorking)
			return workers.add(agent);
		return false;
	}

	/**
	 * 
	 * @param agent
	 *            The working agent to unregister from this event
	 * @return <ul>
	 *         <li><i>true</i> if the agent was unregistered</li>
	 *         <li><i>false</i>, if the agent is already unregistered or the
	 *         event does not allow (un-)registering</li>
	 *         </ul>
	 */
	public boolean unregisterWorker(AbstractAgent agent) {
		if (enableWorking)
			return workers.remove(agent);
		return false;
	}

	public int getEventPoints() {
		return eventPoints;
	}

	/**
	 * This method is called by the scheduler at the beginning of a clock cycle
	 * and allows the event to change its state.
	 */
	abstract public void clock();

	public int getId() {
		return id;
	}

	public int[][] getImpactMap() {
		return impactMap;
	}

	public abstract int getSizeIndicator();

	public abstract boolean isActive();

}
