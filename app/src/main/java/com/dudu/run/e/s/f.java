package com.dudu.run.e.s;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.RecordItem;
import com.dudu.run.e.k;
import com.dudu.run.e.l;
import java.util.List;
public class f implements k {
    private l a;
    private com.dudu.run.g.a b = new com.dudu.run.g.b.a();
    class a implements com.dudu.run.f.b.c<BaseResponse<List<RecordItem>>> {
        final /* synthetic */ int a;

        a(int i) {
            this.a = i;
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            f.this.a.t(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<List<RecordItem>> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                f.this.a.N(this.a, baseResponse.b());
            } else {
                f.this.a.t(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            f.this.a.v(this.a);
        }
    }

    public f(l lVar) {
        this.a = lVar;
    }

    @Override // com.dudu.run.e.k
    public void a(int i, int i2) {
        this.b.e(i, i2, new a(i));
    }
}
