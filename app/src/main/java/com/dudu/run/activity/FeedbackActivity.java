package com.dudu.run.activity;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import android.widget.Toast;
import com.dudu.run.R;
import com.dudu.run.ui.NativeMaterial;
import com.dudu.run.ui.RunnerUi;
import com.google.android.material.button.MaterialButton;

public final class FeedbackActivity extends MotionActivity implements com.dudu.run.e.b {
    private com.dudu.run.e.a presenter;
    private EditText feedback;
    private EditText phone;
    private MaterialButton submit;
    private CircularProgressIndicator progress;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        presenter = new com.dudu.run.e.s.a(this);
        RunnerUi ui = new RunnerUi(this);
        NativeMaterial material = new NativeMaterial(this);
        LinearLayout page = material.settingsPage(this, "意见反馈");
        feedback = material.field(page, "您的意见",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, true);
        feedback.setId(R.id.et_feedback);
        ui.gap(page, 16);
        phone = material.field(page, "手机号码", InputType.TYPE_CLASS_PHONE, false);
        phone.setId(R.id.et_phone);
        ui.gap(page, 20);
        submit = material.button("提交反馈", this::submitFeedback);
        page.addView(submit);
        progress = new CircularProgressIndicator(material.context());
        progress.setIndicatorSize(ui.dp(28));
        progress.setTrackThickness(ui.dp(3));
        progress.setIndeterminate(true);
        progress.setContentDescription("正在提交反馈");
        progress.setIndicatorColor(ui.accent);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams indicator = new LinearLayout.LayoutParams(ui.dp(28), ui.dp(28));
        indicator.gravity = Gravity.CENTER_HORIZONTAL;
        indicator.topMargin = ui.dp(12);
        page.addView(progress, indicator);
    }

    private void submitFeedback() {
        String message = feedback.getText().toString().trim();
        String number = phone.getText().toString().trim();
        if (message.isEmpty()) {
            feedback.setError("请输入您的意见");
            feedback.requestFocus();
        } else if (number.isEmpty()) {
            phone.setError("请输入您的手机号");
            phone.requestFocus();
        } else if (com.dudu.run.utils.l.b(this)) {
            presenter.a(message, number);
        }
    }

    @Override public void K() {
        if (isFinishing() || isDestroyed()) return;
        submit.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
    }

    @Override public void n(String message) {
        if (isFinishing() || isDestroyed()) return;
        submit.setEnabled(true);
        progress.setVisibility(View.GONE);
        Toast.makeText(this, message == null || message.isEmpty() ? "提交失败，请重试" : message,
                Toast.LENGTH_SHORT).show();
    }

    @Override public void u() {
        if (isFinishing() || isDestroyed()) return;
        Toast.makeText(this, "反馈成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}
