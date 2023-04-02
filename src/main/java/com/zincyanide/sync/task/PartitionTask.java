package com.zincyanide.sync.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class PartitionTask
{
    private final String taskName;

    private ExecutorService workers;

    static final int DEFAULT_TIMEOUT = 2500;
    static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    private int timeout;
    private TimeUnit timeoutUnit;

    private Collection<TaskUnit> tasks;

    private BiFunction<Collection<TaskUnit>, Integer, Spliterator<TaskUnit>[]> splitStrategy;

    public PartitionTask()
    {
        this("A Partition Task");
    }
    public PartitionTask(String taskName)
    {
        this(taskName, new ArrayList<>());
    }
    public PartitionTask(String taskName, Collection<TaskUnit> tasks)
    {
        this(taskName, tasks, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }
    public PartitionTask(String taskName, Collection<TaskUnit> tasks, int timeout, TimeUnit timeoutUnit)
    {
        this.taskName = taskName;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    public PartitionTask addTask(TaskUnit task)
    {
        tasks.add(task);
        return this;
    }

    void foldTasksIfExcess(int limit)
    {
        int tasksNum = tasksNum();
        if(tasksNum > limit)
        {
            Spliterator<TaskUnit>[] spliterators = splitTasks(tasks, limit);
            Collection<TaskUnit> taskUnits = new ArrayList<>();
            for(Spliterator<TaskUnit> spliterator : spliterators)
            {
                if(spliterator == null)
                    continue;
                Collection<TaskUnit> tasks = new ArrayList<>();
                spliterator.forEachRemaining(tasks::add);
                taskUnits.add(tasks.stream().reduce(TaskUnit::unite).get());
            }
            this.tasks = taskUnits;
        }
    }

    public void prepare(ExecutorService workers, int workerNum,
                        CountDownLatch dispatcherLatch, CountDownLatch workersLatch, AtomicBoolean surprise)
    {
        if(tasks == null || tasks.isEmpty())
            return;

        this.workers = workers;

        int tasksNum = tasksNum();
        if(tasksNum > workerNum)
            throw new IndexOutOfBoundsException(
                    String.format("任务数量大于执行线程数，excepted le: %s, current: %s", workerNum, tasksNum));

        for(TaskUnit tu : tasks)
        {
            tu.setDispatcherLatch(dispatcherLatch);
            tu.setWorkersLatch(workersLatch);
            tu.setSurprise(surprise);
        }
    }

    @SuppressWarnings({"SuspiciousSystemArraycopy", "unchecked"})
    private Spliterator<TaskUnit>[] splitTasks(Collection<TaskUnit> tasks, int maxPieceNum)
    {
        if(splitStrategy != null)
        {
            Spliterator<TaskUnit>[] spliterators = splitStrategy.apply(tasks, maxPieceNum);
            if(Objects.requireNonNull(spliterators).length > maxPieceNum)
                throw new IndexOutOfBoundsException("split into too much pieces");
            return spliterators;
        }

        Spliterator<?>[] deque = new Spliterator[Integer.highestOneBit(maxPieceNum)];
        int p1 = -1;
        int p2 = deque.length;

        deque[++p1] = tasks.spliterator();
        int round = 31 - Integer.numberOfLeadingZeros(maxPieceNum);
        while(round-- > 0)
        {
            if(p1 > -1)
            {
                while(p1 > -1)
                {
                    Spliterator<?> s = deque[p1--];
                    Spliterator<?> as = s.trySplit();
                    deque[--p2] = s;
                    if(as != null)
                        deque[--p2] = as;
                }
            }
            else
            {
                while (p2 < deque.length)
                {
                    Spliterator<?> s = deque[p2++];
                    Spliterator<?> as = s.trySplit();
                    deque[++p1] = s;
                    if (as != null)
                        deque[++p1] = as;
                }
            }
        }

        Spliterator<TaskUnit>[] spliterators;
        if(p1 > -1)
        {
            spliterators = new Spliterator[p1 + 1];
            System.arraycopy(deque, 0, spliterators, 0, p1+1);
        }
        else
        {
            spliterators = new Spliterator[deque.length - p2];
            System.arraycopy(deque, p2, spliterators, 0, deque.length - p2);
        }
        return spliterators;
    }

    public void execute()
    {
        for (TaskUnit task : tasks)
        {
            workers.submit(task);
        }
    }

    public String getTaskName()
    {
        return taskName;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public TimeUnit getTimeoutUnit()
    {
        return timeoutUnit;
    }

    public int tasksNum()
    {
        return tasks.size();
    }

    public void setSplitStrategy(BiFunction<Collection<TaskUnit>, Integer, Spliterator<TaskUnit>[]> splitStrategy)
    {
        this.splitStrategy = splitStrategy;
    }
}
