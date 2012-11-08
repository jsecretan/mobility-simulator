package de.tu_darmstadt.kom.mobilitySimulator.core.scheduler;

import java.util.Iterator;

import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;

public class AgentRunnable implements Runnable {

	Iterator<AbstractAgent> iterator;
	TerminationListener listener;

	public AgentRunnable(Iterator<AbstractAgent> iterator,
			TerminationListener listener) {
		this.iterator = iterator;
		this.listener = listener;
	}

	@Override
	public void run() {
		// System.out.println("New Task started in: "
		// + Thread.currentThread().getName());
		boolean continiue = true;
		AbstractAgent a = null;
		while (continiue) {
			synchronized (iterator) {
				if (iterator.hasNext())
					a = iterator.next();
				else {
					break;
				}
			}
			a.behave();
		}
		listener.terminated(this);
	}
}
