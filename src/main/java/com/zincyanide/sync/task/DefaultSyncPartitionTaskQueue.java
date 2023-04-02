package com.zincyanide.sync.task;

public class DefaultSyncPartitionTaskQueue extends SyncPartitionTaskQueue
{
    public DefaultSyncPartitionTaskQueue()
    {   }
    public DefaultSyncPartitionTaskQueue(int queueCapacity, int workerNum)
    {
        super(queueCapacity, workerNum);
    }

    @Override
    protected void preOffer(PartitionTask partitionTask)
    {   }

    @Override
    protected void postOffer()
    {   }

    @Override
    protected void access()
    {   }

    @Override
    protected void disrupt()
    {   }
}
