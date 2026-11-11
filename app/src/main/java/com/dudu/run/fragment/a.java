package com.dudu.run.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dudu.legacy.androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
public abstract class a extends Fragment {
    abstract int o();

    void prepare(View root) {
    }

    @Override // com.dudu.legacy.androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View viewInflate = layoutInflater.inflate(o(), viewGroup, false);
        prepare(viewInflate);
        ButterKnife.bind(this, viewInflate);
        return viewInflate;
    }
}
