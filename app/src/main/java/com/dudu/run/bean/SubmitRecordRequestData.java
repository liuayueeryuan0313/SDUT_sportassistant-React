package com.dudu.run.bean;
public class SubmitRecordRequestData {
    private String beginTime;
    private int campus;
    private String endTime;
    private String markList;
    private int totalLength;
    private int totalTime;
    private int userId;

    public void a(String str) {
        this.beginTime = str;
    }

    public void b(int i) {
        this.campus = i;
    }

    public void c(String str) {
        this.endTime = str;
    }

    public void d(String str) {
        this.markList = str;
    }

    public void e(int i) {
        this.totalLength = i;
    }

    public void f(int i) {
        this.totalTime = i;
    }

    public void g(int i) {
        this.userId = i;
    }
}
