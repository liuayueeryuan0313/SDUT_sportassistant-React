package com.dudu.run.e.s;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.RecordDetailResponseData;
import com.dudu.run.e.j;
public class e implements com.dudu.run.e.i {
    private j a;
    private com.dudu.run.g.a b = new com.dudu.run.g.b.a();
    class a implements com.dudu.run.f.b.c<BaseResponse<RecordDetailResponseData>> {
        a() {
        }

        @Override // com.dudu.run.f.b.c
        public void a() {
        }

        @Override // com.dudu.run.f.b.c
        public void c(String str) {
            e.this.a.q(str);
        }

        @Override // com.dudu.run.f.b.c
        public void b(BaseResponse<RecordDetailResponseData> baseResponse) {
            if (com.dudu.run.utils.c.a(baseResponse)) {
                e.this.a.A(baseResponse.b());
            } else {
                e.this.a.q(baseResponse == null ? "" : baseResponse.c());
            }
        }

        @Override // com.dudu.run.f.b.c
        public void onStart() {
            e.this.a.x();
        }
    }

    public e(j jVar) {
        this.a = jVar;
    }

    @Override // com.dudu.run.e.i
    public void a(int i) {
        this.b.a(i, new a());
    }
}
