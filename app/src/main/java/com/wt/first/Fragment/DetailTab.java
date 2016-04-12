package com.wt.first.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wt.first.Atys.MainActivity;
import com.wt.first.Bean.TakeitUser;
import com.wt.first.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jing107 on 2016/3/22 0022.
 */
public class DetailTab extends Fragment implements View.OnClickListener {

    private MainActivity atyMain;

    private View root;

    private ViewPager viewPager;
    private FragmentPagerAdapter fpAdapter;

    private List<Fragment> pagers;

    private LayoutInflater inflater;

    //我的
    private TextView tvMy;
    //@我
    private TextView tvAtme;

    private ImageView ivTabline;

    private TakeitUser userMe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;

        root = inflater.inflate(com.wt.first.R.layout.activity_detail_tab,container,false);
        tvAtme = (TextView) root.findViewById(R.id.tv_atme);
        tvAtme.setOnClickListener(this);
        tvMy = (TextView) root.findViewById(R.id.tv_my);
        tvMy.setOnClickListener(this);

        ivTabline = (ImageView) root.findViewById(R.id.iv_tabline);

        initViews();

        return root;
    }

    private void initViews() {
        atyMain = (MainActivity) getActivity();

        viewPager = (ViewPager) root.findViewById(R.id.vp_detail);

        pagers = new ArrayList<Fragment>();

        MyDetailTab myDetailTab = new MyDetailTab();
        myDetailTab.setUserMe(userMe);
        AtmeDetailTab atmeDetailTab = new AtmeDetailTab();
        atmeDetailTab.setUserMe(userMe);

        pagers.add(myDetailTab);
        pagers.add(atmeDetailTab);


        fpAdapter = new FragmentPagerAdapter(atyMain.getSupportFragmentManager()) {
            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                return pagers.get(position);
            }

            @Override
            public int getCount() {
                return pagers.size();
            }
        };
        viewPager.setAdapter(fpAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //控制滑块的位置
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ivTabline.getLayoutParams();
                lp.leftMargin = (int) (position*lp.width + positionOffset*lp.width);
                ivTabline.setLayoutParams(lp);
            }

            @Override
            public void onPageSelected(int position) {

                resetTextColor();

                switch (position) {
                    case 0:
                        tvMy.setTextColor(Color.parseColor("#FF0C99FE"));
                        break;
                    case 1:
                        tvAtme.setTextColor(Color.parseColor("#FF0C99FE"));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void resetTextColor() {
        tvMy.setTextColor(Color.DKGRAY);
        tvAtme.setTextColor(Color.DKGRAY);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_my:
                viewPager.setCurrentItem(0,true);
                break;

            case R.id.tv_atme:
                viewPager.setCurrentItem(1,true);
                break;

            default:
                break;
        }
    }

    public void setUserMe(TakeitUser user) {
        this.userMe = user;
    }
}
