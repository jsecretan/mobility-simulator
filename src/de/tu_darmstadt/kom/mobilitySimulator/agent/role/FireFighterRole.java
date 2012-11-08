package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.RandomWalkOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.StaticMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeFireMapEvent;

public class FireFighterRole extends AbstractRole {

	private final int EXPLORE = 0;
	private final int GO_TO_FIRE = 1;
	private final int WORK_ON_FIRE = 2;
	// private final int explore = 3;
	// private final int explore = 4;

	private Set<AbstractMapEvent> eventSet;

	private int status;

	private LinkedRTreeFireMapEvent event;

	public FireFighterRole(AbstractAgent agent) {
		super(agent);
		init();
	}

	private void init() {
		agent.setMobilityModel(new RandomWalkOnGradientMobilityModel(agent));
		status = EXPLORE;
		eventSet = new HashSet<AbstractMapEvent>(5);
		event = null;
	}

	@Override
	public String getName() {
		return "FireFighterRole";
	}

	@Override
	public void behave() {

		if (Scheduler.mapEventRepository.size() == 0)
			agent.setRole(new FleeRole(agent));

		switch (status) {
		case EXPLORE:
			eventSet.clear();
			Object circle = Scheduler.mapEventRepository.getCircle(
					agent.getX(), agent.getY(), 15);
			Scheduler.mapEventRepository.findIntersectingEvents(circle,
					eventSet);
			Scheduler.mapEventRepository.recycleShape(circle);

			if (eventSet.size() > 0) {
				WalkToOnGradientMobilityModel mobModel = new WalkToOnGradientMobilityModel(
						agent);
				event = (LinkedRTreeFireMapEvent) eventSet.iterator().next();

				mobModel.setDestination(event.getWorkingArea().getX(), event
						.getWorkingArea().getY(), event.getWorkingArea()
						.getRadius());
				agent.setMobilityModel(mobModel, false);
				status = GO_TO_FIRE;
			}
			break;

		case GO_TO_FIRE:
			if (agent.getMobilityModel().ismobilityTerminated()) {
				event.registerWorker(agent);
				agent.setMobilityModel(new StaticMobilityModel(agent), false);
				status = WORK_ON_FIRE;
			}
			break;

		case WORK_ON_FIRE:
			for (String message : agent.getMessageQueue()) {
				if (message.equals("Event terminated")) {
					init();
				}
			}
			agent.getMessageQueue().clear();
			break;
		}

		agent.getMobilityModel().move();

	}

	@Override
	public Color getPreferedColor() {
		return Color.RED;
	}

}
