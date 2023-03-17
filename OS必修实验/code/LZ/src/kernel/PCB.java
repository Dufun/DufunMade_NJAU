package kernel;

import java.util.Random;

/**
 * 进程控制块
 *
 * 负责进程的信息存储，调度管理等
 *
 * @author 19220201杜放
 */
public class PCB {
    // 进程状态常量
    public static final short READY_STATE = 0;
    public static final short RUNNING_STATE = 1;
    public static final short FINISH_STATE = 2;

    /**
     * 调度模块
     */
    private ProcessScheduling processschedulingthread;
    /**
     * 进程编号
     */
    private int ProID;
    /**
     * 进程优先级
     * 取值范围1-5，数字越小，优先级越大
     */
    private int Priority;
    /**
     * 指令数量
     */
    private int InstrucNum;
    /**
     * 作业指令集
     */
    private Instruction[] Instruc;
    /**
     * 程序计数器，下一条指令的执行编号
     */
    private int PC;
    /**
     * 指令寄存器，正在执行的指令编号
     */
    private int IR;
    /**
     * 进程状态
     */
    private short PSW;
    /**
     * 进程创建时间
     */
    private int InTimes;
    /**
     * 进程结束时间
     */
    private int EndTimes;
    /**
     * 进程周转时间
     */
    private int TurnTimes;
    /**
     * 进程运行时间
     */
    private int RunTimes;

    public PCB(ProcessScheduling processschedulingthread) {
        this.processschedulingthread = processschedulingthread;
        this.PC                 = 1;
        this.IR                 = 0;
        this.PSW = READY_STATE;
    }

    /**
     * 进程创建原语
     * @param jcb JCB作业控制块
     */
    public void create(JCB jcb) {
        //初始化信息
        this.ProID = jcb.getJobId();
        this.Priority = new Random().nextInt(5)+1;
        this.InstrucNum = jcb.getInstruNum();
        this.InTimes = this.processschedulingthread.getManager().getClock().getCurrentTime();
        this.TurnTimes = this.InTimes-jcb.getInTimes();
        this.RunTimes = 0;
        this.Instruc = jcb.getInstructions();
     synchronized (this.processschedulingthread) {
         //切换至核心态
         this.processschedulingthread.getManager().getCpu().switchToKernelState();
         this.processschedulingthread.getAllPCBQueue().add(this);
         //创建后即加入就绪队列
         this.processschedulingthread.getReadyQueue().add(this);
         //切换回用户态
         this.processschedulingthread.getManager().getCpu().switchToUserState();
         this.processschedulingthread.getManager().getDashboard().process(this.getSchedule().getManager().getClock().getCurrentTime()
                 +"：[创建进程：" + this.ProID +"]");
        }
    }

    /**
     * 进程撤销原语
     */
    public void cancel() {
        synchronized (this.processschedulingthread) {
            //切换至核心态
            this.processschedulingthread.getManager().getCpu().switchToKernelState();
            // 设置收尾信息
            this.PSW = FINISH_STATE;
            this.EndTimes = this.processschedulingthread.getManager().getClock().getCurrentTime();
            this.RunTimes = this.EndTimes - this.InTimes;
            this.TurnTimes=this.TurnTimes+this.RunTimes;
            this.processschedulingthread.getEndPCBQueue().add(this);
            this.processschedulingthread.getAllPCBQueue().remove(this);
            //切换回用户态
            this.processschedulingthread.getManager().getCpu().switchToUserState();
            this.processschedulingthread.getManager().getDashboard().process( this.processschedulingthread.getManager().getClock().getCurrentTime()+"：[终止进程：" + this.ProID +"]");
        }
    }
    public ProcessScheduling getSchedule() {
        return processschedulingthread;
    }

    public void setSchedule(ProcessScheduling processschedulingthread) {
        this.processschedulingthread = processschedulingthread;
    }

    public int getProID() {
        return ProID;
    }

    public void setProID(int proID) {
        this.ProID = proID;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        this.Priority = priority;
    }

    public int getInstrucNum() {
        return InstrucNum;
    }

    public void setInstrucNum(int instrucNum) {
        this.InstrucNum = instrucNum;
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

    public short getPSW() {
        return PSW;
    }

    public void setPSW(short PSW) {
        this.PSW = PSW;
    }

    public int getInTimes() {
        return InTimes;
    }

    public void setInTimes(int inTimes) {
        this.InTimes = inTimes;
    }

    public int getEndTimes() {
        return EndTimes;
    }

    public void setEndTimes(int endTimes) {
        this.EndTimes = endTimes;
    }

    public int getTurnTimes() {
        return TurnTimes;
    }

    public void setTurnTimes(int turnTimes) {
        this.TurnTimes = turnTimes;
    }

    public int getRunTimes() {
        return RunTimes;
    }

    public void setRunTimes(int runTimes) {
        this.RunTimes = runTimes;
    }

    public Instruction[] getInstruc() {
        return Instruc;
    }

    public void setInstruc(Instruction[] instruc) {
        this.Instruc = instruc;
    }
}
