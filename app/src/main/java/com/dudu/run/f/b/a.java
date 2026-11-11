package com.dudu.run.f.b;

import android.os.Build;
import com.dudu.run.utils.j;
import com.google.gson.e;
import com.google.gson.f;
import com.tencent.lbssearch.object.RequestParams;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.a0;
import okhttp3.b0;
import okhttp3.d0;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.x;
import retrofit2.t;
public class a {
    private static HttpLoggingInterceptor a;
    private static x b;
    private static a0 f1595c;
    static class C0037a implements x {
        C0037a() {
        }

        @Override // okhttp3.x
        public d0 a(x.a aVar) throws IOException {
            b0.a aVarH = aVar.S().h();
            aVarH.a("Content-Type", RequestParams.APPLICATION_JSON);
            aVarH.a("X-Re-Os", "android");
            aVarH.a("X-Re-Device", Build.BRAND + " " + Build.MODEL);
            return aVar.a(com.dudu.run.simulation.RequestMetadataPolicy.sanitize(aVarH.b()));
        }
    }
    static class b implements X509TrustManager {
        b() {
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
        }

        @Override // javax.net.ssl.X509TrustManager
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
    static class c implements HttpLoggingInterceptor.a {
        c() {
        }

        @Override // okhttp3.logging.HttpLoggingInterceptor.a
        public void a(String str) {
            j.b("mine", "OkHttp====Message:" + str);
        }
    }

    public static t a() {
        if (a == null) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            a = httpLoggingInterceptor;
            httpLoggingInterceptor.d(HttpLoggingInterceptor.Level.BODY);
        }
        if (b == null) {
            b = new C0037a();
        }
        f fVar = new f();
        fVar.d("yyyy-MM-dd HH:mm:ss");
        fVar.c();
        e eVarB = fVar.b();
        TrustManager[] trustManagerArr = {new b()};
        HttpLoggingInterceptor httpLoggingInterceptor2 = new HttpLoggingInterceptor(new c());
        httpLoggingInterceptor2.d(HttpLoggingInterceptor.Level.BODY);
        try {
            if (f1595c == null) {
                SSLContext sSLContext = SSLContext.getInstance("SSL");
                sSLContext.init(null, trustManagerArr, new SecureRandom());
                sSLContext.getSocketFactory();
                a0.a aVar = new a0.a();
                TimeUnit timeUnit = TimeUnit.MILLISECONDS;
                aVar.I(30000L, timeUnit);
                aVar.c(30000L, timeUnit);
                aVar.a(new com.dudu.run.simulation.OfflineApiGuard());
                aVar.a(new com.dudu.run.f.b.b());
                aVar.a(b);
                aVar.a(httpLoggingInterceptor2);
                f1595c = aVar.b();
            }
        } catch (KeyManagementException e2) {
            e2.printStackTrace();
        } catch (NoSuchAlgorithmException e3) {
            e3.printStackTrace();
        }
        t.b bVar = new t.b();
        bVar.f(f1595c);
        bVar.a(retrofit2.y.a.a.f(eVarB));
        bVar.b("http://sports2.sdut.edu.cn/");
        return bVar.d();
    }
}
