package net.msrandom.events;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class SimpleEventHandler<S, A extends EventArgs> implements EventHandler<S, A> {
    private final Set<BiConsumer<S, A>> subscribers = new HashSet<>();

    @Override
    public void subscribe(BiConsumer<S, A> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(BiConsumer<S, A> subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void accept(S sender, A args) {
        for (BiConsumer<S, A> subscriber : subscribers) {
            subscriber.accept(sender, args);
        }
    }
}
