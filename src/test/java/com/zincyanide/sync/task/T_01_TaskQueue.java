package com.zincyanide.sync.task;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class T_01_TaskQueue
{
    private static DefaultSyncPartitionTaskQueue syncPartitionTaskQueue;

    @BeforeClass
    public static void setup()
    {
        syncPartitionTaskQueue = new DefaultSyncPartitionTaskQueue(SyncPartitionTaskQueueConfig.getDefault());
        syncPartitionTaskQueue.boot();
    }

    @Test
    public void t_01_syncTaskWithoutUniteTasks() throws ExecutionException, InterruptedException
    {
        PartitionTask partitionTask = new PartitionTask();
        for (int i = 0; i < syncPartitionTaskQueue.workerNum(); i++)
        {
            final Integer fI = i;
            partitionTask.addTask(new VoidImplTaskUnit(() -> System.out.println(fI)));
        }
        Future<Boolean> future = syncPartitionTaskQueue.offer(partitionTask);

        Assert.assertTrue(future.get());
    }

    @Test
    public void t_02_syncTaskWithUniteTasks() throws ExecutionException, InterruptedException
    {
        PartitionTask partitionTask = new PartitionTask("t_02_syncTaskWithUniteTasks", null,
                3000, TimeUnit.MILLISECONDS);
        for (int i = 0; i < 3 * syncPartitionTaskQueue.workerNum(); i++)
        {
            final int fI = i;
            partitionTask.addTask(
                    new VoidImplTaskUnit(() -> System.out.println(
                            Thread.currentThread().getName()+" : "+fI)));
        }
        Future<Boolean> future = syncPartitionTaskQueue.offer(partitionTask);

        Assert.assertTrue(future.get());
    }

    @Test
    public void t_03_certainTaskOccurException()
    {
        PartitionTask partitionTask = new PartitionTask();
        for (int i = 0; i < syncPartitionTaskQueue.workerNum(); i++)
        {
            final int fI = i;
            partitionTask.addTask(new VoidImplTaskUnit(() -> System.out.println(
                    Thread.currentThread().getName()+" : "+fI)));
        }
        partitionTask.addTask(new VoidImplTaskUnit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println(
                    threadName +" : "+111111);
            throw new RuntimeException(threadName+" occurs an exception");
        }));

        Assert.assertFalse(syncPartitionTaskQueue.execute(partitionTask));
    }

    @AfterClass
    public static void teardown()
    {
        syncPartitionTaskQueue.stop();
    }
}
