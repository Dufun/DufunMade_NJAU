package os;


import java.util.Arrays;

/**
 * main函数
 *
 * 主程序入口
 *
 * @author 19220201杜放
 */
public class Main {
    public static void main(String[] args) {
        // 创建系统
        Manager manager = new Manager();
        // 系统启动完毕，开始运行
        manager.start();
    }
}
