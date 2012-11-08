package de.tu_darmstadt.kom.mobilitySimulator.core.agent;

import java.awt.Color;

import de.tu_darmstadt.kom.mobilitySimulator.output.GenericFileOutput;

public abstract class AbstractRole {

	protected AbstractAgent agent;

	/**
	 * Creates a Role and logs it through {@link GenericFileOutput} as new line.
	 * 
	 * @param agent
	 *            The Agent, the role belongs to.
	 */
	public AbstractRole(AbstractAgent agent) {
		this.agent = agent;
	}

	public abstract String getName();

	public abstract void behave();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractRole)
			return getName().equals(((AbstractRole) obj).getName());
		else if (obj instanceof AbstractAgent)
			return getName().equals(((AbstractAgent) obj).getRoleName());
		else
			return false;
	}

	abstract public Color getPreferedColor();
}
