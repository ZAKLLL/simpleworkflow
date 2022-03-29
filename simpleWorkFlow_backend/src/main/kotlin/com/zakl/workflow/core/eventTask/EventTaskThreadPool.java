package com.zakl.workflow.core.eventTask;

import com.zakl.workflow.common.SpringContextBeanUtils;
import com.zakl.workflow.core.service.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import static com.zakl.workflow.core.eventTask.EventTaskThreadPool.COMPONENT_BEAN_NAME;

/**
 * @program: javaconcurrency
 * @description:
 * @author: ZakL
 * @create: 2019-05-13 10:48
 **/
@Component(value = COMPONENT_BEAN_NAME)
@DependsOn(value = "processService")
@Slf4j
public class EventTaskThreadPool {

    public final static String COMPONENT_BEAN_NAME = "EventTaskThreadPool";

    @Value("${event-task-thread-pool.size:10}")
    private int size;


    private static int seq = 0;

    private final static String THREAD_PREFIX = "EVENT_TASK_THREAD_POOL";

    private final static LinkedList<Callable<EventTaskExecuteResult>> TASK_QUEUE = new LinkedList<>();

    private final static ThreadGroup group = new ThreadGroup("Pool_Group");

    private final static LinkedList<WorkerTask> Thread_Queue = new LinkedList<>();


    private static ProcessService processService;


    @PostConstruct()
    public void beanInit() {
        processService = SpringContextBeanUtils.getBean(ProcessService.class);
        threadPoolInit();
    }


    private EventTaskThreadPool() {
    }


    public void submit(Callable<EventTaskExecuteResult> callable) {
        log.info("接收到EventTaskThread :{}",callable.toString());
        synchronized (TASK_QUEUE) {
            TASK_QUEUE.addLast(callable);
            TASK_QUEUE.notifyAll();
        }
    }

    private void threadPoolInit() {
        for (int i = 0; i < size; i++) {
            createworktask();
        }
    }

    private void createworktask() {
        WorkerTask workerTask = new WorkerTask(group, THREAD_PREFIX + (seq++));
        workerTask.start();
        Thread_Queue.add(workerTask);
    }

    private enum TaskState {
        RUNNING, BLOCKED, FREE, DEAD
    }

    private static class WorkerTask extends Thread {
        private volatile TaskState taskState = TaskState.FREE; //线程池中的工作线程的状态，默认为FREE，可进行工作的状态

        private WorkerTask(ThreadGroup group, String name) {
            super(group, name);
        }

        private TaskState getTaskState() {
            return this.taskState;
        }

        @Override
        public void run() {
            OUTER:
            while (this.taskState != TaskState.DEAD) {
                Callable<EventTaskExecuteResult> callable;
                synchronized (TASK_QUEUE) {
                    while (TASK_QUEUE.isEmpty()) {
                        try {
                            taskState = TaskState.BLOCKED;
                            TASK_QUEUE.wait();
                        } catch (InterruptedException e) {
                            break OUTER;
                        }
                    }
                    callable = TASK_QUEUE.removeFirst(); //取出任务队列的第一个线程任务
                }
                if (callable != null) {
                    taskState = TaskState.RUNNING;
                    try {
                        EventTaskExecuteResult res = callable.call();
                        log.info("EventTask:{} 任务处理完毕,即将执行自动审批动作",res.getIdentityTaskId());
                        //自动审批任务进行完毕后执行自动审批动作
                        processService.completeIdentityTask(res.getIdentityTaskId(), res.getVariables(), res.getAssignValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    taskState = TaskState.FREE;
                }
            }
        }

        private void close() {
            this.taskState = TaskState.DEAD;
        }
    }

}
