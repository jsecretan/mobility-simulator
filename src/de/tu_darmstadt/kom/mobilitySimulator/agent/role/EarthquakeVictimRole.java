package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.StaticMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;

public class EarthquakeVictimRole extends AbstractRole {

	public static int SECURED = 0;
	private static Set<AbstractAgent> known = new HashSet<AbstractAgent>();
	public static Set<AbstractAgent> toRescue = new HashSet<AbstractAgent>();
	public static Set<AbstractAgent> inRescueProc = new HashSet<AbstractAgent>();
	public static Set<AbstractAgent> totalVictims = new HashSet<AbstractAgent>();

	public static Set<AbstractAgent> getKnown() {
		return known;
	}

	public static void addKnown(AbstractAgent a) {
		if (!known.contains(a)) {
			known.add(a);
			toRescue.add(a);
		}
	}

	public static void addKnown(Collection<AbstractAgent> c) {
		for (AbstractAgent a : c) {
			if (!known.contains(a)) {
				known.add(a);
				toRescue.add(a);
			}
		}
	}

	private boolean rescued;
	private int maxLifepoints;
	private int lifepoints;

	public EarthquakeVictimRole(AbstractAgent agent) {
		super(agent);
		init(agent);
		totalVictims.add(agent);
		if (agent.isMobileCommunicationEnabled())
			agent.addVictimToAgentTable(agent);
	}

	private void init(AbstractAgent agent) {
		agent.setMobilityModel(new StaticMobilityModel(agent));
		rescued = false;
		agent.setResistance(0);
		maxLifepoints = Scheduler.rand.nextInt(100) + 351;
		rescued = false;
		agent.setResistance(0);
	}

	@Override
	public void behave() {
		// do nothing
	}

	@Override
	public String getName() {
		return "EarthquakeVictimRole";
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

	public int getMaxLifepoints() {
		return maxLifepoints;
	}

	public void setLifepoints(int i) {
		lifepoints = lifepoints;
	}

	public void decreaseLifepoints(int i) {
		lifepoints -= i;
		if (lifepoints < 0)
			lifepoints = 0;
	}

	public int getLifepoints() {
		return lifepoints;
	}

	public void setMaxLifepoints(int maxLifepoints) {
		this.maxLifepoints = maxLifepoints;
	}

}
