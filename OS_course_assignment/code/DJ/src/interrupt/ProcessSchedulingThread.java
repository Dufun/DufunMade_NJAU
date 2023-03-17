package interrupt;

import os.Manager;

import java.util.TimerTask;

/**
 * 时钟中断
 *
 * 仅仅被时钟类调用，负责进行在一个时钟周期内系统的全部操作
 * 时钟暂停，则所有工作暂停
 *
 * @author 19220201杜放
 */
public class ProcessSchedulingThread extends TimerTask {
    /**
     * 系统管理器，用以获取系统资源
     */
    private Manager manager;

    public ProcessSchedulingThread(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        // 当前暂停标志为真，则不进行相关操作，调度也无法进行
        if (this.manager.getClock().isPause()) {
            return;
        }
        // 时钟中断处理操作
        this.handleClockInterrupt();
    }

    /**
     * 时钟中断处理函数
     */
    public void handleClockInterrupt() {
        // 系统时间递增
        this.manager.getClock().addTime();
        // 刷新GUI
        this.manager.getDashboard().refreshTime(this.manager.getClock().getCurrentTime());
        this.manager.getCpu().interrupt(InterruptVector.CLOCK_INTERRUPT);

    }
}
