package net.msrandom.events;

import java.util.function.BiConsumer;

public interface EventHandler<S, A extends EventArgs> extends BiConsumer<S, A> {
    void subscribe(BiConsumer<S, A> subscriber);

    void unsubscribe(BiConsumer<S, A> subscriber);

    @Override
    void accept(S sender, A args);
}
