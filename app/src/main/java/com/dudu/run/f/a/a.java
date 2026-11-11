package com.dudu.run.f.a;

import com.dudu.run.bean.BaseResponse;
import com.dudu.run.bean.CampusResponseData;
import com.dudu.run.bean.CheckAppUpdateResponseData;
import com.dudu.run.bean.ConfigResponseData;
import com.dudu.run.bean.FeedbackRequestData;
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
import java.util.List;
import retrofit2.d;
import retrofit2.z.o;
import retrofit2.z.t;
public interface a {
    @o("api/run/queryRunList")
    d<BaseResponse<List<RecordItem>>> a(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a RecordListRequestData recordListRequestData);

    @o("api/semester/queryPoint")
    d<BaseResponse<List<RandomPoint>>> b(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a RandomPointRequestData randomPointRequestData);

    @o("api/semester/getCurrSemester")
    d<BaseResponse<ConfigResponseData>> c(@t("salt") String str, @t("sign") String str2);

    @o("api/run/getRunCnt")
    d<BaseResponse<Integer>> d(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a UserIdRequestData userIdRequestData);

    @o("api/run/addRunInfo")
    d<BaseResponse> e(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a SubmitRecordRequestData submitRecordRequestData);

    @o("/api/semester/getNearestCampus")
    d<BaseResponse<CampusResponseData>> f(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a SelectCampusRequestData selectCampusRequestData);

    @o("api/run/getRunInfo")
    d<BaseResponse<RecordDetailResponseData>> g(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a RecordDetailRequestData recordDetailRequestData);

    @o("/api/sys/subFeedback")
    d<BaseResponse> h(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a FeedbackRequestData feedbackRequestData);

    @o("/api/run/checkRunAllow")
    d<BaseResponse> i(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a UserIdRequestData userIdRequestData);

    @o("/api/sys/currVersion")
    d<BaseResponse<CheckAppUpdateResponseData>> j(@t("salt") String str, @t("sign") String str2);

    @o("api/run/getRunLengthKm")
    d<BaseResponse<Float>> k(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a UserIdRequestData userIdRequestData);

    @o("api/user/login")
    d<BaseResponse<LoginResponseData>> l(@t("salt") String str, @t("sign") String str2, @retrofit2.z.a LoginRequestData loginRequestData);
}
