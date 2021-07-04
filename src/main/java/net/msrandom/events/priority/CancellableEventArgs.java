package net.msrandom.events.priority;

import net.msrandom.events.EventArgs;

public class CancellableEventArgs extends EventArgs {
    private boolean isCanceled;

    public void cancel() {
        isCanceled = true;
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
