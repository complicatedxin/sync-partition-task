package com.zincyanide.sync.task;

import com.zincyanide.sync.task.internal.TaskRunner;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TaskUnit implements Runnable
{
    protected CountDownLatch dispatcherLatch;
    protected CountDownLatch workersLatch;
    protected AtomicBoolean surprise;

    private TaskRunner runner;

    public TaskUnit(Runnable runnable)
    {
        Objects.requireNonNull(runnable);
        runner = runnable::run;
    }

    TaskUnit unite(TaskUnit task) throws UnsupportedOperationException
    {
        if(!this.getClass().equals(task.getClass()))
            throw new UnsupportedOperationException("cannot unite the different type tasks");
        runner = runner.andThen(task.getRunner());
        return this;
    }

    @Override
    public void run()
    {
        oversee(runner).run();
    }

    public Runnable oversee(Runnable runnable)
    {
        return () -> {
            if (surprise.get())
                return;

            try {
                preRun();
                runnable.run();

                dispatcherLatch.countDown();
                workersLatch.await();
                if(!surprise.get())
                    success();
                else
                    corrupt();

            } catch (Exception any) {
                any.printStackTrace();

                surprise.set(true);
                workersLatch.countDown();
                dispatcherLatch.countDown();
                corrupt();

            } finally {
                postRun();
            }
        };
    }

    protected abstract void preRun();

    protected abstract void postRun();

    protected abstract void success();

    protected abstract void corrupt();


    void setDispatcherLatch(CountDownLatch dispatcherLatch)
    {
        this.dispatcherLatch = dispatcherLatch;
    }

    void setWorkersLatch(CountDownLatch workersLatch)
    {
        this.workersLatch = workersLatch;
    }

    void setSurprise(AtomicBoolean surprise)
    {
        this.surprise = surprise;
    }

    public TaskRunner getRunner()
    {
        return runner;
    }
}
