package com.zincyanide.sync.task;

import com.zincyanide.sync.task.util.SyncPartitionTaskQueueConfigBuilder;

public class SyncPartitionTaskQueueConfig
{
    public static final int DEFAULT_QUEUE_CAPACITY = 200;
    public static final int DEFAULT_WORKER_NUM = 8;

    private int queueCapacity;

    private int workerNum;

    /**
     *  folding when tasks is excessive for the number of workers
     */
    private boolean enableAutoFoldTasks;

    /**
     *  default config
     */
    public static final SyncPartitionTaskQueueConfig getDefault()
    {
        SyncPartitionTaskQueueConfig config = new SyncPartitionTaskQueueConfig();
        config.setQueueCapacity(DEFAULT_QUEUE_CAPACITY);
        config.setWorkerNum(DEFAULT_WORKER_NUM);
        config.setEnableAutoFoldTasks(true);
        return config;
    }

    public static final SyncPartitionTaskQueueConfigBuilder builder()
    {
        return new SyncPartitionTaskQueueConfigBuilder();
    }




    public Boolean getEnableAutoFoldTasks()
    {
        return enableAutoFoldTasks;
    }

    public void setEnableAutoFoldTasks(Boolean enableAutoFoldTasks)
    {
        this.enableAutoFoldTasks = enableAutoFoldTasks;
    }

    public Integer getQueueCapacity()
    {
        return queueCapacity;
    }

    public void setQueueCapacity(Integer queueCapacity)
    {
        this.queueCapacity = queueCapacity;
    }

    public Integer getWorkerNum()
    {
        return workerNum;
    }

    public void setWorkerNum(Integer workerNum)
    {
        this.workerNum = workerNum;
    }
}
