package de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.NeighborPosition;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.WorkingMapPool;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;

public class WalkToOnGradientMobilityModel extends AbstractMobilityModel {

	public static boolean enableStatistics = false;
	public static int movingAgents = 0;
	public static Map<int[], Integer> destinations = new HashMap<int[], Integer>();
	private int[] lastDest;

	public static final boolean USE_DEVISION = true;
	private static final int DIVISIONS = 5;
	private static int divisionSizeX, divisionSizeY;
	private boolean towardsDivision;
	private int divisionX, divisionY;
	private int[] divisionDest;

	private float movemnetMod;
	private float movementModCount;
	private int stepsPerCycle;

	protected boolean go;

	protected int dest[];

	protected int includeEventWithId;

	protected short[][] obstaclesMap;

	protected Set<AbstractAgent> neighborSet;
	private int lockCounter;

	public WalkToOnGradientMobilityModel(AbstractAgent agent) {
		super(agent);
		init();
	}

	private void init() throws InstantiationError {
		go = false;
		obstaclesMap = DiscreteMap.getInstance().getObstacles();
		neighborSet = new HashSet<AbstractAgent>();
		includeEventWithId = 0;
		towardsDivision = false;
		if (divisionSizeX == 0)
			divisionSizeX = DiscreteMap.sizeX / DIVISIONS;
		if (divisionSizeY == 0)
			divisionSizeY = DiscreteMap.sizeY / DIVISIONS;
		lockCounter = 0;
		lastDest = null;
		velocityChanged();

		movementModCount = 0;
	}

	private void setDivision() {
		divisionX = getDivision(dest[0], true);
		divisionY = getDivision(dest[1], false);
		if (divisionX != getDivision(agent.getX(), true)
				|| divisionY != getDivision(agent.getY(), false)) {
			divisionDest = new int[4];
			divisionDest[0] = divisionX * divisionSizeX;
			divisionDest[1] = divisionY * divisionSizeY;
			divisionDest[2] = divisionX + 1 == DIVISIONS ? DiscreteMap.sizeX
					: ((divisionX + 1) * divisionSizeX);
			divisionDest[3] = divisionY + 1 == DIVISIONS ? DiscreteMap.sizeY
					: ((divisionY + 1) * divisionSizeY);
			towardsDivision = true;
		} else {
			towardsDivision = false;
		}
	}

	private int getDivision(int val, boolean x) {
		int div;
		if (x) {
			div = val / divisionSizeX;
			if (divisionX >= DIVISIONS)
				divisionSizeX = DIVISIONS - 1;
		} else {
			div = val / divisionSizeY;
			if (divisionY >= DIVISIONS)
				divisionSizeY = DIVISIONS - 1;
		}
		return div;
	}

	/**
	 * Sets a destination point and starts the movement
	 * 
	 * @param x
	 * @param y
	 */
	public void setDestination(int x, int y) {
		dest = new int[] { x, y };
		if (USE_DEVISION)
			setDivision();
		go = true;

		if (enableStatistics)
			doStatistics();
	}

	/**
	 * Sets a destination region (circle) and starts the movement
	 * 
	 * @param x
	 * @param y
	 */
	public void setDestination(int x, int y, int r) {
		dest = new int[] { x, y, r };
		go = true;
		if (enableStatistics)
			doStatistics();
	}

	/**
	 * Sets a destination region (rectangle) and starts the movement
	 * 
	 * @param x
	 * @param y
	 */
	public void setDestination(int x1, int y1, int x2, int y2) {
		dest = new int[] { x1, y1, x2, y2 };
		go = true;
		if (enableStatistics)
			doStatistics();
	}

	/**
	 * Sets a destination (point or region) and starts the movement
	 * 
	 * @param dest
	 */
	public void setDestination(int dest[]) {
		if (dest.length < 1 && dest.length > 4) {
			Exception e = new Exception(
					"Unsupported amount of destination parameter ("
							+ dest.length + " parameters).");
			e.printStackTrace();
			System.exit(0);
		}
		this.dest = dest;
		if (dest.length == 2 && USE_DEVISION)
			setDivision();
		go = true;
		if (WalkToOnGradientMobilityModel.enableStatistics)
			doStatistics();
	}

	@Override
	public void velocityChanged() {
		movemnetMod = agent.getVelocity()
				% Scheduler.getInstance().getSecondsPerCycle();
		stepsPerCycle = (int) (agent.getVelocity() - movemnetMod)
				/ Scheduler.getInstance().getSecondsPerCycle();
	}

