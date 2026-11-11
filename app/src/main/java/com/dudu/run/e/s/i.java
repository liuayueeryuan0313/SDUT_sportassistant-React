package com.dudu.run.e.s;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.CampusResponseData;
import com.dudu.run.bean.RandomPoint;
import com.dudu.run.e.q;
import com.dudu.run.e.r;
import java.util.List;
public class i implements q {
    private r a;
    private com.dudu.run.g.a b = new com.dudu.run.g.b.a();
    class a implements com.dudu.run.f.b.c<BaseResponse<List<RandomPoint>>> {
        a() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            i.this.a.i(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<List<RandomPoint>> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                i.this.a.m(baseResponse.b());
            } else {
                i.this.a.i(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            i.this.a.f();
        }
    }
    class b implements com.dudu.run.f.b.c<BaseResponse<CampusResponseData>> {
        final /* synthetic */ double a;
        final /* synthetic */ double b;

        b(double d2, double d3) {
            this.a = d2;
            this.b = d3;
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            i.this.a.d(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<CampusResponseData> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                i.this.a.g(baseResponse.b().a(), this.a, this.b);
            } else {
                i.this.a.d(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            i.this.a.j();
        }
    }
    class c implements com.dudu.run.f.b.c<BaseResponse> {
        c() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            i.this.a.l(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                i.this.a.k();
            } else {
                i.this.a.l(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            i.this.a.n();
        }
    }

    public i(r rVar) {
        this.a = rVar;
    }

    @Override // com.dudu.run.e.q
    public void a(int i, double d2, double d3) {
        this.b.f(i, d2, d3, new a());
    }

    @Override // com.dudu.run.e.q
    public void b(double d2, double d3) {
        this.b.h(d2, d3, new b(d2, d3));
    }

    @Override // com.dudu.run.e.q
    public void c() {
        this.b.d(new c());
    }
}
