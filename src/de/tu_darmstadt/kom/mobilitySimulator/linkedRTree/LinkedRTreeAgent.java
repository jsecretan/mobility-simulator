package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

import de.tu_darmstadt.kom.linkedRTree.LinkedRTreeLeafInterface;
import de.tu_darmstadt.kom.linkedRTree.LinkedRTreeNode;
import de.tu_darmstadt.kom.linkedRTree.Point;
import de.tu_darmstadt.kom.linkedRTree.PointInterface;
import de.tu_darmstadt.kom.linkedRTree.ShapeInterface;
import de.tu_darmstadt.kom.linkedRTree.Exception.PositionOutOfScope;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;

public class LinkedRTreeAgent extends AbstractAgent implements
		LinkedRTreeLeafInterface, PointInterface {

	PointInterface position;

	private LinkedRTreeNode<LinkedRTreeLeafInterface> owner;

	public LinkedRTreeAgent(int x, int y, boolean enableMobileCom) {
		super(x, y, enableMobileCom);
		position = new Point(x, y);
	}

	@Override
	public PointInterface getCenter() {
		return this;
	}

	@Override
	public boolean contains(ShapeInterface shape) {
		return position.contains(shape);
	}

	@Override
	public boolean intersects(ShapeInterface shape) {
		return position.intersects(shape);
	}

	@Override
	public int getX() {
		return position.getX();
	}

	@Override
	public void setX(int X) {
		position.setX(X);
		applyAndLogMovement();
	}

	@Override
	public int getY() {
		return position.getY();
	}

	@Override
	public void setY(int Y) {
		position.setY(Y);
		applyAndLogMovement();
	}

	@Override
	public int getZ() {
		return position.getZ();
	}

	@Override
	public void setZ(int zPos) {
		position.setZ(zPos);
		applyAndLogMovement();
	}

	public void setPos(PointInterface p) {
		position = p;
		applyAndLogMovement();
	}

	@Override
	public void setPos(int x, int y) {
		position.setX(x);
		position.setY(y);
		applyAndLogMovement();
	}

	@Override
	public void setPos(int x, int y, int z) {
		position.setX(x);
		position.setY(y);
		position.setZ(z);
		applyAndLogMovement();
	}

	@Override
	public void setPos(int[] pos) {
		position.setX(pos[0]);
		position.setY(pos[1]);
		if (pos.length > 2)
			position.setZ(pos[2]);
		applyAndLogMovement();
	}

	@Override
	public void setCenter(PointInterface p) {
		position = p;
		applyAndLogMovement();
	};

	public void setCenter(int x, int y, int z) {
		position.setX(x);
		position.setY(y);
		position.setZ(z);
		applyAndLogMovement();
	}

	@Override
	public void setCenter(int x, int y) {
		position.setX(x);
		position.setY(y);
		applyAndLogMovement();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PointInterface)
			equals((PointInterface) obj);
		return super.equals(obj);
	}

	public boolean equals(PointInterface point) {
		return (point.getX() == position.getX() && point.getY() == position
				.getY());
	}

	@Override
	public void move(PointInterface p) {
		setPos(p);
		applyAndLogMovement();
	}

	@Override
	public void move(int x, int y) {
		setPos(x, y);
		applyAndLogMovement();
	}

	@Override
	public void move(int x, int y, int z) {
		setPos(x, y, z);
		applyAndLogMovement();
	}

	@Override
	public void move(int[] pos) {
		setPos(pos);
		applyAndLogMovement();
	}

	private void applyAndLogMovement() {
		try {
			if (owner != null)
				owner.moveData(this);
			Scheduler.getInstance().getOutput()
					.agentPositionChanged(id, position.getX(), position.getY());
		} catch (PositionOutOfScope e) {
			// TODO implement better catching
			System.err.println("Position: " + position.getX() + ","
					+ position.getY() + "," + position.getZ());
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public int getArea() {
		return position.getArea();
	}

	@Override
	public String toString() {
		return "Agent " + id + " (" + getX() + "," + getY() + "," + getZ()
				+ ")";
	}

	public LinkedRTreeNode<LinkedRTreeLeafInterface> getOwner() {
		return owner;
	}

	public void setOwner(LinkedRTreeNode<LinkedRTreeLeafInterface> owner) {
		this.owner = owner;
	}

	@Override
	public void settle() {
		// Nothing to do because Agentshape is Point
	}

	@Override
	public void unsettle() {
		// Nothing to do because Agentshape is Point
	}

}