	private void doStatistics() {
		if (lastDest != null) {
			Integer i = null;

			for (Entry<int[], Integer> entry : destinations.entrySet()) {
				if (Arrays.equals(lastDest, entry.getKey())) {
					i = entry.getValue();
					lastDest = entry.getKey();
					break;
				}
			}

			if (i != null && i > 1)
				destinations.put(lastDest, i - 1);
			if (i != null && i <= 1)
				destinations.remove(lastDest);
			movingAgents--;
			lastDest = null;
		}

		if (go) {
			Integer i = null;
			if (USE_DEVISION && towardsDivision) {

				for (Entry<int[], Integer> entry : destinations.entrySet()) {
					if (Arrays.equals(divisionDest, entry.getKey())) {
						i = entry.getValue();
						divisionDest = entry.getKey();
						break;
					}
				}

				destinations.put(divisionDest, (i != null ? (i + 1) : 1));
				lastDest = divisionDest;
			} else {
				for (Entry<int[], Integer> entry : destinations.entrySet()) {
					if (Arrays.equals(dest, entry.getKey())) {
						i = entry.getValue();
						dest = entry.getKey();
						break;
					}
				}
				destinations.put(dest, (i != null ? (i + 1) : 1));
				lastDest = dest;
			}
			movingAgents++;
		}
	}

	@Override
	public String getName() {
		return "WalkToOnGradientMobilityModel";
	}

	@Override
	public void move() {

		int extraSteps = 0;
		movementModCount += movemnetMod;
		if (movementModCount >= 1) {
			movementModCount -= 1;
			extraSteps = 1;
		}

		int totalSteps = stepsPerCycle + extraSteps;

		for (int i = 0; i < totalSteps; i++) {

			if (go) {

				int[] dest = USE_DEVISION && towardsDivision ? divisionDest
						: this.dest;
				// If agent did not reach its destination
				if (!isAtDestination(dest)) {

					// Get potential map
					int[][] overlayMap;
					if (includeEventWithId == 0)
						overlayMap = MapFactory.getInstance().getMap(dest,
								agent.getMaxUp(), agent.getMaxDown(), false);
					else
						overlayMap = MapFactory.getInstance().getMap(dest,
								agent.getMaxUp(), agent.getMaxDown(), true);

					// Get neighbor nodes
					neighborSet.clear();
					Scheduler.agentRepository.findNeighbors(agent,
							totalSteps + 1, neighborSet);
					int[][] workMap = WorkingMapPool.getInstance()
							.getWorkingMap(true);
					try {
						for (AbstractAgent agent : neighborSet) {
							workMap[agent.getY()][agent.getX()] += agent
									.getResistance();
						}
					} catch (NullPointerException e) {
						return;
					}

					if (includeEventWithId != 0) {
						int[][] eventMap = ((AbstractMapEvent) Scheduler.mapEventRepository
								.get(includeEventWithId)).getImpactMap();

						// Add eventMapValue to working Map
						/*
						 * Only loop through values around the agent and discard
						 * values that are farer away then 20 cells for better
						 * performance
						 */
						int x = agent.getX() - 20 >= 0 ? agent.getX() - 20 : 0;
						int y = agent.getY() - 20 >= 0 ? agent.getY() - 20 : 0;
						int x_max = agent.getX() + 20 < DiscreteMap.sizeX ? agent
								.getX() + 20 : DiscreteMap.sizeX;
						int y_max = agent.getY() + 20 < DiscreteMap.sizeY ? agent
								.getY() + 20 : DiscreteMap.sizeY;
						for (; y < y_max; y++) {
							for (; x < x_max; x++) {
								workMap[y][x] += eventMap[y][x];
							}
						}
					}

					NeighborPosition choosenNeighbor = null;
					int choosenNeighborValue = Short.MAX_VALUE;
					int[] neighborPos, choosenNeighborPos = null;

					int difference;

					neighborPos = DiscreteMap.neighbourPos(agent.getX(),
							agent.getY(), NeighborPosition.TOP);
					if (neighborPos != null) {
						difference = obstaclesMap[neighborPos[1]][neighborPos[0]]
								- obstaclesMap[agent.getY()][agent.getX()];
						if (difference <= agent.getMaxUp()
								&& difference >= -1 * agent.getMaxDown()
								&& overlayMap[neighborPos[1]][neighborPos[0]]
										+ workMap[neighborPos[1]][neighborPos[0]] < choosenNeighborValue) {
							choosenNeighborValue = overlayMap[neighborPos[1]][neighborPos[0]]
									+ workMap[neighborPos[1]][neighborPos[0]];
							choosenNeighbor = NeighborPosition.TOP;
							choosenNeighborPos = neighborPos;
						}
					}

					neighborPos = DiscreteMap.neighbourPos(agent.getX(),
							agent.getY(), NeighborPosition.RIGHT);
					if (neighborPos != null) {
						difference = obstaclesMap[neighborPos[1]][neighborPos[0]]
								- obstaclesMap[agent.getY()][agent.getX()];
						if (difference <= agent.getMaxUp()
								&& difference >= -1 * agent.getMaxDown()
								&& ((overlayMap[neighborPos[1]][neighborPos[0]])
										+ workMap[neighborPos[1]][neighborPos[0]] < choosenNeighborValue || (overlayMap[neighborPos[1]][neighborPos[0]]
										+ workMap[neighborPos[1]][neighborPos[0]] == choosenNeighborValue && Scheduler.rand
											.nextBoolean()))) {
							choosenNeighborValue = overlayMap[neighborPos[1]][neighborPos[0]]
									+ workMap[neighborPos[1]][neighborPos[0]];
							choosenNeighbor = NeighborPosition.RIGHT;
							choosenNeighborPos = neighborPos;
						}
					}

					neighborPos = DiscreteMap.neighbourPos(agent.getX(),
							agent.getY(), NeighborPosition.BOTTOM);
					if (neighborPos != null) {
						difference = obstaclesMap[neighborPos[1]][neighborPos[0]]
								- obstaclesMap[agent.getY()][agent.getX()];
						if (difference <= agent.getMaxUp()
								&& difference >= -1 * agent.getMaxDown()
								&& ((overlayMap[neighborPos[1]][neighborPos[0]])
										+ workMap[neighborPos[1]][neighborPos[0]] < choosenNeighborValue || (overlayMap[neighborPos[1]][neighborPos[0]]
										+ workMap[neighborPos[1]][neighborPos[0]] == choosenNeighborValue && Scheduler.rand
											.nextBoolean()))) {
							choosenNeighborValue = overlayMap[neighborPos[1]][neighborPos[0]]
									+ workMap[neighborPos[1]][neighborPos[0]];
							choosenNeighbor = NeighborPosition.BOTTOM;
							choosenNeighborPos = neighborPos;
						}
					}

					neighborPos = DiscreteMap.neighbourPos(agent.getX(),
							agent.getY(), NeighborPosition.LEFT);

					if (neighborPos != null) {
						difference = obstaclesMap[neighborPos[1]][neighborPos[0]]
								- obstaclesMap[agent.getY()][agent.getX()];
						if (difference <= agent.getMaxUp()
								&& difference >= -1 * agent.getMaxDown()
								&& ((overlayMap[neighborPos[1]][neighborPos[0]])
										+ workMap[neighborPos[1]][neighborPos[0]] < choosenNeighborValue || (overlayMap[neighborPos[1]][neighborPos[0]]
										+ workMap[neighborPos[1]][neighborPos[0]] == choosenNeighborValue && Scheduler.rand
											.nextBoolean()))) {
							choosenNeighborValue = overlayMap[neighborPos[1]][neighborPos[0]]
									+ workMap[neighborPos[1]][neighborPos[0]];
							choosenNeighbor = NeighborPosition.LEFT;
							choosenNeighborPos = neighborPos;
						}
					}

					WorkingMapPool.getInstance().recicleWorkingMap(workMap);

					/*
					 * TODO Reconsider where to put the move call. Where to set
					 * the position of an agent and where to trigger refreshing
					 * of rTree
					 */
					if (choosenNeighbor != null)
						Scheduler.agentRepository.agentMoved(agent,
								choosenNeighborPos);

				} else {
					if (USE_DEVISION && towardsDivision) {
						// If division reached, switch to real destination
						movementModCount += movemnetMod + stepsPerCycle;
						towardsDivision = false;
					} else {
						// If destination was reached, disable (and fall back to
						// old mobility Model if possible)
						go = false;

						if (agent.getPreviousMobilityModel() != null) // Fall
																		// back
							agent.setMobilityModel(agent
									.getPreviousMobilityModel());
						else
							// No fall back -> Notification
							go = false;

					}

					if (enableStatistics)
						doStatistics();

				}
			}
		}
	}

