package com.dudu.run.g;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.CampusResponseData;
import com.dudu.run.bean.CheckAppUpdateResponseData;
import com.dudu.run.bean.ConfigResponseData;
import com.dudu.run.bean.LatLngInfo;
import com.dudu.run.bean.LoginResponseData;
import com.dudu.run.bean.RandomPoint;
import com.dudu.run.bean.RecordDetailResponseData;
import com.dudu.run.bean.RecordItem;
import com.dudu.run.f.b.c;
import java.util.List;
public interface a {
    void a(int i, c<BaseResponse<RecordDetailResponseData>> cVar);

    void b(String str, String str2, c<BaseResponse<LoginResponseData>> cVar);

    void c(int i, long j, long j2, float f2, int i2, List<LatLngInfo> list, c<BaseResponse> cVar);

    void d(c<BaseResponse> cVar);

    void e(int i, int i2, c<BaseResponse<List<RecordItem>>> cVar);

    void f(int i, double d2, double d3, c<BaseResponse<List<RandomPoint>>> cVar);

    void g(c<BaseResponse<ConfigResponseData>> cVar);

    void h(double d2, double d3, c<BaseResponse<CampusResponseData>> cVar);

    void i(String str, String str2, c<BaseResponse> cVar);

    void j(c<BaseResponse<Integer>> cVar);

    void k(c<BaseResponse<CheckAppUpdateResponseData>> cVar);

    void l(c<BaseResponse<Float>> cVar);
}
