package com.zubin.personalspace;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CalendarFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        FloatingActionButton fab = (FloatingActionButton) container.getRootView().findViewById(R.id.fab);
        fab.show();
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        CalendarView calendar = (CalendarView) view.findViewById(R.id.calendar);
        CollapsingToolbarLayout bar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(calendar.getDate());
        bar.setTitle(month_name);
        bar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        bar.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white));
        return view;
    }

}