package com.dudu.run.e.s;

import com.dudu.run.bean.BaseResponse;
public class a implements com.dudu.run.e.a {
    private com.dudu.run.e.b a;
    private com.dudu.run.g.a b = new com.dudu.run.g.b.a();
    class C0035a implements com.dudu.run.f.b.c<BaseResponse> {
        C0035a() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            a.this.a.n(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                a.this.a.u();
            } else {
                a.this.a.n(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            a.this.a.K();
        }
    }

    public a(com.dudu.run.e.b bVar) {
        this.a = bVar;
    }

    @Override // com.dudu.run.e.a
    public void a(String str, String str2) {
        this.b.i(str, str2, new C0035a());
    }
}
