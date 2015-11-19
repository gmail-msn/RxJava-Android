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
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class FilteringObservablesFragment
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
        View layout = inflater.inflate(R.layout.fragment_filtering_observables, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_debounce_operation)
    public void debounceOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Debounce Clicked");

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (subscriber.isUnsubscribed()) return;
                try {
                    //产生结果的间隔时间分别为100、200、300...900毫秒
                    for (int i = 1; i < 10; i++) {
                        subscriber.onNext(i);
                        Thread.sleep(i * 100);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .debounce(400, TimeUnit.MILLISECONDS)  //超时时间为400毫秒
                .subscribe(
                        new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                System.out.println("Next:" + integer);
                                _log("Next:" + integer);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                System.out.println("Error:" + throwable.getMessage());
                                _log("Error:" + throwable.getMessage());
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                System.out.println("completed!");
                                _log("completed!");
                            }
                        });
    }

    @OnClick(R.id.btn_distinct_operation)
    public void distinctOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Distinct Clicked");

        //Distinct
        Observable.just(1, 2, 1, 1, 2, 3).subscribeOn(Schedulers.newThread())
                .distinct()
                .subscribe(_getObserver());

        //element At
        Observable.just(1, 2, 3, 4, 5, 6).elementAt(2)
                .subscribe(
                        new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                System.out.println("Next:" + integer);
                                _log("element At Next:" + integer);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                System.out.println("Error:" + throwable.getMessage());
                                _log("element At Error:" + throwable.getMessage());
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                System.out.println("completed!");
                                _log("element At completed!");
                            }
                        });
        //filter
        Observable.just(1, 2, 3, 4, 5)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer item) {
                        return (item < 4);
                    }
                }).subscribe(_getObserver());

        //ofType
        Observable.just(1, "hello world", true, 200L, 0.23f)
                .ofType(Float.class)
                .subscribe(_getObjectObserver());
        //single
        Observable.just(1, 2, 3, 4, 5, 6, 7, 8)
                .single(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        //取大于10的第一个数字
                        return integer > 10;
                    }
                }).subscribe(_getObserver());
        //last
        Observable.just(1, 2, 3)
                .last().subscribe(_getObserver());

        //ignore elements
        Observable.just(1, 2, 3, 4, 5, 6, 7, 8).ignoreElements().subscribe(_getObserver());

        //Sample
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (subscriber.isUnsubscribed()) return;
                try {
                    //前8个数字产生的时间间隔为1秒，后一个间隔为3秒
                    for (int i = 1; i < 9; i++) {
                        subscriber.onNext(i);
                        Thread.sleep(1000);
                    }
                    Thread.sleep(2000);
                    subscriber.onNext(9);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .sample(2200, TimeUnit.MILLISECONDS)  //采样间隔时间为2200毫秒
                .subscribe(_getObserver());

        //Skip
        Observable.just(1, 2, 3, 4, 5, 6, 7).skip(3).subscribe(_getObserver());
        //skip last
        Observable.just(1, 2, 3, 4, 5, 6, 7).skipLast(3).subscribe(_getObserver());
        //Take
        Observable.just(1, 2, 3, 4, 5, 6, 7, 8).take(4).subscribe(_getObserver());
        //Take First
        Observable.just(1, 2, 3, 4, 5, 6, 7).takeFirst(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                //获取数值大于3的数据
                return integer > 3;
            }
        }).subscribe(_getObserver());
        //Take Last
        Observable.just(1, 2, 3, 4, 5, 6, 7).takeLast(2).subscribe(_getObserver());
    }

    @RxLogObservable
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
    private Observer<Integer> _getObserver() {
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

    private Observer<Object> _getObjectObserver() {
        return new Observer<Object>() {

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
            public void onNext(Object item) {
                _log(String.format("onNext with return value \"%s\"", item));
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