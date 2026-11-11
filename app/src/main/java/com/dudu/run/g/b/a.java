package com.dudu.run.g.b;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.CampusResponseData;
import com.dudu.run.bean.CheckAppUpdateResponseData;
import com.dudu.run.bean.ConfigResponseData;
import com.dudu.run.bean.FeedbackRequestData;
import com.dudu.run.bean.LatLngInfo;
import com.dudu.run.bean.LoginRequestData;
import com.dudu.run.bean.LoginResponseData;
import com.dudu.run.bean.RandomPoint;
import com.dudu.run.bean.RandomPointRequestData;
import com.dudu.run.bean.RecordDetailRequestData;
import com.dudu.run.bean.RecordDetailResponseData;
import com.dudu.run.bean.RecordItem;
import com.dudu.run.bean.RecordListRequestData;
import com.dudu.run.bean.SelectCampusRequestData;
import com.dudu.run.bean.SubmitRecordRequestData;
import com.dudu.run.bean.UserIdRequestData;
import com.dudu.run.utils.n;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit2.s;
public class a implements com.dudu.run.g.a {
    private com.dudu.run.f.a.a a = (com.dudu.run.f.a.a) com.dudu.run.f.b.a.a().b(com.dudu.run.f.a.a.class);
    class C0038a implements retrofit2.f<BaseResponse<CampusResponseData>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        C0038a(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<CampusResponseData>> dVar, s<BaseResponse<CampusResponseData>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<CampusResponseData>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class b implements retrofit2.f<BaseResponse> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        b(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse> dVar, s<BaseResponse> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class c implements retrofit2.f<BaseResponse> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        c(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse> dVar, s<BaseResponse> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class d implements retrofit2.f<BaseResponse<LoginResponseData>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        d(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<LoginResponseData>> dVar, s<BaseResponse<LoginResponseData>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<LoginResponseData>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class e implements retrofit2.f<BaseResponse<ConfigResponseData>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        e(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<ConfigResponseData>> dVar, s<BaseResponse<ConfigResponseData>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<ConfigResponseData>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class f implements retrofit2.f<BaseResponse<List<RandomPoint>>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        f(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<List<RandomPoint>>> dVar, s<BaseResponse<List<RandomPoint>>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<List<RandomPoint>>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class g implements retrofit2.f<BaseResponse> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        g(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse> dVar, s<BaseResponse> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class h implements retrofit2.f<BaseResponse<RecordDetailResponseData>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        h(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<RecordDetailResponseData>> dVar, s<BaseResponse<RecordDetailResponseData>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<RecordDetailResponseData>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class i implements retrofit2.f<BaseResponse<List<RecordItem>>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        i(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<List<RecordItem>>> dVar, s<BaseResponse<List<RecordItem>>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<List<RecordItem>>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class j implements retrofit2.f<BaseResponse<Integer>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        j(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<Integer>> dVar, s<BaseResponse<Integer>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<Integer>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class k implements retrofit2.f<BaseResponse<CheckAppUpdateResponseData>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        k(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<CheckAppUpdateResponseData>> dVar, s<BaseResponse<CheckAppUpdateResponseData>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<CheckAppUpdateResponseData>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }
    class l implements retrofit2.f<BaseResponse<Float>> {
        final /* synthetic */ com.dudu.run.f.b.c a;

        l(a aVar, com.dudu.run.f.b.c cVar) {
            this.a = cVar;
        }

        @Override // retrofit2.f
        public void a(retrofit2.d<BaseResponse<Float>> dVar, s<BaseResponse<Float>> sVar) {
            if (sVar != null) {
                this.a.b(sVar.a());
            }
            this.a.a();
        }

        @Override // retrofit2.f
        public void b(retrofit2.d<BaseResponse<Float>> dVar, Throwable th) {
            this.a.c(th.toString());
            this.a.a();
        }
    }

    @Override // com.dudu.run.g.a
    public void a(int i2, com.dudu.run.f.b.c<BaseResponse<RecordDetailResponseData>> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        RecordDetailRequestData recordDetailRequestData = new RecordDetailRequestData();
        recordDetailRequestData.a(i2);
        retrofit2.d<BaseResponse<RecordDetailResponseData>> dVarG = this.a.g(strA, strB, recordDetailRequestData);
        cVar.onStart();
        dVarG.W(new h(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void b(String str, String str2, com.dudu.run.f.b.c<BaseResponse<LoginResponseData>> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        LoginRequestData loginRequestData = new LoginRequestData();
        loginRequestData.b(str);
        loginRequestData.a(str2);
        retrofit2.d<BaseResponse<LoginResponseData>> dVarL = this.a.l(strA, strB, loginRequestData);
        cVar.onStart();
        dVarL.W(new d(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void c(int i2, long j2, long j3, float f2, int i3, List<LatLngInfo> list, com.dudu.run.f.b.c<BaseResponse> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        SubmitRecordRequestData submitRecordRequestData = new SubmitRecordRequestData();
        submitRecordRequestData.g(com.dudu.run.b.c().n());
        submitRecordRequestData.b(i2);
        submitRecordRequestData.a(com.dudu.run.utils.e.b(j2));
        submitRecordRequestData.c(com.dudu.run.utils.e.b(j3));
        submitRecordRequestData.e((int) f2);
        submitRecordRequestData.f(i3);
        ArrayList arrayList = new ArrayList();
        for (LatLngInfo latLngInfo : list) {
            HashMap map = new HashMap();
            map.put("latLng", latLngInfo.latLng);
            if (latLngInfo.isStartPosition) {
                map.put("isStartPosition", Boolean.TRUE);
            }
            arrayList.add(map);
        }
        submitRecordRequestData.d(new com.google.gson.e().r(arrayList));
        retrofit2.d<BaseResponse> dVarE = this.a.e(strA, strB, submitRecordRequestData);
        cVar.onStart();
        dVarE.W(new g(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void d(com.dudu.run.f.b.c<BaseResponse> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        UserIdRequestData userIdRequestData = new UserIdRequestData();
        userIdRequestData.a(com.dudu.run.b.c().n());
        retrofit2.d<BaseResponse> dVarI = this.a.i(strA, strB, userIdRequestData);
        cVar.onStart();
        dVarI.W(new b(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void e(int i2, int i3, com.dudu.run.f.b.c<BaseResponse<List<RecordItem>>> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        RecordListRequestData recordListRequestData = new RecordListRequestData();
        recordListRequestData.c(com.dudu.run.b.c().n());
        recordListRequestData.a(i2);
        recordListRequestData.b(i3);
        retrofit2.d<BaseResponse<List<RecordItem>>> dVarA = this.a.a(strA, strB, recordListRequestData);
        cVar.onStart();
        dVarA.W(new i(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void f(int i2, double d2, double d3, com.dudu.run.f.b.c<BaseResponse<List<RandomPoint>>> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        RandomPointRequestData randomPointRequestData = new RandomPointRequestData();
        randomPointRequestData.d(com.dudu.run.b.c().n());
        randomPointRequestData.a(i2);
        randomPointRequestData.b(d2);
        randomPointRequestData.c(d3);
        retrofit2.d<BaseResponse<List<RandomPoint>>> dVarB = this.a.b(strA, strB, randomPointRequestData);
        cVar.onStart();
        dVarB.W(new f(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void g(com.dudu.run.f.b.c<BaseResponse<ConfigResponseData>> cVar) {
        String strA = n.a();
        retrofit2.d<BaseResponse<ConfigResponseData>> dVarC = this.a.c(strA, n.b(strA));
        cVar.onStart();
        dVarC.W(new e(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void h(double d2, double d3, com.dudu.run.f.b.c<BaseResponse<CampusResponseData>> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        SelectCampusRequestData selectCampusRequestData = new SelectCampusRequestData();
        selectCampusRequestData.a(d2);
        selectCampusRequestData.b(d3);
        retrofit2.d<BaseResponse<CampusResponseData>> dVarF = this.a.f(strA, strB, selectCampusRequestData);
        cVar.onStart();
        dVarF.W(new C0038a(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void i(String str, String str2, com.dudu.run.f.b.c<BaseResponse> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        FeedbackRequestData feedbackRequestData = new FeedbackRequestData();
        feedbackRequestData.c(com.dudu.run.b.c().n());
        feedbackRequestData.a(str);
        feedbackRequestData.b(str2);
        retrofit2.d<BaseResponse> dVarH = this.a.h(strA, strB, feedbackRequestData);
        cVar.onStart();
        dVarH.W(new c(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void j(com.dudu.run.f.b.c<BaseResponse<Integer>> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        UserIdRequestData userIdRequestData = new UserIdRequestData();
        userIdRequestData.a(com.dudu.run.b.c().n());
        retrofit2.d<BaseResponse<Integer>> dVarD = this.a.d(strA, strB, userIdRequestData);
        cVar.onStart();
        dVarD.W(new j(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void k(com.dudu.run.f.b.c<BaseResponse<CheckAppUpdateResponseData>> cVar) {
        String strA = n.a();
        retrofit2.d<BaseResponse<CheckAppUpdateResponseData>> dVarJ = this.a.j(strA, n.b(strA));
        cVar.onStart();
        dVarJ.W(new k(this, cVar));
    }

    @Override // com.dudu.run.g.a
    public void l(com.dudu.run.f.b.c<BaseResponse<Float>> cVar) {
        String strA = n.a();
        String strB = n.b(strA);
        UserIdRequestData userIdRequestData = new UserIdRequestData();
        userIdRequestData.a(com.dudu.run.b.c().n());
        retrofit2.d<BaseResponse<Float>> dVarK = this.a.k(strA, strB, userIdRequestData);
        cVar.onStart();
        dVarK.W(new l(this, cVar));
    }
}
