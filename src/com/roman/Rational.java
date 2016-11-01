package com.roman;

/**
 * Created by roman on 18.10.2016.
 */
public class Rational {
    public int up;
    public int down;

    public Rational(int pUp, int pDown) {
        up = pUp;
        down = pDown;
        tryDivide();
    }

    public Rational(Rational pOrig) {
        up = pOrig.up;
        down = pOrig.down;
    }

    public Rational() {
        up = 0;
        down = 1;
    }

    private void tryDivide() {
        for (int i = Math.min(up, down); i >= 1; i--) {
            if (up % i == 0 && down % i == 0) {
                up /= i;
                down /= i;
                break;
            }
        }
    }

    public Rational divide(int pOperand) {
        if (pOperand == 0) {
            return this;
        }
        down *= pOperand;
        tryDivide();
        return this;
    }

    public Rational divide(Rational pOperand) {
        up *= pOperand.down;
        down *= pOperand.up;
        tryDivide();
        return this;
    }

    public Rational multiply(int pOperand) {
        up *= pOperand;
        tryDivide();
        return this;
    }

    public Rational multiply(Rational pOperand) {
        up *= pOperand.up;
        down *= pOperand.down;
        tryDivide();
        return this;
    }

    public Rational increment() {
        up += down;
        return this;
    }

    public Rational plus(Rational pOperand) {
        up *= pOperand.down;
        int pUp = pOperand.up;
        pUp *= down;
        down *= pOperand.down;
        up += pUp;
        tryDivide();
        return this;
    }

    public Rational minus(Rational pOperand) {
        up *= pOperand.down;
        int pUp = pOperand.up;
        pUp *= down;
        down *= pOperand.down;
        up -= pUp;
        tryDivide();
        return this;
    }

    public int compare(Rational pOperand) {
        int tempUp1 = up * pOperand.down;
        int tempUp2 = pOperand.up * down;
        int tempDown = down * pOperand.down;
        return tempUp1 - tempUp2;
    }

    public static Rational findEconomical(Rational pStart, Rational pEnd, boolean toBigger) {
        if (pStart.up + pStart.down < 8) {
            return toBigger ? pStart : pEnd;
        }
        int min = pStart.up + pEnd.down;
        Rational minResult = new Rational(pStart);
        Rational start = new Rational(pStart.up * pEnd.down, pStart.down * pEnd.down);
        Rational end = new Rational(pEnd.up * pStart.down, pEnd.down * pStart.down);

        if (toBigger) {
            for (int i = start.up; i < (start.up + end.up) / 2; i++) {
                Rational temp = new Rational(i, start.down);
                if (temp.up + temp.down < min) {
                    minResult = temp;
                    min = temp.up + temp.down;
                }
            }
        } else {
            for (int i = end.up; i > (start.up + end.up) / 2; i--) {
                Rational temp = new Rational(i, start.down);
                if (temp.up + temp.down < min) {
                    minResult = temp;
                    min = temp.up + temp.down;
                }
            }
        }


        return minResult;
    }

    public String toString() {
        return up + "/" + down;
    }
}
