package de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractMobilityModel;

public class FleeFromEventMobilityModel extends AbstractMobilityModel {

	private boolean go;

	public FleeFromEventMobilityModel(AbstractAgent agent, int eventID) {
		super(agent);
		init();
	}

	private void init() {

	}

	@Override
	public void move() {

	}

	@Override
	public String getName() {
		return "FleeMobilityModel";
	}

	@Override
	public boolean ismobilityTerminated() {
		return go;
	}
	
	@Override
	public void velocityChanged() {	
	}

}
