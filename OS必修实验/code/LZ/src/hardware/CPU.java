package hardware;

import interrupt.InterruptVector;
import interrupt.JobRequestThread;
import kernel.Instruction;
import kernel.PCB;
import kernel.ProcessScheduling;
import os.Manager;

/**
 * CPU中央处理器，负责程序运行的相关操作
 *
 * 用以处理中断，执行指令等
 *
 * @author 19220201杜放
 */
public class CPU implements InterruptVector {
    /**
     * 系统管理器，用以获取系统资源
     */
    private Manager manager;
    /**
     * 程序计数器，下一条指令的执行编号
     */
    private int PC;
    /**
     * 指令寄存器，正在执行的指令编号
     */
    private int IR;
    /**
     * 状态寄存器 0用户态  1内核态
     */
    private int PSW;
    public static final int USER_STATE = 0;
    public static final int KERNEL_STATE = 1;
    /**
     * 当前运行态的PCB指针
     */
    private PCB runningPCB;
    /**
     * 允许调度标志，控制调度时机
     * 只有该标志打开后，才可以进行三级调度，否则CPU执行指令和调度操作将会出现混乱
     */
    private volatile boolean canSchedule;
    /**
     * 当前剩余时间片
     */
    private int timeSlice;

    public CPU(Manager manager) {
        this.manager                    = manager;
        this.PC                         = 1;
        this.IR                         = 0;
        this.PSW                        = USER_STATE;
        this.runningPCB                 = null;
        //默认不可调度 等待时钟中断处理操作
        this.canSchedule                = false;
        this.timeSlice                  = 0;

    }

    /**
     * 中断处理
     * @param interruptVector 中断向量 用以区别中断例程
     */
    public synchronized void interrupt(int interruptVector) {
        switch (interruptVector) {
            // 时钟中断处理
            case CLOCK_INTERRUPT: {
                // 允许调度
                this.openSchedule();
                break;
            }
            // 实时作业请求中断
            case JOB_ADD_INTERRUPT: {
                //切换至内核态
                this.switchToKernelState();
                //作业请求中断处理
                new JobRequestThread(this.manager,JOB_ADD_INTERRUPT).start();
                break;
            }
            case JOB_REED_INTERRUPT:{
                //切换至内核态
                this.switchToKernelState();
                //作业请求中断处理
                new JobRequestThread(this.manager,JOB_REED_INTERRUPT).start();
                break;
            }
            default: {
                break;
            }
        }
        this.switchToUserState();
    }

    /**
     * 执行当前指令
     */
    public synchronized void execute() {
        // 刷新GUI
        this.manager.getDashboard().refreshRunningProcess();
        if (this.runningPCB == null) {
            return;
        }
        // 指令指针自增并获取当前指令
        this.IR = this.PC++;
        this.getRunningPCB().setPC(this.PC);
        this.getRunningPCB().setIR(this.IR);
        Instruction currentInstrction = this.runningPCB.getInstruc()[IR-1];
        //更新运行时间
        this.getRunningPCB().setRunTimes(this.manager.getClock().getCurrentTime()-this.getRunningPCB().getInTimes());
        //输出信息
        this.manager.getDashboard().consoleLog(this.manager.getClock().getCurrentTime()+"："+"[运行进程：" + this.runningPCB.getProID() + "，" +
                 currentInstrction.getInstruc_ID() +"，计算]");
        // 时间片 -1
        --this.timeSlice;
    }

    /**
     * 恢复CPU现场
     * @param pcb 即将进入运行态的进程 PCB
     */
    public synchronized void recoverSpot(PCB pcb) {
        this.PC         = pcb.getPC();
        this.IR         = pcb.getIR();
        this.timeSlice  = ProcessScheduling.SYSTEM_TIME_SLICE;
        // 进程设置运行态
        this.runningPCB = pcb;
        // 更新GUI
        this.manager.getDashboard().refreshRunningProcess();
    }

    /**
     * 保护CPU现场
     */
    public synchronized void protectSpot() {
        this.runningPCB.setIR(this.IR);
        this.runningPCB.setPC(this.PC);
        // 进程解除运行态
        this.runningPCB = null;
        // 更新GUI
        this.manager.getDashboard().refreshRunningProcess();
    }

    /**
     * 打开调度
     */
    public synchronized void openSchedule() {
        this.canSchedule = true;
    }

    /**
     * 关闭调度
     */
    public synchronized void closeSchedule() {
        this.canSchedule = false;
    }

    /**
     * 切换内核态
     */
    public synchronized void switchToKernelState() {
        if (this.PSW == KERNEL_STATE) {
            return;
        }
        this.PSW = KERNEL_STATE;
        this.manager.getDashboard().refreshCPU();
    }

    /**
     * 切换用户态
     */
    public synchronized void switchToUserState() {
        if (this.PSW == USER_STATE) {
            return;
        }
        this.PSW = USER_STATE;
        this.manager.getDashboard().refreshCPU();
    }

    public Manager getManager() {
        return manager;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getIR() {
        return IR;
    }

    public void setIR(int IR) {
        this.IR = IR;
    }

    public int getPSW() {
        return PSW;
    }

    public void setPSW(int PSW) {
        this.PSW = PSW;
    }

    public PCB getRunningPCB() {
        return runningPCB;
    }

    public void setRunningPCB(PCB runningPCB) {
        this.runningPCB = runningPCB;
    }

    public boolean isCanSchedule() {
        return canSchedule;
    }

    public void setCanSchedule(boolean canSchedule) {
        this.canSchedule = canSchedule;
    }

    public int getTimeSlice() {
        return timeSlice;
    }

    public void setTimeSlice(int timeSlice) {
        this.timeSlice = timeSlice;
    }
}
