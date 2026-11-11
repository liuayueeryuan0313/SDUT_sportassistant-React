package com.dudu.run.bean;

import java.util.List;
public class CurrentSportDataInfo {
    public boolean simulated;
    public long localSteps;
    public boolean localStepsKnown;
    private int campus;
    private int needRunMaxDistance;
    private List<RandomPoint> randomPointList;
    private int randomPointPassedCount;
    private int runMaxDistanceCostTime;
    private long startTime;
    private float totalRunDistance;
    private int totalRunSeconds;
    private List<LatLngInfo> trackList;

    public int a() {
        return this.campus;
    }

    public int b() {
        return this.needRunMaxDistance;
    }

    public List<RandomPoint> c() {
        return this.randomPointList;
    }

    public int d() {
        return this.randomPointPassedCount;
    }

    public int e() {
        return this.runMaxDistanceCostTime;
    }

    public long f() {
        return this.startTime;
    }

    public float g() {
        return this.totalRunDistance;
    }

    public int h() {
        return this.totalRunSeconds;
    }

    public List<LatLngInfo> i() {
        return this.trackList;
    }

    public void j(int i) {
        this.campus = i;
    }

    public void k(int i) {
        this.needRunMaxDistance = i;
    }

    public void l(List<RandomPoint> list) {
        this.randomPointList = list;
    }

    public void m(int i) {
        this.randomPointPassedCount = i;
    }

    public void n(int i) {
        this.runMaxDistanceCostTime = i;
    }

    public void o(long j) {
        this.startTime = j;
    }

    public void p(float f2) {
        this.totalRunDistance = f2;
    }

    public void q(int i) {
        this.totalRunSeconds = i;
    }

    public void r(List<LatLngInfo> list) {
        this.trackList = list;
    }
}
