package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.kom.linkedRTree.Circle;
import de.tu_darmstadt.kom.linkedRTree.CircleInterface;
import de.tu_darmstadt.kom.linkedRTree.LinkedRTreeLeafInterface;
import de.tu_darmstadt.kom.linkedRTree.LinkedRTreeNode;
import de.tu_darmstadt.kom.linkedRTree.PointInterface;
import de.tu_darmstadt.kom.linkedRTree.RectangleInterface;
import de.tu_darmstadt.kom.linkedRTree.ShapeInterface;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.EarthquakeVictimRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.WorkingMapPool;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;
import de.tu_darmstadt.kom.mobilitySimulator.mapEvent.FireMapEvent;

/**
 * @author motte
 * 
 */
public class LinkedRTreeFireMapEvent extends LinkedRTreeMapEvent implements
		CircleInterface, FireMapEvent {

	protected Circle impactArea;
	protected Circle damageArea;
	protected Set<LinkedRTreeNode<LinkedRTreeLeafInterface>> intersectionNodes;
	protected LinkedRTreeNode<LinkedRTreeLeafInterface> owner;

	/**
	 * Decrease per worker per cycle
	 */
	protected int decreaseFunction;

	/**
	 * Increase per cycle
	 */
	protected int increaseFunction;

	protected int maxImpactValue;
	private int decayRate;
	private float damageAreaRatio;
	private boolean active;
	private int counter;

	/**
	 * @param coverage
	 * @param damageAreaRatio
	 *            percentage of the impact area will become the damage area. For
	 *            <code>damageAreaRatio</code> > 1 the damage area is as large
	 *            as the impact area.
	 */
	public LinkedRTreeFireMapEvent(Circle coverage, float damageAreaRatio) {
		super();
		this.impactArea = coverage;
		init(damageAreaRatio);
	}

	/**
	 * @param center
	 * @param r
	 * @param damageAreaRatio
	 *            percentage of the impact area will become the damage area. For
	 *            <code>damageAreaRatio</code> > 1 the damage area is as large
	 *            as the impact area.
	 */
	public LinkedRTreeFireMapEvent(PointInterface center, int r,
			float damageAreaRatio) {
		super();
		this.impactArea = new Circle(center, r);
		init(damageAreaRatio);
	}

	/**
	 * @param x
	 * @param y
	 * @param r
	 * @param damageAreaRatio
	 *            percentage of the impact area will become the damage area. For
	 *            <code>damageAreaRatio</code> > 1 the damage area is as large
	 *            as the impact area.
	 */
	public LinkedRTreeFireMapEvent(int x, int y, int r, float damageAreaRatio) {
		super();
		this.impactArea = new Circle(x, y, r);
		init(damageAreaRatio);
	}

	private void init(float damageAreaRatio) {
		intersectionNodes = new HashSet<LinkedRTreeNode<LinkedRTreeLeafInterface>>();
		maxImpactValue = 255;
		eventPoints = 10800 * 2;
		decayRate = (int) (eventPoints / impactArea.getRadius());
		active = true;

		calculateImpactMap(true);

		decreaseFunction = 1;
		increaseFunction = 6;

		counter = 0;

		// Calculate the damage area:
		// damageArea[shape] = impactArea[shape] * damageAreaRatio[%]
		this.damageAreaRatio = damageAreaRatio;
		if (this.damageAreaRatio > 1)
			this.damageAreaRatio = 1;
		if (this.damageAreaRatio < 0)
			this.damageAreaRatio = 0;
		this.damageArea = new Circle(impactArea.getCenter(),
				(int) (Math.sqrt(Math.pow(impactArea.getRadius(), 2)
						* this.damageAreaRatio)));

		Set<AbstractAgent> agents = new HashSet<AbstractAgent>();

		Scheduler.agentRepository.findIntersectingAgents(damageArea,
				FilterFactory.getFilter(FilterFactory.AGENT_ROLE_FILTER,
						"NormalRole"), agents);

		for (AbstractAgent agent : agents) {
			agent.setRole(new EarthquakeVictimRole(agent));
//			EarthquakeVictimRole.addKnown(agent);
		}

	}

	@Override
	public void clock() {

		if (workers.size() > 0) {
			int s = 0;
		}

		counter += increaseFunction - (workers.size() * decreaseFunction);

		eventPoints = eventPoints + increaseFunction
				- (workers.size() * decreaseFunction);

		if (eventPoints <= 0) {

			Scheduler.mapEventRepository.remove(this.getId());
			active = false;
		}

		boolean change = false;
		boolean increase = true;
		if (counter > 1800) {
			impactArea.setRadius(impactArea.getRadius() + 1);
			damageArea.setRadius(damageArea.getRadius() + 1);
			counter = 0;
			change = true;
			increase = true;
		}
		if (counter < -1800) {
			impactArea.setRadius(impactArea.getRadius() - 1);
			damageArea.setRadius(damageArea.getRadius() - 1);
			counter = 0;
			change = true;
			increase = false;
		}

		if (change) {

			if (increase)
				calculateImpactMap(false);
			else
				calculateImpactMap(true);

			Scheduler.mapEventRepository.eventSizeChanged(this);
		}

		// int newRadius = (int) (eventPoints / decayRate);
		// if (newRadius == impactArea.getRadius())
		// return;
		// else {
		// boolean increase = newRadius < impactArea.getRadius();
		// impactArea.setRadius(newRadius);
		// damageArea.setRadius((int) (Math.sqrt(Math.pow(newRadius, 2)
		// * this.damageAreaRatio)));
		//
		// if (increase)
		// calculateImpactMap(false);
		// else
		// calculateImpactMap(true);
		//
		// Scheduler.mapEventRepository.eventSizeChanged(this);
		// }
	}

	@Override
	protected void calculateImpactMap() {
		calculateImpactMap(true);
	}

	protected void calculateImpactMap(boolean increase) {
		if (!increase) {
			WorkingMapPool.getInstance().clearMap(impactMap);
		}

		RectangleInterface boundingBox = impactArea.getBoundingBox();

		int maxY = boundingBox.getTop() + boundingBox.getHeight() <= DiscreteMap.sizeY ? boundingBox
				.getTop() + boundingBox.getHeight()
				: DiscreteMap.sizeY;
		int maxX = boundingBox.getLeft() + boundingBox.getWidth() <= DiscreteMap.sizeX ? boundingBox
				.getLeft() + boundingBox.getWidth()
				: DiscreteMap.sizeX;

		for (int y = boundingBox.getTop() < 0 ? 0 : boundingBox.getTop(); y < maxY; y++) {
			for (int x = boundingBox.getLeft() < 0 ? 0 : boundingBox.getLeft(); x < maxX; x++) {
				int abs = (int) Math
						.sqrt((Math.pow(impactArea.getX() - x, 2) + Math.pow(
								impactArea.getY() - y, 2)));
				if (abs < impactArea.getRadius())
					impactMap[y][x] = (int) (((impactArea.getRadius() - abs) / (float) impactArea
							.getRadius()) * 255);
				// impactMap[y][x] = 255;
				else
					impactMap[y][x] = 0;

				// if (impactMap[y][x] < 10)
				// System.out.print("0");
				// if (impactMap[y][x] < 100)
				// System.out.print("0");
				// System.out.print(impactMap[y][x] + " ");
			}
			// System.out.println("");
		}
	}

	@Override
	public int getSizeIndicator() {
		return impactArea.getRadius();
	}

	@Override
	public RectangleInterface getBoundingBox() {
		return impactArea.getBoundingBox();
	}

	@Override
	public int getRadius() {
		return impactArea.getRadius();
	}

	@Override
	public int getX() {
		return impactArea.getX();
	}

	@Override
	public int getY() {
		return impactArea.getY();
	}

	@Override
	public int getArea() {
		return impactArea.getArea();
	}

	@Override
	public CircleInterface getImpactArea() {
		return impactArea;
	}

	@Override
	public CircleInterface getDamageArea() {
		// TODO Auto-generated method stub
		return damageArea;
	}

	@Override
	public CircleInterface getWorkingArea() {
		return damageArea;
	}

	@Override
	public int[] getImpactAreaAsCoordinates() {
		return new int[] { impactArea.getX(), impactArea.getY(),
				impactArea.getRadius() };
	}

	@Override
	public int[] getDamageAreaAsCoordinates() {
		return new int[] { damageArea.getX(), damageArea.getY(),
				damageArea.getRadius() };
	}

	@Override
	public int[] getWorkingAreaAsCoordinates() {
		return new int[] { damageArea.getX(), damageArea.getY(),
				damageArea.getRadius() };
	}

	@Override
	public void setRadius(int r) {
		impactArea.setRadius(r);
	}

	@Override
	public boolean contains(ShapeInterface shape) {
		return impactArea.contains(shape);
	}

	@Override
	public PointInterface getCenter() {
		return impactArea.getCenter();
	}

	@Override
	public boolean intersects(ShapeInterface shape) {
		return impactArea.intersects(shape);
	}

	@Override
	public void setCenter(PointInterface p) {
		impactArea.setCenter(p);
	}

	@Override
	public void setCenter(int x, int y) {
		impactArea.setCenter(x, y);
	}

	@Override
	public void setCenter(int x, int y, int z) {
		impactArea.setCenter(x, y, z);
	}

	@Override
	public void move(PointInterface p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void move(int x, int y) {
		// TODO Auto-generated method stub
	}

	@Override
	public void move(int x, int y, int z) {
		// TODO Auto-generated method stub

	}

	@Override
	public void move(int[] pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOwner(LinkedRTreeNode<LinkedRTreeLeafInterface> owner) {
		this.owner = owner;
	}

	@Override
	public LinkedRTreeNode<LinkedRTreeLeafInterface> getOwner() {
		return owner;
	}

	@Override
	public void settle() {
		if (owner != null) {
			intersectionNodes.clear();
			owner.intersectingNodesQuery(this, intersectionNodes);

			for (LinkedRTreeNode<LinkedRTreeLeafInterface> node : intersectionNodes) {
				node.registerIntersectionData(this);
			}

		}

	}

	@Override
	public void unsettle() {
		for (LinkedRTreeNode<LinkedRTreeLeafInterface> node : intersectionNodes) {
			node.unRegisterIntersectionData(this);
		}

	}

	@Override
	public String toString() {
		return "MapEvent  (" + impactArea.getX() + "," + impactArea.getY()
				+ ", R=" + impactArea.getRadius() + ")";
	}

	@Override
	public boolean isActive() {
		return active;
	}
}
