/*******************************************************************************
 * Copyright 2014 Celartem, Inc., dba LizardTech
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.lizardtech.expresszip.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.lizardtech.expresszip.vaadin.ExpressZipApplication;

public class BackgroundExecutor implements Executor {

	private static final int THREADPOOL_TIMEOUT = 300;
	static final Logger logger = Logger.getLogger(BackgroundExecutor.class);

	public static class Factory {
		private static BackgroundExecutor instance = null;

		public Factory() {
			if (instance == null) {
				instance = new BackgroundExecutor();

				// Configure logger
				BasicConfigurator.configure();
				logger.debug("BackgroundExecutor logger enabled");

			}
		}

		public BackgroundExecutor getBackgroundExecutor() {
			return instance;
		}

		public Executor getExecutor() {
			return instance;
		}
	};

	private enum TaskStatus {
		FINISHED, QUEUED, RUNNING
	}

	class LTThreadPoolExecutor extends ThreadPoolExecutor {

		public LTThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			try {
				if (t == null) {
					updateStatus(r, TaskStatus.FINISHED);
				} else {
					notifyError(r, t);
				}
			} catch (Exception e) {
				// log & hide the exception
				// is appropriate in this case
				// as throwing the exception upstream
				// would fail the thread pool
			} finally {
				super.afterExecute(r, t);
			}
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			super.beforeExecute(t, r);
			try {
				updateStatus(r, TaskStatus.RUNNING);
			} catch (Exception e) {
			}
		}
	}

	private List<ExecutorListener> taskListeners;
	private BlockingQueue<Runnable> queueRunnable;
	private Map<Runnable, TaskStatus> taskStatusMap;
	private ThreadPoolExecutor threadPool;

	private int numProcs = 1;
	private MailServices mailServices;

	private BackgroundExecutor() {

		queueRunnable = new LinkedBlockingQueue<Runnable>();
		String sThreadPoolSize = ExpressZipApplication.Configuration.getProperty("threadpoolsize");
		int nThreadPoolSize = 5;
		if (sThreadPoolSize != null) {
			try {
				nThreadPoolSize = Integer.parseInt(sThreadPoolSize);
			} catch (NumberFormatException e) {
				ExpressZipApplication.logger.error("Ignoring invalid 'threadpoolsize': " + sThreadPoolSize + " using default of "
						+ nThreadPoolSize + ".");
			}
		}
		threadPool = new LTThreadPoolExecutor(nThreadPoolSize, nThreadPoolSize, THREADPOOL_TIMEOUT, TimeUnit.SECONDS, queueRunnable);
		taskListeners = new ArrayList<ExecutorListener>();
		taskStatusMap = new HashMap<Runnable, TaskStatus>();

		Session session = null;
		try {
			Context inctx = new InitialContext();
			Context enctx = (Context) inctx.lookup("java:comp/env");
			session = (Session) enctx.lookup("mail/Session");
		} catch (NamingException e) {

		}
		if (session != null)
			mailServices = new MailServices(session);
	}

	public MailServices getMailServices() {
		return mailServices;
	}

	public long getNumberOfTasks() {
		return taskStatusMap.size();
	}

	public synchronized void addListener(ExecutorListener listener) {
		taskListeners.add(listener);
	}

	public synchronized void removeListener(ExecutorListener listener) {
		taskListeners.remove(listener);
	}

	public void terminateAllTasks() throws InterruptedException, TimeoutException {
		try {
			threadPool.shutdown(); // Disable new tasks from being submitted

			List<Runnable> waiting = threadPool.shutdownNow(); // terminate all

			// notify any yet to be run
			for (Runnable r : waiting) {
				notifyError(r, new InterruptedException("Cancelled"));
			}

			// Wait a while for existing tasks to terminate
			if (!threadPool.awaitTermination(THREADPOOL_TIMEOUT, TimeUnit.SECONDS)) {
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			threadPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		} finally {
			threadPool = new LTThreadPoolExecutor(numProcs, numProcs * 2, THREADPOOL_TIMEOUT, TimeUnit.SECONDS, queueRunnable);
		}
	}

	public synchronized void execute(Runnable task) {
		threadPool.execute(task);
		updateStatus(task, TaskStatus.QUEUED);
	}

	public synchronized void removeAllListeners() {
		taskListeners.clear();
	}

	public synchronized void updateStatus(Runnable runnable, TaskStatus status) {
		switch (status) {
		case QUEUED:
			taskStatusMap.put(runnable, status);
			break;
		case RUNNING:
			taskStatusMap.put(runnable, status);
			break;
		case FINISHED:
			taskStatusMap.remove(runnable);
			break;
		}

		for (ExecutorListener listener : taskListeners) {
			notifyObserver(listener, runnable, status);
		}

	}

	public synchronized void notifyError(Runnable runnable, Throwable throwable) {
		for (ExecutorListener listener : taskListeners) {
			listener.taskError(runnable, throwable);
		}
		taskStatusMap.remove(runnable);
	}

	private synchronized void notifyObserver(ExecutorListener listener, Runnable runnable, TaskStatus status) {
		switch (status) {
		case QUEUED:
			listener.taskQueued(runnable);
			break;
		case RUNNING:
			listener.taskRunning(runnable);
			break;
		case FINISHED:
			listener.taskFinished(runnable);
			break;
		}
	}
}
