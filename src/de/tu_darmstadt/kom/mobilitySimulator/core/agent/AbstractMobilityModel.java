package de.tu_darmstadt.kom.mobilitySimulator.core.agent;

import de.tu_darmstadt.kom.mobilitySimulator.output.GenericFileOutput;

public abstract class AbstractMobilityModel {

	protected AbstractAgent agent;

	/**
	 * Creates a MobilityModel and logs it through {@link GenericFileOutput} as
	 * a new line
	 * 
	 * @param agent
	 *            The Agent, the model belongs to.
	 */
	public AbstractMobilityModel(AbstractAgent agent) {
		this.agent = agent;
	}

	public abstract void move();

	public abstract String getName();

	public abstract boolean ismobilityTerminated();

	public abstract void velocityChanged();
}
