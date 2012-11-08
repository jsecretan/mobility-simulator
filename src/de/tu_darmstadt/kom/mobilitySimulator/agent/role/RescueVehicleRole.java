package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.RandomWalkOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.StaticMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;
import de.tu_darmstadt.kom.mobilitySimulator.mapEvent.CollapsedBuildingMapEvent;

public class RescueVehicleRole extends AbstractRole implements
		FirstResponderInterface {

	public static Collection<AbstractMapEvent> reportedEvents = new ArrayList<AbstractMapEvent>();

	public final int EXPLORE = 0;
	public final int ON_WAY_TO_INCIDENT = 1;
	public final int AT_INCIDENT = 2;

	public final static int[] collPoint1 = new int[] { 510, 135, 50 };
	public final static int[] collPoint2 = new int[] { 115, 665, 30 };
	public final static int[] collPoint3 = new int[] { 678, 468, 40 };

	public final static int[] collPoint4 = new int[] { 119, 373, 20 };

	private final boolean OUT = false;
	private final boolean IN = true;

	private int[] base;

	private AbstractMapEvent event;

	private int status;

	private AbstractAgent[] crew;
	private boolean[] crewStatus;
	private int refreshCount;

	public RescueVehicleRole(AbstractAgent agent, int crewAmount) {
		super(agent);
		crew = new AbstractAgent[crewAmount];
		crewStatus = new boolean[crewAmount];
		for (int i = 0; i < crewAmount; i++) {
			AbstractAgent newAgent = Scheduler.agentRepository
					.createAgent(0, 0);
			crew[i] = newAgent;
			Scheduler.agentRepository.putPassive(newAgent);
			newAgent.setRole(new TeamRescuerRole(crew[i], agent));
		}
		base = new int[] { agent.getX(), agent.getY() };
		status = EXPLORE;
		agent.setMobilityModel(new StaticMobilityModel(agent));
		agent.setMaxVelocity((int) (100 / 3.6));
		agent.setVelocity((int) (40 / 3.6));
	}

	@Override
	public String getName() {
		return "RescueVehicleRole";
	}

	@Override
	public void behave() {
		switch (status) {
		case EXPLORE:
		default:

			lookForOthers();

			if (reportedEvents.size() > 0) {
				int distance = Integer.MAX_VALUE;
				AbstractMapEvent tmpGoal = null;

				for (Iterator<AbstractMapEvent> iterator = reportedEvents
						.iterator(); iterator.hasNext();) {
					AbstractMapEvent event = iterator.next();

					if (event.isActive()
							&& event instanceof CollapsedBuildingMapEvent
							&& ((CollapsedBuildingMapEvent) event)
									.getMaxWorkerCapacity() > ((CollapsedBuildingMapEvent) event)
									.getSpecializedWorkerCount()) {
						if (getDistance(event.getX(), event.getY()) < distance) {
							distance = getDistance(event.getX(), event.getY());
							tmpGoal = event;
						}
					} else {
						iterator.remove();
					}

				}
				if (tmpGoal != null) {
					event = tmpGoal;
					WalkToOnGradientMobilityModel mobModel = new WalkToOnGradientMobilityModel(
							agent);
					mobModel.setDestination(event.getImpactAreaAsCoordinates());
					agent.setMobilityModel(mobModel, false);
					status = ON_WAY_TO_INCIDENT;
					refreshCount = 0;
				}
			} else {
				if (!(agent.getMobilityModel() instanceof RandomWalkOnGradientMobilityModel)) {
					agent.setMobilityModel(new RandomWalkOnGradientMobilityModel(
							agent));
				}
			}
			break;
		case ON_WAY_TO_INCIDENT:

			lookForOthers();

			if (!event.isActive()
					&& event instanceof CollapsedBuildingMapEvent
					&& ((CollapsedBuildingMapEvent) event)
							.getMaxWorkerCapacity() > ((CollapsedBuildingMapEvent) event)
							.getSpecializedWorkerCount()) {
				if (reportedEvents.contains(event))
					reportedEvents.remove(event);
				driveHome();
				break;
			}

			if (agent.getMobilityModel().ismobilityTerminated()) {

				Object circle = Scheduler.mapEventRepository.getCircle(
						agent.getX(), agent.getY(), 15);
				Set<AbstractMapEvent> container = Scheduler.mapEventRepository
						.findIntersectingEvents(circle);
				Scheduler.mapEventRepository.recycleShape(circle);
				if (container.contains(event)) {
					for (int i = 0; i < crew.length; i++) {
						crew[i].setPos(agent.getX(), agent.getY());
						Scheduler.agentRepository.setActive(crew[i].getId(),
								true);
						crewStatus[i] = OUT;
						agent.setMobilityModel(new StaticMobilityModel(agent));
					}
					status = AT_INCIDENT;
				} else {
					((WalkToOnGradientMobilityModel) agent.getMobilityModel())
							.setDestination(event.getImpactAreaAsCoordinates());
				}
			} else {
				refreshCount++;
				if (refreshCount >= 100) {
					((WalkToOnGradientMobilityModel) agent.getMobilityModel())
							.setDestination(event.getImpactAreaAsCoordinates());
					refreshCount = 0;
				}
			}
			break;
		case AT_INCIDENT:

			if (agent.getMessagesSize() > 0) {
				while (agent.getMessagesSize() > 0) {
					String message = agent.getMessageQueue().poll();
					String[] messageParts = message.split(":");
					if (messageParts[0].equals("Agent returned")) {
						for (int i = 0; i < crewStatus.length; i++) {
							if (crew[i].getId() == Integer
									.parseInt(messageParts[1]))
								crewStatus[i] = IN;
						}
					}
				}
				boolean allIn = IN;
				for (int i = 0; i < crewStatus.length; i++) {
					if (crewStatus[i] == OUT)
						allIn = OUT;
				}
				if (allIn == IN) {
					driveHome();
				}
			}
			break;
		}
		agent.getMobilityModel().move();
	}

	private void lookForOthers() {

		int[] collPoint = null;

		int dist = Integer.MAX_VALUE;

		if (getDistance(collPoint1[0], collPoint1[1]) < dist) {
			collPoint = collPoint1;
			dist = getDistance(collPoint1[0], collPoint1[1]);
		}
		if (getDistance(collPoint2[0], collPoint2[1]) < dist) {
			collPoint = collPoint2;
			dist = getDistance(collPoint2[0], collPoint2[1]);
		}
		if (getDistance(collPoint3[0], collPoint3[1]) < dist) {
			collPoint = collPoint3;
			dist = getDistance(collPoint3[0], collPoint3[1]);
		}

		// collPoint = collPoint4;

		HashSet<AbstractAgent> container = new HashSet<AbstractAgent>();
		Object circle = Scheduler.agentRepository.getCircle(agent.getX(),
				agent.getY(), 20);
		Scheduler.agentRepository.findIntersectingAgents(circle, FilterFactory
				.getFilter(FilterFactory.AGENT_ROLE_FILTER,
						"NormalRole,noCollectionPointSet=true"), container);

		for (AbstractAgent a : container) {
			a.getMessageQueue().add(
					"GoTo:" + collPoint[0] + "," + collPoint[1] + ","
							+ collPoint[2]);
			int b = 0;
		}

		container.clear();
		Scheduler.agentRepository.findIntersectingAgents(circle, FilterFactory
				.getFilter(FilterFactory.AGENT_ROLE_FILTER,
						"EarthquakeVictimRole,unknown"), container);
		EarthquakeVictimRole.addKnown(container);
		Scheduler.agentRepository.recycleShape(circle);
	}

	private int getDistance(int x, int y) {
		return (int) Math.sqrt(Math.pow(x - agent.getX(), 2)
				+ Math.pow(y - agent.getY(), 2));
	}

	private void driveHome() {
		agent.setMobilityModel(new StaticMobilityModel(agent));
		status = EXPLORE;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public Color getPreferedColor() {
		return Color.GREEN;
	}

	@Override
	public void getVictims(Collection<AbstractAgent> agents) {
		EarthquakeVictimRole.addKnown(agents);
	}
}
