package com.example.cryptocoursework;

import java.math.BigInteger;
import java.util.HashMap;

public class Tracerr {
    private BigInteger p;
    private GFP2 c = null;
    private HashMap<BigInteger, GFP2> dict_c = new HashMap<>();
    public Tracerr(BigInteger p) {
        this.p = p;
    }
    public GFP2 calcTracer(BigInteger p, GFP2 c) {
        if (this.c == null || (this.c != null && !this.c.myEquals(c))) {
            this.c = c;
            this.dict_c.clear();
            dict_c.put(BigInteger.ZERO, new GFP2(p, BigInteger.valueOf(3)));
            dict_c.put(BigInteger.ONE, this.c);
        }
        return getC(p);
    }
    private GFP2 getC(BigInteger n) {
        if (dict_c.containsKey(n)) {
            return dict_c.get(n);
        }

        BigInteger current = BigInteger.ONE;
        int nbits = n.bitLength();
        BigInteger runner = BigInteger.ONE.shiftLeft(nbits - 2);
        for (int i = 1; i < nbits; i++) {
            BigInteger bit = n.and(runner);
            BigInteger newN = current.shiftLeft(1).or(BigInteger.valueOf(bit.equals(BigInteger.ZERO) ? 0 : 1));
            if (!dict_c.containsKey(newN)) {
                GFP2 currentC = dict_c.get(current);
                dict_c.put(newN, (!bit.equals(BigInteger.ZERO)) ?
                        GFP2.specOp(getC(current.add(BigInteger.ONE)), this.c, currentC)
                                .add(getC(current.subtract(BigInteger.ONE)).getSwaped()) :
                        currentC.getSquared().sub(currentC.add(currentC).getSwaped()));
            }
            current = newN;
            runner = runner.shiftRight(1);
        }
        return dict_c.get(n);
    }
}

