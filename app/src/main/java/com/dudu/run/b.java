package com.dudu.run;

import android.content.Context;
public class b {
    private static b b;
    private Context a;

    private b() {
    }

    private float b(String str) {
        return this.a.getSharedPreferences("Pref_Name_Data", 0).getFloat(str, 0.0f);
    }

    public static b c() {
        if (b == null) {
            b = new b();
        }
        return b;
    }

    private int d(String str) {
        return this.a.getSharedPreferences("Pref_Name_Data", 0).getInt(str, 0);
    }

    private String k(String str) {
        return this.a.getSharedPreferences("Pref_Name_Data", 0).getString(str, null);
    }

    public void a() {
        this.a.getSharedPreferences("Pref_Name_Data", 0).edit().putInt("Key_UserId", 0).putInt("Key_Campus", 0).putString("Key_UserCode", null).putString("Key_UserCardNo", null).putString("Key_UserName", null).putString("Key_Sex", null).putString("Key_UserClass", null).commit();
    }

    public int e() {
        return r() ? d("Key_GirlMaxValidLength") : d("Key_BoyMaxValidLength");
    }

    public int f() {
        return r() ? d("Key_GirlMaxSpeed") : d("Key_BoyMaxSpeed");
    }

    public int g() {
        return r() ? d("Key_GirlMinLength") : d("Key_BoyMinLength");
    }

    public int h() {
        return r() ? d("Key_GirlMinSpeed") : d("Key_BoyMinSpeed");
    }

    public int i() {
        return d("Key_RunCount");
    }

    public float j() {
        return b("Key_RunLengthKm");
    }

    public int semesterRunGoal() {
        return d(r() ? "Key_SemesterTotalCountGirl" : "Key_SemesterTotalCountBoy");
    }

    public String l() {
        return k("Key_UserClass");
    }

    public String m() {
        return k("Key_UserCode");
    }

    public int n() {
        return d("Key_UserId");
    }

    public String o() {
        return k("Key_UserName");
    }

    public void p(Context context) {
        this.a = context.getApplicationContext();
    }

    public boolean q() {
        return m() != null;
    }

    public boolean r() {
        return "0".equals(k("Key_Sex"));
    }

    public void s(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
        this.a.getSharedPreferences("Pref_Name_Data", 0).edit().putInt("Key_SemesterTotalCountBoy", i).putInt("Key_SemesterTotalCountGirl", i2).putInt("Key_BoyMinLength", i3).putInt("Key_BoyMaxValidLength", i4).putInt("Key_BoyMinSpeed", i5).putInt("Key_BoyMaxSpeed", i6).putInt("Key_GirlMinLength", i7).putInt("Key_GirlMaxValidLength", i8).putInt("Key_GirlMinSpeed", i9).putInt("Key_GirlMaxSpeed", i10).commit();
    }

    public void t(int i) {
        this.a.getSharedPreferences("Pref_Name_Data", 0).edit().putInt("Key_RunCount", i).commit();
    }

    public void u(float f2) {
        this.a.getSharedPreferences("Pref_Name_Data", 0).edit().putFloat("Key_RunLengthKm", f2).commit();
    }

    public void v(int i, int i2, String str, String str2, String str3, String str4, String str5) {
        this.a.getSharedPreferences("Pref_Name_Data", 0).edit().putInt("Key_UserId", i).putInt("Key_Campus", i2).putString("Key_UserCode", str).putString("Key_UserCardNo", str2).putString("Key_UserName", str3).putString("Key_Sex", str4).putString("Key_UserClass", str5).commit();
    }
}
