package com.dudu.run.d;

import android.view.ViewGroup;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.d.d;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dudu.run.R;
import com.dudu.run.bean.RecordItem;
import com.dudu.run.utils.o;
import com.dudu.run.ui.RunnerUi;
import java.util.List;
public class a extends BaseQuickAdapter<RecordItem, BaseViewHolder> implements d {
    private final List<RecordItem> records;
    private RunnerUi palette;

    public a(List<RecordItem> list) {
        super(R.layout.layout_record_item, list);
        records = list;
    }

    @Override
    public BaseViewHolder q(ViewGroup parent, int viewType) {
        return d0(parent, viewType);
    }

    @Override
    public void o(BaseViewHolder holder, int position) {
        a0(holder, position);
    }
    @Override // com.chad.library.adapter.base.BaseQuickAdapter
    public void E(BaseViewHolder baseViewHolder, RecordItem recordItem) {
        android.view.View card = baseViewHolder.getView(R.id.runner_record_card);
        if(palette==null)palette=new RunnerUi(card.getContext());
        RunnerUi ui = palette;
        int position = baseViewHolder.getAdapterPosition();
        float top = position == 0 ? ui.dp(22) : 0;
        float bottom = position == records.size() - 1 ? ui.dp(22) : 0;
        android.graphics.drawable.GradientDrawable shape = ui.background(ui.surface, 0);
        shape.setCornerRadii(new float[]{top, top, top, top, bottom, bottom, bottom, bottom});
        card.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(ui.dark ? 0x28ffffff : 0x20000000), shape, null));
        android.widget.ImageView icon = baseViewHolder.getView(R.id.runner_record_icon);
        icon.setColorFilter(ui.accent);
        icon.setBackground(ui.background(ui.tone, 15));
        baseViewHolder.setText(R.id.tv_item_time, RunnerUi.recordDate(recordItem.a()));
        ((android.widget.TextView) baseViewHolder.getView(R.id.tv_item_time)).getPaint().setFakeBoldText(true);
        baseViewHolder.setText(R.id.tv_item_distance, o.b(recordItem.c()) + " km");
        baseViewHolder.setText(R.id.tv_item_speed, "  ·  " + RunnerUi.duration(recordItem.d()));
        baseViewHolder.setTextColor(R.id.tv_item_time, ui.ink);
        baseViewHolder.setTextColor(R.id.tv_item_distance, ui.muted);
        baseViewHolder.setTextColor(R.id.tv_item_speed, ui.muted);
        ((android.widget.ImageView) baseViewHolder.getView(R.id.runner_record_chevron)).setColorFilter(ui.ink);
    }
}
