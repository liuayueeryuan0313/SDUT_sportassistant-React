package com.dudu.run.f.b;

import java.io.IOException;
import java.nio.charset.Charset;
import okhttp3.b0;
import okhttp3.d0;
import okhttp3.e0;
import okhttp3.j;
import okhttp3.x;
import okio.e;
import okio.g;
public class b implements x {
    private static final Charset a = Charset.forName("UTF-8");

    @Override // okhttp3.x
    public d0 a(x.a aVar) throws IOException {
        b0 b0VarS = aVar.S();
        j jVarB = aVar.b();
        if (jVarB != null) {
            jVarB.a();
        }
        d0 d0VarA = aVar.a(b0VarS);
        e0 e0VarB = d0VarA.b();
        long jE = e0VarB.e();
        g gVarI = e0VarB.I();
        gVarI.request(Long.MAX_VALUE);
        e eVarM = gVarI.m();
        if (jE != 0) {
            com.dudu.run.utils.j.b("mine", "response-->" + eVarM.c().R(a));
        }
        return d0VarA;
    }
}