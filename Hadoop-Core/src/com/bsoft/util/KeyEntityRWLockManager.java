package com.bsoft.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 * 
 * @author wjg
 *
 */
public class KeyEntityRWLockManager {
	private ConcurrentHashMap<String, KeyEntityRWLock> locks = new ConcurrentHashMap<String, KeyEntityRWLock>();
	private boolean fair = false;

	public KeyEntityRWLockManager() {

	}

	public KeyEntityRWLockManager(boolean fair) {
		this.fair = fair;
	}

	public int getLockCount() {
		return locks.size();
	}

	public boolean tryReadLock(String key) {
		KeyEntityRWLock lock = locks.get(key);
		if (lock != null) {
			lock.readLock();
			return true;
		} else {
			return false;
		}
	}

	private KeyEntityRWLock getOrCreateLock(String key) {
		KeyEntityRWLock lock = null;
		if (locks.containsKey(key)) {
			lock = locks.get(key);
		} else {
			KeyEntityRWLock newLock = new KeyEntityRWLock(key, fair);
			newLock.setManager(this);
			lock = locks.putIfAbsent(key, newLock);
			if (lock == null) {
				lock = newLock;
			}
		}
		return lock;
	}

	public void readLock(String key) {
		KeyEntityRWLock lock = getOrCreateLock(key);
		lock.readLock();
	}

	public void readLock(String key, long time, TimeUnit unit) {
		KeyEntityRWLock lock = getOrCreateLock(key);
		lock.readLock(time, unit);
	}

	public void readUnlock(String key) {
		if (locks.containsKey(key)) {
			KeyEntityRWLock lock = locks.get(key);
			lock.readUnlock();
		}
	}

	public void writeLock(String key, long time, TimeUnit unit) {
		KeyEntityRWLock lock = getOrCreateLock(key);
		lock.writeLock(time, unit);
	}

	public void writeLock(String key) {
		KeyEntityRWLock lock = getOrCreateLock(key);
		lock.writeLock();
	}

	public void writeUnlock(String key) {
		KeyEntityRWLock lock = locks.get(key);
		if (lock != null) {
			lock.writeUnlock();
		}
	}

	protected void onIdle(String key) {
		locks.remove(key);
	}

}

class KeyEntityRWLock {
	private String key;
	private ReadWriteLock lock;
	private AtomicInteger threadCount = new AtomicInteger(0);
	private KeyEntityRWLockManager manager;

	public KeyEntityRWLock(String key, boolean fair) {
		this.key = key;
		lock = new ReentrantReadWriteLock(fair);
	}

	private void increaseThreads() {
		threadCount.incrementAndGet();
	}

	private void decreaseThreads() {
		int value = threadCount.decrementAndGet();
		if (value <= 0) {
			if (manager != null) {
				manager.onIdle(key);
				manager = null;
			}
		}
	}

	public void readLock() {
		increaseThreads();
		lock.readLock().lock();
	}

	public boolean readLock(long time, TimeUnit unit) {
		increaseThreads();
		try {
			boolean isLocked = lock.readLock().tryLock(time, unit);
			if (!isLocked) {
				decreaseThreads();
			}
			return isLocked;
		} catch (InterruptedException e) {
			decreaseThreads();
			return false;
		}
	}

	public void readUnlock() {
		lock.readLock().unlock();
		decreaseThreads();
	}

	public void writeLock() {
		increaseThreads();
		lock.writeLock().lock();
	}

	public boolean writeLock(long time, TimeUnit unit) {
		increaseThreads();
		try {
			boolean isLocked = lock.writeLock().tryLock(time, unit);
			if (!isLocked) {
				decreaseThreads();
			}
			return isLocked;
		} catch (InterruptedException e) {
			decreaseThreads();
			return false;
		}

	}

	public void writeUnlock() {
		lock.writeLock().unlock();
		decreaseThreads();
	}

	public void setManager(KeyEntityRWLockManager manager) {
		this.manager = manager;
	}

}
