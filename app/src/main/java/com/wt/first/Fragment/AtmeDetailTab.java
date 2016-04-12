package com.wt.first.Fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wt.first.Adapter.Bill;
import com.wt.first.Adapter.BillAdapter;
import com.wt.first.Bean.RelationUserAndBill;
import com.wt.first.Bean.TakeitBill;
import com.wt.first.Bean.TakeitUser;
import com.wt.first.CustomView.RefreshableView;
import com.wt.first.R;

import java.util.ArrayList;
import java.util.List;

import Utils.Utils;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by jing107 on 2016/3/31 0031.
 */
public class AtmeDetailTab extends Fragment implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    private static final String TAG = AtmeDetailTab.class.getSimpleName();
    private static final int AtmeDetailTab_ID = 2;

    private TakeitUser userMe;

    private View root;

    private RefreshableView freshView;

    private List<Bill> billDatas;
    private ListView listView;
    private BillAdapter adapter;
    private View footer;
    private ProgressBar progressBar;
    private TextView tvMore;

    private int lastItem;
    private int lastItem_before = lastItem;

    private boolean loading_data;
    private boolean no_more_data;

    private int requestSize = 12;
    private int database_page = 0;

    private int[] typeIds = new int[]{
            R.drawable.play, R.drawable.hamburger ,R.drawable.life, R.drawable.other
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.activity_atmedetail_tab,container,false);
        footer = inflater.inflate(R.layout.load_more_data, null);
        progressBar = (ProgressBar) footer.findViewById(R.id.pb_load_more_data);
        tvMore = (TextView) footer.findViewById(R.id.tv_load_more_data);

        freshView = (RefreshableView) root.findViewById(R.id.fresh_atmedetail);

        initListView();

        freshView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                database_page = 0;
                no_more_data = false;

                BmobQuery<RelationUserAndBill> query = new BmobQuery<RelationUserAndBill>();
                query.setLimit(requestSize);
                query.addWhereEqualTo("userId",userMe.getObjectId());
                query.order("-createdAt");
                query.findObjects(getContext(), new FindListener<RelationUserAndBill>() {
                    @Override
                    public void onSuccess(List<RelationUserAndBill> list) {
                        Log.e(TAG,"查询账单ID成功");
                        List<String> billId = new ArrayList<String>();
                        for (RelationUserAndBill rub : list) {
                            billId.add(rub.getBillId());
                        }

                        BmobQuery<TakeitBill> q = new BmobQuery<TakeitBill>();
                        q.addWhereContainedIn("objectId",billId);
                        q.order("createdAt");
                        q.findObjects(getContext(), new FindListener<TakeitBill>() {
                            @Override
                            public void onSuccess(List<TakeitBill> list) {
                                Log.e(TAG,"刷新成功");
                                billDatas.clear();
                                if (list.size() == requestSize) {
                                    tvMore.setText(R.string.loading_more_data);
                                    progressBar.setVisibility(View.VISIBLE);
                                    database_page++;
                                    for (TakeitBill data : list) {
                                        String strDate = data.getCreatedAt().substring(5, 10);
                                        String strTime = data.getCreatedAt().substring(11, 16);
                                        String money = data.getMoney().toString();
                                        int type = data.getType();
                                        Bill bill = new Bill(strDate, strTime, typeIds[type - 1], money);
                                        bill.setContent(data.getContent());

                                        billDatas.add(bill);
                                    }
                                    //发送更新通知
                                    adapter.notifyDataSetChanged();
                                } else {
                                    no_more_data = true;
                                    tvMore.setText(R.string.no_more_data);
                                    progressBar.setVisibility(View.GONE);
                                    lastItem_before = -1; //如果不设置的话，就不能再加载数据了，因为之前的数值等于lastItem的值

                                    if (list.size() > 0) {
                                        for (TakeitBill b : list) {
                                            String strDate = b.getCreatedAt().substring(5, 10);
                                            String strTime = b.getCreatedAt().substring(11, 16);
                                            String money = b.getMoney().toString();
                                            int type = b.getType();
                                            Bill bill = new Bill(strDate, strTime, typeIds[type - 1], money);
                                            bill.setContent(b.getContent());

                                            billDatas.add(bill);
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                freshView.finishRefreshing();
                            }

                            @Override
                            public void onError(int i, String s) {
                                Log.e(TAG, "刷新失败：" + Utils.getErrorString(i,s));
                                Toast.makeText(getContext(), "刷新失败：" + s, Toast.LENGTH_SHORT).show();
                                freshView.finishRefreshing();
                            }
                        });
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.e(TAG, "刷新失败：" + Utils.getErrorString(i,s));
                        Toast.makeText(getContext(), "刷新失败：" + s, Toast.LENGTH_SHORT).show();
                        freshView.finishRefreshing();
                    }
                });
            }
        }, AtmeDetailTab_ID);

        return root;
    }

    private void initListView() {
        listView = (ListView) root.findViewById(R.id.lv_atmedetail);
        listView.addFooterView(footer);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(this);
        billDatas = new ArrayList<Bill>();

        BmobQuery<RelationUserAndBill> query = new BmobQuery<RelationUserAndBill>();
        query.addWhereEqualTo("userId",userMe.getObjectId());
        query.setLimit(requestSize);
        query.order("-createdAt");
        query.findObjects(getContext(), new FindListener<RelationUserAndBill>() {
            @Override
            public void onSuccess(List<RelationUserAndBill> list) {
                Log.e(TAG,"查询账单ID成功");
                List<String> billId = new ArrayList<String>();
                for (RelationUserAndBill rub : list) {
                    billId.add(rub.getBillId());
                }

                BmobQuery<TakeitBill> q = new BmobQuery<TakeitBill>();
                q.addWhereContainedIn("objectId",billId);
                q.order("createdAt");
                q.findObjects(getContext(), new FindListener<TakeitBill>() {
                    @Override
                    public void onSuccess(List<TakeitBill> list) {
                        Log.e(TAG,"子查询中获得订单信息成功...");

                        if (list.size() < requestSize) {
                            no_more_data = true;
                            tvMore.setText(R.string.no_more_data);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            database_page++;
                        }

                        for (TakeitBill data : list) {
                            String strDate = data.getCreatedAt().substring(5, 10);
                            String strTime = data.getCreatedAt().substring(11, 16);
                            String money = data.getMoney().toString();
                            int type = data.getType();
                            Bill bill = new Bill(strDate, strTime, typeIds[type - 1], money);
                            bill.setContent(data.getContent());

                            billDatas.add(bill);
                        }

                        adapter = new BillAdapter(getContext(), R.layout.detail_item, billDatas);
                        listView.setAdapter(adapter);
                    }

                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getContext(), "子查询中获得订单信息失败:" + s, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(getContext(), "查询账单ID失败:" + s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setUserMe(TakeitUser user) {
        this.userMe = user;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        lastItem_before = lastItem;

        if (lastItem == adapter.getCount() && !loading_data
                && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (!no_more_data) {
                loadMoreData();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastItem = firstVisibleItem + visibleItemCount - 1;
        if (lastItem == lastItem_before) {
            loading_data = true;
        } else {
            loading_data = false;
        }
    }

    private void loadMoreData() {
        BmobQuery<RelationUserAndBill> query = new BmobQuery<RelationUserAndBill>();
        query.setLimit(requestSize);
        query.addWhereEqualTo("userId",userMe.getObjectId());
        query.order("-createdAt");
        query.setSkip(database_page * requestSize);
        query.findObjects(getContext(), new FindListener<RelationUserAndBill>() {
            @Override
            public void onSuccess(List<RelationUserAndBill> list) {
                Log.e(TAG,"查询账单ID成功");
                List<String> billId = new ArrayList<String>();
                for (RelationUserAndBill rub : list) {
                    billId.add(rub.getBillId());
                }

                BmobQuery<TakeitBill> q = new BmobQuery<TakeitBill>();
                q.addWhereContainedIn("objectId",billId);
                q.order("createdAt");
                q.findObjects(getContext(), new FindListener<TakeitBill>() {
                    @Override
                    public void onSuccess(List<TakeitBill> list) {
                        Log.e(TAG,"子查询中获得订单信息成功...");
                        if (list.size() == requestSize) {
                            database_page++;
                            tvMore.setText(R.string.loading_more_data);
                            progressBar.setVisibility(View.VISIBLE);
                            for (TakeitBill b : list) {
                                String strDate = b.getCreatedAt().substring(5, 10);
                                String strTime = b.getCreatedAt().substring(11, 16);
                                String money = b.getMoney().toString();
                                int type = b.getType();
                                Bill bill = new Bill(strDate, strTime, typeIds[type - 1], money);
                                bill.setContent(b.getContent());

                                billDatas.add(bill);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            no_more_data = true;
                            tvMore.setText(R.string.no_more_data);
                            progressBar.setVisibility(View.GONE);
                            lastItem_before = -1;

                            if (list.size() > 0) {
                                for (TakeitBill b : list) {
                                    String strDate = b.getCreatedAt().substring(5, 10);
                                    String strTime = b.getCreatedAt().substring(11, 16);
                                    String money = b.getMoney().toString();
                                    int type = b.getType();
                                    Bill bill = new Bill(strDate, strTime, typeIds[type - 1], money);
                                    bill.setContent(b.getContent());

                                    billDatas.add(bill);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onError(int i, String s) {

                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Toast.makeText(getContext(), "点击了该项", Toast.LENGTH_SHORT).show();
    }
}


