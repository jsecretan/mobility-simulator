package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

import java.util.Collection;
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
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.NormalRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.TeamRescuerRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.VictimRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.WorkingMapPool;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;
import de.tu_darmstadt.kom.mobilitySimulator.mapEvent.CollapsedBuildingMapEvent;

/**
 * @author motte
 * 
 */
public class LinkedRTreeCollapsedBuildingMapEvent extends LinkedRTreeMapEvent
		implements CircleInterface, CollapsedBuildingMapEvent {

	protected Circle impactArea;
	protected Set<LinkedRTreeNode<LinkedRTreeLeafInterface>> intersectionNodes;
	protected LinkedRTreeNode<LinkedRTreeLeafInterface> owner;

	/**
	 * Decrease per worker per cycle
	 */
	protected int decreaseFunctionNormal, decreaseFunctionRescuer;

	protected int maxImpactValue;
	private float damageAreaRatio;
	private boolean active;

	// private Stack<AbstractAgent> victims;
	private int counter;
	private int releaseVictim;
	private int releaseStep;
	private int maxWorkerCapacity;
	private int specializedWorker;
	private int normalWorker;

	/**
	 * @param coverage
	 * @param damageAreaRatio
	 *            percentage of the impact area will become the damage area. For
	 *            <code>damageAreaRatio</code> > 1 the damage area is as large
	 *            as the impact area.
	 */
	public LinkedRTreeCollapsedBuildingMapEvent(Circle coverage,
			float damageAreaRatio) {
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
	public LinkedRTreeCollapsedBuildingMapEvent(PointInterface center, int r,
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
	public LinkedRTreeCollapsedBuildingMapEvent(int x, int y, int r,
			float damageAreaRatio) {
		super();
		this.impactArea = new Circle(x, y, r);
		init(damageAreaRatio);
	}

	private void init(float damageAreaRatio) {
		intersectionNodes = new HashSet<LinkedRTreeNode<LinkedRTreeLeafInterface>>();
		maxImpactValue = 255;
		eventPoints = 1296000;
		active = true;

		calculateImpactMap(true);

		decreaseFunctionNormal = 1;
		decreaseFunctionRescuer = 3;

		maxWorkerCapacity = 18;
		specializedWorker = 0;
		normalWorker = 0;

		releaseStep = (int) (eventPoints * 0.025);
		releaseVictim = eventPoints - releaseStep;

		Set<AbstractAgent> agents = new HashSet<AbstractAgent>();
		Scheduler.agentRepository.findIntersectingAgents(impactArea,
				FilterFactory.getFilter(FilterFactory.AGENT_ROLE_FILTER,
						"FleeRole"), agents);

		for (AbstractAgent agent : agents) {
			agent.setRole(new VictimRole(agent));
		}

		// victims = new Stack<AbstractAgent>();
		// for (int i = 0; i < 20; i++) {
		// AbstractAgent a = Scheduler.agentRepository.createAgent(
		// impactArea.getX(), impactArea.getY());
		// Scheduler.agentRepository.putPassive(a);
		// EarthquakeVictimRole v = new EarthquakeVictimRole(a);
		// a.setRole(v);
		// v.setLifepoints(v.getMaxLifepoints() * Scheduler.rand.nextInt(61)
		// / 100);
		// victims.add(a);
		// }

		counter = 0;

	}

	@Override
	public boolean registerWorker(AbstractAgent agent) {
		if (agent.getRole() instanceof TeamRescuerRole) {

			if (getWorkerCount() >= getMaxWorkerCapacity() && normalWorker > 0) {
				for (AbstractAgent worker : workers) {
					if (!(worker.getRole() instanceof TeamRescuerRole)) {
						worker.addMessage("LeaveEvent");
						break;
					}
				}
			} else if (getWorkerCount() >= getMaxWorkerCapacity()
					&& normalWorker <= 0) {
				return false;
			}
			specializedWorker++;
		} else {
			if (getWorkerCount() >= getMaxWorkerCapacity())
				return false;
			normalWorker++;
		}
		return super.registerWorker(agent);
	}

	@Override
	public boolean unregisterWorker(AbstractAgent agent) {
		if (agent.getRole() instanceof TeamRescuerRole) {
			specializedWorker--;
		} else {
			normalWorker--;
		}
		return super.unregisterWorker(agent);
	}

	@Override
	public void clock() {

		// Decrease eventPoints
		for (AbstractAgent worker : this.workers) {
			if (worker.getRole() instanceof NormalRole)
				eventPoints--;
			if (worker.getRole() instanceof TeamRescuerRole)
				eventPoints -= 3;
		}

		// Release victim
		if (eventPoints <= releaseVictim) {
			releaseVictim -= releaseStep;
			AbstractAgent a = Scheduler.agentRepository.createAgent(
					impactArea.getX(), impactArea.getY());
			EarthquakeVictimRole v = new EarthquakeVictimRole(a);
			a.setRole(v);
			Scheduler.agentRepository.put(a);
			EarthquakeVictimRole.addKnown(a);
			// AmbulanceRole.reportedVictims.add(victim);
		}

		if (eventPoints <= 0) {

			Scheduler.mapEventRepository.remove(this.getId());
			active = false;
		}

		// Decrease victim lifepoints
		// counter++;
		// if (counter > 60) {
		// counter = 0;
		// for (AbstractAgent victim : this.victims) {
		// ((EarthquakeVictimRole) victim.getRole()).decreaseLifepoints(1);
		// }
		// }

		if (eventPoints <= 0) {
			Scheduler.mapEventRepository.remove(this.getId());
			active = false;
		}

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
		return impactArea;
	}

	@Override
	public CircleInterface getWorkingArea() {
		return impactArea;
	}

	@Override
	public int[] getImpactAreaAsCoordinates() {
		return new int[] { impactArea.getX(), impactArea.getY(),
				impactArea.getRadius() };
	}

	@Override
	public int[] getDamageAreaAsCoordinates() {
		return new int[] { impactArea.getX(), impactArea.getY(),
				impactArea.getRadius() };
	}

	@Override
	public int[] getWorkingAreaAsCoordinates() {
		return new int[] { impactArea.getX(), impactArea.getY(),
				impactArea.getRadius() };
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

	@Override
	public int getWorkerCount() {
		return workers.size();
	}

	@Override
	public Collection<AbstractAgent> workers() {
		return workers();
	}

	@Override
	public int getMaxWorkerCapacity() {
		return maxWorkerCapacity;
	}

	@Override
	public int getNormalWorkerCount() {
		return normalWorker;
	}

	@Override
	public int getSpecializedWorkerCount() {
		return specializedWorker;
	}
}
