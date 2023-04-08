package com.zincyanide.sync.task;

public class DefaultSyncPartitionTaskQueue extends SyncPartitionTaskQueue
{
    public DefaultSyncPartitionTaskQueue(SyncPartitionTaskQueueConfig config)
    {
        super(config);
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
