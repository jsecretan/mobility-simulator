package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;

public class WalkRightRole extends AbstractRole {

	public WalkRightRole(AbstractAgent agent) {
		super(agent);
		WalkToOnGradientMobilityModel mobMod = new WalkToOnGradientMobilityModel(
				agent);
		mobMod.setDestination(10, DiscreteMap.sizeY / 2);
		agent.setMobilityModel(mobMod);
		agent.setVelocity(2.5f);
	}

	@Override
	public String getName() {
		return "WalkRightRole";
	}

	@Override
	public void behave() {
		agent.getMobilityModel().move();
	}

	@Override
	public Color getPreferedColor() {
		return Color.GREEN;
	}

}
