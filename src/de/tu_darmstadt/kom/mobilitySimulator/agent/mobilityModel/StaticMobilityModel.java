package de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractMobilityModel;

public class StaticMobilityModel extends AbstractMobilityModel {

	public StaticMobilityModel(AbstractAgent agent) {
		super(agent);
	}

	@Override
	public String getName() {
		return "StaticMobilityModel";
	}

	@Override
	public void move() {
		// Do nothing because agent is static
	}

	@Override
	public boolean ismobilityTerminated() {
		return true;
	}

	@Override
	public void velocityChanged() {	
	}
}
