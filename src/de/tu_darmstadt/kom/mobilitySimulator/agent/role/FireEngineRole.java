package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.StaticMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;

public class FireEngineRole extends AbstractRole implements FirstResponderInterface {

	public static Collection<AbstractMapEvent> reportedFires = new ArrayList<AbstractMapEvent>();

	public final int WAIT_FOR_ORDERS = 0;
	public final int ON_WAY_TO_INCIDENT = 1;
	public final int AT_INCIDENT = 2;

	private final boolean OUT = false;
	private final boolean IN = true;

	private int[] base;

	private AbstractMapEvent event;

	private int status;

	private AbstractAgent[] team;
	private boolean[] teamStatus;
	private int refreshCount;

	public FireEngineRole(AbstractAgent agent, int crewAmount) {
		super(agent);
		team = new AbstractAgent[crewAmount];
		teamStatus = new boolean[crewAmount];
		for (int i = 0; i < crewAmount; i++) {
			AbstractAgent newAgent = Scheduler.agentRepository
					.createAgent(0, 0);
			team[i] = newAgent;
			Scheduler.agentRepository.putPassive(newAgent);
			newAgent.setRole(new TeamFireFighterRole(team[i], agent));
		}
		base = new int[] { agent.getX(), agent.getY() };
		status = WAIT_FOR_ORDERS;
		agent.setMobilityModel(new StaticMobilityModel(agent));
		agent.setMaxVelocity((int)(80/3.6));
		agent.setVelocity((int)(30/3.6));
	}

	@Override
	public String getName() {
		return "FireEngineRole";
	}

	@Override
	public void behave() {
		switch (status) {
		case WAIT_FOR_ORDERS:
		default:

			if (reportedFires.size() > 0) {
				int distance = Integer.MAX_VALUE;
				AbstractMapEvent tmpGoal = null;

				for (Iterator<AbstractMapEvent> iterator = reportedFires
						.iterator(); iterator.hasNext();) {
					AbstractMapEvent fire = iterator.next();

					if (fire.isActive()) {
						if (getDistance(fire.getX(), fire.getY()) < distance) {
							distance = getDistance(fire.getX(), fire.getY());
							tmpGoal = fire;
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
				// FIXME: rolechange to Rescuer role
				agent.setRole(new RescueVehicleRole(agent, team.length));
			}
			break;
		case ON_WAY_TO_INCIDENT:

			lookForVictims();
			
			if (!event.isActive()) {
				if (reportedFires.contains(event))
					reportedFires.remove(event);
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
					for (int i = 0; i < team.length; i++) {
						team[i].setPos(agent.getX(), agent.getY());
						Scheduler.agentRepository.setActive(team[i].getId(),
								true);
						teamStatus[i] = OUT;
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
						for (int i = 0; i < teamStatus.length; i++) {
							if (team[i].getId() == Integer
									.parseInt(messageParts[1]))
								teamStatus[i] = IN;
						}
					}
				}
				boolean allIn = IN;
				for (int i = 0; i < teamStatus.length; i++) {
					if (teamStatus[i] == OUT)
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
	
	private void lookForVictims() {
		HashSet<AbstractAgent> container = new HashSet<AbstractAgent>();
		Object circle = Scheduler.agentRepository.getCircle(agent.getX(),
				agent.getY(), 20);
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
		status = WAIT_FOR_ORDERS;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public Color getPreferedColor() {
		return new Color(123, 0, 0, 255);
	}
	
	@Override
	public void getVictims(Collection<AbstractAgent> agents) {
		EarthquakeVictimRole.addKnown(agents);
	}
}
