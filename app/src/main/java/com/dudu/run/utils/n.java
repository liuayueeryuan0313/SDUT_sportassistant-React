package com.dudu.run.utils;

import java.util.UUID;
public class n {
    public static String a() {
        String strReplaceAll = UUID.randomUUID().toString().replaceAll("-", "");
        return (strReplaceAll == null || strReplaceAll.length() < 8) ? "1234asdf" : strReplaceAll.substring(0, 8);
    }

    public static String b(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(str);
        stringBuffer.append(e.a(System.currentTimeMillis()));
        stringBuffer.append("duDusDut");
        return k.b(stringBuffer.toString()).toLowerCase();
    }
}
