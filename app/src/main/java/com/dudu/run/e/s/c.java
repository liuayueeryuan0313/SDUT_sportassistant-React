package com.dudu.run.e.s;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.LoginResponseData;
public class c implements com.dudu.run.e.e {
    private com.dudu.run.e.f a;
    private com.dudu.run.g.a b = new com.dudu.run.g.b.a();
    class a implements com.dudu.run.f.b.c<BaseResponse<LoginResponseData>> {
        a() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            c.this.a.J(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<LoginResponseData> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                c.this.a.s(baseResponse.b());
            } else {
                c.this.a.J(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            c.this.a.i();
        }
    }

    public c(com.dudu.run.e.f fVar) {
        this.a = fVar;
    }

    @Override // com.dudu.run.e.e
    public void a(String str, String str2) {
        this.b.b(str, str2, new a());
    }
}
