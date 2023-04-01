package com.zincyanide.sync.task;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class T_01_TaskQueue
{
    @Test
    public void t_01_syncTaskWithoutUniteTasks() throws ExecutionException, InterruptedException
    {
        DefaultSyncPartitionTaskQueue syncPartitionTaskQueue =
                new DefaultSyncPartitionTaskQueue();

        PartitionTask partitionTask = new PartitionTask();
        for (int i = 0; i < DefaultSyncPartitionTaskQueue.workerNum(); i++)
        {
            final Integer fI = i;
            partitionTask.addTask(new VoidImplTaskUnit(() -> System.out.println(fI)));
        }
        Future<?> future = syncPartitionTaskQueue.offer(partitionTask);

        System.out.println(future.get());
    }

    @Test
    public void t_02_syncTaskWithUniteTasks() throws ExecutionException, InterruptedException
    {
        DefaultSyncPartitionTaskQueue syncPartitionTaskQueue =
                new DefaultSyncPartitionTaskQueue();

        PartitionTask partitionTask = new PartitionTask("t_02_syncTaskWithUniteTasks", null,
                3000, TimeUnit.MILLISECONDS);
        for (int i = 0; i < 3 * DefaultSyncPartitionTaskQueue.workerNum(); i++)
        {
            final int fI = i;
            partitionTask.addTask(
                    new VoidImplTaskUnit(() -> System.out.println(
                            Thread.currentThread().getName()+" : "+fI)));
        }
        Future<?> future = syncPartitionTaskQueue.offer(partitionTask);

        System.out.println(future.get());
    }

    @Test
    public void t_03_occurException() throws ExecutionException, InterruptedException
    {
        DefaultSyncPartitionTaskQueue syncPartitionTaskQueue =
                new DefaultSyncPartitionTaskQueue();

        PartitionTask partitionTask = new PartitionTask();
        for (int i = 0; i < DefaultSyncPartitionTaskQueue.workerNum(); i++)
        {
            final Integer fI = i;
            partitionTask.addTask(new VoidImplTaskUnit(() -> System.out.println(
                    Thread.currentThread().getName()+" : "+fI)));
        }
        partitionTask.addTask(new VoidImplTaskUnit(() -> {
            int i = 1 / 0;
            System.out.println(
                    Thread.currentThread().getName()+" : "+111111);
        }));

        System.out.println(syncPartitionTaskQueue.execute(partitionTask));
    }


}
