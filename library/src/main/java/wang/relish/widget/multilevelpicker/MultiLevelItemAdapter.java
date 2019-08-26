package wang.relish.widget.multilevelpicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @param <T> 实现node接口的数据源
 *
 * @author relish
 * @since 20190802
 */
/* package */ class MultiLevelItemAdapter<T extends Node> extends
        RecyclerView.Adapter<MultiLevelItemAdapter.VHolder> {

    private T tree;


    private int INDEX;

    MultiLevelItemAdapter(T tree, int index) {
        this.tree = tree;
        this.INDEX = index;
    }

    @NonNull
    @Override
    public VHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.mlp_item_chioce, parent, false);
        return new VHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiLevelItemAdapter.VHolder holder, int position) {
        if (tree == null) return;
        List<? extends Node> children = tree.children();
        if (children == null) return;
        //noinspection unchecked
        T item = (T) children.get(position);

        holder.tvName.setText(item.text());

        if (item.id() == tree.selectedChild()) {
            holder.tvName.setTextColor(ContextCompat.getColor(holder.tvName.getContext(),
                    R.color.mlp_item_selected_text));
            if (INDEX == 0) {
                holder.tvName.setBackgroundResource(R.color.mlp_bg_level_2);
            } else if (INDEX == 1) {
                holder.tvName.setBackgroundResource(R.color.mlp_bg_level_3);
            } else if (INDEX == 2) {
                holder.tvName.setBackgroundResource(R.color.transparent);
            }
        } else {
            holder.tvName.setTextColor(ContextCompat.getColor(
                    holder.tvName.getContext(),
                    R.color.mlp_item_unselected_text
            ));
            holder.tvName.setBackgroundResource(R.color.transparent);
        }

        holder.tvName.setTag(item);
        holder.tvName.setOnClickListener(view -> {
            //noinspection unchecked
            T selectedChild = (T) view.getTag();

            if (mOnSelectListener != null) {
                notifyDataSetChanged();
                //noinspection unchecked
                mOnSelectListener.onSelect(tree, selectedChild);
            }

        });
    }

    @Override
    public int getItemCount() {
        if (tree == null) return 0;
        List<? extends Node> children = tree.children();
        return children == null ? 0 : children.size();
    }

    void setNewData(T data) {
        tree = data;
        notifyDataSetChanged();
    }

    class VHolder extends RecyclerView.ViewHolder {

        private TextView tvName;

        VHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    private OnItemPickerListener mOnSelectListener;

    void setOnSelectListener(OnItemPickerListener onSelectListener) {
        mOnSelectListener = onSelectListener;
    }

    interface OnItemPickerListener<T extends Node> {
        void onSelect(T parent, T selectedChild);
    }

    T getTree() {
        return tree;
    }
}
