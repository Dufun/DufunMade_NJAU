package kernel;

import interrupt.InterruptVector;
import os.Manager;

import java.util.Vector;

/**
 * 调度模块
 *
 * 负责作业调度（高级）、进程调度（低级）
 *
 * @author 19220201杜放
 */
public class ProcessScheduling extends Thread{
    /**
     * 系统时间片长度
     */
    public static final int[] SYSTEM_TIME_SLICE = {3,4,5,6,7};
    /**
     * 最大并发进程数，等于PCB池容量，设置为 30
     */
    public static final int MAX_CONCURRENT_PROCESS_NUM = 30;
    /**
     * 系统管理器，用以获取系统资源
     */
    private Manager manager;
    /**
     * 作业管理模块
     */
    private JobManage jobManage;
    /**
     * 全体PCB队列(系统PCB表)
     */
    private Vector<PCB> allPCBQueue;
    /**
     * 完成PCB队列
     */
    private Vector<PCB> endPCBQueue;
    /**
     * 就绪队列
     */
    private Vector<Vector<PCB>> readyQueue;
    private Vector<PCB> readyQueue1;
    private Vector<PCB> readyQueue2;
    private Vector<PCB> readyQueue3;
    private Vector<PCB> readyQueue4;
    private Vector<PCB> readyQueue5;
    /**
     * 作业后备队列
     */
    private Vector<JCB> reserveQueue;
    /**
     * 多级队列（就绪队列为第一队列）
     */



    public ProcessScheduling(Manager manager) {
        super("Process_scheduling_thread");
        this.manager                = manager;
        this.jobManage              = new JobManage(this);
        this.allPCBQueue            = new Vector<>();
        this.endPCBQueue            = new Vector<>();
        this.readyQueue             = new Vector<>();
        this.reserveQueue           = new Vector<>();
        this.readyQueue1            = new Vector<>();
        this.readyQueue2            = new Vector<>();
        this.readyQueue3            = new Vector<>();
        this.readyQueue4            = new Vector<>();
        this.readyQueue5            = new Vector<>();
        this.readyQueue.add(readyQueue1);
        this.readyQueue.add(readyQueue2);
        this.readyQueue.add(readyQueue3);
        this.readyQueue.add(readyQueue4);
        this.readyQueue.add(readyQueue5);
    }

