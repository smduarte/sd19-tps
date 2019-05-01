package utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class Locks {

	public static void lock(Object id) {
		ReentrantLock lock = locks.get(id), newLock;
		if (lock == null) {
			lock = locks.putIfAbsent(id, newLock = new ReentrantLock(true));
			if (lock == null)
				lock = newLock;
		}
		lock.lock();
	}

	public static void unlock(Object id) {
		ReentrantLock lock = locks.get(id);
		if (lock == null)
			throw new RuntimeException("Unbalanced unlock for :" + id);

		lock.unlock();
	}

	public boolean interrupted() {
		return Thread.currentThread().isInterrupted();
	}

	static ConcurrentHashMap<Object, ReentrantLock> locks = new ConcurrentHashMap<Object, ReentrantLock>();
}
