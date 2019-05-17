package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Lock implements AutoCloseable {

	List<Object> _ids;

	public Lock(Object... ids) {
		_ids = new ArrayList<>(new HashSet<>(Arrays.asList(ids)));
		_ids.forEach(Lock::acquire);
	}

	@Override
	public void close() throws Exception {
		Collections.reverse(_ids);
		_ids.forEach(i -> _locks.get(i).unlock());
	}

	static private void acquire(Object id) {
		ReentrantLock lock = _locks.computeIfAbsent(id, (__) -> new ReentrantLock(true));
		lock.lock();
	}

	public static void disposeAll() {
		_locks.clear();
	}

	static ConcurrentHashMap<Object, ReentrantLock> _locks = new ConcurrentHashMap<>();
}
