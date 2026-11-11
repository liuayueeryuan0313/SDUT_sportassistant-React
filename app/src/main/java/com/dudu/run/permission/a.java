package com.dudu.run.permission;

import android.app.Activity;
import com.dudu.legacy.androidx.fragment.app.FragmentActivity;
import com.dudu.run.utils.f;
import com.dudu.run.utils.j;
import com.dudu.run.utils.r;
import java.util.Arrays;
public class a {
    private b a;
    public static class b {
        private String[] a;
        private FragmentActivity b;
        private c f1617c;
        private int f1618d;

        public b e(c cVar) {
            this.f1617c = cVar;
            return this;
        }

        public a f() {
            a aVar = new a();
            aVar.a = this;
            String[] strArr = this.a;
            if (strArr != null && strArr.length != 0) {
                aVar.h();
            }
            FragmentActivity fragmentActivity = this.b;
            if (fragmentActivity instanceof PermissionActivity) {
                ((PermissionActivity) fragmentActivity).p0(aVar);
            }
            return aVar;
        }

        public b g(String[] strArr) {
            j.a("permissionListener--setPermissions" + Arrays.toString(strArr));
            this.a = strArr;
            return this;
        }

        private b(Activity activity) {
            this.f1618d = 400;
            this.b = (FragmentActivity) activity;
        }
    }

    public static b c(Activity activity) {
        return new b(activity);
    }
    public void h() {
        if (d.c(this.a.b, this.a.a) || !f.a()) {
            this.a.f1617c.b();
            return;
        }
        String[] strArrA = d.a(this.a.b, this.a.a);
        if (strArrA.length == 0) {
            this.a.f1617c.b();
        } else {
            com.dudu.legacy.androidx.core.app.a.k(this.a.b, strArrA, this.a.f1618d);
        }
    }

    public void d() {
        this.a = null;
    }

    c e() {
        b bVar = this.a;
        if (bVar == null) {
            return null;
        }
        return bVar.f1617c;
    }

    void f(int i, String[] strArr, int[] iArr) {
        if ((i == this.a.f1618d && d.e(iArr)) || d.c(this.a.b, this.a.a)) {
            this.a.f1617c.b();
            return;
        }
        boolean z = !d.d(this.a.b, strArr);
        if (this.a.f1617c.a(z)) {
            return;
        }
        if (z) {
            r.a(this.a.b, "权限缺失");
        } else {
            r.a(this.a.b, "权限缺失");
        }
    }

    public void g(a aVar) {
        aVar.h();
    }
}
