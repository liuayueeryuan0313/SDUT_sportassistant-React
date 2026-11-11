package com.dudu.run.e.s;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.CheckAppUpdateResponseData;
import com.dudu.run.e.m;
import com.dudu.run.e.n;
public class g implements m {
    private n a;
    private com.dudu.run.g.a b = new com.dudu.run.g.b.a();
    class a implements com.dudu.run.f.b.c<BaseResponse<CheckAppUpdateResponseData>> {
        a() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            g.this.a.a(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<CheckAppUpdateResponseData> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                g.this.a.c(baseResponse.b());
            } else {
                g.this.a.a(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            g.this.a.b();
        }
    }

    public g(n nVar) {
        this.a = nVar;
    }

    @Override // com.dudu.run.e.m
    public void a() {
        this.b.k(new a());
    }
}
