package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.Set;

import de.tu_darmstadt.kom.linkedRTree.Circle;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.RandomWalkOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeAgent;

public class FRRole extends AbstractRole {

	private int mode;

	private final int EXPLORE_MODE = 0;
	private final int GO_TO_VICTIM_MODE = 1;
	private final int CARRY_VICTIM_MODE = 2;

	private final int[] COLLECTION_POINT = new int[] { 700, 400, 100 };

	private AbstractAgent victim;

	public FRRole(AbstractAgent agent) {
		super(agent);
		init();
	}

	private void init() {
		agent.setMobilityModel(new RandomWalkOnGradientMobilityModel(agent,
				false));
		mode = EXPLORE_MODE;
		// System.out.println("EXPLORE_MODE");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void behave() {

		switch (mode) {

		default:

		case EXPLORE_MODE: // Random Walk and explore
			Set<AbstractAgent> set;
			// Explore 1: Find agents that can move (FleeRole) and send them to
			// collection point
			set = Scheduler.agentRepository.findIntersectingAgents(new Circle(
					(LinkedRTreeAgent) agent, 15), FilterFactory.getFilter(
					FilterFactory.AGENT_ROLE_FILTER,
					"FleeRole,onWayToCollectionPoint=false"));

			// System.out.println("Set: "+set.size());
			if (set.size() > 0) {
				for (AbstractAgent abstractAgent : set) {
					// System.out.println(abstractAgent.getRoleName());
					abstractAgent.getMessageQueue().add(
							"GoTo:" + COLLECTION_POINT[0] + ","
									+ COLLECTION_POINT[1] + ","
									+ COLLECTION_POINT[2]);
				}

			}

			// // Explore 2: Find agents that can not move (VictimRole) and
			// carry
			// // them to collection point
			// set = Scheduler.agentRepository.findIntersectingAgents(
			// new Rectangle((LinkedRTreeAgent) agent, 15, 15),
			// FilterFactory.getFilter(FilterFactory.AGENT_ROLE_FILTER,
			// "VictimRole,rescued=false"));
			//
			// // System.out.println("Set: "+set.size());
			// if (set.size() > 0) { // Victims where found
			// // System.out.println("GO_TO_VICTIM_MODE");
			// victim = set.iterator().next(); // Get first Agent
			// // System.out.println("Victim type: "+victim.getRoleName());
			// mode = GO_TO_VICTIM_MODE; // Go to rescue status
			// WalkToOnGradientMobilityModel model = new
			// WalkToOnGradientMobilityModel(
			// agent);
			// model.setDestination(victim.getX(), victim.getY());
			// agent.setMobilityModel(model, false);
			//
			// }

			agent.getMobilityModel().move();
			break;

		case GO_TO_VICTIM_MODE:
			if (agent.getMobilityModel().ismobilityTerminated()) {
				mode = CARRY_VICTIM_MODE;
				// System.out.println("CARRY_VICTIM_MODE");
				((VictimRole) victim.getRole()).setRescued(true);
				WalkToOnGradientMobilityModel model = new WalkToOnGradientMobilityModel(
						agent);
				model.setDestination(COLLECTION_POINT[0], COLLECTION_POINT[1]);
				agent.setMobilityModel(model);
			} else {
				if (((VictimRole) victim.getRole()).isRescued()) {
					// If victim was picked up by another FR-agent during
					// approach, reset agent
					init();
				}
				agent.getMobilityModel().move();
			}
			break;
		//
		case CARRY_VICTIM_MODE:
			if (agent.getMobilityModel().ismobilityTerminated()) {
				// Victim brought to collection point. Reset FR-agent to
				// random-walk
				Scheduler.agentRepository.setActive(victim.getId(), false);
				init();
			} else {
				// Move FR-Agent
				agent.getMobilityModel().move();
				// Move victim agent the same way (carrying)
				Scheduler.agentRepository.agentMoved(victim, agent.getPos());
			}
			break;
		}

	}

	@Override
	public String getName() {
		return "FRRole";
	}

	@Override
	public Color getPreferedColor() {
		return Color.GREEN;
	}

}
