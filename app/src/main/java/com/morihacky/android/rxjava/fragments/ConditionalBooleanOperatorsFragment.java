package com.morihacky.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.morihacky.android.rxjava.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ConditionalBooleanOperatorsFragment
        extends BaseFragment {

    @InjectView(R.id.progress_operation_running)
    ProgressBar _progress;
    @InjectView(R.id.list_threading_log)
    ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;
    private Subscription _subscription;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_subscription != null) {
            _subscription.unsubscribe();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_conditional_and_boolean_operators, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_amb_operation)
    public void ambOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("AMB Clicked");
        Integer[] items = {11, 22, 11, 33, 4, 444, 5555, 6666};

        Observable observable1 = Observable.just(1, 2, 3);
        Observable observable2 = Observable.just(5, 6, 7, 8, 9);
        Observable observable3 = Observable.from(items);
        observable3.ambWith(observable2).ambWith(observable1).subscribe(_getIntegerObserver());
    }

    @OnClick(R.id.btn_contains_operation)
    public void containsOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Contains Clicked");
        Integer[] items = {11, 22, 11, 33, 4, 444, 5555, 6666};

        Observable.from(items).contains(11).subscribe(_getBooleanObserver());
        Observable.from(items).exists(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer == 100;
            }
        }).subscribe(_getBooleanObserver());

        Observable.just(null).isEmpty().subscribe(_getBooleanObserver());

    }

    @OnClick(R.id.btn_take_util_operation)
    public void takeUtilOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Take Util Clicked");
        Integer[] items = {11, 22, 11, 33, 4, 444, 5555, 6666};

        Observable observable1 = Observable.just(4);
        Observable observable2 = Observable.from(items);
//        Observable observable2 = Observable.just(5, 6, 7, 8, 9);

        observable2.takeUntil(observable1).subscribe(_getIntegerObserver());
    }

    private Observable<Boolean> _getObservable() {
        return Observable.just(true).map(new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean aBoolean) {
                _log("Within Observable");
                _doSomeLongOperation_thatBlocksCurrentThread();
                return aBoolean;
            }
        });
    }

    /**
     * Observer that handles the result through the 3 important actions:
     * <p/>
     * 1. onCompleted
     * 2. onError
     * 3. onNext
     */
    private Observer<Integer> _getIntegerObserver() {
        return new Observer<Integer>() {

            @Override
            public void onCompleted() {
                _log("On complete");
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error in RxJava Demo concurrency");
                _log(String.format("Boo! Error %s", e.getMessage()));
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNext(Integer item) {
                _log(String.format("onNext with return value \"%d\"", item));
            }
        };
    }

    private Observer<Boolean> _getBooleanObserver() {
        return new Observer<Boolean>() {

            @Override
            public void onCompleted() {
                _log("On complete");
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error in RxJava Demo concurrency");
                _log(String.format("Boo! Error %s", e.getMessage()));
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNext(Boolean item) {
                _log(String.format("onNext with return value \"%b\"", item));
            }
        };
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private void _doSomeLongOperation_thatBlocksCurrentThread() {
        _log("performing long operation");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Timber.d("Operation was interrupted");
        }
    }

    private void _log(String logMsg) {

        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ");
            _adapter.clear();
            _adapter.addAll(_logs);
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ");

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    _adapter.clear();
                    _adapter.addAll(_logs);
                }
            });
        }
    }

    private void _setupLogger() {
        _logs = new ArrayList<String>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logsList.setAdapter(_adapter);
    }

    private boolean _isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private class LogAdapter
            extends ArrayAdapter<String> {

        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}