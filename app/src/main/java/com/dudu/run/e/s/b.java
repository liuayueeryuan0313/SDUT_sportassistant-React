package com.dudu.run.e.s;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.ConfigResponseData;
public class b implements com.dudu.run.e.c {
    private com.dudu.run.e.d a;
    private com.dudu.run.g.a b = new com.dudu.run.g.b.a();
    class a implements com.dudu.run.f.b.c<BaseResponse<ConfigResponseData>> {
        a() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            b.this.a.C(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<ConfigResponseData> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                b.this.a.E(baseResponse.b());
            } else {
                b.this.a.C(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            b.this.a.y();
        }
    }
    class C0036b implements com.dudu.run.f.b.c<BaseResponse<Integer>> {
        C0036b() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            b.this.a.D(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<Integer> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                b.this.a.j(baseResponse.b().intValue());
            } else {
                b.this.a.D(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            b.this.a.z();
        }
    }
    class c implements com.dudu.run.f.b.c<BaseResponse<Float>> {
        c() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            b.this.a.B(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<Float> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                b.this.a.p(baseResponse.b().floatValue());
            } else {
                b.this.a.B(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            b.this.a.G();
        }
    }

    public b(com.dudu.run.e.d dVar) {
        this.a = dVar;
    }

    @Override // com.dudu.run.e.c
    public void a() {
        this.b.j(new C0036b());
    }

    @Override // com.dudu.run.e.c
    public void b() {
        this.b.g(new a());
    }

    @Override // com.dudu.run.e.c
    public void c() {
        this.b.l(new c());
    }
}
