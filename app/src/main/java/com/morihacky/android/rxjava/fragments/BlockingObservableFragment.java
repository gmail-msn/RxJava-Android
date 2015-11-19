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

import com.fernandocejas.frodo.annotation.RxLogObservable;
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
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BlockingObservableFragment
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
        View layout = inflater.inflate(R.layout.fragment_blocking_observable, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_for_each_operation)
    public void forEachOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("For Each Clicked");

//        _subscription = AppObservable.bindSupportFragment(this, _getObservable())      // Observable
        _getIntegerObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .forEach(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        _log("Integer:" + integer);
                    }
                });
    }

    @OnClick(R.id.btn_first_operation)
    public void firstOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("First Clicked");

//        _subscription = AppObservable.bindSupportFragment(this, _getObservable())      // Observable
        _getIntegerObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .first(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        if (integer == 3) {
                            return true;
                        }
                        return false;
                    }
                }).subscribe(_getIntegerObserver());
    }

    @OnClick(R.id.btn_last_operation)
    public void lastOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Last Clicked");

//        _subscription = AppObservable.bindSupportFragment(this, _getObservable())      // Observable
        _getIntegerObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .last(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        if (integer == 3) {
                            return true;
                        }
                        return false;
                    }
                }).subscribe(_getIntegerObserver());
    }

    @OnClick(R.id.btn_blocking_single_operation)
    public void blockingObservableOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Single Blocking Clicked, waiting to improve");

        Integer[] items = new Integer[]{1, 2, 3, 4, 5};
        Integer[] items1 = new Integer[]{1};
        Observable observable1 = Observable.from(items1);
        observable1.singleOrDefault(_getIntegerObserver());
    }

    @RxLogObservable
    private Observable<Integer> _getIntegerObservable() {
        return Observable.just(1, 2, 3, 4).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                _log("Within Observable");
                _doSomeLongOperation_thatBlocksCurrentThread();
                return integer;
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
    private Observer<Boolean> _getObserver() {
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
            public void onNext(Boolean bool) {
                _log(String.format("onNext with return value \"%b\"", bool));
            }
        };
    }

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
            public void onNext(Integer bool) {
                _log(String.format("onNext with return value \"%d\"", bool));
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