    @Override
    public void run() {
        // 每5秒读取一次新作业请求(至少等时钟开始后才会开始即按下启动按钮后)
        this.manager.getCpu().interrupt(InterruptVector.JOB_REED_INTERRUPT);
        while (true) {
            // 等待可调度时机
            while (!this.manager.getCpu().isCanSchedule()) { }
            // 作业调度
            this.highLevelSchdule();
            // 进程调度
            this.lowLevelSchdule_DJ();
            // 关闭调度
            this.manager.getCpu().closeSchedule();
            // 刷新GUI
            this.manager.getDashboard().refreshRunningProcess();
            // 判断是否存在运行态进程
            if (this.manager.getCpu().getRunningPCB() == null) {
                //若完成全部进程，打印信息
                if(this.jobManage.getReadJobNum()==this.jobManage.getAlljobnum()){
                    this.manager.getDashboard().headtitle("状态统计信息：");
                    for(int i=0;i<this.endPCBQueue.size();i++){
                        PCB temp=this.endPCBQueue.get(i);
                        this.manager.getDashboard().process(temp.getEndTimes()
                                +":["+temp.getProID()+"："+(temp.getEndTimes()-temp.getTurnTimes())+"+"+temp.getInTimes()+"+"+temp.getRunTimes()+"]");
                    }
                    this.manager.getDashboard().startButton.doClick();

                    // 修改文件名为 ProcessResults-？？？-算法名称代号.txt
                    int totalSeconds = this.endPCBQueue.get(this.endPCBQueue.size()-1).getEndTimes();
                    this.manager.renameFile(totalSeconds);
                    return;
                }
                // 不存在，则CPU空闲
                this.manager.getDashboard().consoleLog(this.getManager().getClock().getCurrentTime()+"："+"[CPU空闲]");
            } else {
                // 刷新GUI
                this.manager.getDashboard().refreshCPU();
                this.manager.getDashboard().refreshRunningProcess();
                // 存在 执行当前指令
                this.manager.getCpu().execute();
                // 判断进程是否运行完毕
                if (this.manager.getCpu().getPC() > this.manager.getCpu().getRunningPCB().getInstrucNum()) {
                    // 进程运行完毕
                    // CPU切换内核态
                    this.getManager().getCpu().switchToKernelState();
                    // 撤销当前进程
                    this.manager.getCpu().getRunningPCB().cancel();
                    this.manager.getCpu().setRunningPCB(null);
                    this.manager.getCpu().setTimeSlice(0);
                    // CPU切换用户态
                    this.getManager().getCpu().switchToUserState();
                } else {
                    // 进程未运行完毕
                    // 如果时间片用完，则当前进程 运行态 -> 就绪态
                    if (this.manager.getCpu().getTimeSlice() == 0) {
                        // CPU切换内核态
                        this.getManager().getCpu().switchToKernelState();
                        // 当前PCB 运行态 -> 下一队列
                        synchronized (this) {
                            PCB temp=this.manager.getCpu().getRunningPCB();
                            this.manager.getDashboard().process( this.manager.getClock().getCurrentTime()+"："+"[进入就绪队列"
                                    +(Math.min(temp.getPriority()+1,5))+"：" +temp.getProID()+"，" + (temp.getInstrucNum()-temp.getIR())+"]");
                            this.readyQueue.get(Math.min(temp.getPriority(),4)).add(temp);
                            temp.setPriority(Math.min(temp.getPriority()+1,5));

                        }
                        // 保护CPU现场
                        this.manager.getCpu().getRunningPCB().setPSW(PCB.READY_STATE);
                        this.manager.getCpu().protectSpot();
                        // CPU切换用户态
                        this.getManager().getCpu().switchToUserState();
                    }
                }
            }
            // 刷新GUI
            this.manager.getDashboard().refreshQueues();
        }
    }

    /**
     * 作业调度
     */
    public synchronized void highLevelSchdule() {
        // 尝试从后备队列中挑选作业，创建相应进程
        this.getJobManage().tryAddProcess();
    }

    /**
     * 进程调度
     */
    public synchronized void lowLevelSchdule_DJ() {
        boolean empty=true;
        int index=0;
        for(int i=0;i<5;i++){
            if(this.readyQueue.get(i).size()>0)
            {
                empty=false;
                index=i;
                break;
            }
        }
        // 就绪队列为空，或已有进程处于运行态，则低级调度结束
        if (empty|| this.manager.getCpu().getRunningPCB() != null) {
            return;
        }
        synchronized (this){
            // CPU切换内核态
            this.getManager().getCpu().switchToKernelState();
            // 指定PCB 就绪态 -> 运行态
            this.readyQueue.get(index).get(0).setPSW(PCB.RUNNING_STATE);
            // 恢复CPU现场
            this.getManager().getCpu().recoverSpot(this.readyQueue.get(index).get(0));
            this.manager.getCpu().setTimeSlice(SYSTEM_TIME_SLICE[Math.min(this.readyQueue.get(index).get(0).getPriority()-1,4)]);
            this.readyQueue.get(index).remove(0);
            // CPU切换用户态
            this.getManager().getCpu().switchToUserState();
        }
    }


    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public JobManage getJobManage() {
        return jobManage;
    }

    public void setJobManage(JobManage jobManage) {
        this.jobManage = jobManage;
    }

    public Vector<PCB> getAllPCBQueue() {
        return allPCBQueue;
    }

    public Vector<PCB> getEndPCBQueue() {
        return endPCBQueue;
    }

    public void setAllPCBQueue(Vector<PCB> allPCBQueue) {
        this.allPCBQueue = allPCBQueue;
    }

    public Vector<Vector<PCB>> getReadyQueue() {
        return readyQueue;
    }

    public Vector<JCB> getReserveQueue() {
        return reserveQueue;
    }

}
