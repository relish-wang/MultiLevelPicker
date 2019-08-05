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
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190802
 */
public class MultiLevelPickerWindow<T extends Node> extends PopupWindow {

    private RecyclerView rv1, rv2, rv3;

    private int selectedLevel = -1;
    private T selectedItem = null;

    private MultiLevelItemAdapter<T> mAdapter1, mAdapter2, mAdapter3;

    /**
     * 储存已选择的数据
     */
    private long[] storage = new long[]{-1L/* 全部 */, -1L, -1L};

    public MultiLevelPickerWindow(Context context) {
        init(context);
    }

    private View mRootView;


    private void init(final Context context) {
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
            selectedLevel = 0;
            storage[0] = selectedChild.id();
            storage[1] = -1;
            storage[2] = -1;


            parent.setSelectedChild(selectedChild.id());
            //noinspection unchecked
            mAdapter1.setNewData((T) parent);

            if (selectedChild.id() != mDefaultRootId) {//是全部的话 后面2级不展示啦
                selectedChild.setSelectedChild(-1);
                //noinspection unchecked
                mAdapter2.setNewData((T) selectedChild);
            } else {
                mAdapter2.setNewData(null);
            }
            mAdapter3.setNewData(null);
        });

        mAdapter2 = new MultiLevelItemAdapter<>(null, 1);
        rv2.setAdapter(mAdapter2);
        mAdapter2.setOnSelectListener((parent, selectedChild) -> {
            // 1 数据记录工作
            //noinspection unchecked
            selectedItem = (T) selectedChild;
            selectedLevel = 1;
            storage[1] = selectedChild.id();
            storage[2] = -1;

            // 2 刷新父节点
            parent.setSelectedChild(selectedChild.id());
            //noinspection unchecked
            mAdapter2.setNewData((T) parent);

            // 3 展示子树
            selectedChild.setSelectedChild(-1);
            //noinspection unchecked
            mAdapter3.setNewData((T) selectedChild);
        });

        mAdapter3 = new MultiLevelItemAdapter<>(null, 2);
        rv3.setAdapter(mAdapter3);
        mAdapter3.setOnSelectListener((parent, selectedChild) -> {
            // 1 数据记录工作
            //noinspection unchecked
            selectedItem = (T) selectedChild;
            selectedLevel = 2;
            storage[2] = selectedChild.id();

            // 2 刷新父节点
            parent.setSelectedChild(selectedChild.id());
            //noinspection unchecked
            mAdapter3.setNewData((T) parent);
        });

        view.findViewById(R.id.btnclose).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btndo).setOnClickListener(v -> {
            callbackSelected(false);
            MultiLevelPickerWindow.this.dismiss();
        });
    }

    /**
     * 更新节点数据, 并采用降级策略
     * 降级策略:
     * 被选择级别节点被删除则选中它的上一级选中节点, 一级节点丢失, 则选中"全部"(即id为0的那项)
     * 比如:
     * 1 被选择的三级节点被删除(或三级菜单丢失),选中二级菜单
     * 2 被选择的二级节点被删除(或二级菜单丢失),选中一级菜单
     * 2 被选择的一级节点被删除(或二级菜单丢失),选中一级菜单的"全部"(要是全部节点也没有? 就恢复无选择状态)
     *
     * @return 是否发生节点变更(节点丢失, backup策略启动)
     */
    public boolean updateData(T t) {
        boolean isDownGraded = false;
        if (t == null) {
            mAdapter1.setNewData(null);
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return false;
        }
        /* ***************** 1级 ***************** */
        // 1级数据监测与校准
        if (storage[0] < 0) {
            // 第一次进来 或 没选择过
            mAdapter1.setNewData(t);
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return false;
        }
        // 从内存中读取上次选中的一级菜单ID
        t.setSelectedChild(storage[0]);
        // 尝试获取上次选中的一级目录节点
        //noinspection unchecked
        @Nullable T selectedLevel1 = (T) t.getSelectedChild();
        //noinspection StatementWithEmptyBody
        if (selectedLevel1 != null) { // 找到了上次选中的节点
            // as normal. do nothing. 不用给selectedItem赋值
        } else { // 没找到上次选中的一级节点
            isDownGraded = true;
            storage[0] = mDefaultRootId; // 切到"全部"
            t.setSelectedChild(storage[0]);
            // 重点: 当原来选择的一级目录被删除后, 选择"全部"
            //noinspection unchecked
            T all = (T) t.getSelectedChild();
            if (all != null) { // 找到了全部
                selectedItem = all;
                selectedLevel = 0;
                callbackSelected(true);
                storage[0] = all.id();
                storage[1] = -1;
                storage[2] = -1;
            } else { // 没找到全部, 降级到未选择状态
                selectedItem = null;
                selectedLevel = -1;
                callbackSelected(true);
                storage[0] = -1;
                storage[1] = -1;
                storage[2] = -1;
            }
        }
        mAdapter1.setNewData(t);
        if (isDownGraded) {
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return true;
        }

        /* ***************** 2级 ***************** */
        //noinspection unchecked
        List<T> secondList = (List<T>) t.children();
        if (secondList == null) {
            //noinspection StatementWithEmptyBody
            if (selectedLevel > 0) {// 说明之前选中的二或三级目录被删除了
                isDownGraded = true; // useless, but for good reading.
                selectedItem = selectedLevel1;
                selectedLevel = 0;
                callbackSelected(true);
                storage[0] = selectedItem.id();
                storage[1] = -1;
                storage[2] = -1;
            } else {
                // 之前选中的二级目录没被删, 本来就是选的一级目录，都不需要通知外界
            }
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return isDownGraded;
        }
        if (storage[1] < 0) { // 表示本来就没选择二级菜单
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return false;
        }
        //noinspection UnnecessaryLocalVariable
        T second = selectedLevel1; // 二级菜单的父节点
        second.setSelectedChild(storage[1]);
        //noinspection unchecked
        T selectedLevel2 = (T) second.getSelectedChild(); // 尝试获取上次选择的二级菜单节点
        //noinspection StatementWithEmptyBody
        if (selectedLevel2 != null) {
            // 之前选中的二级目录没被删, 且找到了.(有可能新增了菜单项, 且index也变了)
        } else {
            // 之前的二级目录被删除
            isDownGraded = true; // useless, but for good reading.
            selectedItem = selectedLevel1;
            selectedLevel = 0;
            callbackSelected(true);
            storage[0] = selectedItem.id();
            storage[1] = -1;
            storage[2] = -1;
            mAdapter2.setNewData(null);
            mAdapter3.setNewData(null);
            return true;
        }
        mAdapter2.setNewData(second);

        /* ***************** 3级 ***************** */
        //noinspection unchecked
        List<T> thirdList = (List<T>) second.children();
        if (thirdList == null) {
            //noinspection StatementWithEmptyBody
            if (selectedLevel > 1) {// 说明之前选中的三级目录被删除了
                isDownGraded = true; // useless, but for good reading.
                selectedItem = selectedLevel2;
                selectedLevel = 1;
                callbackSelected(true);
                storage[1] = selectedItem.id();
                storage[2] = -1;
            } else {
                // 说明本来就没来选三级菜单
            }
            mAdapter3.setNewData(null);
            return isDownGraded;
        }
        if (storage[2] < 0) { // 表示本来就没选择三级菜单
            mAdapter3.setNewData(null);
            return false;
        }
        //noinspection UnnecessaryLocalVariable
        T third = selectedLevel2; // 三级菜单的父节点
        // 其实到了这里用selectedItem/selectedLevel和storage[2]已经没有区别
        third.setSelectedChild(storage[2]);
        //noinspection unchecked
        T selectedLevel3 = (T) third.getSelectedChild();// 尝试获取上次选择的三级菜单节点
        //noinspection StatementWithEmptyBody
        if (selectedLevel3 != null) {
            // 之前选中的三级目录没被删, 且找到了.(有可能新增了菜单项, 且index也变了)
        } else {
            // 之前选择的三级菜单被删除了
            isDownGraded = true; // useless, but for good reading.
            selectedItem = selectedLevel2;
            selectedLevel = 1;
            callbackSelected(true);
            storage[1] = selectedItem.id();
            storage[2] = -1;
            mAdapter3.setNewData(null);
            return true;
        }
        mAdapter3.setNewData(third);
        return false;
    }


    private void callbackSelected(boolean isDownGraded) {
        if (isDownGraded) {
            updateSelection();
            if (mListener != null) {
                //noinspection unchecked
                mListener.onDownGraded(selectedLevel, selectedItem);
            }
            return;
        }
        if (mListener != null) {
            //noinspection unchecked
            mListener.onSelect(selectedLevel, selectedItem);
        }
    }

    /**
     * 尝试刷新selectedLevel和selectedItem
     * (其实可以把这部分工作放在updateData中, 但逻辑过于臃肿)
     */
    private void updateSelection() {
        // 更新数据以确保selectedItem的其他值(除id以外的值变更)变更,比如修改了名字, 数量变化等
        T root = mAdapter1.getTree();
        if (root == null) return;// never occur
        if (storage[0] < 0) return; // never occur
        root.setSelectedChild(storage[0]);
        //noinspection unchecked
        T selectedLevel1 = (T) root.getSelectedChild();// 获取被选择的一级节点
        if (selectedLevel1 == null) {
            // 选择了一级目录, 却没找到节点，可能吗? 不可能，updateData中已采用降级策略
            return;
        }
        // 代码执行到这句注释时, 已取到了正确的选中的一级节点
        if (storage[1] < 0) { // 说明只选了一级节点
            selectedLevel = 0;
            selectedItem = selectedLevel1;
            // 下面三行代码其实可以不用执行, how to say, insurance.
            storage[0] = selectedLevel1.id();
            storage[1] = -1;
            storage[2] = -1;
            return;
        }
        selectedLevel1.setSelectedChild(storage[1]);
        // 代码执行到这句注释时, 说明选择了不止一级菜单(二级、三级 or more)
        //noinspection unchecked
        T selectedLevel2 = (T) selectedLevel1.getSelectedChild();// 获取被选择的二级节点
        if (selectedLevel2 == null) {
            // 选择了二级目录, 却没找到节点，可能吗? 不可能，updateData中已采用降级策略
            return;
        }
        // 代码执行到这句注释时, 已取到了正确的选中的二级节点
        if (storage[2] < 0) { // 说明只选到了二级节点
            selectedLevel = 1;
            selectedItem = selectedLevel2;
            // 下面两行代码其实可以不用执行, how to say, insurance.
            storage[1] = selectedLevel2.id();
            storage[2] = -1;
            return;
        }
        selectedLevel2.setSelectedChild(storage[2]);
        // 代码执行到这句注释时, 说明选择了不止二级菜单(三级 or more)
        //noinspection unchecked
        T selectedLevel3 = (T) selectedLevel2.getSelectedChild();// 获取被选择的三级节点
        if (selectedLevel3 == null) {
            // 选择了三级目录, 却没找到节点，可能吗? 不可能，updateData中已采用降级策略
            return;
        }
        // 代码执行到这句注释时, 已取到了正确的选中的三级节点
        selectedLevel = 2;
        selectedItem = selectedLevel3;
        // 下面这行代码其实可以不用执行, how to say, insurance.
        storage[2] = selectedLevel3.id();
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


    private OnSelectListener mListener;

    public void setOnSelectListener(OnSelectListener l) {
        setOnSelectListener(0L, l);
    }

    private long mDefaultRootId = -1;

    public void setOnSelectListener(long defaultRootId, OnSelectListener l) {
        mListener = l;
        mDefaultRootId = defaultRootId;
    }

    public void removeSelectListener() {
        if (mListener != null) {
            mListener = null;
        }
    }

    public interface OnSelectListener<T> {
        /**
         * @param selectLevel 被选择的菜单节点所处层级
         * @param data        数据
         */
        void onSelect(int selectLevel, T data);

        /**
         * 当执行了降级策略时
         *
         * @param selectLevel -1 表示降级到了未选择状态
         */
        void onDownGraded(int selectLevel, T data);

        void onShow();

        void onDismiss();
    }
}
