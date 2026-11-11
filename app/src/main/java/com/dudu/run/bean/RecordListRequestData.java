package com.dudu.run.bean;
public class RecordListRequestData {
    private int pageNo;
    private int pageSize;
    private int userId;

    public void a(int i) {
        this.pageNo = i;
    }

    public void b(int i) {
        this.pageSize = i;
    }

    public void c(int i) {
        this.userId = i;
    }
}
