package com.zincyanide.sync.task;

/**
 *  未实现任何功能的同步任务单元
 */
public class VoidImplTaskUnit extends TaskUnit
{
    public VoidImplTaskUnit(Runnable task)
    {
        super(task);
    }

    @Override
    protected void preRun()
    {   }

    @Override
    protected void postRun()
    {   }

    @Override
    protected void success()
    {   }

    @Override
    protected void corrupt()
    {   }
}
