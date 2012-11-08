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
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeFireMapEvent;

public class TeamFireFighterRole extends AbstractRole {

	private AbstractAgent leader;

	private final int EXPLORE = 0;
	private final int GO_TO_FIRE = 1;
	private final int WORK_ON_FIRE = 2;
	private final int GO_TO_LEADER = 3;

	private Set<AbstractMapEvent> eventSet;

	private int status;

	private LinkedRTreeFireMapEvent event;

	private int resistance;

	public TeamFireFighterRole(AbstractAgent agent, AbstractAgent leader) {
		super(agent);
		this.leader = leader;
		eventSet = new HashSet<AbstractMapEvent>();
		status = EXPLORE;
		resistance = agent.getResistance();
	}

	@Override
	public String getName() {
		return "TeamFirefighterRole";
	}

	@Override
	public void behave() {

		computeMessages();

		switch (status) {
		case EXPLORE:
			eventSet.clear();
			Object circle = Scheduler.mapEventRepository.getCircle(
					agent.getX(), agent.getY(), 15);
			Scheduler.mapEventRepository.findIntersectingEvents(circle,
					FilterFactory.getFilter(FilterFactory.EVENT_TYPE_FILTER,
							"fire"), eventSet);
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
			} else if (agent.getMobilityModel() == null) {
				agent.setMobilityModel(new RandomWalkOnGradientMobilityModel(
						agent));
			}
			break;

		case GO_TO_FIRE:
			if (agent.getMobilityModel().ismobilityTerminated()) {
				event.registerWorker(agent);
				agent.setMobilityModel(new StaticMobilityModel(agent), false);
				agent.setResistance(0);
				status = WORK_ON_FIRE;
			}
			break;

		case WORK_ON_FIRE:
			if (!event.isActive()) {
				event = null;
				agent.setResistance(resistance);
				goToLeader();
			}
			break;

		case GO_TO_LEADER:
			if (agent.getMobilityModel().ismobilityTerminated()) {
				leader.getMessageQueue().add("Agent returned:" + agent.getId());
				status = EXPLORE;
				Scheduler.agentRepository.setActive(agent.getId(), false);
			}
			break;
		}

		agent.getMobilityModel().move();
	}

	private void computeMessages() {
		for (String message : agent.getMessageQueue()) {
			if (message.equals("Back to leader")) {
				if (event != null) {
					event.unregisterWorker(agent);
					event = null;
				}
				goToLeader();
				break;
			}
		}
		agent.getMessageQueue().clear();
	}

	private void goToLeader() {
		status = GO_TO_LEADER;
		WalkToOnGradientMobilityModel mobModel = new WalkToOnGradientMobilityModel(
				agent);
		mobModel.setDestination(leader.getX(), leader.getY(), 3);
		agent.setMobilityModel(mobModel);
	}

	@Override
	public Color getPreferedColor() {
		return Color.RED;
	}

}
