package de.tu_darmstadt.kom.mobilitySimulator.linkedRTree;

import de.tu_darmstadt.kom.linkedRTree.LinkedRTreeLeafInterface;
import de.tu_darmstadt.kom.linkedRTree.ShapeInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;

public abstract class LinkedRTreeMapEvent extends AbstractMapEvent implements
		ShapeInterface, LinkedRTreeLeafInterface {

}
