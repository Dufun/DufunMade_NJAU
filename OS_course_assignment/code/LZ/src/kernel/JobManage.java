package kernel;

import java.io.*;
import java.util.Random;
import java.util.Vector;

/**
 * 作业管理
 *
 * 负责作业的相关操作
 *
 * @author 19220201杜放
 */
public class JobManage {
    /**
     * 调度模块
     */
    private ProcessScheduling processschedulingthread;
    /**
     * 作业总数（系统已读取）
     */
    private int readJobNum;
    /**
     * 作业文件里的所有作业
     */
    private int alljobnum;
    /**
     * 加载所有作业的动态数组
     */
    private Vector<JCB> alljobs;
    /**
     * 存放未读的作业的动态数组
     */
    private Vector<JCB> notread;

    public JobManage(ProcessScheduling schedue) {
        this.processschedulingthread = schedue;
        this.alljobs =new Vector<>();
        this.notread =new Vector<>();
        this.readJobNum = 0;
        this.alljobnum=0;
        // 把现有作业读入数组里
        File jobsInputFile = new File("./input1/jobs-input.txt");
        try {
            if (!jobsInputFile.getParentFile().exists())
            {
                jobsInputFile.getParentFile().mkdir();
            }
            // 如果文件不存在，则新建
            if (!jobsInputFile.exists()) {
                jobsInputFile.createNewFile();
            }
            String jobContent;
            String[] jobInfo;
            BufferedReader jobReader = new BufferedReader(new FileReader(jobsInputFile));
            while ((jobContent= jobReader.readLine()) != null&&!jobContent.equals("")) {
                jobInfo = jobContent.split(",");
                int id=Integer.parseInt(jobInfo[0]);
                int intime=Integer.parseInt(jobInfo[1]);
                int instru=Integer.parseInt(jobInfo[2]);
                JCB temp=new JCB(id,intime,instru);
                this.alljobs.add(temp);
                this.notread.add(temp);
                this.notread.get(this.notread.size()-1).setInstructions(readInstructionSet(id,instru));
                this.alljobnum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取指令集
     * @param jobsId 作业id
     * @param instructionNum 指令数
     * @return 指令集数组
     */
    public Instruction[] readInstructionSet(int jobsId, int instructionNum) {
        File instructionFile = new File("./input1/" + jobsId + ".txt");
        try {
            // 读取对应指令集文件
            BufferedReader instructionReader = new BufferedReader(new FileReader(instructionFile));
            Instruction[] instructions = new Instruction[instructionNum];
            for (int i = 0; i < instructionNum; ++i) {
                String[] instructionInfo = instructionReader.readLine().split(",");
                int id = Integer.parseInt(instructionInfo[0]);
                int state = Integer.parseInt(instructionInfo[1]);
                instructions[i] = new Instruction(id, state);
            }
            instructionReader.close();
            return instructions;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 添加作业请求
     *
     * 随机生成新的作业请求，追加到job-input.txt文件中
     */
    public synchronized void addJob() {
        File jobsInputFile = new File("./input1/jobs-input.txt");
        try {
            // 设置追加写入txt
            BufferedWriter appendJob = new BufferedWriter(new FileWriter(jobsInputFile,true));
            // 随机生成新作业信息
            int jobId = ++this.alljobnum;
            int inTime =Math.max(this.processschedulingthread.getManager().getClock().getCurrentTime(),0);
            int instructionNum = new Random().nextInt(10) + 10;
            appendJob.write(jobId + ","  + inTime + "," + instructionNum );
            appendJob.newLine();
            // 关闭文件输出流
            appendJob.close();
            //加入后备作业
            JCB temp=new JCB(jobId,inTime,instructionNum);
            this.alljobs.add(temp);
            this.readJobNum++;
            this.processschedulingthread.getReserveQueue().add(temp);
            // 生成对应的指令集文件
            this.addInstructionSet(jobId, instructionNum);
            this.processschedulingthread.getManager().getDashboard().workinfo(  Math.max(this.processschedulingthread.getManager().getClock().getCurrentTime(),0)+"："
                    +"[新增作业："+ jobId + "," + inTime + "," + instructionNum+"]");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(alljobnum>=5)
        //重置启动按钮的状态
        {
            this.processschedulingthread.getManager().getDashboard().StartButton(true);
            this.processschedulingthread.getManager().getDashboard().label1.setText(" ");
        }
    }

    /**
     * 添加指令集文件
     *
     * @param jobId 作业id
     * @param instructionNum 作业指令数
     */
    public void addInstructionSet(int jobId, int instructionNum) {
        File instructions = new File("./input1/" + jobId + ".txt");
        Instruction[] allInstructions = new Instruction[instructionNum];
        try {
            if(!instructions.exists()){
            instructions.createNewFile();
            }
            int id;
            int state;
            for (int i = 0; i < instructionNum; ++i) {
                id = i + 1;
                state = 0;
                allInstructions[i] = new Instruction(id, state);
            }
            this.processschedulingthread.getReserveQueue().get(this.processschedulingthread.getReserveQueue().size()-1).setInstructions(allInstructions);
            // 写入指令集文件
            BufferedWriter addInstructions = new BufferedWriter(new FileWriter(instructions));
            for (int i = 0; i < instructionNum; ++i) {
                if(i!=0){
                    addInstructions.newLine();
                }
                addInstructions.write(allInstructions[i].toString());
            }
            addInstructions.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 系统读取作业，进入后备队列
     */
    public synchronized void readJobs() {
        for(int i = 0; i<this.notread.size(); i++) {
            //判断作业进入时间
            if (this.notread.get(i).getInTimes() <= this.processschedulingthread.getManager().getClock().getCurrentTime()) {
                //加入后备队列
                this.processschedulingthread.getReserveQueue().add(this.notread.get(i));
                this.processschedulingthread.getManager().getDashboard().workinfo(this.processschedulingthread.getManager().getClock().getCurrentTime()+"："
                        +"[新增作业："+this.notread.get(i).getJobId()+ "，"+notread.get(i).getInTimes()+"，"+this.notread.get(i).getInstruNum()+"]");
                //移出未读队列
                this.notread.remove(i);
                i=0;
                // 系统作业数 +1
                ++this.readJobNum;
            }
        }
    }
    /**
     * 从后备队列中尝试寻找可行的作业，将其转化为进程
     */
    public synchronized void tryAddProcess() {
        for (int i = 0; i<this.processschedulingthread.getReserveQueue().size(); i++ ) {
            JCB tempJcb =this.processschedulingthread.getReserveQueue().get(i);
            // 并发进程数超出系统上限，则不创建新进程
            if (this.processschedulingthread.getAllPCBQueue().size() >= ProcessScheduling.MAX_CONCURRENT_PROCESS_NUM) {
                break;
            }
            // 创建新进程
            PCB newPCB = new PCB(this.processschedulingthread);
            newPCB.create(tempJcb);
            this.processschedulingthread.getManager().getDashboard().process( this.processschedulingthread.getManager().getClock().getCurrentTime()+"：["+"进入就绪队列："
                    +newPCB.getProID()+"，" +newPCB.getInstrucNum()+"]");
            // 后备队列中删除该作业
            this.processschedulingthread.getReserveQueue().remove(i);}
    }

    public int getAlljobnum() {return alljobnum;}
    public int getReadJobNum() {return readJobNum;}
}
