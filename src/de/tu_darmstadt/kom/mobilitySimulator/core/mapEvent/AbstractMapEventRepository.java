package de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent;

import java.util.Map;
import java.util.Set;

import de.tu_darmstadt.kom.linkedRTree.FilterInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.ShapePoolInterface;
import de.tu_darmstadt.kom.mobilitySimulator.exception.MobilitySimulatorException;

public abstract class AbstractMapEventRepository<E extends AbstractMapEvent>
		implements Map<Integer, E>, ShapePoolInterface {

	/**
	 * 
	 * @param shape
	 *            Shape of the intersection region
	 * @param container
	 *            set in which the query results are put
	 */
	public abstract void findIntersectingEvents(Object shape, Set<E> container);

	/**
	 * Adds the element to the put queue. The Element will be added to the
	 * repository after the executePut() method is called.
	 */
	public abstract E put(Integer key, E value);

	/**
	 * Adds all elements to the put queue. The Element will be added to the
	 * repository after the executePut() method is called.
	 */
	public abstract void putAll(Map<? extends Integer, ? extends E> m);

	/**
	 * Marks this entry as out of date by putting it to the remove queue. These
	 * entries will be removed from the list next time the executeRemovals()
	 * method is called.
	 */
	public abstract E remove(Object key);

	/**
	 * Removes all elements from the repository that are in the remove queue.
	 */
	public abstract void executeRemove();

	/**
	 * Adds all Elements to the repository that are in the put queue.
	 */
	public abstract void executePut();

	/**
	 * 
	 * @param shape
	 *            Shape of the intersection region
	 * @param filter
	 * @param container
	 *            set in which the query results are put
	 */
	public abstract void findIntersectingEvents(Object shape,
			FilterInterface filter, Set<E> container);

	/**
	 * 
	 * @param shape
	 *            Shape of the intersection region
	 * @return List of events or null if no events where found
	 */
	public abstract Set<E> findIntersectingEvents(Object shape);

	/**
	 * 
	 * @param shape
	 *            Shape of the intersection region
	 * @param filter
	 * @return List of events or null if no events where found
	 */
	public abstract Set<E> findIntersectingEvents(Object shape,
			FilterInterface filter);

	/**
	 * 
	 * @param mapEvent
	 *            The map event
	 * @param radius
	 *            The radius defining the neighborhood area around the event
	 * @return List of Events or null if no event was found
	 * @throws MobilitySimulatorException
	 */
	public abstract Set<E> findNeighbors(E mapEvent, int radius);

	/**
	 * 
	 * @param mapEvent
	 *            The map event
	 * @param radius
	 *            The radius defining the neighborhood area around the event
	 * @param container
	 *            set in which the query results are put
	 * @throws MobilitySimulatorException
	 */
	public abstract void findNeighbors(E mapEvent, int radius, Set<E> container);

	/**
	 * If an event was moved, the repository has to be refreshed by calling this
	 * method
	 * 
	 * @param mapEvent
	 *            The event as its new position
	 */
	public abstract void eventMoved(E mapEvent, int x, int y, int z);

	/**
	 * If an event was moved, the repository has to be refreshed by calling this
	 * method
	 * 
	 * @param mapEvent
	 *            The event as its new position
	 */
	public abstract void eventMoved(E mapEvent, int x, int y);

	/**
	 * If an event was moved, the repository has to be refreshed by calling this
	 * method
	 * 
	 * @param mapEvent
	 *            The event as its new position
	 */
	public abstract void eventMoved(E AbstractMapEvent, int[] pos);

	/**
	 * An event changed its size but not its position
	 * 
	 * @param event
	 */
	public abstract void eventSizeChanged(E event);
}
