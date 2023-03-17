package kernel;

/**
 * 指令，用以记录每条指令的相关信息
 *
 * @author 19220201杜放
 */
public class Instruction {
    /**
     * 指令编号，当作指令的逻辑地址
     */
    private int Instruc_ID;
    /**
     * 指令状态，用于区分类型
     */
    private int Instruc_State;

    public Instruction() {
        this.Instruc_ID = 0;
        this.Instruc_State = 0;
    }

    public Instruction(int id, int state) {
        this.Instruc_ID = id;
        this.Instruc_State = state;
    }

    @Override
    public String toString() {
        return this.Instruc_ID + "," + this.Instruc_State;
    }

    public int getInstruc_ID() {
        return Instruc_ID;
    }

    public void setInstruc_ID(int instruc_ID) {
        this.Instruc_ID = instruc_ID;
    }

    public int getInstruc_State() {
        return Instruc_State;
    }

    public void setInstruc_State(int instruc_State) {
        this.Instruc_State = instruc_State;
    }

}
