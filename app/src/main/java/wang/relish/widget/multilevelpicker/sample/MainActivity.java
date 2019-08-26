package wang.relish.widget.multilevelpicker.sample;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import wang.relish.widget.multilevelpicker.MultiLevelPickerWindow;

public class MainActivity extends AppCompatActivity {

    MultiLevelPickerWindow<NodeImpl> window;
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
        window.setOnSelectListener(new MultiLevelPickerWindow.OnSelectListener<NodeImpl>() {
            @Override
            public void onSelect(int level, NodeImpl nodeImpl) {
                if (nodeImpl == null) {
                    tvText.setText("");
                    return;
                }
                tvText.setText(nodeImpl.text());
            }

            @Override
            public void onDownGraded(int selectLevel, NodeImpl nodeImpl) {
                if (selectLevel <= 0) {
                    Toast.makeText(
                            MainActivity.this,
                            "降级到: " + selectLevel,
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                Toast.makeText(MainActivity.this,
                        String.format(Locale.ENGLISH, "降级到%s级: %s", selectLevel, nodeImpl.toString()),
                        Toast.LENGTH_SHORT).show();
                Log.d("onDownGraded",
                        String.format(
                                Locale.ENGLISH,
                                "降级到%d级: %s",
                                selectLevel,
                                nodeImpl.toString()
                        )
                );
            }

            @Override
            public void onShow() {
                Log.d("onDownGraded", "show");
            }

            @Override
            public void onDismiss() {
                Log.d("onDownGraded", "dismiss");
            }
        });
        window.updateData(generate(this));
        window.show(tvText);
    }

    private static boolean isFirst = true;

    private static NodeImpl generate(Context context) {
        try {
            InputStream is = context.getAssets().open("tree.json");
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                result.append(line);
            }
            NodeImpl tree = new Gson().fromJson(result.toString(), NodeImpl.class);
            if (isFirst) {
                isFirst = false;
                return tree;
            } else {
                //noinspection ConstantConditions
                tree.children().remove(0);
                return tree;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NodeImpl.EMPTY;
    }
}
