package de.tu_darmstadt.kom.mobilitySimulator.map;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapStrategyInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.Watchdog;
import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.WatchdogStateListener;

public class EHCacheMapFactory extends MapFactory implements
		WatchdogStateListener, SchedulerHookInterface {

	private final Ehcache cache;

	private MapStrategyInterface strategy;

	private short memoryState;

	private long hits;

	private long misses;

	// public void setAsMapFactory() {
	// instance = this;
	// }

	public EHCacheMapFactory() {
		instance = this;
		cache = CacheManager.getInstance().getCache("largeMemoryNoDisk");
		Watchdog.getInstance().register(this);
		strategy = new MapStrategy();
		Scheduler.getInstance().register(this);

		hits = 0;
		misses = 0;
	}

	@Override
	public int[][] getMap(int[] dest, int maxUp, int maxDown, boolean withEvents) {

		StringBuilder identifier = new StringBuilder();

		identifier.append(dest[0]);
		for (int i = 1; i < dest.length; i++) {
			identifier.append("-" + dest[i]);
		}

		Element element;
		if ((element = cache.get(identifier.toString())) != null) {
			// Cache hit
			return (int[][]) element.getValue();
		} else {
			// Cache miss
			int[][] map = new int[DiscreteMap.getInstance().getSizeY()][DiscreteMap
					.getInstance().getSizeX()];
			strategy.buildSinkMap(map, dest, maxUp, maxDown, withEvents);
			cache.put(new Element(identifier.toString(), map));
			return map;
		}
	}

	@Override
	public int[][] getMapIncludingEvents(int[] dest, int maxUp, int maxDown) {
		return getMap(dest, maxUp, maxDown, true);
	}

	@Deprecated
	public int[][] getMapIncludingEvent(int[] dest, int eventID) {
		StringBuilder identifier = new StringBuilder();
		identifier.append(dest[0]);
		for (int i = 1; i < dest.length; i++) {
			identifier.append("-" + dest[i]);
		}
		identifier
				.append("#"
						+ eventID
						+ "~"
						+ ((AbstractMapEvent) Scheduler.mapEventRepository
								.get(eventID)).getSizeIndicator());

		Element element;
		if ((element = cache.get(identifier.toString())) != null) {
			// Cache hit
			return (int[][]) element.getValue();
		} else {
			// Cache miss
			int[][] map = new int[DiscreteMap.getInstance().getSizeX()][DiscreteMap
					.getInstance().getSizeY()];
			int[][] eventMap = ((AbstractMapEvent) Scheduler.mapEventRepository
					.get(eventID)).getImpactMap();
			// strategy.makeInfinitieMap(map);
			// strategy.buildSinkMap(dest, map, eventMap);
			cache.put(new Element(identifier.toString(), map));
			return map;
		}

	}

	@Override
	public void memoryStateChanged(short state) {
		memoryState = state;
	}

	@Override
	public void reportToWatchdog() {
		StringBuffer sb = new StringBuffer("Maps in Chache: "
				+ cache.getStatistics().getObjectCount() + "\n\thit/miss:"
				+ cache.getStatistics().getInMemoryHits() + "/"
				+ cache.getStatistics().getInMemoryMisses());
		Watchdog.getInstance().report(this, sb.toString());

	}

	public long getSize() {
		return cache.getStatistics().getObjectCount();
	}

	public long getHits() {
		long deltaHits = cache.getStatistics().getInMemoryHits() - hits;
		hits = cache.getStatistics().getInMemoryHits();
		return deltaHits;

	}

	public long getMisses() {
		long deltaMisses = cache.getStatistics().getInMemoryMisses() - misses;
		misses = cache.getStatistics().getInMemoryMisses();
		return deltaMisses;
	}

	@Override
	public String getName() {
		return "MapCache";
	}

	@Override
	public void preRun() {
	}

	@Override
	public void preSimulation() {
	}

	@Override
	public void postSimulation() {
	}

	@Override
	public void preCycle() {
	}

	@Override
	public void postCycle() {
	}

	@Override
	public String toString() {
		return "MapCache root";
	}

	@Override
	public void unregisterFromWatchdog() {
		Watchdog.getInstance().unregister(this);
	}
}
