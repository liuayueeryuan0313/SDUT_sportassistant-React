package com.dudu.run.activity;

import android.os.Bundle;
import com.dudu.run.R;
import com.dudu.run.fragment.RuleFragment;
import com.dudu.run.ui.RunnerUi;

public final class RuleActivity extends BaseActivity {
    @Override int u0() {
        return R.layout.runner_rule;
    }

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        RunnerUi ui = new RunnerUi(this);
        ui.applySystemBars(this);
        findViewById(R.id.container).setBackgroundColor(ui.page);
        if (saved == null) B0(R.id.container, null, new RuleFragment(), "full_run_rules");
    }
}
