package wang.relish.widget.multilevelpicker.sample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wang.relish.widget.multilevelpicker.Node;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190802
 */
public class NodeImpl implements Node, Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private int count;
    private List<NodeImpl> children = new ArrayList<>();
    private transient long selectedChildId;

    static final transient NodeImpl EMPTY = new NodeImpl(0, "全部", 0);


    public NodeImpl(long id, String name, int count) {
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

    public void setChildren(List<NodeImpl> children) {
        this.children = children;
    }

    @Override
    public Node getSelectedChild() {
        int i = getSelectedChildPosition();
        if (i == -1) return null;
        // in case of IndexOutOfBoundsException
        if (i >= 0 && i < children.size()) {
            return children.get(i);
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return text() + "[" + id + "]";
    }

    private int getSelectedChildPosition() {
        if (children == null) return -1;
        for (int i = 0; i < children.size(); i++) {
            NodeImpl nodeImpl = children.get(i);
            if (nodeImpl == null) continue;
            if (nodeImpl.id == selectedChildId) {
                return i;
            }
        }
        return -1;
    }
}
