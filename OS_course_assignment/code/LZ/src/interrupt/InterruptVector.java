package interrupt;

/**
 * 中断向量表
 *
 * 用以检索不同向量的类型，提供给CPU进行处理
 *
 * @author 19220201杜放
 */
public interface InterruptVector {
    // 时钟中断向量
    int CLOCK_INTERRUPT = 0;
    // 实时作业请求向量
    int JOB_ADD_INTERRUPT = 1;
    int JOB_REED_INTERRUPT =2;
}
