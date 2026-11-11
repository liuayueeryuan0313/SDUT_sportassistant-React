package com.dudu.run.simulation;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.dudu.run.ui.NativeMaterial;
import com.dudu.run.ui.RunnerUi;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.text.InputType;
import android.widget.*;
import java.util.Locale;

/** Live speed changes do not open the route editor or restart/pause the recorder. */
final class QuickSpeedDialog {
    static void show(Activity activity){
        SimulationController controller=SimulationController.get(activity);
        SimulationEngine.Config current=controller.currentParameters();
        NativeMaterial material=new NativeMaterial(activity);RunnerUi ui=new RunnerUi(activity);
        LinearLayout page=material.dialogBody();
        EditText speed=field(material,page,"目标速度 · km/h",current.speedKmh);
        EditText variation=field(material,page,"速度波动 · ±km/h",current.speedVariationKmh);
        page.addView(ui.text("保存后平滑调整速度，本次跑步的计时、里程和路线继续。",12,ui.muted,false));
        AlertDialog dialog=material.dialog().setTitle("直接调整速度").setView(page)
            .setNegativeButton("取消",null).setPositiveButton("立即应用",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            try{controller.updateSpeed(Double.parseDouble(speed.getText().toString().trim()),Double.parseDouble(variation.getText().toString().trim()));dialog.dismiss();}
            catch(Exception error){speed.setError(error.getMessage()==null?"请检查速度与波动范围":error.getMessage());}
        }));dialog.show();
    }
    private static EditText field(NativeMaterial material,LinearLayout page,String label,double value){
        TextInputLayout box=new TextInputLayout(material.context());
        box.setHint(label);
        TextInputEditText text=new TextInputEditText(material.editContext());
        text.setSingleLine(true);text.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        text.setText(String.format(Locale.US,"%.1f",value));box.addView(text);
        page.addView(box,new LinearLayout.LayoutParams(-1,-2));
        return text;
    }
}

