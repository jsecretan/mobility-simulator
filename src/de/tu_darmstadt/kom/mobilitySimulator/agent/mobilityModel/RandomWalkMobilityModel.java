package de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;

public class RandomWalkMobilityModel extends AbstractMobilityModel {

	@Override
	public boolean ismobilityTerminated() {
		return false;
	}

	public RandomWalkMobilityModel(AbstractAgent agent) {
		super(agent);
		count = 0;
	}

	private int count, offsetX, offsetY;

	@Override
	public String getName() {
		return "RandomWalkMobilityModel";
	}
	
@Override
	public void velocityChanged() {	
	}

	@Override
	public void move() {
		if (count <= 0) {
			count = Scheduler.rand.nextInt(20);
			offsetX = Scheduler.rand.nextInt(21) - 10;
			offsetY = Scheduler.rand.nextInt(21) - 10;
		}

		if (agent.getX() + offsetX < DiscreteMap.getInstance().getSizeX()
				&& agent.getX() + offsetX >= 0
				&& agent.getY() + offsetY < DiscreteMap.getInstance()
						.getSizeY() && agent.getY() + offsetY >= 0) {

			/*
			 * TODO Rethink where to put the move call. Whrer to set the
			 * position of an agent and where to trigger refreshing of rTree
			 */
			Scheduler.agentRepository.agentMoved(agent, agent.getX() + offsetX,
					agent.getY() + offsetY);
			// agent.move(agent.getX() + offsetX, agent.getY() + offsetY);

			count--;
		} else {
			count = 0;
		}
	}
}
