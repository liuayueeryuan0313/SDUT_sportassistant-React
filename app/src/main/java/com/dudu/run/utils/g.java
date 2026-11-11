package com.dudu.run.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.a0;
import okhttp3.b0;
import okhttp3.d0;
public class g {
    private static g b;
    private a0 a;
    class a implements X509TrustManager {
        a(g gVar) {
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
    class b implements HostnameVerifier {
        b(g gVar) {
        }

        @Override // javax.net.ssl.HostnameVerifier
        public boolean verify(String str, SSLSession sSLSession) {
            return true;
        }
    }
    class c implements X509TrustManager {
        c(g gVar) {
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
    class d implements okhttp3.g {
        final /* synthetic */ e a;
        final /* synthetic */ String b;
        final /* synthetic */ String f1621c;

        d(g gVar, e eVar, String str, String str2) {
            this.a = eVar;
            this.b = str;
            this.f1621c = str2;
        }

        @Override // okhttp3.g
        public void a(okhttp3.f fVar, d0 d0Var) {
            File destination = new File(this.b, this.f1621c);
            File partial = new File(this.b, this.f1621c + "_download");
            try (d0 response = d0Var) {
                if (response.e() < 200 || response.e() >= 300) {
                    throw new IOException("Download HTTP status: " + response.e());
                }
                if (response.b() == null) throw new IOException("Empty download response");
                File directory = destination.getParentFile();
                if (!directory.isDirectory() && !directory.mkdirs()) {
                    throw new IOException("Cannot create download directory");
                }
                if (partial.exists() && !partial.delete()) {
                    throw new IOException("Cannot remove incomplete download");
                }
                long total = response.b().e();
                long downloaded = 0;
                try (InputStream input = response.b().b();
                     FileOutputStream output = new FileOutputStream(partial)) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        output.write(buffer, 0, count);
                        downloaded += count;
                        if (total > 0) this.a.a((int) Math.min(100L, downloaded * 100L / total));
                    }
                    output.flush();
                }
                if (downloaded == 0 || (total >= 0 && downloaded != total)) {
                    throw new IOException("Incomplete download");
                }
                if (destination.exists() && !destination.delete()) {
                    throw new IOException("Cannot replace download file");
                }
                if (!partial.renameTo(destination)) {
                    throw new IOException("Cannot finalize download file");
                }
            } catch (Exception error) {
                partial.delete();
                this.a.b(error);
                return;
            }
            this.a.c(destination);
        }

        @Override // okhttp3.g
        public void b(okhttp3.f fVar, IOException iOException) {
            this.a.b(iOException);
        }
    }
    public interface e {
        void a(int i);

        void b(Exception exc);

        void c(File file);
    }

    public g() {
        TrustManager[] trustManagerArr = {new a(this)};
        try {
            SSLContext sSLContext = SSLContext.getInstance("SSL");
            sSLContext.init(null, trustManagerArr, new SecureRandom());
            SSLSocketFactory socketFactory = sSLContext.getSocketFactory();
            a0.a aVar = new a0.a();
            aVar.J(socketFactory, new c(this));
            aVar.H(new b(this));
            this.a = aVar.b();
        } catch (KeyManagementException e2) {
            e2.printStackTrace();
        } catch (NoSuchAlgorithmException e3) {
            e3.printStackTrace();
        }
        if (this.a == null) {
            this.a = new a0();
        }
    }

    public static g b() {
        if (b == null) {
            b = new g();
        }
        return b;
    }

    public void a(String str, String str2, String str3, e eVar) {
        b0.a aVar = new b0.a();
        aVar.j(str);
        this.a.a(aVar.b()).V(new d(this, eVar, str2, str3));
    }
}