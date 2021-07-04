package net.msrandom.events.priority;

import net.msrandom.events.EventHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class PriorityEventHandler<S, A extends CancellableEventArgs> implements EventHandler<S, A> {
    private static final Priority[] PRIORITIES = Priority.values();
    private final Map<Priority, Set<BiConsumer<S, A>>> subscribers = new HashMap<>();
    //Instead of going through priorities to see which one contains the event, we just store the priority.
    private final Map<BiConsumer<S, A>, Priority> reversePriority = new HashMap<>();

    public void subscribe(BiConsumer<S, A> subscriber, Priority priority) {
        subscribers.computeIfAbsent(priority, k -> new HashSet<>()).add(subscriber);
        reversePriority.put(subscriber, priority);
    }

    @Override
    public void subscribe(BiConsumer<S, A> subscriber) {
        subscribe(subscriber, Priority.NORMAL);
    }

    @Override
    public void unsubscribe(BiConsumer<S, A> subscriber) {
        subscribers.get(reversePriority.get(subscriber)).remove(subscriber);
        reversePriority.remove(subscriber);
    }

    @Override
    public void accept(S sender, A args) {
        for (int i = PRIORITIES.length - 1; i >= 0; --i) {
            Priority priority = PRIORITIES[i];
            Set<BiConsumer<S, A>> prioritySubscribers = subscribers.get(priority);
            if (prioritySubscribers != null) {
                if (acceptSubscribers(sender, args, prioritySubscribers)) {
                    return;
                }
            }
        }
    }

    private static <S, A extends CancellableEventArgs> boolean acceptSubscribers(S sender, A args, Set<BiConsumer<S, A>> subscribers) {
        for (BiConsumer<S, A> subscriber : subscribers) {
            subscriber.accept(sender, args);
            if (args.isCanceled()) {
                return true;
            }
        }
        return false;
    }
}
