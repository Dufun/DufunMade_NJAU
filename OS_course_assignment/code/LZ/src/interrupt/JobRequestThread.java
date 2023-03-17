package interrupt;

import os.Manager;

import java.awt.event.ActionEvent;
import java.util.TimerTask;

/**
 * 作业请求中断
 *
 * 用以添加一条作业请求
 *
 *
 * @author 19220201杜放
 */
public class JobRequestThread extends Thread {
    /**
     * 系统管理器，用以获取系统资源
     */
    private Manager manager;
    /**
     * 判断作业请求的类型
     */
    private int  index;
    public JobRequestThread(Manager manager,int index) {
        super("JobRequest");
        this.manager = manager;
        this.index=index;
    }

    @Override
    public void run() {
        if(this.index==InterruptVector.JOB_REED_INTERRUPT)
        {
            while (true)
            {
                // 每5秒读取一次新作业请求
                if (this.manager.getClock().getCurrentTime() % 5 == 0) {
                    this.manager.getSchedule().getJobManage().readJobs();
                }
            }
        }
        if(this.index==InterruptVector.JOB_ADD_INTERRUPT)
        {
            // 调用作业管理 -> 添加作业
            this.manager.getSchedule().getJobManage().addJob();
        }
    }
}
