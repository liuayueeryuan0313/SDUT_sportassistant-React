package com.dudu.run.bean;
public class ConfigResponseData {
    private int boyMaxSpeed;
    private int boyMaxValidLength;
    private int boyMinLength;
    private int boyMinSpeed;
    private int girlMaxSpeed;
    private int girlMaxValidLength;
    private int girlMinLength;
    private int girlMinSpeed;
    private int semesterTotalCountBoy;
    private int semesterTotalCountGirl;

    public int a() {
        return this.boyMaxSpeed;
    }

    public int b() {
        return this.boyMaxValidLength;
    }

    public int c() {
        return this.boyMinLength;
    }

    public int d() {
        return this.boyMinSpeed;
    }

    public int e() {
        return this.girlMaxSpeed;
    }

    public int f() {
        return this.girlMaxValidLength;
    }

    public int g() {
        return this.girlMinLength;
    }

    public int h() {
        return this.girlMinSpeed;
    }

    public int i() {
        return this.semesterTotalCountBoy;
    }

    public int j() {
        return this.semesterTotalCountGirl;
    }

    public String toString() {
        return "ConfigResponseData{semesterTotalCountBoy=" + this.semesterTotalCountBoy + ", semesterTotalCountGirl=" + this.semesterTotalCountGirl + ", boyMinLength=" + this.boyMinLength + ", boyMaxValidLength=" + this.boyMaxValidLength + ", boyMinSpeed=" + this.boyMinSpeed + ", boyMaxSpeed=" + this.boyMaxSpeed + ", girlMinLength=" + this.girlMinLength + ", girlMaxValidLength=" + this.girlMaxValidLength + ", girlMinSpeed=" + this.girlMinSpeed + ", girlMaxSpeed=" + this.girlMaxSpeed + '}';
    }
}
