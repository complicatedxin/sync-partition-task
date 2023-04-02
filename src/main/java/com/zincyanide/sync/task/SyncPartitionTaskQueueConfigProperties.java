package com.zincyanide.sync.task;

public class SyncPartitionTaskQueueConfigProperties
{
    public static final int DEFAULT_QUEUE_CAPACITY = 200;
    public static final int DEFAULT_WORKER_NUM = 8;

    private Integer queueCapacity;

    private Integer workerNum;

    /**
     *  folding when tasks is excessive for the number of workers
     */
    private Boolean enableAutoFoldTasks;

    /**
     *  default config
     */
    public SyncPartitionTaskQueueConfigProperties()
    {
        queueCapacity = DEFAULT_QUEUE_CAPACITY;
        workerNum = DEFAULT_WORKER_NUM;
        enableAutoFoldTasks = true;
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
