package org.qumodo.miscaclient.fragments;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.qumodo.data.MessageCenter;
import org.qumodo.data.models.GroupListItem;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.GroupsContentProvider;

public class QMiscaGroupsListFragment extends Fragment {

    public static final String TAG = "QMiscaGroupsListFragment";

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MessageCenter.RELOAD_UI:
                    GroupsContentProvider.reloadData(getContext());
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public QMiscaGroupsListFragment() {
    }

    @SuppressWarnings("unused")
    public static QMiscaGroupsListFragment newInstance(int columnCount) {
        QMiscaGroupsListFragment fragment = new QMiscaGroupsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        ActionBar ab = getActivity().getActionBar();
        if (ab != null)
            ab.setTitle(getString(R.string.actionbar_title_messages));
    }

    public QMiscaGroupsListRecyclerViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_misca_groups_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            adapter = new QMiscaGroupsListRecyclerViewAdapter(GroupsContentProvider.ITEMS, mListener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessageCenter.RELOAD_UI);
        getContext().registerReceiver(receiver, intentFilter);

        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMessageListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        getContext().unregisterReceiver(receiver);
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(GroupListItem item);
    }
}
