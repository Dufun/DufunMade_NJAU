
package gui;

import hardware.CPU;
import interrupt.InterruptVector;
import kernel.PCB;
import os.Manager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


/**
 * 操作控制仪表盘，用以实现图像化界面
 *
 * @author 19220201杜放
 */
public class Dashboard extends JFrame {
    private Manager manager;

    public StyledDocument doc;
    public JTable readyTable;
    public DefaultTableModel readyTableInfo;

    private SimpleAttributeSet logStyle;
    private SimpleAttributeSet process;
    private SimpleAttributeSet workinfo;
    private SimpleAttributeSet headtitle;

    public Dashboard(Manager manager) {
        this.manager = manager;
        // 初始化界面元素
        this.initComponents();
        // 初始化界面数据格式
        this.initDataStructure();
        // 显示界面
        this.setVisible(true);
    }

    /**
     * 控制台运行日志输出
     * @param content 输出内容
     */
    public synchronized void consoleLog(String content) {
        System.out.println(content);
        try {
            this.doc.insertString(this.doc.getLength(), content + "\n", logStyle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制台进程调度日志输出
     * @param content 输出内容
     */
    public synchronized void process(String content) {
        System.out.println(content);
        try {
            this.doc.insertString(this.doc.getLength(), content + "\n", process);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制台作业调度日志输出
     * @param content 输出内容
     */
    public synchronized void workinfo(String content) {
        System.out.println(content);
        try {
            this.doc.insertString(this.doc.getLength(), content + "\n", workinfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制台标题信息日志输出
     * @param content 输出内容
     */
    public synchronized void headtitle(String content) {
        System.out.println(content);
        try {
            this.doc.insertString(this.doc.getLength(), content + "\n", headtitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 激活按钮
     */
    public synchronized void StartButton(boolean t) {
        this.startButton.setEnabled(t);
    }
    public synchronized void AddjobButton(boolean t){this.addJobButton.setEnabled(t);}

    /**
     * 刷新时间
     * @param time 时间
     */
    public synchronized void refreshTime(int time) {
        this.time.setText("" + time);
    }

    /**
     * 刷新CPU信息
     */
    public synchronized void refreshCPU() {
        this.cpuState.setText(this.manager.getCpu().getPSW() == CPU.USER_STATE ? "用户态" : "内核态");
        this.PC.setText("" + this.manager.getCpu().getPC());
        this.IR.setText("" + this.manager.getCpu().getIR());
    }

    /**
     * 刷新运行进程信息
     */
    public synchronized void refreshRunningProcess() {
        PCB running = this.manager.getCpu().getRunningPCB();
        this.runningPCBId.setText(running == null ? " " : "" + running.getProID());
        this.runningPCBInstructionNum.setText(running == null ? " " : "" + (running.getInstrucNum()-running.getIR()));
        this.runningPCBPC.setText(running == null ? " " : "" + running.getPC());
        this.runningPCBIR.setText(running == null ? " " : "" + running.getIR());
        this.runningPCBTimeSlice.setText(running == null ? " " : "" + this.manager.getCpu().getTimeSlice());
        this.Priority.setText(running == null ? " " : "" + running.getPriority());
    }


    /**
     * 刷新队列信息
     */
    public synchronized void refreshQueues() {
        // 刷新就绪队列
        while (this.readyTableInfo.getRowCount() > 0) {
            this.readyTableInfo.removeRow(0);
        }
        for (int j=0;j<5;j++) {
            for (int i = 0; i < this.manager.getSchedule().getReadyQueue().get(j).size(); ++i) {
                PCB temp = this.manager.getSchedule().getReadyQueue().get(j).get(i);
                this.readyTableInfo.addRow(new String[]{
                        Integer.toString(temp.getProID()),
                        Integer.toString(temp.getPriority()),
                        Integer.toString(temp.getInstrucNum() - temp.getIR()),
                        Integer.toString(temp.getPC()),
                        Integer.toString(temp.getIR())});
            }
        }
    }
    /**
     * 初始化控制台
     */
    public void initConsole() {
        // 绘制控制台富文本
        doc = this.console.getStyledDocument();
        // 保持滚动条在底端
        DefaultCaret caret = (DefaultCaret) this.console.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        // 设置控制台输出样式
        this.logStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.logStyle, "Microsoft YaHei UI");
        StyleConstants.setFontSize(this.logStyle, 14);
        StyleConstants.setForeground(this.logStyle, Color.gray);

        this.process = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.process, "Microsoft YaHei UI");
        StyleConstants.setFontSize(this.process, 14);
        StyleConstants.setForeground(this.process, Color.darkGray);

        this.headtitle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.headtitle, "Microsoft YaHei UI");
        StyleConstants.setFontSize(this.headtitle, 20);
        StyleConstants.setForeground(this.headtitle, Color.black);

        this.workinfo = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.workinfo, "Microsoft YaHei UI");
        StyleConstants.setFontSize(this.workinfo, 14);
        StyleConstants.setForeground(this.workinfo, Color.BLUE);

    }

    /**
     * 初始化所有表格
     */
    public void initTable() {
        // 就绪队列
        String[] readyTableHeader = new String[]{"进程ID", "优先级", "剩余指令数", "PC", "IR"};
        this.readyTableInfo = new DefaultTableModel(new String[][]{}, readyTableHeader) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 关闭单元格编辑
                return false;
            }
        };
        this.readyTable = new JTable(this.readyTableInfo);
        this.readyTable.getTableHeader().setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        this.readyTable.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        this.readyQueueScrollPane.setViewportView(this.readyTable);

    }
    /**
     * 添加按钮点击事件
     */
    public void addButtonHandler() {
        // 启动/暂停按钮
        this.startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.getClock().setPause(!manager.getClock().isPause());
                if (manager.getClock().isPause()) {
                    clockState.setText("暂停");
                    startButton.setText("启动");
                } else {
                    clockState.setText("运行中");
                    startButton.setText("暂停");
                }
            }
        });
        // 添加作业按钮
        this.addJobButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //作业请求中断
                manager.getCpu().interrupt(InterruptVector.JOB_ADD_INTERRUPT);
            }
        });
    }

    /**
     * 初始化页面数据结构
     */
    public void initDataStructure() {
        // 初始化控制台
        this.initConsole();
        // 设置表格格式
        this.initTable();
        // 添加按钮绑定函数
        this.addButtonHandler();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel = new JPanel();
        startButton = new JButton();
        addJobButton = new JButton();
        consoleScrollPane = new JScrollPane();
        console = new JTextPane();
        consoleLabel = new JLabel();
        readyQueueLabel = new JLabel();
        clockLabel = new JLabel();
        timeLabel = new JLabel();
        clockStateLabel = new JLabel();
        time = new JLabel();
        clockState = new JLabel();
        clockBox = new JLabel();
        cpuLabel = new JLabel();
        PCLabel = new JLabel();
        IRLabel = new JLabel();
        PC = new JLabel();
        IR = new JLabel();
        cpuStateLabel = new JLabel();
        cpuState = new JLabel();
        cpuBox = new JLabel();
        runningPCBLabel = new JLabel();
        runningPCBIdLabel = new JLabel();
        runningPCBId = new JLabel();
        runningPCBInstructionNumLabel = new JLabel();
        runningPCBInstructionNum = new JLabel();
        runningPCBPCLabel = new JLabel();
        runningPCBPC = new JLabel();
        runningPCBIRLabel = new JLabel();
        runningPCBIR = new JLabel();
        runningPCBTimeSliceLabel = new JLabel();
        Priority = new JLabel();
        PriorityLabel = new JLabel();
        runningPCBTimeSlice = new JLabel();
        runnningBox = new JLabel();
        label1 = new JLabel();
        label2 = new JLabel();
        readyQueueScrollPane = new JScrollPane();

        //======== this ========
        setTitle("[19220201\u675c\u653e]\u5e76\u53d1\u73af\u5883\u4ee5\u53ca\u8fdb\u7a0b\u4f4e\u7ea7\u8c03\u5ea6\u4eff\u771f\u5b9e\u73b0-\u591a\u7ea7\u53cd\u9988\u961f\u5217\u7b97\u6cd5");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel ========
        {
            panel.setBackground(new Color(0xdcf2f2));
            panel.setMaximumSize(null);
            panel.setLayout(null);

            //---- startButton ----
            startButton.setText("\u542f\u52a8");
            startButton.setFont(new Font("\u65b9\u6b63\u5b8b\u523b\u672c\u79c0\u6977\u7b80\u4f53", Font.PLAIN, 16));
            startButton.setEnabled(false);
            startButton.setBackground(UIManager.getColor("Button.startBorderColor"));
            startButton.setContentAreaFilled(false);
            panel.add(startButton);
            startButton.setBounds(235, 325, 115, startButton.getPreferredSize().height);

            //---- addJobButton ----
            addJobButton.setText("\u6dfb\u52a0\u4f5c\u4e1a");
            addJobButton.setFont(new Font("\u65b9\u6b63\u5b8b\u523b\u672c\u79c0\u6977\u7b80\u4f53", Font.PLAIN, 16));
            addJobButton.setEnabled(false);
            addJobButton.setBackground(UIManager.getColor("Button.startBorderColor"));
            addJobButton.setContentAreaFilled(false);
            panel.add(addJobButton);
            addJobButton.setBounds(95, 325, 115, addJobButton.getPreferredSize().height);

            //======== consoleScrollPane ========
            {
                consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                //---- console ----
                console.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                console.setEditable(false);
                console.setBackground(new Color(0x99ffff));
                consoleScrollPane.setViewportView(console);
            }
            panel.add(consoleScrollPane);
            consoleScrollPane.setBounds(15, 35, 720, 280);

            //---- consoleLabel ----
            consoleLabel.setText("\u8fd0\u884c\u65e5\u5fd7\uff1a");
            consoleLabel.setFont(new Font("\u65b9\u6b63\u7c97\u9ed1\u5b8b\u7b80\u4f53", Font.PLAIN, 17));
            consoleLabel.setBackground(new Color(0xffffcc));
            consoleLabel.setForeground(Color.gray);
            consoleLabel.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(consoleLabel);
            consoleLabel.setBounds(10, 5, 95, 30);

            //---- readyQueueLabel ----
            readyQueueLabel.setText("\u5c31\u7eea\u961f\u5217:");
            readyQueueLabel.setFont(new Font("\u65b9\u6b63\u7c97\u9ed1\u5b8b\u7b80\u4f53", Font.PLAIN, 16));
            readyQueueLabel.setForeground(Color.gray);
            panel.add(readyQueueLabel);
            readyQueueLabel.setBounds(10, 325, 75, 30);

            //---- clockLabel ----
            clockLabel.setText("\u7cfb\u7edf\u65f6\u949f");
            clockLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
            panel.add(clockLabel);
            clockLabel.setBounds(770, 15, 80, 32);

            //---- timeLabel ----
            timeLabel.setText("\u5f53\u524d\u65f6\u95f4\uff1a");
            timeLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(timeLabel);
            timeLabel.setBounds(775, 45, 75, 35);

            //---- clockStateLabel ----
            clockStateLabel.setText("\u5f53\u524d\u72b6\u6001\uff1a");
            clockStateLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(clockStateLabel);
            clockStateLabel.setBounds(775, 80, 75, 35);

            //---- time ----
            time.setText("0");
            time.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 18));
            panel.add(time);
            time.setBounds(850, 45, 75, 35);

            //---- clockState ----
            clockState.setText("\u672a\u542f\u52a8");
            clockState.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(clockState);
            clockState.setBounds(850, 80, 75, 35);

            //---- clockBox ----
            clockBox.setBorder(LineBorder.createGrayLineBorder());
            panel.add(clockBox);
            clockBox.setBounds(770, 45, 160, 75);

            //---- cpuLabel ----
            cpuLabel.setText("CPU");
            cpuLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
            panel.add(cpuLabel);
            cpuLabel.setBounds(950, 15, 80, 32);

            //---- PCLabel ----
            PCLabel.setText("PC\uff1a");
            PCLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(PCLabel);
            PCLabel.setBounds(955, 45, 35, 35);

            //---- IRLabel ----
            IRLabel.setText("IR\uff1a");
            IRLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(IRLabel);
            IRLabel.setBounds(1030, 45, 35, 35);

            //---- PC ----
            PC.setText("0");
            PC.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 18));
            panel.add(PC);
            PC.setBounds(990, 45, 40, 35);

            //---- IR ----
            IR.setText("0");
            IR.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 18));
            panel.add(IR);
            IR.setBounds(1065, 45, 40, 35);

            //---- cpuStateLabel ----
            cpuStateLabel.setText("CPU\u72b6\u6001\uff1a");
            cpuStateLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(cpuStateLabel);
            cpuStateLabel.setBounds(955, 80, 75, 35);

            //---- cpuState ----
            cpuState.setText("\u7528\u6237\u6001");
            cpuState.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(cpuState);
            cpuState.setBounds(1030, 80, 75, 35);

            //---- cpuBox ----
            cpuBox.setBorder(LineBorder.createGrayLineBorder());
            panel.add(cpuBox);
            cpuBox.setBounds(950, 45, 160, 75);

            //---- runningPCBLabel ----
            runningPCBLabel.setText("\u5f53\u524d\u8fd0\u884c\u8fdb\u7a0b\u4fe1\u606f");
            runningPCBLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
            panel.add(runningPCBLabel);
            runningPCBLabel.setBounds(770, 150, 160, 32);

            //---- runningPCBIdLabel ----
            runningPCBIdLabel.setText("\u8fdb\u7a0b ID");
            runningPCBIdLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBIdLabel);
            runningPCBIdLabel.setBounds(775, 205, 75, 35);

            //---- runningPCBId ----
            runningPCBId.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBId);
            runningPCBId.setBounds(855, 205, 75, 35);

            //---- runningPCBInstructionNumLabel ----
            runningPCBInstructionNumLabel.setText("\u5269\u4f59\u6307\u4ee4\u6570");
            runningPCBInstructionNumLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBInstructionNumLabel);
            runningPCBInstructionNumLabel.setBounds(775, 275, 75, 35);

            //---- runningPCBInstructionNum ----
            runningPCBInstructionNum.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBInstructionNum);
            runningPCBInstructionNum.setBounds(855, 275, 75, 35);

            //---- runningPCBPCLabel ----
            runningPCBPCLabel.setText("\u8fdb\u7a0b PC");
            runningPCBPCLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBPCLabel);
            runningPCBPCLabel.setBounds(775, 240, 75, 35);

            //---- runningPCBPC ----
            runningPCBPC.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBPC);
            runningPCBPC.setBounds(855, 240, 75, 35);

            //---- runningPCBIRLabel ----
            runningPCBIRLabel.setText("\u8fdb\u7a0b IR");
            runningPCBIRLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBIRLabel);
            runningPCBIRLabel.setBounds(945, 240, 75, 35);

            //---- runningPCBIR ----
            runningPCBIR.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBIR);
            runningPCBIR.setBounds(1025, 240, 75, 35);

            //---- runningPCBTimeSliceLabel ----
            runningPCBTimeSliceLabel.setText("\u5269\u4f59\u65f6\u95f4\u7247");
            runningPCBTimeSliceLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBTimeSliceLabel);
            runningPCBTimeSliceLabel.setBounds(945, 275, 75, 35);

            //---- Priority ----
            Priority.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(Priority);
            Priority.setBounds(1025, 205, 75, 35);

            //---- PriorityLabel ----
            PriorityLabel.setText("\u8fdb\u7a0b\u4f18\u5148\u7ea7");
            PriorityLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(PriorityLabel);
            PriorityLabel.setBounds(945, 205, 75, 35);

            //---- runningPCBTimeSlice ----
            runningPCBTimeSlice.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runningPCBTimeSlice);
            runningPCBTimeSlice.setBounds(1025, 275, 75, 35);

            //---- runnningBox ----
            runnningBox.setBorder(LineBorder.createGrayLineBorder());
            runnningBox.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
            panel.add(runnningBox);
            runnningBox.setBounds(770, 195, 340, 120);

            //---- label1 ----
            label1.setForeground(Color.red);
            label1.setHorizontalAlignment(SwingConstants.LEFT);
            label1.setFont(new Font("\u6977\u4f53", Font.BOLD, 14));
            panel.add(label1);
            label1.setBounds(90, 10, 650, 26);

            //---- label2 ----
            label2.setText("DJ\u7b97\u6cd5\u53ef\u6267\u884cexe\u6587\u4ef6\u9700\u8981\u4e0einput2\u6587\u4ef6\u5939\u653e\u5728\u540c\u4e00\u76ee\u5f55");
            label2.setForeground(Color.blue);
            label2.setFont(new Font("\u65b9\u6b63\u5b8b\u523b\u672c\u79c0\u6977\u7b80\u4f53", Font.BOLD, 16));
            panel.add(label2);
            label2.setBounds(355, 335, 615, label2.getPreferredSize().height);

            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < panel.getComponentCount(); i++) {
                    Rectangle bounds = panel.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel.setMinimumSize(preferredSize);
                panel.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(panel, BorderLayout.PAGE_START);

        //======== readyQueueScrollPane ========
        {
            readyQueueScrollPane.setBackground(new Color(0xdcf2f2));
            readyQueueScrollPane.setAutoscrolls(true);
        }
        contentPane.add(readyQueueScrollPane, BorderLayout.CENTER);
        setSize(1140, 665);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    public JPanel panel;
    public JButton startButton;
    public JButton addJobButton;
    public JScrollPane consoleScrollPane;
    public JTextPane console;
    public JLabel consoleLabel;
    public JLabel readyQueueLabel;
    public JLabel clockLabel;
    public JLabel timeLabel;
    public JLabel clockStateLabel;
    public JLabel time;
    public JLabel clockState;
    public JLabel clockBox;
    public JLabel cpuLabel;
    public JLabel PCLabel;
    public JLabel IRLabel;
    public JLabel PC;
    public JLabel IR;
    public JLabel cpuStateLabel;
    public JLabel cpuState;
    public JLabel cpuBox;
    public JLabel runningPCBLabel;
    public JLabel runningPCBIdLabel;
    public JLabel runningPCBId;
    public JLabel runningPCBInstructionNumLabel;
    public JLabel runningPCBInstructionNum;
    public JLabel runningPCBPCLabel;
    public JLabel runningPCBPC;
    public JLabel runningPCBIRLabel;
    public JLabel runningPCBIR;
    public JLabel runningPCBTimeSliceLabel;
    private JLabel Priority;
    private JLabel PriorityLabel;
    public JLabel runningPCBTimeSlice;
    public JLabel runnningBox;
    public JLabel label1;
    public JLabel label2;
    public JScrollPane readyQueueScrollPane;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public void setManager(Manager manager) {
        this.manager = manager;
    }


}
