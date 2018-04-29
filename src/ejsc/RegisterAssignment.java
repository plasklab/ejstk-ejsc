package ejsc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RegisterAssignment {
    static class RealRegister extends Register {
        private RealRegister(int n) {
            super(n);
        }
    }
    
    static class RegisterFactory {
        private final HashMap<Integer, RealRegister> regs = new HashMap<Integer, RealRegister>();
        private int maxRegNum = 0;
        RealRegister get(int n) {
            RealRegister r = regs.get(n);
            if (r != null)
                return r;
            r = new RealRegister(n);
            regs.put(n, r);
            if (n >= maxRegNum)
                maxRegNum = n;
            return r;
        }
        public int getMaxRegNum() {
            return maxRegNum;
        }
    }
    
    RegisterFactory rf = new RegisterFactory();
    List<BCode> bcodes;
    LiveRegisterAnalyser lra;
    HashMap<Register, RealRegister> assign = new HashMap<Register, RealRegister>();
    
    public RegisterAssignment(List<BCode> bcodes) {
        this.bcodes = bcodes;
        lra = new LiveRegisterAnalyser(bcodes);
    }
    
    private boolean[] getRegUsageBitmap(BCode bc) {
        boolean[] used = new boolean[rf.getMaxRegNum() + 1];
        for (Register lr: lra.getLiveRegisters(bc)) {
            RealRegister rr = assign.get(lr);
            if (rr != null)
                used[rr.getRegisterNumber()] = true;
        }
        return used;
    }

    private boolean checkConflict(BCode thisBC, Register from, RealRegister to) {
        for (BCode bc: bcodes) {
            if (bc == thisBC)
                continue;
            Set<Register> liveRegs = lra.getLiveRegisters(bc);
            if (!liveRegs.contains(from))
                continue;
            for (Register r: liveRegs) {
                if (assign.get(r) == to)
                    return true;
            }
        }
        return false;
    }
    
    private void makeAssignment() {
        for (BCode bcx: bcodes) {
            boolean[] used = getRegUsageBitmap(bcx);
//            for (Register lr: bcx.getSrcRegisters())
//                if (lr != null && assign.get(lr) == null) {
//                    System.out.println(bcx+" | r"+lr+" | "+showAssignment());
//                    throw new Error("source register is not assigned");
//                }
            
            if (bcx instanceof MParameter) {
                MParameter bc = (MParameter) bcx;     
                Register dst = bc.dst;
                if (dst.getRegisterNumber() <= rf.getMaxRegNum() && used[dst.getRegisterNumber()])
                    throw new Error("cannot assign register to parameter");
                RealRegister rdst = rf.get(dst.getRegisterNumber());
                assign.put(dst, rdst);
                continue;
            } 

            /* coalesce */
            if (bcx instanceof IMove){
                IMove bc = (IMove) bcx;
                Register dst = bc.dst;
                if (assign.get(dst) == null) {
                    RealRegister rsrc = assign.get(bc.src);
                    if (!checkConflict(bc, dst, rsrc)) {
                        assign.put(dst, rsrc);
                        continue;
                    }
                }
            }

            /* default case */
            Register dst = bcx.getDestRegister();
            if (dst != null && assign.get(dst) == null) {
                int n = 1;
                for (; n < used.length; n++)
                    if (!used[n] && !checkConflict(bcx, dst, rf.get(n)))
                        break;
                RealRegister rdst = rf.get(n);
                assign.put(dst, rdst);
            }
        }
    }
    
    private void replaceRegisterField(BCode bc, Field f) throws IllegalArgumentException, IllegalAccessException {
        Register r = (Register) f.get(bc);
        if (r != null) {
            RealRegister rr = assign.get(r);
            if (rr == null)
                throw new Error("internal error");
            f.set(bc, rr);
        }
    }
    
    private void replaceRegisterArrayField(BCode bc, Field f) throws IllegalArgumentException, IllegalAccessException {
        Register[] rs = (Register[]) f.get(bc);
        for (int i = 0; i < rs.length; i++) {
            Register r = rs[i];
            if (r != null) {
                RealRegister rr = assign.get(r);
                if (rr == null)
                    throw new Error("internal error");
                rs[i] = rr;
            }
        }
    }
 
    private void replaceDstRegister(BCode bc) {
        Register r = bc.dst;
        if (r != null) {
            RealRegister rr = assign.get(r);
            if (rr == null)
                throw new Error("internal error");
            bc.dst = rr;
        }
    }

    private void replaceRegisters() {
        for (BCode bcx: bcodes) {
//            System.out.print(showAssignment()+": "+lra.showRegs(lra.getLiveRegisters(bcx))+bcx+" => ");
            try {
                Class<? extends BCode> c = bcx.getClass();
                for (Field f: c.getDeclaredFields()) {
                    if (f.getType() == Register.class)
                        replaceRegisterField(bcx, f);
                    else if (f.getType() == Register[].class)
                        replaceRegisterArrayField(bcx, f);
                }
                replaceDstRegister(bcx);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
    
    public void exec() {
        makeAssignment();
        replaceRegisters();
    }
    
    public int getMaxRegNum() {
        return rf.getMaxRegNum();
    }
 
    public String showAssignment() {
        String s = "{";
        for (Register r: assign.keySet()) {
            RealRegister rr = assign.get(r);
            s += "r"+r+"=>r"+rr+" ";
        }
        return s + "}";
    }
    
    public String showLiveAssignment(BCode bc) {
        String s = "{";
        for (Register r: lra.getLiveRegisters(bc)) {
            RealRegister rr = assign.get(r);
            s += "r"+r+"(r"+rr+") ";
        }
        return s + "}";
    }
}
