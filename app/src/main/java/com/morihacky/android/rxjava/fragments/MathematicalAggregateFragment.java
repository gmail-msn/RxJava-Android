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
import rx.observables.MathObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MathematicalAggregateFragment
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
        View layout = inflater.inflate(R.layout.fragment_mathematical_aggregate, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_average_integer_operation)
    public void averageIntegerOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Button Clicked");

        Integer[] items = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

        MathObservable.max(Observable.just(1, 2, 3, 4, 6)).subscribe(_getIntegerObserver());
        MathObservable.min(Observable.just(1, 2, 3, 4, 6)).subscribe(_getIntegerObserver());
        MathObservable.sumInteger(Observable.just(1, 2, 3, 4, 6)).subscribe(_getIntegerObserver());
    }

    @OnClick(R.id.btn_concat_operation)
    public void concatOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("concat Clicked");

        Observable odds = Observable.from(new Integer [] {1, 3, 5, 7});
        Observable evens = Observable.from(new Integer [] {2, 4, 6});

        Observable.concat(odds, evens).subscribe(_getIntegerObserver());
    }

    @OnClick(R.id.btn_sorted_list_operation)
    public void sortedListOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("concat Clicked");

        Observable odds = Observable.just(1, 10, 2, 22, 2, 3, 5, 7);
        Observable evens = Observable.just(2, 4, 6);

        Observable.concat(odds, evens).toSortedList().subscribe(_getArrayObserver());
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
            public void onNext(Integer integer) {
                _log(String.format("onNext with return value \"%d\"", integer));
            }
        };
    }

    private Observer<ArrayList> _getArrayObserver() {
        return new Observer<ArrayList>() {

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
            public void onNext(ArrayList list) {
                if (null != list && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        _log(String.format("onNext with return value \"%d\"", list.get(i)));
                    }
                }
            }
        };
    }

    private Observer<Long> _getLongObserver() {
        return new Observer<Long>() {

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
            public void onNext(Long integer) {
                _log(String.format("onNext with return value \"%s\"", integer));
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