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
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.StringObservable;
import timber.log.Timber;

public class SideEffectFragment
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
        View layout = inflater.inflate(R.layout.fragment_side_effect_observables, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_do_on_error_operation)
    public void doOnErrorOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Do On Error Clicked");
        Observable<Integer> observable = Observable.create(_getIntegerObservable());      // Observable
        observable.doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                _log("show error:" + throwable.getMessage());
            }
        }).subscribe(_getIntegerSubscriber());
    }

    @OnClick(R.id.btn_do_on_complete_operation)
    public void doOnComplete() {

        _progress.setVisibility(View.VISIBLE);
        _log("Do On Complete Clicked");
        Observable.from(Arrays.asList(new Integer[]{2, 3, 4, 5, 7, 8, 11})).doOnNext(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                _log("items:" + integer);
            }
        }).filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return (integer % 2 == 0);
            }
        }).doOnNext(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                _log("filter items:" + integer);
            }
        }).count().doOnNext(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                _log("count items:" + integer);
            }
        }).map(new Func1<Integer, String>() {

            @Override
            public String call(Integer integer) {
                return String.format("Contains %d elements", integer);
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                _progress.setVisibility(View.INVISIBLE);
                _log("do on complete");
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String str) {
                _log("last:" + str);
            }
        });
    }

    private Observable.OnSubscribe<Integer> _getIntegerObservable() {
        return new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (subscriber.isUnsubscribed()) return;
                //循环输出数字
                try {
                    for (int i = 0; i < 10; i++) {
                        if (i == 4) {
                            throw new Exception("this is number 4 error！");
                        }
                        subscriber.onNext(i);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        };
    }

    private Subscriber<Integer> _getIntegerSubscriber() {
        return new Subscriber<Integer>() {

            @Override
            public void onCompleted() {
                _log("On complete");
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error in RxJava Demo handler");
                _log(String.format("Boo! Error %s", e.getMessage()));
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNext(Integer integer) {
                _log(String.format("onNext with return value \"%s\"", integer));
            }
        };
    }
    private Subscriber<String> _getStringSubscriber() {
        return new Subscriber<String>() {

            @Override
            public void onCompleted() {
                _log("On complete");
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error in RxJava Demo handler");
                _log(String.format("Boo! Error %s", e.getMessage()));
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNext(String string) {
                _log(String.format("onNext with return value \"%s\"", string));
            }
        };
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

    private Observer<String> _getStringObserver() {
        return new Observer<String>() {

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
            public void onNext(String str) {
                _log(String.format("onNext with return value \"%s\"", str));
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