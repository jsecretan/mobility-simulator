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
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeCollapsedBuildingMapEvent;

public class TeamRescuerRole extends AbstractRole {

	private AbstractAgent leader;

	private final int EXPLORE = 0;
	private final int GO_TO_EVENT = 1;
	private final int WORK_ON_EVENT = 2;
	private final int GO_TO_LEADER = 3;

	private Set<AbstractMapEvent> eventSet;

	private int status;

	private LinkedRTreeCollapsedBuildingMapEvent event;

	private int resistance;

	public TeamRescuerRole(AbstractAgent agent, AbstractAgent leader) {
		super(agent);
		this.leader = leader;
		eventSet = new HashSet<AbstractMapEvent>();
		resistance = agent.getResistance();
		status = EXPLORE;
	}

	@Override
	public String getName() {
		return "TeamRescuerRole";
	}

	@Override
	public void behave() {

		computeMessages();

		switch (status) {
		case EXPLORE:
			eventSet.clear();
			if (!(agent.getMobilityModel() instanceof RandomWalkOnGradientMobilityModel)) {
				agent.setMobilityModel(new RandomWalkOnGradientMobilityModel(
						agent));
			}
			Object circle = Scheduler.mapEventRepository.getCircle(
					agent.getX(), agent.getY(), 15);
			Scheduler.mapEventRepository.findIntersectingEvents(circle,
					FilterFactory.getFilter(FilterFactory.EVENT_TYPE_FILTER,
							"collapsedBuilding,freeForRescueAgent"), eventSet);
			Scheduler.mapEventRepository.recycleShape(circle);

			if (eventSet.size() > 0) {
				WalkToOnGradientMobilityModel mobModel = new WalkToOnGradientMobilityModel(
						agent);
				event = (LinkedRTreeCollapsedBuildingMapEvent) eventSet
						.iterator().next();

				mobModel.setDestination(event.getWorkingArea().getX(), event
						.getWorkingArea().getY(), event.getWorkingArea()
						.getRadius());
				agent.setMobilityModel(mobModel, false);
				status = GO_TO_EVENT;
			}
			break;

		case GO_TO_EVENT:
			if (agent.getMobilityModel().ismobilityTerminated()) {

				event.registerWorker(agent);
				agent.setMobilityModel(new StaticMobilityModel(agent), false);
				agent.setResistance(0);
				status = WORK_ON_EVENT;
			}
			break;

		case WORK_ON_EVENT:
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

		if (agent.getMobilityModel() != null)
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
		return new Color(142, 255, 142, 255);
	}

}
