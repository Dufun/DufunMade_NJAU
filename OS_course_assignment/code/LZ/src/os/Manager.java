package os;

import gui.Dashboard;
import hardware.*;
import kernel.ProcessScheduling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * 管理器
 *
 * 管理仿真系统的一切操作
 *
 * @author 19220201杜放
 */
public class Manager {
    /**
     * 系统时钟
     */
    private Clock clock;
    /**
     * CPU 中央处理器
     */
    private CPU cpu;
    /**
     * 进程调度模块
     */
    private ProcessScheduling schedue;
    /**
     * 图像化界面
     */
    private Dashboard dashboard;

    /**
     * 系统输出流与文件流
     */
    private PrintStream out = System.out;
    private PrintStream fileStream;

    public Manager() {
        // 将控制台内容重定向到指定文件
        File ProcessResults = new File("./output1/ProcessResults-LZ.txt");
        try {
            //如果路径不存在则新建路径
            if (!ProcessResults.getParentFile().exists())
            {
                ProcessResults.getParentFile().mkdir();
            }
            // 如果文件不存在，则新建
            if (!ProcessResults.exists()) {
                ProcessResults.createNewFile();
            }
            // 改变系统输出流
            this.fileStream = new PrintStream(new FileOutputStream(ProcessResults));
            System.setOut(this.fileStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 启动图形界面
        this.dashboard      = new Dashboard(this);
        this.clock          = new Clock(this);
        this.cpu            = new CPU(this);
        this.schedue = new ProcessScheduling(this);
    }

    /**
     * 开始运行
     */
    public void start() {
        //激活添加作业按钮
        this.dashboard.AddjobButton(true);
        //若作业数大于等于3，激活启动按钮，否则提示添加作业
        this.dashboard.StartButton(this.schedue.getJobManage().getAlljobnum()>=3);
        if (this.schedue.getJobManage().getAlljobnum()<3){
            this.schedue.getManager().getDashboard().label1.setText("并发作业数不足3,请点击“添加作业”按钮加至3后按下启动按钮开始!");
        }
        // 时钟线程启动 等待启动按钮按下开始计时
        this.clock.start();
        // 调度线程启动(至少等时钟开始后才会开始即按下启动按钮后)
        this.schedue.start();
        this.dashboard.headtitle("进程调度事件：");
    }

    /**
     * 修改文件名为 ProcessResults-？？？-算法名称代号.txt
     * @param totalSeconds
     */
    public void renameFile(int totalSeconds) {
        // 文件流关闭，换回原有输出流
        this.fileStream.close();
        System.setOut(this.out);

        File oldName = new File("./output1/ProcessResults-LZ.txt");
        File newName = new File("./output1/ProcessResults-" + totalSeconds + "-LZ.txt");

        // 删除已经存在的文件
        if(newName.exists()) {
            newName.delete();
        }

        // System.out.println("./output1/ProcessResults-" + totalSeconds + "-LZ.txt");
        if(oldName.renameTo(newName)){
            // System.out.println("文件重命名成功");
        } else {
            System.out.println("文件重命名失败");
        }
    }

    public Clock getClock() {
        return clock;
    }

    public CPU getCpu() {
        return cpu;
    }

    public ProcessScheduling getSchedule() {
        return schedue;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

}
