package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

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
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEventRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;

public class LinkedRTreeMapEventRepository extends
		AbstractMapEventRepository<LinkedRTreeMapEvent> implements
		SchedulerHookInterface {

	private static final int NEIGHBOR_RADIUS_TRESHOLD = 11;

	private Map<Integer, LinkedRTreeMapEvent> events;

	LinkedRTreeNode<LinkedRTreeMapEvent> rootNode;

	protected Set<Integer> removeQueue;
	protected Map<Integer, LinkedRTreeMapEvent> putQueue;

	private LinkedRTreeShapePool shapePool;

	public LinkedRTreeMapEventRepository(int sizeX, int sizeY, int capacity) {
		events = new HashMap<Integer, LinkedRTreeMapEvent>(capacity + 1, 1);
		init(sizeX, sizeY);
	}

	public LinkedRTreeMapEventRepository(int sizeX, int sizeY) {
		events = new HashMap<Integer, LinkedRTreeMapEvent>();
		init(sizeX, sizeY);
	}

	private void init(int sizeX, int sizeY) {
		shapePool = LinkedRTreeShapePool.getInstance_();
		rootNode = new LinkedRTreeNode<LinkedRTreeMapEvent>(new Rectangle(0, 0,
				sizeX, sizeY));
		putQueue = new HashMap<Integer, LinkedRTreeMapEvent>();
		removeQueue = new HashSet<Integer>();
		Scheduler.getInstance().register(this);
	}

	@Override
	public void clear() {
		events.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return events.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return events.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<Integer, LinkedRTreeMapEvent>> entrySet() {
		return events.entrySet();
	}

	@Override
	public LinkedRTreeMapEvent get(Object key) {
		return events.get(key);
	}

	@Override
	public boolean isEmpty() {
		return events.isEmpty();
	}

	@Override
	public Set<Integer> keySet() {
		return events.keySet();
	}

	@Override
	public LinkedRTreeMapEvent put(Integer key, LinkedRTreeMapEvent value) {
		synchronized (this) {
			return putQueue.put(key, value);
		}
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends LinkedRTreeMapEvent> m) {
		// events.putAll(m);
		Exception e = new Exception(
				"putAll() in StaticRTreeMapEvent not implemented yet");
		e.printStackTrace();
		System.exit(0);
	}

	@Override
	public LinkedRTreeMapEvent remove(Object key) {

		synchronized (this) {
			if (key instanceof Integer) {
				removeQueue.add((Integer) key);
				return events.get(key);
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
				rootNode.deleteData(events.get(key));

				// remove from agenetMap
				events.remove(key);
			}
			removeQueue.clear();
		}
	}

	@Override
	public void executePut() {
		for (Iterator<Integer> iterator = putQueue.keySet().iterator(); iterator
				.hasNext();) {
			Integer key = iterator.next();
			events.put(key, putQueue.get(key));
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
	public int size() {
		return events.size();

	}

	@Override
	public Collection<LinkedRTreeMapEvent> values() {
		return events.values();
	}

	@Override
	public Set<LinkedRTreeMapEvent> findIntersectingEvents(Object shape) {
		Set<LinkedRTreeMapEvent> container = new HashSet<LinkedRTreeMapEvent>();
		findIntersectingEvents(shape, container);
		return container;
	}

	@Override
	public Set<LinkedRTreeMapEvent> findIntersectingEvents(Object shape,
			FilterInterface filter) {
		Set<LinkedRTreeMapEvent> container = new HashSet<LinkedRTreeMapEvent>();
		findIntersectingEvents(shape, filter, container);
		return container;
	}

	@Override
	public void findIntersectingEvents(Object shape,
			Set<LinkedRTreeMapEvent> container) {
		if (events.size() > 0) {
			if (shape instanceof ShapeInterface) {
				rootNode.intersectionQuery((ShapeInterface) shape, container);
			} else
				throw new ClassCastException(
						"Shape has to implement the ShapeInterface.");
		}

	}

	@Override
	public void findIntersectingEvents(Object shape, FilterInterface filter,
			Set<LinkedRTreeMapEvent> container) {
		if (shape instanceof ShapeInterface) {
			rootNode.intersectionQuery((ShapeInterface) shape, container,
					filter);
		} else
			throw new ClassCastException(
					"Shape has to implement the ShapeInterface.");
	}

	@Override
	public Set<LinkedRTreeMapEvent> findNeighbors(LinkedRTreeMapEvent mapEvent,
			int radius) {
		Set<LinkedRTreeMapEvent> container = new HashSet<LinkedRTreeMapEvent>();
		findNeighbors(mapEvent, radius, container);
		return container;
	}

	@Override
	public void findNeighbors(LinkedRTreeMapEvent mapEvent, int radius,
			Set<LinkedRTreeMapEvent> container) {

		Circle c = getCircle(mapEvent.getX(), mapEvent.getY(), radius);
		// Circle c = new Circle(agent, radius);
		if (radius <= NEIGHBOR_RADIUS_TRESHOLD)
			findIntersectingEvents(c, container);
		else
			rootNode.intersectionQuery(c, container);
		container.remove(mapEvent);
		recycleShape(c);

	}

	@Override
	public void eventSizeChanged(LinkedRTreeMapEvent event) {
		event.unsettle();
		event.settle();
	}

	@Override
	public void eventMoved(LinkedRTreeMapEvent mapEvent, int x, int y, int z) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventMoved(LinkedRTreeMapEvent mapEvent, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventMoved(LinkedRTreeMapEvent AbstractMapEvent, int[] pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preRun() {
	}

	@Override
	public void preSimulation() {
	}

	@Override
	public void postSimulation() {
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
