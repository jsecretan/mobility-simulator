package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.StaticMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;

public class VictimRole extends AbstractRole {

	private boolean rescued;

	public VictimRole(AbstractAgent agent) {
		super(agent);
		init(agent);
	}

	private void init(AbstractAgent agent) {
		agent.setMobilityModel(new StaticMobilityModel(agent));
		rescued = false;
		agent.setResistance(0);
	}

	@Override
	public void behave() {
		// do nothing
	}

	@Override
	public String getName() {
		return "VictimRole";
	}

	public boolean isRescued() {
		return rescued;
	}

	public void setRescued(boolean rescued) {
		this.rescued = rescued;
	}

	@Override
	public Color getPreferedColor() {
		return Color.YELLOW;
	}

}
