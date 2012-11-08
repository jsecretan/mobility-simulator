package de.tu_darmstadt.kom.mobilitySimulator.core.agent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.tu_darmstadt.kom.linkedRTree.FilterInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.ShapePoolInterface;
import de.tu_darmstadt.kom.mobilitySimulator.exception.MobilitySimulatorException;

/**
 * <p>
 * A structure for an AgentRepository
 * </p>
 * <p>
 * The repository acts quickly as a {@link Map} when iterating over all Agents
 * and allows R* intersection queries.
 * </p>
 * <p>
 * Delete and Put requests are queued and have do be executed by calling the
 * executePut()/Remove() methods.
 * </p>
 * <p>
 * <strong>All methods of this class are synchronized</strong>
 * </p>
 * 
 * @author Mathieu Münch
 * 
 * @param <E>
 *            The class-type of objects, stored in this repository
 */
public abstract class AbstractAgentRepository<E extends AbstractAgent>
		implements Map<Integer, E>, ShapePoolInterface {

	/**
	 * Creates a new agent that is compatible with this agent repository. The
	 * agent is not already inserted into the agent repository.
	 * 
	 * @param X
	 *            coordinate of the center of the agent
	 * @param Y
	 *            coordinate of the center of the agent
	 * @return The new agent
	 */
	public abstract AbstractAgent createAgent(int x, int y);

	/**
	 * Creates a new agent that is compatible with this agent repository. The
	 * agent is not already inserted into the agent repository.
	 * 
	 * @param X
	 *            coordinate of the center of the agent
	 * @param Y
	 *            coordinate of the center of the agent
	 * @param eneableMobile
	 *            defines, if the agent should be enabled to participate at
	 *            mobile communication
	 * @return The new agent
	 */
	public abstract AbstractAgent createAgent(int x, int y,
			boolean eneableMobile);

	/**
	 * 
	 * @param shape
	 *            Shape of the intersection region
	 * @return List of Agents or null if no agents where found
	 */
	public abstract Set<E> findIntersectingAgents(Object shape);

	/**
	 * 
	 * @param shape
	 *            Shape of the intersection region
	 * @param comparator
	 * @return List of Agents or null if no agents where found
	 */
	public abstract Set<E> findIntersectingAgents(Object shape,
			FilterInterface filter);

	/**
	 * 
	 * @param agent
	 *            The agent
	 * @param radius
	 *            The radius defining the neighborhood area around the agent
	 * @return List of Agents or null if no agents where found
	 * @throws MobilitySimulatorException
	 */
	public abstract Set<E> findNeighbors(E agent, int radius);

	/**
	 * 
	 * @param shape
	 *            Shape of the intersection region
	 * @param container
	 *            set, the results of the intersection queries are added to
	 */
	public abstract void findIntersectingAgents(Object shape, Set<E> container);

	/**
	 * 
	 * @param shape
	 *            Shape of the intersection region
	 * @param comparator
	 * @param container
	 *            set, the results of the intersection queries are added to
	 */
	public abstract void findIntersectingAgents(Object shape,
			FilterInterface filter, Set<E> container);

	/**
	 * 
	 * @param agent
	 *            The agent
	 * @param radius
	 *            The radius defining the neighborhood area around the agent
	 * @param container
	 *            set, the results of the intersection queries are added to
	 * @throws MobilitySimulatorException
	 */
	public abstract void findNeighbors(E agent, int radius, Set<E> container);

	/**
	 * If an agent was moved, the repository has to be updated by calling this
	 * method
	 * 
	 * @param agent
	 *            The agent as its new position
	 */
	public abstract void agentMoved(AbstractAgent agent, int x, int y, int z);

	/**
	 * If an agent was moved, the repository has to be updated by calling this
	 * method
	 * 
	 * @param agent
	 *            The agent as its new position
	 */
	public abstract void agentMoved(AbstractAgent agent, int x, int y);

	/**
	 * If an agent was moved, the repository has to be updated by calling this
	 * method
	 * 
	 * @param agent
	 *            The agent as its new position
	 */
	public abstract void agentMoved(AbstractAgent agent, int[] pos);

	/**
	 * Removes all elements from the repository that are in the remove queue.
	 */
	public abstract void executeRemove();

	/**
	 * Adds all Elements to the repository that are in the put queue.
	 */
	public abstract void executePut();

	public abstract Map<Integer, E> getPutQueue();

	/**
	 * Activates or deactivates all elements that are in the activation queue
	 */
	public abstract void executeActivation();

	/**
	 * Marks this entry as out of date by putting it to the remove queue. These
	 * entries will be removed from the list next time the executeRemovals()
	 * method is called.
	 */
	public abstract E remove(Object key);

	/**
	 * Adds the element to the put queue. The Element will be added to the
	 * repository after the executePut() method is called.
	 */
	public abstract E put(Integer key, E value);

	/**
	 * Adds the element to the put queue. The Element will be added to the
	 * repository after the executePut() method is called.
	 * 
	 * @param value
	 *            the agent
	 * @return
	 */
	public abstract E put(E value);

	/**
	 * Adds an element to the repository, that is immediately deactivated
	 * 
	 * @param key
	 *            the agent id
	 * @param value
	 *            the agent
	 * @return
	 */
	public abstract E putPassive(Integer key, E value);

	/**
	 * Adds an element to the repository, that is immediately deactivated
	 * 
	 * @param value
	 *            the agent
	 * @return
	 */
	public abstract E putPassive(E value);

	/**
	 * Adds all elements to the put queue. The Element will be added to the
	 * repository after the executePut() method is called.
	 */
	public abstract void putAll(Map<? extends Integer, ? extends E> m);

	/**
	 * <p>
	 * Puts a specified agent to the passive/active queue. The element will be
	 * activated after the executeActivation() method is called
	 * </p>
	 * 
	 * <p>
	 * Passive agents are not called by the scheduler and can not be found in
	 * the repository. Passive agents do not participate. And have no influence
	 * on other nodes. But are logged in the tracefile.
	 * </p>
	 * 
	 * @param agentId
	 * @param setActive
	 */
	public abstract boolean setActive(int agentId, boolean setActive);

	public abstract int getActiveAgentCount();

	public abstract int getPassiveAgentCount();

	public abstract Map<Integer, E> getPassiveAgents();

	/**
	 * Returns the amount of possible spatial agent subsets returned by the
	 * function <code>independentAgentCollectionsCount(..)</code>.
	 * 
	 * @return
	 */
	public abstract int maxSpatialIndependentAgentSubsets();

	/**
	 * Returns a spatial subset of all agents in this repository. The spatial
	 * independence ensures less agent interference when agents are handled by
	 * multiple threads.
	 * 
	 * The maximal amount of spatial independen agent subsets is given by the
	 * function <code>spatialIndependentAgentSubsetsAmount()</code>
	 * 
	 * The methods throws an {@link IndexOutOfBoundsException} if a subset is
	 * requested that does not exist (<code>index > subsetAmount</code>).
	 * 
	 * @param index
	 * @return
	 */
	public abstract Collection<E> spatialIndependentAgentSubset(int indes)
			throws IndexOutOfBoundsException;
}
