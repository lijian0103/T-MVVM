package com.code.mvvm.core.view.material;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.code.mvvm.base.BaseListFragment;
import com.code.mvvm.core.data.pojo.material.MateriaVo;
import com.code.mvvm.core.viewmodel.MaterialViewModel;
import com.code.mvvm.util.AdapterPool;
import com.trecyclerview.multitype.MultiTypeAdapter;

/**
 * @author：tqzhang  on 18/7/2 14:40
 */
public class MaterialListFragment extends BaseListFragment<MaterialViewModel> {
    private String subId;

    public static MaterialListFragment newInstance() {
        return new MaterialListFragment();
    }

    @Override
    public void initView(Bundle state) {
        super.initView(state);
        if (getArguments() != null) {
            subId = getArguments().getString("sub_id");
        }
    }

    @Override
    protected void dataObserver() {
        mViewModel.getMaterialList().observe(this, new Observer<MateriaVo>() {
            @Override
            public void onChanged(@Nullable MateriaVo materialListVo) {
                if (materialListVo != null) {
                    lastId = materialListVo.data.content.get(materialListVo.data.content.size() - 1).tid;
                    setData(materialListVo.data.content);
                }
            }
        });
        mViewModel.getMaterialMoreList().observe(this, new Observer<MateriaVo>() {
            @Override
            public void onChanged(@Nullable MateriaVo materialListVo) {
                if (materialListVo != null && materialListVo.data != null && materialListVo.data.content.size() > 0) {
                    lastId = materialListVo.data.content.get(materialListVo.data.content.size() - 1).tid;
                    setData(materialListVo.data.content);
                }
            }
        });
    }

    @Override
    protected MaterialViewModel createViewModelProviders() {
        return ViewModelProviders.of(this).get(MaterialViewModel.class);
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    protected MultiTypeAdapter createAdapter() {
        return AdapterPool.newInstance().getMaterialListAdapter(getActivity());
    }

    @Override
    protected void onStateRefresh() {
        super.onStateRefresh();
        getNewData();
    }

    @Override
    protected void lazyLoad() {
        super.lazyLoad();
        getNewData();
    }


    @Override
    protected void onRefreshAction() {
        super.onRefreshAction();
        getNewData();
    }

    @Override
    protected void getRemoteData() {
        mViewModel.getMaterialMoreList("0", subId, lastId, "20");
    }

    private void getNewData() {
        mViewModel.getMaterialList("0", subId, "20");

    }
}