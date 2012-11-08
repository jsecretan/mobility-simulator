package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.RandomWalkOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.StaticMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;

public class NormalRole extends AbstractRole {

	private final int DO_NOTHING = 0;
	private final int GO_TO_EVENT = 1;
	private final int WORK_ON_EVENT = 2;
	private final int GO_TO_COLLECTION_POINT = 3;
	// private final int RESCUE = 4;

	private final int MEMORY_ENTRIES = 3;

	private int status;

	private int[] collectionPoint;

	private boolean rescued;

	private AbstractMapEvent event;
	private int resistance;

	private LinkedList<AbstractAgent> agentMemory;

	public NormalRole(AbstractAgent agent) {
		super(agent);
		init(agent);
	}

	private void init(AbstractAgent agent) {
		// if (Scheduler.rand.nextInt(100) < 20) {
		// agent.setMobilityModel(new RandomWalkOnGradientMobilityModel(agent));
		// // WalkToOnGradientMobilityModel mobMod = new
		// // WalkToOnGradientMobilityModel(
		// // agent);
		// // mobMod.setDestination(AmbulanceRole.HOSPITAL_X,
		// // AmbulanceRole.HOSPITAL_Y);
		// // agent.setMobilityModel(mobMod);
		// } else
		agent.setMobilityModel(new StaticMobilityModel(agent));
		rescued = false;
		status = DO_NOTHING;
		resistance = agent.getResistance();

		agentMemory = new LinkedList<AbstractAgent>();
	}

	@Override
	public void behave() {
		switch (status) {
		case DO_NOTHING:
		default:

			computeMessages();

			lookForOthers();
			lookForEvents();
			if (isCollectionPointSet())
				lookForOthers();
			break;

		case GO_TO_COLLECTION_POINT:
			lookForEvents();
			lookForOthers();

			if (agent.getMobilityModel().ismobilityTerminated()) {
				Scheduler.agentRepository.setActive(agent.getId(), false);
				EarthquakeVictimRole.addKnown(agentMemory);
			}

			break;

		case GO_TO_EVENT:
			if (isCollectionPointSet())
				lookForOthers();
			if (agent.getMobilityModel().ismobilityTerminated()) {
				event.registerWorker(agent);
				agent.setResistance(0);
				agent.setMobilityModel(new StaticMobilityModel(agent), false);
				status = WORK_ON_EVENT;
			}
			break;
		case WORK_ON_EVENT:
			computeMessages();
			if (!event.isActive()) {
				leaveEvent();
			}
			break;
		}

		agent.getMobilityModel().move();
	}

	private void leaveEvent() {
		agent.setResistance(resistance);
		if (collectionPoint != null) {
			WalkToOnGradientMobilityModel mobMode = new WalkToOnGradientMobilityModel(
					agent);
			mobMode.setDestination(collectionPoint);
			agent.setMobilityModel(mobMode);
			status = GO_TO_COLLECTION_POINT;
		} else {
			status = DO_NOTHING;
		}
	}

	private void lookForOthers() {
		if (EarthquakeVictimRole.getKnown().size() < EarthquakeVictimRole.totalVictims
				.size() || isCollectionPointSet()) {
			HashSet<AbstractAgent> container = new HashSet<AbstractAgent>();
			Object circle = Scheduler.agentRepository.getCircle(agent.getX(),
					agent.getY(), AbstractAgent.VISUAL_RANGE);

			if (isCollectionPointSet()) {
				Scheduler.agentRepository.findIntersectingAgents(circle,
						FilterFactory.getFilter(
								FilterFactory.AGENT_ROLE_FILTER,
								"NormalRole,noCollectionPointSet=false"),
						container);
				for (AbstractAgent a : container) {
					a.addMessage("GoTo:" + collectionPoint[0] + ","
							+ collectionPoint[1] + "," + collectionPoint[2]);
				}
			}

			// if (EarthquakeVictimRole.getKnown().size() <
			// EarthquakeVictimRole.totalVictims
			// .size()) {
			//
			// // Find Victims
			// container.clear();
			// Scheduler.agentRepository.findIntersectingAgents(circle,
			// FilterFactory.getFilter(FilterFactory.AGENT_ROLE_FILTER,
			// "EarthquakeVictimRole,unknown"), container);
			//
			// for (AbstractAgent agent : container) {
			// if (agentMemory.size() == 3)
			// agentMemory.removeFirst();
			// agentMemory.add(agent);
			// }
			//
			// // // Find FR's to tell about Victims
			// // container.clear();
			// // Scheduler.agentRepository.findIntersectingAgents(circle,
			// // FilterFactory.getFilter(FilterFactory.AGENT_ROLE_FILTER,
			// // "FirstResponder"), container);
			// // if (container.size() > 0) {
			// // for (AbstractAgent agent : container) {
			// // ((FirstResponderInterface) agent.getRole())
			// // .getVictims(agentMemory);
			// // }
			// // agentMemory.clear();
			// // }
			// }

			Scheduler.agentRepository.recycleShape(circle);
		}
	}

	private void lookForEvents() {
		HashSet<AbstractMapEvent> container = new HashSet<AbstractMapEvent>();
		Object circle = Scheduler.mapEventRepository.getCircle(agent.getX(),
				agent.getY(), 20);
		Scheduler.mapEventRepository.findIntersectingEvents(circle,
				FilterFactory.getFilter(FilterFactory.EVENT_TYPE_FILTER,
						"collapsedBuilding,freeForNormalAgent"), container);
		Scheduler.mapEventRepository.recycleShape(circle);
		if (container.size() > 0) {
			// System.err.println("found");
			WalkToOnGradientMobilityModel mobMod = new WalkToOnGradientMobilityModel(
					agent);
			event = container.iterator().next();
			mobMod.setDestination(event.getImpactAreaAsCoordinates());
			agent.setMobilityModel(mobMod);
			status = GO_TO_EVENT;
		}
	}

	private void computeMessages() {
		while (agent.hasMessage()) {
			String message = agent.pollMessage();

			String[] messageParts = message.split(":");
			if (messageParts[0].equals("GoTo")) {

				agent.getMessageQueue().clear();

				String[] coordinates = messageParts[1].split(",");
				collectionPoint = new int[coordinates.length];
				for (int i = 0; i < coordinates.length; i++) {
					collectionPoint[i] = Integer.parseInt(coordinates[i]);
				}

				if (status == DO_NOTHING) {
					WalkToOnGradientMobilityModel mobMod = new WalkToOnGradientMobilityModel(
							agent);
					mobMod.setDestination(collectionPoint);
					agent.setMobilityModel(mobMod);
					status = GO_TO_COLLECTION_POINT;
				}

				agent.getMessageQueue().clear();
			} else if (messageParts[0].equals("LeaveEvent")) {
				leaveEvent();
				agent.getMessageQueue().clear();
			}
		}
	}

	@Override
	public String getName() {
		return "NormalRole";
	}

	@Override
	public Color getPreferedColor() {
		return Color.BLUE;
	}

	public boolean isCollectionPointSet() {
		return collectionPoint != null;
	}

}
