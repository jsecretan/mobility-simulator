package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.tu_darmstadt.kom.linkedRTree.Circle;
import de.tu_darmstadt.kom.linkedRTree.FilterInterface;
import de.tu_darmstadt.kom.linkedRTree.LinkedRTreeNode;
import de.tu_darmstadt.kom.linkedRTree.Point;
import de.tu_darmstadt.kom.linkedRTree.Rectangle;
import de.tu_darmstadt.kom.linkedRTree.ShapeInterface;
import de.tu_darmstadt.kom.linkedRTree.Exception.PositionOutOfScope;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;

/**
 * <p>
 * This AgentRepository works on a modified R*-Tree by Mathieu Münch.
 * </p>
 * <p>
 * It is Thread safe.
 * </p>
 * 
 * 
 * @author Mathieu Münch
 * 
 */
public class LinkedRTreeAgentRepository extends
		AbstractAgentRepository<LinkedRTreeAgent> implements
		SchedulerHookInterface {

	private static final int NEIGHBOR_RADIUS_TRESHOLD = 11;
	protected Map<Integer, LinkedRTreeAgent> activeAgentMap;
	protected Map<Integer, LinkedRTreeAgent> passiveAgentMap;
	protected LinkedRTreeNode<LinkedRTreeAgent> rootNode;

	protected Set<Integer> removeQueue;
	protected Map<Integer, LinkedRTreeAgent> putQueue;

	protected Map<Integer, Boolean> activePassiveQueue;

	protected Rectangle neighborCheckRectangle;
	private LinkedRTreeShapePool shapePool;
	private boolean eneableMobile;

	public LinkedRTreeAgentRepository(int sizeX, int sizeY,
			boolean eneableMobile) {
		this.eneableMobile = eneableMobile;
		activeAgentMap = new HashMap<Integer, LinkedRTreeAgent>();

		init(sizeX, sizeY);
	}

	public LinkedRTreeAgentRepository(int sizeX, int sizeY, int capacity,
			boolean eneableMobile) {
		this.eneableMobile = eneableMobile;
		activeAgentMap = new HashMap<Integer, LinkedRTreeAgent>(capacity + 1, 1);

		init(sizeX, sizeY);
	}

	private void init(int sizeX, int sizeY) {
		shapePool = LinkedRTreeShapePool.getInstance_();
		passiveAgentMap = new HashMap<Integer, LinkedRTreeAgent>();
		rootNode = new LinkedRTreeNode<LinkedRTreeAgent>(new Rectangle(0, 0,
				sizeX, sizeY));
		removeQueue = new HashSet<Integer>();
		putQueue = new HashMap<Integer, LinkedRTreeAgent>();
		activePassiveQueue = new HashMap<Integer, Boolean>();
		Scheduler.getInstance().register(this);
		neighborCheckRectangle = new Rectangle(0, 0, 1, 1);
	}

	@Override
	public AbstractAgent createAgent(int x, int y) {
		return new LinkedRTreeAgent(x, y, eneableMobile);
	}

	@Override
	public AbstractAgent createAgent(int x, int y, boolean eneableMobile) {
		return new LinkedRTreeAgent(x, y, eneableMobile);
	}

	@Override
	public Set<LinkedRTreeAgent> findIntersectingAgents(Object shape) {
		Set<LinkedRTreeAgent> container = new HashSet<LinkedRTreeAgent>();
		findIntersectingAgents(shape, container);
		return container;
	}

	public Set<LinkedRTreeAgent> findIntersectingAgents(Object shape,
			FilterInterface filter) {

		Set<LinkedRTreeAgent> container = new HashSet<LinkedRTreeAgent>();
		findIntersectingAgents(shape, filter, container);
		return container;

	}

	@Override
	public AbstractAgent findAgentClosestToDistance(LinkedRTreeAgent agent, double radius, Set<LinkedRTreeAgent> filterSet) {
		
		AbstractAgent closestAgent = null;

		double minDistanceDifference = Double.MAX_VALUE;

		for(LinkedRTreeAgent ag : values()) {
			if(!filterSet.contains(ag)) {
				double distanceDifference = Math.sqrt(Math.pow(agent.getX()-ag.getX(),2)+Math.pow(agent.getY()-ag.getY(),2));
				if(distanceDifference < minDistanceDifference) {
					minDistanceDifference = distanceDifference;
					closestAgent = ag;
				}
			}
		}

		return closestAgent;
	}

	@Override
	public void findIntersectingAgents(Object shape,
			Set<LinkedRTreeAgent> container) {
		if (shape instanceof ShapeInterface) {
			rootNode.intersectionQuery((ShapeInterface) shape, container);
		} else
			throw new ClassCastException(
					"Shape has to implement the ShapeInterface.");
	}

	@Override
	public void findIntersectingAgents(Object shape, FilterInterface filter,
			Set<LinkedRTreeAgent> container) {
		if (shape instanceof ShapeInterface) {
			rootNode.intersectionQuery((ShapeInterface) shape, container,
					filter);
		} else
			throw new ClassCastException(
					"Shape has to implement the ShapeInterface.");
	}

	@Override
	public Set<LinkedRTreeAgent> findNeighbors(LinkedRTreeAgent agent,
			int radius) {
		Set<LinkedRTreeAgent> container = new HashSet<LinkedRTreeAgent>();
		findNeighbors(agent, radius, container);
		return container;
	}

	@Override
	public void findNeighbors(LinkedRTreeAgent agent, int radius,
			Set<LinkedRTreeAgent> container) {
		Circle c = getCircle(agent.getX(), agent.getY(), radius);
		// Circle c = new Circle(agent, radius);
		if (radius <= NEIGHBOR_RADIUS_TRESHOLD)
			findIntersectingAgents(c, container);
		else
			rootNode.intersectionQuery(c, container);
		container.remove(agent);
		recycleShape(c);
	}

	@Override
	public void agentMoved(AbstractAgent agent, int x, int y) {
		if (agent instanceof LinkedRTreeAgent)
			((LinkedRTreeAgent) agent).move(x, y);
	}

	@Override
	public void agentMoved(AbstractAgent agent, int x, int y, int z) {
		if (agent instanceof LinkedRTreeAgent)
			((LinkedRTreeAgent) agent).move(x, y);
	}

	@Override
	public void agentMoved(AbstractAgent agent, int[] pos) {
		if (agent instanceof LinkedRTreeAgent)
			((LinkedRTreeAgent) agent).move(pos);
	}

	@Override
	public void clear() {
		activeAgentMap.clear();
	}

	@Override
	public Map<Integer, LinkedRTreeAgent> getPassiveAgents() {
		return passiveAgentMap;
	}

	private void getChildData(int child, Collection<LinkedRTreeAgent> container) {
		if (rootNode.isEndNode())
			return;
		rootNode.getChild(true, LinkedRTreeNode.DIRECT_DATA, container, child);
	}

	@Override
	public boolean containsKey(Object key) {
		return activeAgentMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return activeAgentMap.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<Integer, LinkedRTreeAgent>> entrySet() {
		return activeAgentMap.entrySet();
	}

	@Override
	public LinkedRTreeAgent get(Object key) {
		return activeAgentMap.get(key);
	}

	@Override
	public boolean isEmpty() {
		return activeAgentMap.isEmpty();
	}

	@Override
	public Set<Integer> keySet() {
		return activeAgentMap.keySet();
	}

	@Override
	public LinkedRTreeAgent put(Integer key, LinkedRTreeAgent agent) {
		synchronized (this) {
			return putQueue.put(key, agent);
		}
	}

	@Override
	public LinkedRTreeAgent put(LinkedRTreeAgent agent) {
		synchronized (this) {
			return putQueue.put(agent.getId(), agent);
		}
	}

	@Override
	public LinkedRTreeAgent putPassive(Integer key, LinkedRTreeAgent value) {
		synchronized (this) {
			return passiveAgentMap.put(key, value);
		}
	}

	@Override
	public LinkedRTreeAgent putPassive(LinkedRTreeAgent value) {
		synchronized (this) {
			Scheduler.getInstance().getOutput()
					.agentActivated(value.getId(), false);
			return passiveAgentMap.put(value.getId(), value);
		}

	}

	@Override
	public void putAll(Map<? extends Integer, ? extends LinkedRTreeAgent> m) {
		// TODO Implement

		// boolean castProblem = false;
		//
		// Collection<? extends AbstractAgent> agents = m.values();
		//
		// for (AbstractAgent agent : agents) {
		// if (!(agent instanceof SpatialindexAgent)) {
		// castProblem = true;
		// }
		// }
		// if (castProblem) {
		// throw new ClassCastException(
		// "Agents must be an instance of SpatialindexAgent.\n Make sure to use SpatialindexAgents and IShape-regions with the SpatialindexAgentRepository");
		// }
		// for (Entry<? extends Integer, ? extends AbstractAgent> entry : m
		// .entrySet()) {
		// agentMap.put(entry.getKey(), entry.getValue());
		// }

		Exception e = new Exception(
				"putAll() in StaticRTreeAgent not implemented yet");
		e.printStackTrace();
		System.exit(0);

	}

	@Override
	public LinkedRTreeAgent remove(Object key) {
		synchronized (this) {
			if (key instanceof Integer) {
				removeQueue.add((Integer) key);
				return activeAgentMap.get(key);
			}
			return null;
		}
	}

	@Override
	public void executeRemove() {
		synchronized (this) {
			for (Iterator<Integer> iterator = removeQueue.iterator(); iterator
					.hasNext();) {
				Integer key = iterator.next();

				// Remove from Tree
				rootNode.deleteData(activeAgentMap.get(key));

				// remove from agenetMap
				activeAgentMap.remove(key);
			}
			removeQueue.clear();
		}
	}

	@Override
	public void executePut() {
		for (Iterator<Integer> iterator = putQueue.keySet().iterator(); iterator
				.hasNext();) {
			Integer key = iterator.next();
			activeAgentMap.put(key, putQueue.get(key));
			try {
				rootNode.addData(putQueue.get(key));
			} catch (PositionOutOfScope e) {
				// TODO Implement better catching!
				e.printStackTrace();
			}
		}
		putQueue.clear();
	}

	@Override
	public void executeActivation() {
		synchronized (this) {
			for (Integer agentID : activePassiveQueue.keySet()) {
				if (activePassiveQueue.get(agentID)) {
					// activate
					if (passiveAgentMap.containsKey(agentID)) {
						try {
							rootNode.addData(activeAgentMap.put(agentID,
									passiveAgentMap.remove(agentID)));
						} catch (PositionOutOfScope e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				} else {
					if (activeAgentMap.containsKey(agentID)) {
						rootNode.deleteData(passiveAgentMap.put(agentID,
								activeAgentMap.remove(agentID)));
					}
				}
			}
		}

	}

	@Override
	public int size() {
		return activeAgentMap.size();
	}

	@Override
	public Collection<LinkedRTreeAgent> values() {
		return activeAgentMap.values();
	}

	@Override
	public boolean setActive(int agentID, boolean setActive) {
		synchronized (this) {
			Scheduler.getInstance().getOutput()
					.agentActivated(agentID, setActive);
			if (setActive) {
				if (passiveAgentMap.containsKey(agentID)) {
					put(agentID, passiveAgentMap.remove(agentID));
					return true;
				}
			} else {
				if (activeAgentMap.containsKey(agentID)) {
					passiveAgentMap.put(agentID, activeAgentMap.get(agentID));
					remove(agentID);
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public int getActiveAgentCount() {
		return activeAgentMap.size();
	}

	@Override
	public int getPassiveAgentCount() {
		return passiveAgentMap.size();
	}

	@Override
	public Map<Integer, LinkedRTreeAgent> getPutQueue() {
		return putQueue;
	}

	@Override
	public int maxSpatialIndependentAgentSubsets() {
		return rootNode.isEndNode() ? 0 : 4;
	}

	@Override
	public Collection<LinkedRTreeAgent> spatialIndependentAgentSubset(int index)
			throws IndexOutOfBoundsException {
		ArrayList<LinkedRTreeAgent> retCol = new ArrayList<LinkedRTreeAgent>();
		getChildData(index, retCol);
		return retCol;
	}

	@Override
	public void preRun() {
	}

	@Override
	public void preSimulation() {
	}

	@Override
	public void postSimulation() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preCycle() {
		executeRemove();
		executePut();
	}

	@Override
	public void postCycle() {
	}

	@Override
	public Point getPoint(int x, int y) {
		return shapePool.getPoint(x, y);
	}

	@Override
	public Circle getCircle(int x, int y, int radius) {
		return shapePool.getCircle(x, y, radius);
	}

	@Override
	public Rectangle getRectangle(int x, int y, int x2, int y2) {
		return shapePool.getRectangle(x, y, x2, y2);
	}

	@Override
	public Object getPolygon(int[] cornerPoints) {
		return shapePool.getPolygon(cornerPoints);
	}

	@Override
	public void recycleShape(Object shape) {
		shapePool.recycleShape(shape);
	}
}
