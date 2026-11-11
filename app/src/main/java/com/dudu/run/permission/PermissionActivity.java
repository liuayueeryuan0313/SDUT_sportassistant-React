package com.dudu.run.permission;

import android.content.Intent;
import com.dudu.legacy.androidx.appcompat.app.AppCompatActivity;
import com.dudu.run.utils.j;
public abstract class PermissionActivity extends AppCompatActivity {
    private a y;

    protected void o0(int i) {
        j.a("permission--setting" + i);
        a aVar = this.y;
        if (aVar != null) {
            aVar.g(aVar);
        }
    }

    @Override // com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 401) {
            o0(i);
        }
    }

    @Override // com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        a aVar = this.y;
        if (aVar != null) {
            aVar.d();
            this.y = null;
        }
        super.onDestroy();
    }

    @Override // com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity, com.dudu.legacy.androidx.core.app.a.b
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        a aVar = this.y;
        if (aVar == null || aVar.e() == null) {
            return;
        }
        this.y.f(i, strArr, iArr);
    }

    public void p0(a aVar) {
        this.y = aVar;
    }
}
