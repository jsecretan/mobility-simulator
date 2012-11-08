package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.StaticMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;

public class AmbulanceRole extends AbstractRole implements FirstResponderInterface {

	// public static Stack<AbstractAgent> reportedVictims = new
	// Stack<AbstractAgent>();

	public static final int HOSPITAL_Y = 689;
	public static final int HOSPITAL_X = 742;
	public final int AT_HOME = 0;
	public final int ON_WAY_TO_VICTIM = 1;
	public final int CARRY_VICTIM = 2;
	public final int ON_WAY_HOME = 3;

	private int agentResistance;

	private int[] base;

	private int status;
	private int delay;

	private int refreshCount;

	private AbstractAgent victim;

	public AmbulanceRole(AbstractAgent agent) {
		super(agent);

		base = new int[] { agent.getX(), agent.getY(), 10 };
		status = AT_HOME;
		delay = 0;
		agent.setMobilityModel(new StaticMobilityModel(agent));
		agentResistance = agent.getResistance();
		agent.setResistance(0);
		agent.setMaxVelocity((int) (100 / 3.6));
		agent.setVelocity((int) (40 / 3.6));
	}

	@Override
	public String getName() {
		return "AmbulanceRole";
	}

	@Override
	public void behave() {
		switch (status) {
		case AT_HOME:
		default:
			if (delay > 0)
				delay--;
			else {
				// Get latest Victim from EarthquakeVictimRole.toTescue stack
				if (EarthquakeVictimRole.toRescue.size() > 0) {
					// victim = reportedVictims.pop();
					Iterator<AbstractAgent> it = EarthquakeVictimRole.toRescue
							.iterator();
					victim = it.next();
					it.remove();

					EarthquakeVictimRole.inRescueProc.add(victim);

					WalkToOnGradientMobilityModel mobModel = new WalkToOnGradientMobilityModel(
							agent);
					mobModel.setDestination(victim.getPos());
					agent.setMobilityModel(mobModel, false);
					status = ON_WAY_TO_VICTIM;
					refreshCount = 0;
					agent.setResistance(agentResistance);
				}
			}
			break;
		case ON_WAY_TO_VICTIM:

			lookForVictims();

			if (agent.getMobilityModel().ismobilityTerminated()) {
				((EarthquakeVictimRole) victim.getRole()).setRescued(true);
				WalkToOnGradientMobilityModel model = new WalkToOnGradientMobilityModel(
						agent);
				model.setDestination(HOSPITAL_X, HOSPITAL_Y, 5);
				agent.setMobilityModel(model);
				status = CARRY_VICTIM;

				setDelay(60);
			} else {
				if (((EarthquakeVictimRole) victim.getRole()).isRescued()) {
					status = ON_WAY_HOME;
				}
				if (delay > 0)
					delay--;
				else
					agent.getMobilityModel().move();
			}
			break;

		case CARRY_VICTIM:

			lookForVictims();

			if (agent.getMobilityModel().ismobilityTerminated()) {
				// Victim brought to collection point. Reset FR-agent to
				// random-walk
				Scheduler.agentRepository.setActive(victim.getId(), false);
				status = AT_HOME;
				agent.setResistance(0);

				EarthquakeVictimRole.inRescueProc.remove(victim);
				EarthquakeVictimRole.SECURED++;

				victim = null;

				setDelay(5 * 60);

			} else {
				if (delay > 0)
					delay--;
				else {
					// Move Ambulance
					agent.getMobilityModel().move();
					// Move victim agent the same way (carrying)
					Scheduler.agentRepository
							.agentMoved(victim, agent.getPos());
				}
			}
			break;
		case ON_WAY_HOME:

			lookForVictims();

			if (agent.getMobilityModel().ismobilityTerminated()) {
				status = AT_HOME;
				agent.setResistance(0);
			} else {
				if (EarthquakeVictimRole.toRescue.size() > 0) {
					// victim = reportedVictims.pop();
					Iterator<AbstractAgent> it = EarthquakeVictimRole.toRescue
							.iterator();
					victim = it.next();
					it.remove();

					EarthquakeVictimRole.inRescueProc.add(victim);

					WalkToOnGradientMobilityModel mobModel = new WalkToOnGradientMobilityModel(
							agent);
					mobModel.setDestination(victim.getPos());
					agent.setMobilityModel(mobModel, false);
					status = ON_WAY_TO_VICTIM;
					refreshCount = 0;
					agent.setResistance(agentResistance);
				}
			}
			break;

		}

		// agent.getMobilityModel().move();
	}

	private void setDelay(int seconds) {
		delay = seconds / Scheduler.getInstance().getSecondsPerCycle();
	}

	private void lookForVictims() {
		HashSet<AbstractAgent> container = new HashSet<AbstractAgent>();
		Object circle = Scheduler.agentRepository.getCircle(agent.getX(),
				agent.getY(), AbstractAgent.VISUAL_RANGE);
		Scheduler.agentRepository.findIntersectingAgents(circle, FilterFactory
				.getFilter(FilterFactory.AGENT_ROLE_FILTER,
						"EarthquakeVictimRole,unknown"), container);
		EarthquakeVictimRole.addKnown(container);
		Scheduler.agentRepository.recycleShape(circle);
	}

	private int getDistance(int x, int y) {
		return (int) Math.sqrt(Math.pow(x - agent.getX(), 2)
				+ Math.pow(y - agent.getY(), 2));
	}

	private void driveHome() {
		WalkToOnGradientMobilityModel mobModel = new WalkToOnGradientMobilityModel(
				agent);
		mobModel.setDestination(new int[] { HOSPITAL_X, HOSPITAL_Y });
		agent.setMobilityModel(mobModel, true);
		status = ON_WAY_HOME;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public Color getPreferedColor() {
		return Color.WHITE;
	}

	@Override
	public void getVictims(Collection<AbstractAgent> agents) {
		EarthquakeVictimRole.addKnown(agents);
	}
}
