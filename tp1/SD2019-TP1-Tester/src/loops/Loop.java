package loops;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Loop {

	public static _Loop<Integer> times(int loops, boolean parallel) {
		Stream<Integer> stream = IntStream.range(0, loops).boxed();
		return new _Loop<Integer>(loops, parallel ? stream.parallel() : stream);
	}

	public static _Loop<Integer> times(int loops, boolean parallel, AtomicInteger counter, int bound) {
		Stream<Integer> stream = IntStream.range(0, loops).boxed();
		return new _Loop<Integer>(bound, parallel ? stream.parallel() : stream, counter);
	}

	public static <T> _Loop<T> items(Collection<T> items, boolean parallel) {
		Stream<T> stream = items.stream();
		return new _Loop<T>(items.size(), parallel ? stream.parallel() : stream);
	}

	public static class _Loop<T> {

		final int bound;
		final Stream<T> stream;
		final AtomicInteger counter;

		_Loop(int bound, Stream<T> stream) {
			this(bound, stream, new AtomicInteger(0));
		}

		_Loop(int bound, Stream<T> stream, AtomicInteger counter) {
			this.bound = bound;
			this.stream = stream;
			this.counter = counter;
		}

		public void forEach(SilentRunnable r) {
			stream.forEach((i) -> {
				try {
					printCounter();
					r.run();
				} catch (Exception x) {

					throw new RuntimeException(x.getMessage(), x);
				}
			});
		}

		public void forEach(SilentConsumer<T> r) {
			stream.forEach((i) -> {
				try {
					printCounter();
					r.run(i);
				} catch (Exception x) {
					throw new RuntimeException(x.getMessage(), x);
				}
			});
		}

		private void printCounter() {
			System.out.printf("%d/%d%80s\r", counter.incrementAndGet(), bound, " ");
		}
	}

	public static interface SilentRunnable {
		void run() throws Exception;
	}

	public static interface SilentConsumer<T> {
		void run(T i) throws Exception;
	}

}
