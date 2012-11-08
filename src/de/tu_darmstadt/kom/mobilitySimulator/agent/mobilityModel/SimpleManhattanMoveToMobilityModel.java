package de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractMobilityModel;

public class SimpleManhattanMoveToMobilityModel extends AbstractMobilityModel {

	private int destX, destY, offsetX, offsetY, speed;

	private boolean go;

	@Override
	public String getName() {
		return "SimpleManhattanMoveToMobilityModel";
	}

	public SimpleManhattanMoveToMobilityModel(AbstractAgent agent) {
		super(agent);
		go = false;
		offsetX = 0;
		offsetY = 0;
		speed = 0;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	@Override
	public void velocityChanged() {
	}

	public void setDestination(int x, int y) {
		destX = x;
		destY = y;
		go = true;
	}

	@Override
	public void move() {

		if (go) {

			if (agent.getX() != destX || agent.getY() != destY) {
				int deltaX = -1 * (agent.getX() - destX);
				int deltaY = -1 * (agent.getY() - destY);

				short vorzeichen = 1;

				if (Math.abs(deltaX) >= Math.abs(deltaY)) {

					// Move ->X
					if (deltaX < 0)
						vorzeichen = -1;

					if (deltaX * vorzeichen < speed)
						agent.setX(agent.getX() + deltaX);
					else
						agent.setX(agent.getX() + vorzeichen * speed);

				} else {

					// Move ->Y
					if (deltaY < 0)
						vorzeichen = -1;

					if (deltaY * vorzeichen < speed)
						agent.setY(agent.getY() + deltaY);
					else
						agent.setY(agent.getY() + vorzeichen * speed);

				}
			} else {
				go = false;
				if (agent.getPreviousMobilityModel() != null)
					agent.setMobilityModel(agent.getPreviousMobilityModel());
			}
		}
	}

	@Override
	public boolean ismobilityTerminated() {
		return !go;
	}
}
