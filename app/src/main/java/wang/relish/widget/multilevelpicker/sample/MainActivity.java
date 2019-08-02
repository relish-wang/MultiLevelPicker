package wang.relish.widget.multilevelpicker.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import wang.relish.widget.multilevelpicker.MultiLevelPickerWindow;

public class MainActivity extends AppCompatActivity {

    MultiLevelPickerWindow<Data> window;
    TextView tvText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvText = findViewById(R.id.tv);
        tvText.setOnClickListener(this::show);


    }

    private void show(View v) {
        if (window == null) {
            window = new MultiLevelPickerWindow<>(this);
        }
        window.setOnSelectListener(new MultiLevelPickerWindow.OnSelectListener<Data>() {
            @Override
            public void onSelect(Data data) {
                tvText.setText(data.text());
            }

            @Override
            public void onShow() {
                Toast.makeText(MainActivity.this, "show", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDismiss() {
                Toast.makeText(MainActivity.this, "dismiss", Toast.LENGTH_SHORT).show();
            }
        });
        window.updateData(generate());
        window.show(tvText);
    }

    static boolean isFirst = true;

    private static Data generate() {
        Data root = new Data(0, "", 0);
        List<Data> first = new ArrayList<>();
        // 100
        Data all = new Data(100, "全部教案", 20);
        first.add(all);
        // 200
        Data yiji = new Data(200, "一级", 13);
        List<Data> yijiChildren = new ArrayList<>();
            Data erji = new Data(210, "二级", 10);
            List<Data> erjiChildren = new ArrayList<>();
                Data vnfdj = new Data(211, "vnfdj", isFirst?0:1);
            erjiChildren.add(vnfdj);
            erji.children = erjiChildren;
        yiji.children = yijiChildren;
        yijiChildren.add(erji);
        first.add(yiji);
        // 300
        Data sanji = new Data(300, "三级", 13);
        first.add(sanji);
        // 400
        root.children = first;
        isFirst = false;
        return root;
    }
}