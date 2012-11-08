package de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;

public class WalkRightMobilityModel extends AbstractMobilityModel {

	private float movemnetMod;
	private float movementModCount;
	private int stepsPerCycle;

	private boolean go;

	private int destX;

	public WalkRightMobilityModel(AbstractAgent agent) {
		super(agent);
		movementModCount = 0;
		velocityChanged();
		go = true;
		destX = 0;
	}

	@Override
	public String getName() {
		return "WalkRightMobilityModel";
	}

	@Override
	public void move() {
		if (go) {
			
			int extraSteps = 0;
			movementModCount += movemnetMod;
			if (movementModCount >= 1) {
				movementModCount -= 1;
				extraSteps = 1;
			}

			int newPos = agent.getX() + stepsPerCycle + extraSteps;
			if (newPos < destX) {
				agent.setX(newPos);
			} else {
				agent.setX(destX);
				go = false;
				System.out.println("Destination reached in "
						+ Scheduler.getInstance().getCycle() + " cycle(s)");
				System.exit(0);
			}
		}
	}

	@Override
	public boolean ismobilityTerminated() {
		return false;
	}

	@Override
	public void velocityChanged() {
		movemnetMod = agent.getVelocity()
				% Scheduler.getInstance().getSecondsPerCycle();
		stepsPerCycle = (int) (agent.getVelocity() - movemnetMod)
				/ Scheduler.getInstance().getSecondsPerCycle();
	}

	public int getDestX() {
		return destX;
	}

	public void setDestX(int destX) {
		this.destX = destX;
	}

}
