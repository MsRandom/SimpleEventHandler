import net.msrandom.events.EventArgs;
import net.msrandom.events.EventHandler;
import net.msrandom.events.SimpleEventHandler;
import net.msrandom.events.priority.CancellableEventArgs;
import net.msrandom.events.priority.Priority;
import net.msrandom.events.priority.PriorityEventHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicEvents {
    private static EventHandler<Object, IntegerEventArgs> event;
    private static PriorityEventHandler<Object, IntegerEventArgs> priorityEvent;

    @BeforeEach
    public void setup() {
        event = new SimpleEventHandler<>();
        priorityEvent = new PriorityEventHandler<>();
    }

    @Test
    @DisplayName("Subscribing and invoking")
    public void general() {
        AtomicInteger value = new AtomicInteger();
        AtomicInteger value1 = new AtomicInteger();
        BiConsumer<Object, IntegerEventArgs> subscriber = (sender, args) -> value.set(args.value);
        event.subscribe(subscriber);
        event.subscribe((sender, args) -> value1.set((int) Math.pow(2, args.value)));
        event.accept(null, new IntegerEventArgs(5));

        assertEquals(5, value.get());
        assertEquals(32, value1.get());

        event.unsubscribe(subscriber);
        event.accept(null, new IntegerEventArgs(16));

        assertEquals(5, value.get());
        assertEquals(65536, value1.get());
    }

    @Test
    @DisplayName("Priority")
    public void priority() {
        AtomicInteger current = new AtomicInteger();

        BiConsumer<Object, IntegerEventArgs> incrementTo3 = (sender, args) -> assertEquals(3, current.incrementAndGet());

        priorityEvent.subscribe((sender, args) -> assertEquals(1, current.incrementAndGet()), Priority.HIGH);
        priorityEvent.subscribe((sender, args) -> assertEquals(2, current.incrementAndGet()));
        priorityEvent.subscribe(incrementTo3, Priority.LOW);

        priorityEvent.accept(null, new IntegerEventArgs(0));
        assertEquals(3, current.get());
        current.set(0);

        priorityEvent.unsubscribe(incrementTo3);
        priorityEvent.accept(null, new IntegerEventArgs(0));
        assertEquals(2, current.get());
    }

    @Test
    @DisplayName("Cancellation")
    public void cancel() {
        AtomicInteger current = new AtomicInteger();

        priorityEvent.subscribe((sender, args) -> assertEquals(1, current.incrementAndGet()), Priority.HIGH);

        priorityEvent.subscribe((sender, args) -> {
            assertEquals(2, current.incrementAndGet());
            args.cancel();
        });

        priorityEvent.subscribe((sender, args) -> current.incrementAndGet(), Priority.LOW);

        priorityEvent.accept(null, new IntegerEventArgs(0));

        assertEquals(2, current.get());
    }

    private static class IntegerEventArgs extends CancellableEventArgs {
        private final int value;

        private IntegerEventArgs(int value) {
            this.value = value;
        }
    }
}
