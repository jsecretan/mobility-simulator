package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.ArrayList;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;

public class SMFRole extends AbstractRole {

	private final static ArrayList<int[]> destinations = new ArrayList<int[]>();

	private int[] destination;

	public SMFRole(AbstractAgent agent) {
		super(agent);
		agent.setMobilityModel(new WalkToOnGradientMobilityModel(agent));
	}

	@Override
	public String getName() {
		return "SMFRole";
	}

	@Override
	public void behave() {

		if (agent.getMobilityModel().ismobilityTerminated()) {
			chooseNewDestination();
		}

		agent.getMobilityModel().move();

	}

	private void chooseNewDestination() {
		if (destinations.size() > 0) {
			int i;
			do {
				i = Scheduler.rand.nextInt(destinations.size());
			} while (destinations.get(i) == destination);

			destination = destinations.get(i);
			((WalkToOnGradientMobilityModel) agent.getMobilityModel())
					.setDestination(destination);
		}
	}

	@Override
	public Color getPreferedColor() {
		return Color.CYAN;
	}

	public static void addDestination(int[] dest) {
		destinations.add(dest);
	}
}
