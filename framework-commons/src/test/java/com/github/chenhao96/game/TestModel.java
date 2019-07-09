package com.github.chenhao96.game;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestModel {

    private int a;
    private float b;
    private short c;
    private boolean d;
    private byte e;
    private long f;
    private double g;
    private String h;
    private Object i;
    private List<String> j;
    private Set<TestModel> k;
    private Map<String, Object> l;
    private Integer m;
    private Float n;
    private Short o;
    private Boolean p;
    private Byte q;
    private Long r;
    private Double s;
    private Character t;
    private char u;
    private byte[] v;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public short getC() {
        return c;
    }

    public void setC(short c) {
        this.c = c;
    }

    public boolean isD() {
        return d;
    }

    public void setD(boolean d) {
        this.d = d;
    }

    public byte getE() {
        return e;
    }

    public void setE(byte e) {
        this.e = e;
    }

    public long getF() {
        return f;
    }

    public void setF(long f) {
        this.f = f;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public Object getI() {
        return i;
    }

    public void setI(Object i) {
        this.i = i;
    }

    public List<String> getJ() {
        return j;
    }

    public void setJ(List<String> j) {
        this.j = j;
    }

    public Set<TestModel> getK() {
        return k;
    }

    public void setK(Set<TestModel> k) {
        this.k = k;
    }

    public Map<String, Object> getL() {
        return l;
    }

    public void setL(Map<String, Object> l) {
        this.l = l;
    }

    public Integer getM() {
        return m;
    }

    public void setM(Integer m) {
        this.m = m;
    }

    public Float getN() {
        return n;
    }

    public void setN(Float n) {
        this.n = n;
    }

    public Short getO() {
        return o;
    }

    public void setO(Short o) {
        this.o = o;
    }

    public Boolean getP() {
        return p;
    }

    public void setP(Boolean p) {
        this.p = p;
    }

    public Byte getQ() {
        return q;
    }

    public void setQ(Byte q) {
        this.q = q;
    }

    public Long getR() {
        return r;
    }

    public void setR(Long r) {
        this.r = r;
    }

    public Double getS() {
        return s;
    }

    public void setS(Double s) {
        this.s = s;
    }

    public Character getT() {
        return t;
    }

    public void setT(Character t) {
        this.t = t;
    }

    public char getU() {
        return u;
    }

    public void setU(char u) {
        this.u = u;
    }

    public byte[] getV() {
        return v;
    }

    public void setV(byte[] v) {
        this.v = v;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestModel{");
        sb.append("a=").append(a);
        sb.append(", b=").append(b);
        sb.append(", c=").append(c);
        sb.append(", d=").append(d);
        sb.append(", e=").append(e);
        sb.append(", f=").append(f);
        sb.append(", g=").append(g);
        sb.append(", h='").append(h).append('\'');
        sb.append(", i=").append(i);
        sb.append(", j=").append(j);
        sb.append(", k=").append(k);
        sb.append(", l=").append(l);
        sb.append(", m=").append(m);
        sb.append(", n=").append(n);
        sb.append(", o=").append(o);
        sb.append(", p=").append(p);
        sb.append(", q=").append(q);
        sb.append(", r=").append(r);
        sb.append(", s=").append(s);
        sb.append(", t=").append(t);
        sb.append(", u=").append(u);
        sb.append(", v=").append(Arrays.toString(v));
        sb.append('}');
        return sb.toString();
    }
}
