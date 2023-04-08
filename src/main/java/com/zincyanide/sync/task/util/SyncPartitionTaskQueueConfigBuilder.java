package com.zincyanide.sync.task.util;

import com.zincyanide.sync.task.SyncPartitionTaskQueueConfig;

public class SyncPartitionTaskQueueConfigBuilder
{
    private SyncPartitionTaskQueueConfig config;

    public SyncPartitionTaskQueueConfigBuilder()
    {
        this(new SyncPartitionTaskQueueConfig());
    }

    public SyncPartitionTaskQueueConfigBuilder(SyncPartitionTaskQueueConfig config)
    {
        this.config = config;
    }

    public SyncPartitionTaskQueueConfig build()
    {
        return config;
    }

    public SyncPartitionTaskQueueConfigBuilder addEnableAutoFoldTasks(Boolean enableAutoFoldTasks)
    {
        config.setEnableAutoFoldTasks(enableAutoFoldTasks);
        return this;
    }

    public SyncPartitionTaskQueueConfigBuilder addQueueCapacity(Integer queueCapacity)
    {
        config.setQueueCapacity(queueCapacity);
        return this;
    }

    public SyncPartitionTaskQueueConfigBuilder addWorkerNum(Integer workerNum)
    {
        config.setWorkerNum(workerNum);
        return this;
    }

}
