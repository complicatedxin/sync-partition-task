package com.zincyanide.sync.task;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO instantial
public abstract class SyncPartitionTaskQueue
{
    public static final Integer DEFAULT_QUEUE_CAPACITY = 100;

    public static final Integer DEFAULT_WORKER_NUM = 8;

    private static final Integer queueCapacity = DEFAULT_QUEUE_CAPACITY;

    private static final Integer workerNum = DEFAULT_WORKER_NUM;

    private static final ExecutorService dispatcher = new ThreadPoolExecutor(
            1, 1,
            0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(queueCapacity),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    private static final ExecutorService workers = new ThreadPoolExecutor(
            workerNum, workerNum,
            60_000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(workerNum),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    /**
     *  async execute
     */
    public Future<Boolean> offer(PartitionTask partitionTask)
    {
        partitionTask.tryToUniteTasks();

        CountDownLatch dispatcherLatch = new CountDownLatch(partitionTask.tasksNum());
        CountDownLatch workersLatch = new CountDownLatch(1);
        AtomicBoolean surprise = new AtomicBoolean(false);

        partitionTask.prepare(workers, dispatcherLatch, workersLatch, surprise);

        return dispatcher.submit(() -> {

            try {
                preOffer(partitionTask);
                partitionTask.execute();

                if(!dispatcherLatch.await(partitionTask.getTimeout(), partitionTask.getTimeoutUnit()))
                    throw new TimeoutException("任务超时: " + partitionTask.getTaskName());

                if(!surprise.get())
                    success();
                else
                    corrupt();

                workersLatch.countDown();
                return !surprise.get();

            } catch (Exception any) {
                any.printStackTrace();

                surprise.set(true);
                workersLatch.countDown();
                corrupt();
                return false;
            } finally {
                postOffer();
            }

        });
    }

    /**
     *  sync invoke
     *  @return {@code false} on corrupted
     */
    public boolean execute(PartitionTask partitionTask)
    {
        try {
            return offer(partitionTask).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected abstract void preOffer(PartitionTask partitionTask);

    protected abstract void postOffer();

    protected abstract void success();

    protected abstract void corrupt();

    public static Integer capacity()
    {
        return queueCapacity;
    }

    public static Integer workerNum()
    {
        return workerNum;
    }

    public static Integer size()
    {
        return ((ThreadPoolExecutor) dispatcher).getQueue().size();
    }

    public static Boolean idle()
    {
        return size() == 0 && ((ThreadPoolExecutor) workers).getQueue().size() == 0;
    }
}
