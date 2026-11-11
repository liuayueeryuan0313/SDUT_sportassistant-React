package com.dudu.run.e.s;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.LatLngInfo;
import com.dudu.run.e.o;
import com.dudu.run.e.p;
import java.util.List;
public class h implements o {
    private p a;
    private com.dudu.run.g.a b = new com.dudu.run.g.b.a();
    class a implements com.dudu.run.f.b.c<BaseResponse> {
        a() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            h.this.a.m(-1, str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                h.this.a.f();
            } else {
                h.this.a.m(baseResponse == null ? -1 : baseResponse.a(), baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            h.this.a.g();
        }
    }

    public h(p pVar) {
        this.a = pVar;
    }

    @Override // com.dudu.run.e.o
    public void a(int i, long j, long j2, float f2, int i2, List<LatLngInfo> list) {
        this.b.c(i, j, j2, f2, i2, list, new a());
    }
}
