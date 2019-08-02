package wang.relish.widget.multilevelpicker;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;
import androidx.core.widget.PopupWindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 修 改 者:  wangxin<br>
 * 修改时间: 20190730<br>
 * 修改原因: 电子教案2.0-要求被选择的前一级菜单仍然高亮<br>
 * <pre>
 *     author : cris
 *     time   : 2019/07/15
 *     desc   :
 * </pre>
 */
public class MultiLevelPickerWindow<T extends Node> extends PopupWindow {

    private RecyclerView rv1, rv2, rv3;

    private int selectedLevel = 0;
    private T selectedItem;

    private MultiLevelItemAdapter<T> mAdapter1, mAdapter2, mAdapter3;

    /**
     * 储存已选择的数据
     */
    private long[] storage = new long[]{0L/* 全部 */, -1L, -1L};

    public MultiLevelPickerWindow(Context context) {
        init(context);
    }

    private View mRootView;


    public void init(final Context context) {
        mRootView = LayoutInflater.from(context).inflate(
                R.layout.layout_popu_multscreen, null);
        buildView(mRootView);

        this.setOnDismissListener(() -> {
            if (mListener != null) {
                mListener.onDismiss();
            }
        });

        this.setContentView(mRootView);
        this.setWidth(context.getResources().getDisplayMetrics().widthPixels);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(false);
        this.setOutsideTouchable(false);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(context.getResources().getColor(android.R.color.transparent));
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismissListener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);

        this.setAnimationStyle(R.style.popupAnimation);

    }

    private void buildView(View view) {
        rv1 = view.findViewById(R.id.rv_1);
        rv2 = view.findViewById(R.id.rv_2);
        rv3 = view.findViewById(R.id.rv_3);

        mAdapter1 = new MultiLevelItemAdapter<>(null, 0);
        rv1.setAdapter(mAdapter1);
        mAdapter1.setOnSelectListener((parent, selectedChild) -> {

            //noinspection unchecked
            selectedItem = (T) selectedChild;
            storage[0] = selectedChild.id();

            parent.setSelectedChild(selectedChild.id());
            //noinspection unchecked
            mAdapter1.setNewData((T) parent);

            if (selectedChild.id() != 0) {//是全部的话 后面2级不展示啦
                //noinspection unchecked
                mAdapter2.setNewData((T) selectedChild);
            }
            mAdapter3.setNewData(null);
        });

        mAdapter2 = new MultiLevelItemAdapter<>(null, 1);
        rv2.setAdapter(mAdapter2);
        mAdapter2.setOnSelectListener((parent, selectedChild) -> {
            // 1 数据记录工作
            //noinspection unchecked
            selectedItem = (T) selectedChild;
            storage[1] = selectedChild.id();

            // 2 刷新父节点
            parent.setSelectedChild(selectedChild.id());
            //noinspection unchecked
            mAdapter2.setNewData((T) parent);

            // 3 展示子树
            //noinspection unchecked
            mAdapter3.setNewData((T) selectedChild);
        });

        mAdapter3 = new MultiLevelItemAdapter<>(null, 2);
        rv3.setAdapter(mAdapter3);
        mAdapter3.setOnSelectListener((parent, selectedChild) -> {
            // 1 数据记录工作
            //noinspection unchecked
            selectedItem = (T) selectedChild;
            storage[2] = selectedChild.id();

            // 2 刷新父节点
            parent.setSelectedChild(selectedChild.id());
            //noinspection unchecked
            mAdapter3.setNewData((T) parent);
        });

        view.findViewById(R.id.btnclose).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btndo).setOnClickListener(v -> {
            if (mListener != null) {
                //noinspection unchecked
                mListener.onSelect(selectedItem);
            }
            MultiLevelPickerWindow.this.dismiss();
        });
    }

    public void updateData(T t) {
        boolean isChanged = false;
        if (t == null) {
            mAdapter1.setNewData(null);
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return;
        }
        /* ***************** 1级 ***************** */
        // 1级数据监测与校准
        // 从内存中读取上次选中的一级菜单ID
        t.setSelectedChild(storage[0]);
        // 尝试获取上次选中的一级目录节点
        @Nullable Node selectedLevel1 = t.getSelectedChild();
        //noinspection StatementWithEmptyBody
        if (selectedLevel1 != null) { // 找到了上次选中的节点
            // as normal. do nothing.
        } else { // 没找到上次选中的节点
            isChanged = true;
            storage[0] = 0; // 切到"全部"
            t.setSelectedChild(storage[0]);
            // 重点: 当原来选择的一级目录被删除后, 选择"全部"
            //noinspection unchecked
            callbackSelected((T) t.getSelectedChild());
        }
        mAdapter1.setNewData(t);
        if (isChanged) {
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return;
        }

        /* ***************** 2级 ***************** */
        //noinspection unchecked
        List<T> children = (List<T>) t.children();
        if (children == null) {
            //noinspection StatementWithEmptyBody
            if (selectedLevel > 0) {// 说明之前选中的二级目录被删除了
                //noinspection unchecked
                callbackSelected((T) selectedLevel1);
                isChanged = true;
            } else {
                // 之前选中的二级目录没被删, 本来就是选的一级目录，都不需要通知外界
            }
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return;
        }
        //noinspection unchecked
        T second = (T) selectedLevel1; // 二级菜单的父节点
        second.setSelectedChild(storage[1]);
        // TODO 明天从这里开始！！！！！

        // 2级数据监测与校准
        Node selectedLevel2 = t.getSelectedChild();
        if (selectedLevel2 != null) {
            long id = selectedLevel2.id();
            if (id != storage[1]) {
                isChanged = true;
                storage[1] = id;
                //noinspection unchecked
                selectedItem = (T) selectedLevel2;
            }
        } else {

        }


        mAdapter2.setNewData(second);

        /* ***************** 3级 ***************** */
        //noinspection unchecked
        List<T> thirdList = (List<T>) second.children();
        if (thirdList == null) {
            mAdapter3.setNewData(null);
            return;
        }
        //noinspection unchecked
        T third = (T) second.getSelectedChild();
        if (third == null) {
            mAdapter3.setNewData(null);
            return;
        }
        third.setSelectedChild(storage[2]);
        mAdapter3.setNewData(third);
    }


    private void callbackSelected(T t) {
        if (mListener != null) {
            //noinspection unchecked
            mListener.onSelect(t);
        }
    }

    public void show(View view) {
        if (!this.isShowing()) {
            PopupWindowCompat.showAsDropDown(this, view, 0, 0, Gravity.END);
            if (mListener != null)
                mListener.onShow();
        } else {
            this.dismiss();
        }
    }


    public OnSelectListener mListener;

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mListener = onSelectListener;
    }

    public void removeSelectListener() {
        if (mListener != null) {
            mListener = null;
        }
    }

    public interface OnSelectListener<T> {
        void onSelect(T data);

        void onShow();

        void onDismiss();
    }
}
