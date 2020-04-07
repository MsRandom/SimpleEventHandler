package net.msrandom.events.additional;

import net.msrandom.events.IEventHandler;

import java.util.*;
import java.util.function.BiConsumer;

public class PriorityEventHandler<SENDER, ARGS extends CancellableEventArgs> implements IEventHandler<SENDER, ARGS> {
    private Map<Priority, Set<BiConsumer<SENDER, ARGS>>> subscribers = new HashMap<>();
    private Map<BiConsumer<SENDER, ARGS>, Priority> reversePriority = new HashMap<>();

    public void subscribe(BiConsumer<SENDER, ARGS> subscriber, Priority priority) {
        //No usage of computeIfAbsent here as that is slower
        Set<BiConsumer<SENDER, ARGS>> set;
        if (subscribers.containsKey(priority)) set = subscribers.get(priority);
        else {
            set = new HashSet<>();
            subscribers.put(priority, set);
        }
        set.add(subscriber);
        reversePriority.put(subscriber, priority);
    }

    @Override
    public void subscribe(BiConsumer<SENDER, ARGS> subscriber) {
        subscribe(subscriber, Priority.NORMAL);
    }

    @Override
    public void unsubscribe(BiConsumer<SENDER, ARGS> subscriber) {
        subscribers.get(reversePriority.get(subscriber)).remove(subscriber);
        reversePriority.remove(subscriber);
    }

    @Override
    public void accept(SENDER sender, ARGS args) {
        boolean canceled = false;
        for (int i = Priority.values().length - 1; i >= 0; --i) {
            if (canceled) break;
            Priority priority = Priority.values()[i];
            if (subscribers.containsKey(priority)) {
                for (BiConsumer<SENDER, ARGS> subscriber : subscribers.get(priority)) {
                    if (args.isCanceled()) {
                        canceled = true;
                        break;
                    }
                    subscriber.accept(sender, args);
                }
            }
        }
    }
}
