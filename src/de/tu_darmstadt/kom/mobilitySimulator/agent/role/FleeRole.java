package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.kom.linkedRTree.Circle;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.StaticMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;

public class FleeRole extends AbstractRole {

	private String executingMessage;

	private boolean onWayToCollectionPoint;

	Set<AbstractMapEvent> mapEvents;

	public FleeRole(AbstractAgent agent) {
		super(agent);
		init();
	}

	private void init() {
		agent.setMobilityModel(new StaticMobilityModel(agent));
		onWayToCollectionPoint = false;
		mapEvents = new HashSet<AbstractMapEvent>();
		// agent.setMobilityModel(new WalkToOnGradientMobilityModel(agent));
		//
		// ((WalkToOnGradientMobilityModel) agent.getMobilityModel())
		// .setDestination(0, 500, 100, 600);
	}

	@Override
	public void behave() {

		if (!onWayToCollectionPoint) {

			// Compute Messages
			while (agent.getMessagesSize() > 0) {
				String message = agent.getMessageQueue().poll();

				if (executingMessage == null
						|| !message.equals(executingMessage)) {

					executingMessage = message;
					String[] messageParts = message.split(":");
					if (messageParts[0].equals("GoTo")) {

						agent.getMessageQueue().clear();

						String[] coordinates = messageParts[1].split(",");
						int[] dest = new int[coordinates.length];
						for (int i = 0; i < coordinates.length; i++) {
							dest[i] = Integer.parseInt(coordinates[i]);
						}
						WalkToOnGradientMobilityModel mobMod = new WalkToOnGradientMobilityModel(
								agent);
						mobMod.setDestination(dest);
						agent.setMobilityModel(mobMod, false);

						onWayToCollectionPoint = true;
					}
				}
			}
		} else {
			if (agent.getMobilityModel().ismobilityTerminated()
					&& onWayToCollectionPoint) {
				Scheduler.agentRepository.setActive(agent.getId(), false);
				return;
			} else {
				mapEvents.clear();
				Scheduler.mapEventRepository.findIntersectingEvents(new Circle(
						agent.getX(), agent.getY(), 15), mapEvents);
				if (mapEvents.size() > 0)
					((WalkToOnGradientMobilityModel) agent.getMobilityModel())
							.setIncludeEventWithId(mapEvents.iterator().next()
									.getId());
				else
					((WalkToOnGradientMobilityModel) agent.getMobilityModel())
							.resetIncludeEventWithId();
			}
		}
		agent.getMobilityModel().move();

	}

	@Override
	public String getName() {
		return "FleeRole";
	}

	public boolean isOnWayToCollectionPoint() {
		return onWayToCollectionPoint;
	}

	public void setOnWayToCollectionPoint(boolean onWayToCollectionPoint) {
		this.onWayToCollectionPoint = onWayToCollectionPoint;
	}

	@Override
	public Color getPreferedColor() {
		return Color.BLUE;
	}

}
