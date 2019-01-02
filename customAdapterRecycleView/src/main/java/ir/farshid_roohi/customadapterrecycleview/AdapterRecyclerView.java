package ir.farshid_roohi.customadapterrecycleview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ir.farshid_roohi.customadapterrecycleview.listener.OnLoadMoreListener;
import ir.farshid_roohi.customadapterrecycleview.viewHolder.ViewHolder;
import ir.farshid_roohi.customadapterrecycleview.viewHolder.ViewHolderProgress;

/**
 * Created by Farshid Roohi.
 * CustomAdapterRecyclerView | Copyrights 1/1/19.
 */
public abstract class AdapterRecyclerView<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int ITEM_VIEW     = 0;
    private static int ITEM_PROGRESS = 0;

    private List<T>            list;
    private Context            context;
    private OnLoadMoreListener onLoadMoreListener;

    private boolean isLoading;
    private int     totalItemCount, visibleTotalCount, lastVisibleItem;


    @LayoutRes
    public abstract int getItemLayout(int viewType);

    public abstract void onBindView(ViewDataBinding binding, int position, int viewType, T element);

    public AdapterRecyclerView() {
    }

    public AdapterRecyclerView(RecyclerView recyclerView) {
        this.initRecyclerViewListener(recyclerView);
    }

    public AdapterRecyclerView(List<T> items) {
        this.list = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        this.context = viewGroup.getContext();

        if (viewType == ITEM_PROGRESS) {
            ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                    getItemLayout(viewType), viewGroup, false);

            return new ViewHolder(binding);
        }

        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                R.layout.proggress_view, viewGroup, false);

        return new ViewHolderProgress(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        T element = null;

        if (list != null && !list.isEmpty()) {
            element = list.get(position);
        }
        if (viewHolder instanceof ViewHolder) {
            onBindView(((ViewHolder) viewHolder).binding, position, viewHolder.getItemViewType(), element);
        }
    }

    @Override
    public int getItemCount() {
        return this.list == null ? 0 : this.list.size();
    }

    public void remove(int position) {
        if (this.list == null || this.list.isEmpty()) {
            return;
        }
        this.list.remove(position);
        this.notifyItemRemoved(position);
    }

    public void remove(T... item) {
        if (this.list == null || this.list.isEmpty()) {
            return;
        }
        this.list.removeAll(new ArrayList<>(Arrays.asList(item)));
        this.notifyDataSetChanged();
    }

    public void removeAll() {
        if (this.list == null || this.list.isEmpty()) {
            return;
        }
        this.list.clear();
        this.notifyDataSetChanged();
    }

    public void addItem(T... item) {
        this.hiddenLoading();
        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        List<T> items = new ArrayList<>(Arrays.asList(item));
        this.list.addAll(items);
        notifyItemRangeChanged(getItemCount() - items.size(), getItemCount());
    }

    public void addItems(List<T> items) {
        this.hiddenLoading();
        this.list = items;
        this.notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void endLessScrolled(RecyclerView recyclerView) {
        initRecyclerViewListener(recyclerView);
    }

    public Context getContext() {
        return context;
    }

    public List<T> getItems() {
        return this.list;
    }


    private void initRecyclerViewListener(RecyclerView recyclerView) {

        if (this.onLoadMoreListener == null) return;

        if (recyclerView == null) return;

        if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) return;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                totalItemCount = linearLayoutManager.getItemCount();
                visibleTotalCount = linearLayoutManager.getChildCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (totalItemCount <= visibleTotalCount) {
                    return;
                }

                if (!isLoading && (lastVisibleItem + visibleTotalCount) >= totalItemCount) {
                    onLoadMoreListener.onLoadMore();
                    isLoading = true;
                }
            }
        });
    }

    public void showLoading() {
        if (this.list == null) {
            return;
        }
        this.list.add(null);
        this.notifyItemInserted(getItemCount() - 1);
        isLoading = true;

    }

    public void hiddenLoading() {
        if (this.list != null && getItemCount() != 0 && isLoading) {
            this.list.remove(getItemCount() - 1);
            this.notifyItemRemoved(getItemCount());
        }
        isLoading = false;
    }

}