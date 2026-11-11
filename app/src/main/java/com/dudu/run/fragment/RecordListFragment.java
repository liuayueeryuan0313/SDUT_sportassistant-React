package com.dudu.run.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import com.dudu.legacy.androidx.recyclerview.widget.LinearLayoutManager;
import com.dudu.legacy.androidx.recyclerview.widget.RecyclerView;
import com.dudu.legacy.androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.dudu.run.R;
import com.dudu.run.activity.HomeActivity;
import com.dudu.run.activity.RecordDetailActivity;
import com.dudu.run.bean.RecordItem;
import com.dudu.run.ui.RunnerUi;
import java.util.ArrayList;
import java.util.List;

public final class RecordListFragment extends a implements com.dudu.run.e.l {
    private final List<RecordItem> records = new ArrayList<>();
    private com.dudu.run.d.a adapter;
    private com.dudu.run.e.l responses;
    private final android.os.Handler main=new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean loadedOnce;
    private boolean loading;
    private long loadedVersion=-1,requestVersion;
    private long visibleAfter;
    private final Runnable firstLoad=()->{
        if(getView()!=null&&!isHidden()&&!loadedOnce&&!loading)request(1);
    };
    private SwipeRefreshLayout refresh;
    private RecyclerView list;
    private RecyclerView.s scrollListener;
    private int page = 1;
    private int generation;

    @Override int o() { return R.layout.activity_record_list; }

    @Override public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        RunnerUi ui = new RunnerUi(getActivity());
        View root = getView();
        root.setBackgroundColor(ui.page);
        View dock = root.findViewById(R.id.layout_tabs);
        ((ViewGroup) dock.getParent()).removeView(dock);
        TextView title = root.findViewById(R.id.tv_title);
        title.setTextColor(ui.ink);
        title.getPaint().setFakeBoldText(true);
        ((TextView) root.findViewById(R.id.runner_records_brand)).setTextColor(ui.accentText);
        MaterialButton avatar = root.findViewById(R.id.runner_records_avatar);
        ui.styleAvatar(avatar);
        avatar.setOnClickListener(view -> ((HomeActivity) getActivity()).I0(2));
        refresh = root.findViewById(R.id.srl_refresh);
        refresh.setColorSchemeColors(ui.accent);
        refresh.setProgressBackgroundColorSchemeColor(ui.surface);
        list = root.findViewById(R.id.rcv_list);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setItemAnimator(null);
        scrollListener = new RecyclerView.s() {
            @Override public void b(RecyclerView view, int dx, int dy) { updateTopButton(); }
            @Override public void a(RecyclerView view, int state) { updateTopButton(); }
        };
        list.l(scrollListener);
        list.post(this::updateTopButton);
        list.setBackground(ui.background(android.graphics.Color.TRANSPARENT, 22));
        list.setClipToOutline(true);
        adapter = new com.dudu.run.d.a(records);
        list.setAdapter(adapter);
        adapter.g0(R.layout.layout_prompt_nodata);
        adapter.m0((base, view, position) -> {
            if (position < 0 || position >= records.size()) return;
            com.dudu.run.ui.NativeNavigation.launch(getActivity(), new Intent(getActivity(), RecordDetailActivity.class)
                    .putExtra("recordId", records.get(position).b()));
        });
        int current = ++generation;
        responses = new com.dudu.run.e.l() {
            @Override public void N(int loadedPage, List<RecordItem> items) {
                main.post(()->{if (current == generation) RecordListFragment.this.N(loadedPage, items);});
            }
            @Override public void t(String message) {
                main.post(()->{if (current == generation) RecordListFragment.this.t(message);});
            }
            @Override public void v(int ignored) {}
        };
        adapter.U().w(() -> { if (loadedOnce && !loading) request(page + 1); });
        refresh.setOnRefreshListener(() -> {
            if (loading) { refresh.setRefreshing(false); return; }
            request(1);
        });
        scheduleFirstLoad();
    }

    private void request(int nextPage) {
        loading = true;
        if(nextPage==1)requestVersion=com.dudu.run.ui.RunResultSession.recordsVersion();
        if (nextPage == 1) refresh.setRefreshing(true);
        final int token=generation;
        com.dudu.run.e.l callback=responses;
        com.dudu.run.ui.RunnerWork.IO.execute(()->{
            try{new com.dudu.run.e.s.f(callback).a(nextPage,20);}
            catch(RuntimeException error){main.post(()->{if(token==generation)t("记录加载失败");});}
        });
    }

    private void scheduleFirstLoad(){
        if(loadedOnce&&loadedVersion!=com.dudu.run.ui.RunResultSession.recordsVersion())loadedOnce=false;
        visibleAfter=android.os.SystemClock.uptimeMillis()+360;
        if(getView()!=null&&!loadedOnce&&!loading){getView().removeCallbacks(firstLoad);getView().postDelayed(firstLoad,360);}
    }
    @Override public void onHiddenChanged(boolean hidden){
        super.onHiddenChanged(hidden);
        if(!hidden)scheduleFirstLoad();else if(getView()!=null)getView().removeCallbacks(firstLoad);
    }
    @Override public void onResume(){super.onResume();if(!isHidden())scheduleFirstLoad();}

    public boolean isScrolled() {
        return list != null && list.canScrollVertically(-1);
    }

    private void updateTopButton() {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).updateRecordTab(isScrolled());
        }
    }

    public void returnToTop() {
        // Retained AndroidX RecyclerView's native smoothScrollToPosition API.
        if (list != null) list.p1(0);
    }

    public void resetToTop() {
        if (list != null) {
            list.h1(0); // Native scrollToPosition, including stopping a fling.
            list.post(this::updateTopButton);
        }
    }

    @Override public void N(int loadedPage, List<RecordItem> items) {
        if (getView() == null || !isAdded()) return;
        long delay=visibleAfter-android.os.SystemClock.uptimeMillis();
        if(!isHidden()&&delay>0){int token=generation;main.postDelayed(()->{if(token==generation)N(loadedPage,items);},delay);return;}
        loading = false;loadedOnce=true;
        if(loadedPage==1)loadedVersion=requestVersion;
        refresh.setRefreshing(false);
        if (loadedPage == 1) records.clear();
        if (items != null) records.addAll(items);
        page = loadedPage;
        adapter.j();
        list.post(this::updateTopButton);
        if (items == null || items.size() < 20) adapter.U().q();
        else adapter.U().p();
    }

    @Override public void t(String message) {
        loading = false;
        if (getView() == null || !isAdded()) return;
        refresh.setRefreshing(false);
        adapter.U().t();
    }

    @Override public void v(int ignored) {}

    @Override public void onDestroyView() {
        if(getView()!=null)getView().removeCallbacks(firstLoad);
        main.removeCallbacksAndMessages(null);
        generation++;
        loading = false;
        refresh = null;
        adapter = null;
        responses=null;loadedOnce=false;
        if (list != null && scrollListener != null) list.Y0(scrollListener);
        scrollListener = null;
        list = null;
        super.onDestroyView();
    }
}
