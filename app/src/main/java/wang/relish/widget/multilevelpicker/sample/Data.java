package wang.relish.widget.multilevelpicker.sample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import wang.relish.widget.multilevelpicker.Node;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190802
 */
public class Data implements Node {

    private long id;
    private long selectedChildId;
    private String name;
    private int count = 0;

    public List<Data> children = new ArrayList<>();

    public Data(long id, String name, int count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    @Override
    public long id() {
        return id;
    }

    @NonNull
    @Override
    public String text() {
        return name + "(" + count + ")";
    }

    @Override
    public long selectedChild() {
        return selectedChildId;
    }

    @Override
    public void setSelectedChild(long id) {
        selectedChildId = id;
    }

    @Nullable
    @Override
    public List<? extends Node> children() {
        return children;
    }

    @Override
    public int getSelectedChildPosition() {
        if (children == null) return -1;
        for (int i = 0; i < children.size(); i++) {
            Data data = children.get(i);
            if (data == null) continue;
            if (data.id == selectedChildId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Node getSelectedChild() {
        int i = getSelectedChildPosition();
        if (i == -1) return null;
        return children.get(i);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return text() + "[" + id + "]";
    }
}
