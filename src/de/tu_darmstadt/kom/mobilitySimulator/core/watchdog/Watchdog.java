package de.tu_darmstadt.kom.mobilitySimulator.core.watchdog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.plaf.SliderUI;

import de.tu_darmstadt.kom.gui.WatchdogFrame;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;

public class Watchdog {

	private static Watchdog instance;

	public static final short MEMORY_NORMAL = 0;
	public static final short MEMORY_WARNING = 1;
	public static final short MEMORY_ALERT = 2;

	/**
	 * Value between 0 (0%) and 1 (100%) that indicates the percentage of free
	 * memory at which a warning is fired
	 */
	public static final float WARNING_TRESHOLD = 0.3f;

	/**
	 * Value between 0 (0%) and 1 (100%) that indicates the percentage of free
	 * memory at which an alert is fired
	 */
	private static final float ALERT_TRESHOLD = 0.1f;

	/**
	 * Delay in ms between refreshing free memory value
	 */
	private static final int FREEMEM_TEST_DELAY = 400;

	/**
	 * Delay in ms between refreshing total memory value
	 */
	private static final int TOTALMEM_TEST_DELAY = 1000;

	/**
	 * Delay in ms the watchdog asks his listener for reports
	 */
	private static final int REPORT_DELAY = 1000;

	private static final boolean FEEDBACK_ENABLED = false;

	private long totalMemory;
	private long freeMemory;

	private long warningValue;
	private long alertValue;

	private Timer watchdogTimer;
	private TimerTask freeMemTask;
	private TimerTask totalMemTask;
	private TimerTask reportTask;

	private short state;

	// private Set<WatchdogStateListener> listeners;
	private Map<WatchdogStateListener, String> listeners;

	StringBuffer listenerReports = new StringBuffer();

	private WatchdogFrame gui;

	public static Watchdog getInstance() {
		if (instance == null) {
			instance = new Watchdog();
		}
		return instance;
	}

	public static void clearInstance() {
		if (instance != null) {
			instance.gui.setVisible(false);
			instance.freeMemTask.cancel();
			instance.fireUnregisterFromWatchdog();
			if (instance.listeners.size() > 0) {
				instance.listeners.clear();
				System.out.println("Puuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuups");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			instance = null;
		}
	}

	private void fireUnregisterFromWatchdog() {
		Set<WatchdogStateListener> lstCopy = new HashSet<WatchdogStateListener>(listeners.keySet());
		for (WatchdogStateListener listener : lstCopy) {
			listener.unregisterFromWatchdog();
		}
	}

	private Watchdog() {

		totalMemory = Runtime.getRuntime().totalMemory();

		state = -1; // Set state as undefined
		warningValue = 0;
		alertValue = 0;

		gui = new WatchdogFrame();
		gui.setVisible(true);

		setTimer();

		System.out.println("Watchdog started.");

		watchdogTimer.schedule(totalMemTask, TOTALMEM_TEST_DELAY,
				TOTALMEM_TEST_DELAY);
		watchdogTimer.schedule(freeMemTask, 0, FREEMEM_TEST_DELAY);

		listeners = new HashMap<WatchdogStateListener, String>();
	}

	private void setTimer() {
		watchdogTimer = new Timer();
		freeMemTask = new TimerTask() {
			@Override
			public void run() {
				freeMemory = Runtime.getRuntime().freeMemory();
				gui.setFreeMemory(freeMemory);
				checkCritical();
				gui.setCycle(Scheduler.getInstance().getCycle());

			}
		};
		totalMemTask = new TimerTask() {

			@Override
			public void run() {
				totalMemory = Runtime.getRuntime().totalMemory();
				computeWarningAndAlertValues();
				gui.setTotalMemory(totalMemory);
			}
		};

		reportTask = new TimerTask() {
			@Override
			public void run() {
				fireReportCommandToListener();
			}
		};
	}

	private void checkCritical() {
		if (freeMemory <= alertValue && state != MEMORY_ALERT) {
			fireAlert();
			return;
		} else if (freeMemory <= warningValue && freeMemory > alertValue
				&& state != MEMORY_WARNING) {
			fireWarning();
			return;
		} else if (freeMemory > warningValue && state != MEMORY_NORMAL) {
			fireNormal();
			return;
		}
	}

	private void fireAlert() {
		state = MEMORY_ALERT;
		gui.setState(MEMORY_ALERT);
		if (FEEDBACK_ENABLED)
			fireStateToListener(MEMORY_ALERT);
	}

	private void fireWarning() {
		state = MEMORY_WARNING;
		gui.setState(MEMORY_WARNING);
		if (FEEDBACK_ENABLED)
			fireStateToListener(MEMORY_WARNING);
	}

	private void fireNormal() {
		state = MEMORY_NORMAL;
		gui.setState(MEMORY_NORMAL);
		if (FEEDBACK_ENABLED)
			fireStateToListener(MEMORY_NORMAL);
	}

	private void fireStateToListener(short state) {
		for (WatchdogStateListener listener : listeners.keySet()) {
			listener.memoryStateChanged(state);
		}
	}

	private void fireReportCommandToListener() {
		listenerReports.setLength(0);
		for (WatchdogStateListener listener : listeners.keySet()) {
			listenerReports.append(listener.getName()).append(" => ")
					.append(listeners.get(listener)).append("\n");
		}
		gui.setListeners(listenerReports.toString());
		for (WatchdogStateListener listener : listeners.keySet()) {
			listener.reportToWatchdog();
		}
	}

	public short getState() {
		return state;
	}

	public void setState(short state) {
		this.state = state;
	}

	private void computeWarningAndAlertValues() {
		warningValue = (long) (totalMemory * WARNING_TRESHOLD);
		alertValue = (long) (totalMemory * ALERT_TRESHOLD);
	}

	/**
	 * Registers the specified element as listener if it is not already
	 * registered.
	 * 
	 * @param listener
	 *            element to be registered
	 * @return true if not already registered
	 */
	public boolean register(WatchdogStateListener listener) {
		if (listeners.size() == 0) {
			watchdogTimer.schedule(reportTask, REPORT_DELAY, REPORT_DELAY);
		}
		return this.listeners.put(listener, "") == null;
	}

	/**
	 * Unregisters the specified element.
	 * 
	 * @param listener
	 *            listener to be removed
	 * @return true if listener was registered
	 */
	public boolean unregister(WatchdogStateListener listener) {
		if (listeners.size() == 1) {
			reportTask.cancel();
			gui.setListeners("no listeners");
		}
		return listeners.remove(listener) != null;
	}

	public void report(WatchdogStateListener listener, String report) {
		if (listeners.containsKey(listener))
			listeners.put(listener, report);
	}
}