// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.nowa;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

/**
 * A wait-free private semaphore, distilled from the wait-free continuation-stealing approach by Schmaus et. al [1].
 * This semaphore can be used, amongst other things, for efficient coordination and synchronization of fork/join
 * parallelism. See {@link NowaSpanSync} for an fork/join API for virtual threads using this semaphore.
 * <p>
 * This is a private semaphore [2], that means that there can be at most one waiter. The waiter is expected to be the
 * owner of the semaphore, i.e., the thread that constructed the semaphore. The onwer increments the expected signal
 * count of the semaphore by calling {@link #increment()} or {@link #increment(int)}, and eventually waits using
 * {@link #ownerAwait()}. Only after the required number of {@link #signal()} invocations was performed, the owner is
 * allowed to continue.
 * </p>
 * <p>
 * Further threads signal the semaphore by invoking {@link #signal()}.
 * </p>
 * <h2>References</h2>
 * <p>
 * 1: Schmaus, Florian, Nicolas Pfeiffer, Timo Hönig, Jörg Nolte, and Wolfgang Schröder-Preikschat. “Nowa: A Wait-Free
 * Continuation-Stealing Concurrency Platform”. In: 2021 IEEE International Parallel and Distributed Processing
 * Symposium (IPDPS). May 2021, pp. 360–371. doi:
 * <a href="https://doi.org/10.1109/IPDPS49936.2021.00044">10.1109/IPDPS49936.2021.00044</a>.
 * </p>
 * <p>
 * 2: Dijkstra, Edsger W. “The Structure of the “THE”-Multiprogramming System”. In: Communications of the ACM 11.5 (May
 * 1968), pp. 341–346. issn: 0001-0782. doi: <a href="https://doi.org/10.1145/363095.363143">10.1145/363095.363143</a>.
 * </p>
 */
public class NowaSemaphore {

    private int requiredSignalCount;

    private volatile int counter = Integer.MAX_VALUE;
    private static final VarHandle COUNTER;
    private volatile boolean signalled = false;
    private static final VarHandle SIGNALLED;
    static {
        var l = MethodHandles.lookup();
        try {
            COUNTER = l.findVarHandle(NowaSemaphore.class, "counter", int.class);
            SIGNALLED = l.findVarHandle(NowaSemaphore.class, "signalled", boolean.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private final Thread owner = Thread.currentThread();

    private void throwIfNotOwningThread() {
        var currentThread = Thread.currentThread();
        if (currentThread != owner)
            throw new WrongThreadException("Current thread '" + currentThread + "' is not the owner ('" + owner + "')");
    }

    public void increment(int count) {
        throwIfNotOwningThread();
        requiredSignalCount += count;
    }

    public void increment() {
        increment(1);
    }

    public void ownerAwait() throws InterruptedException {
        throwIfNotOwningThread();

        int delta = Integer.MAX_VALUE - requiredSignalCount;

        if (((int) COUNTER.getOpaque(this)) - delta == 0)
            return;

        int oldCounter = (int) COUNTER.getAndAddAcquire(this, -delta);
        if (oldCounter == delta)
            return;

        while (!((boolean) SIGNALLED.getAcquire(this))) {
            LockSupport.park(this);
            if (Thread.interrupted()) throw new InterruptedException();
        }
    }

    public void signal() {
        int oldCounter = (int) COUNTER.getAndAddRelease(this, -1);
        assert oldCounter >= 1;

        if (oldCounter > 1)
            // Counter is still non-zero after decrement, somebody else is responsible for
            // releasing a potential waiter.
            return;

        SIGNALLED.setRelease(this, true);
        LockSupport.unpark(owner);
    }

    public void reset() {
        requiredSignalCount = 0;
        counter = Integer.MAX_VALUE;
        signalled = false;
    }
}
