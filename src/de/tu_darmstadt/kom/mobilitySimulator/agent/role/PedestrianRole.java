package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.RandomWalkOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;

public class PedestrianRole extends AbstractRole {

	public PedestrianRole(AbstractAgent agent) {
		super(agent);
		init();
	}

	private void init() {
		agent.setMobilityModel(new RandomWalkOnGradientMobilityModel(agent));
	}

	@Override
	public void behave() {
		// if(Scheduler.getInstance().getCycle()==900)
		// Scheduler.agentRepository.setActive(agent.getId(), false);
		agent.getMobilityModel().move();
	}

	@Override
	public String getName() {
		return "PedestrianRole";
	}

	@Override
	public Color getPreferedColor() {
		return Color.GREEN;
	}

}
