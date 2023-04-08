package com.zincyanide.sync.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SyncPartitionTaskQueue
{
    static final Logger logger =
            LogManager.getLogger(SyncPartitionTaskQueue.class);

    private final int queueCapacity;

    private final int workerNum;

    private SyncPartitionTaskQueueConfig config;

    private volatile ExecutorService dispatcher;

    private volatile ExecutorService workers;

    private ArrayBlockingQueue<Runnable> dispatcherQueue;

    public SyncPartitionTaskQueue(SyncPartitionTaskQueueConfig config)
    {
        this.queueCapacity = config.getQueueCapacity();
        this.workerNum = config.getWorkerNum();
        this.config = config;
    }

    public void boot()
    {
        if(dispatcher == null)
        {
            synchronized (this)
            {
                if(dispatcher == null)
                {
                    dispatcher = new ThreadPoolExecutor(
                            1, 1,
                            0, TimeUnit.MILLISECONDS,
                            dispatcherQueue = new ArrayBlockingQueue<>(queueCapacity),
                            Executors.defaultThreadFactory(),
                            new ThreadPoolExecutor.AbortPolicy()
                    );
                    workers = new ThreadPoolExecutor(
                            workerNum, workerNum,
                            0, TimeUnit.MILLISECONDS,
                            new ArrayBlockingQueue<>(workerNum),
                            Executors.defaultThreadFactory(),
                            new ThreadPoolExecutor.AbortPolicy()
                    );
                }
            }
            logger.info("SyncPartitionTaskQueue started");
        }
        else if(dispatcher.isShutdown())
            logger.warn("SyncPartitionTaskQueue had started, but shutdown now");
        else
            logger.warn("SyncPartitionTaskQueue is already started");
    }

    public void stop()
    {
        synchronized (this)
        {
            if(dispatcher != null && !dispatcher.isShutdown())
            {
                dispatcher.shutdown();
                workers.shutdown();
            }
        }
        logger.info("SyncPartitionTaskQueue stopped");
    }

    /**
     *  async execute
     */
    public Future<Boolean> offer(PartitionTask partitionTask)
    {
        if(!isStarting())
            throw new RejectedExecutionException("task dispatcher is not starting");

        if(config.getEnableAutoFoldTasks())
            partitionTask.foldTasksIfExcess(workerNum);

        CountDownLatch dispatcherLatch = new CountDownLatch(partitionTask.tasksNum());
        CountDownLatch workersLatch = new CountDownLatch(1);
        AtomicBoolean surprise = new AtomicBoolean(false);
        partitionTask.prepare(workers, workerNum, dispatcherLatch, workersLatch, surprise);

        return dispatcher.submit(() -> {

            try {
                preOffer(partitionTask);
                partitionTask.execute();

                if(!dispatcherLatch.await(partitionTask.getTimeout(), partitionTask.getTimeoutUnit()))
                    throw new TimeoutException("任务超时: " + partitionTask.getTaskName());

                if(!surprise.get())
                    access();
                else
                    disrupt();

                workersLatch.countDown();
                return !surprise.get();

            } catch (Exception any) {
                any.printStackTrace();

                surprise.set(true);
                workersLatch.countDown();
                disrupt();
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

    protected abstract void access();

    protected abstract void disrupt();


    public SyncPartitionTaskQueueConfig getConfig()
    {
        return config;
    }

    public void setConfig(SyncPartitionTaskQueueConfig config)
    {
        this.config = config;
    }


    public boolean isStarting()
    {
        return dispatcher != null && !dispatcher.isShutdown();
    }

    public Integer capacity()
    {
        return queueCapacity;
    }

    public Integer workerNum()
    {
        return workerNum;
    }

    public Integer size()
    {
        return dispatcherQueue.size();
    }

    public Boolean idle()
    {
        return size() == 0 && ((ThreadPoolExecutor) workers).getQueue().size() == 0;
    }

    public Integer remainingCapacity()
    {
        return dispatcherQueue.remainingCapacity();
    }
}
