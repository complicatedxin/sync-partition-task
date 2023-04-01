package com.zincyanide.sync.task.internal;

import java.util.Objects;

@FunctionalInterface
public interface TaskRunner extends Runnable
{
    default TaskRunner compose(Runnable before)
    {
        Objects.requireNonNull(before);
        return () -> {
            before.run();
            this.run();
        };
    }

    default TaskRunner andThen(Runnable after)
    {
        Objects.requireNonNull(after);
        return () -> {
            this.run();
            after.run();
        };
    }

    public static TaskRunner starter()
    {
        return () -> {};
    }
}