	@Override
	public boolean ismobilityTerminated() {
		return !go;
	}

	private boolean isAtDestination(int[] dest) {
		switch (dest.length) {
		case 2:
		default:
			// point destination
			return agent.getX() == dest[0] && agent.getY() == dest[1];
		case 3:
			// circular destination
			return Math.pow(dest[2], 2) >= Math
					.pow((agent.getX() - dest[0]), 2)
					+ Math.pow((agent.getY() - dest[1]), 2);
		case 4:
			// rectangular destination
			return agent.getX() >= dest[0] && agent.getY() >= dest[1]
					&& agent.getX() <= dest[2] && agent.getY() <= dest[3];
		}
	}

	public int getIncludeEventWithId() {
		return includeEventWithId;
	}

	public void setIncludeEventWithId(int includeEventWithId) {
		this.includeEventWithId = includeEventWithId;
	}

	public void resetIncludeEventWithId() {
		includeEventWithId = 0;
	}

	public static boolean isEnableStatistics() {
		return enableStatistics;
	}

	public static void setEnableStatistics(boolean enableStatistics) {
		WalkToOnGradientMobilityModel.enableStatistics = enableStatistics;
	}

	public static int getMovingAgents() {
		return movingAgents;
	}

	public static int getMovingAgentsToDifferentDestinations() {
		return destinations.size();
	}

	public static float getSMF() {
		if (enableStatistics && movingAgents > 0)
			return (destinations.size() / (float) movingAgents);
		else
			return 0f;
	}
}
