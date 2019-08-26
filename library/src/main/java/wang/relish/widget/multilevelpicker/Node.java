package wang.relish.widget.multilevelpicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190802
 */
public interface Node {

    /**
     * 标记唯一性
     *
     * @return -1 when it is root
     */
    long id();

    /**
     * 显示文案
     */
    @NonNull
    String text();

    /**
     * 被选中的Id
     *
     * @return -1 when {@link #children()} is null
     */
    long selectedChild();

    void setSelectedChild(long id);

    /**
     * 子树(为空表示是叶节点)
     *
     * @return null when it is a leaf
     */
    @Nullable
    List<? extends Node> children();

    /**
     * 返回被选中的child
     */
    Node getSelectedChild();

}
