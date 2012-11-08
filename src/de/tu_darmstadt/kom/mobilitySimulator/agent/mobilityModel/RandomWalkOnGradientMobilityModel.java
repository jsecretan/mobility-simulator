package de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel;

import java.util.Arrays;
import java.util.HashSet;
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

public class RandomWalkOnGradientMobilityModel extends AbstractMobilityModel {

	public static final boolean USE_DEVISION = true;
	private static final int DIVISIONS = 5;
	private static int divisionSizeX, divisionSizeY;
	private boolean towardsDivision;
	private int divisionX, divisionY;
	private int[] divisionDest;

	private int lockCounter;

	protected int dest[];

	private boolean go;

	Set<AbstractAgent> neighborSet;

	protected short[][] obstaclesMap;

	protected int includeEventWithId;
	private int[] lastDest;

	public RandomWalkOnGradientMobilityModel(AbstractAgent agent) {
		super(agent);
		init();
	}

	public RandomWalkOnGradientMobilityModel(AbstractAgent agent, boolean log) {
		super(agent);
		init();
	}

	private void init() {

		if (divisionSizeX == 0)
			divisionSizeX = DiscreteMap.sizeX / DIVISIONS;
		if (divisionSizeY == 0)
			divisionSizeY = DiscreteMap.sizeY / DIVISIONS;

		neighborSet = new HashSet<AbstractAgent>();

		towardsDivision = false;

		includeEventWithId = 0;

		go = true;

		setRandomDest();

		// gradientMobModel = new WalkToOnGradientMobilityModel(agent);
		obstaclesMap = DiscreteMap.getInstance().getObstacles();

	}

	private void setRandomDest() {

		dest = new int[2];
		do {
			dest[0] = Scheduler.rand.nextInt(DiscreteMap.getInstance()
					.getSizeX());
			dest[1] = Scheduler.rand.nextInt(DiscreteMap.getInstance()
					.getSizeY());
		} while (DiscreteMap.getInstance().getObstacles()[dest[1]][dest[0]] > 100);
		if (USE_DEVISION)
			setDivision();
		lockCounter = 0;

		if (WalkToOnGradientMobilityModel.enableStatistics)
			doStatistics();
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

	private void doStatistics() {
		if (lastDest != null) {
			Integer i = null;

			for (Entry<int[], Integer> entry : WalkToOnGradientMobilityModel.destinations
					.entrySet()) {
				if (Arrays.equals(lastDest, entry.getKey())) {
					i = entry.getValue();
					lastDest = entry.getKey();
					break;
				}
			}

			if (i != null && i > 1)
				WalkToOnGradientMobilityModel.destinations.put(lastDest, i - 1);
			if (i != null && i <= 1)
				WalkToOnGradientMobilityModel.destinations.remove(lastDest);
			WalkToOnGradientMobilityModel.movingAgents--;
			lastDest = null;
		}

		if (go) {
			Integer i = null;
			if (USE_DEVISION && towardsDivision) {

				for (Entry<int[], Integer> entry : WalkToOnGradientMobilityModel.destinations
						.entrySet()) {
					if (Arrays.equals(divisionDest, entry.getKey())) {
						i = entry.getValue();
						divisionDest = entry.getKey();
						break;
					}
				}

				WalkToOnGradientMobilityModel.destinations.put(divisionDest,
						(i != null ? (i + 1) : 1));
				lastDest = divisionDest;
			} else {
				for (Entry<int[], Integer> entry : WalkToOnGradientMobilityModel.destinations
						.entrySet()) {
					if (Arrays.equals(dest, entry.getKey())) {
						i = entry.getValue();
						dest = entry.getKey();
						break;
					}
				}
				WalkToOnGradientMobilityModel.destinations.put(dest,
						(i != null ? (i + 1) : 1));
				lastDest = dest;
			}
			WalkToOnGradientMobilityModel.movingAgents++;
		}
	}

	@Override
	public boolean ismobilityTerminated() {
		return !go;
	}
	
	@Override
	public void velocityChanged() {
		// TODO Auto-generated method stub	
	}

	@Override
	public String getName() {
		return "RandomWalkOnGradientMobilityModel";
	}

	@Override
	public void move() {

		if (go) {

			if (lockCounter >= 10) {
				setRandomDest();
			}

			int[] dest = USE_DEVISION && towardsDivision ? divisionDest
					: this.dest;

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
				Scheduler.agentRepository.findNeighbors(agent, 2, neighborSet);
				int[][] workMap = WorkingMapPool.getInstance().getWorkingMap(
						true);
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
							&& overlayMap[neighborPos[1]][neighborPos[0]] < choosenNeighborValue) {
						choosenNeighborValue = overlayMap[neighborPos[1]][neighborPos[0]];
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
							&& ((overlayMap[neighborPos[1]][neighborPos[0]]) < choosenNeighborValue || (overlayMap[neighborPos[1]][neighborPos[0]] == choosenNeighborValue && Scheduler.rand
									.nextBoolean()))) {
						choosenNeighborValue = overlayMap[neighborPos[1]][neighborPos[0]];
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
							&& ((overlayMap[neighborPos[1]][neighborPos[0]]) < choosenNeighborValue || (overlayMap[neighborPos[1]][neighborPos[0]] == choosenNeighborValue && Scheduler.rand
									.nextBoolean()))) {
						choosenNeighborValue = overlayMap[neighborPos[1]][neighborPos[0]];
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
							&& ((overlayMap[neighborPos[1]][neighborPos[0]]) < choosenNeighborValue || (overlayMap[neighborPos[1]][neighborPos[0]] == choosenNeighborValue && Scheduler.rand
									.nextBoolean()))) {
						choosenNeighborValue = overlayMap[neighborPos[1]][neighborPos[0]];
						choosenNeighbor = NeighborPosition.LEFT;
						choosenNeighborPos = neighborPos;
					}
				}

				WorkingMapPool.getInstance().recicleWorkingMap(workMap);

				/*
				 * TODO Reconsider where to put the move call. Where to set the
				 * position of an agent and where to trigger refreshing of rTree
				 */
				if (choosenNeighbor != null)
					agent.setPos(choosenNeighborPos);
				// Scheduler.agentRepository.agentMoved(agent,
				// choosenNeighborPos);
				else
					lockCounter++;

			} else {
				if (USE_DEVISION && towardsDivision) {
					towardsDivision = false;
					doStatistics();
				} else {
					setRandomDest();
				}
			}
		}
	}

	private boolean isAtDestination(int[] dest) {
		switch (dest.length) {
		case 2:
		default:
			// point destination
			return agent.getX() == dest[0] || agent.getY() == dest[1];
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
}
