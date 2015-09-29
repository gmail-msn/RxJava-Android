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
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class CustomDemoFragment
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
        View layout = inflater.inflate(R.layout.fragment_custom, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_create_operation)
    public void createOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Button Clicked");

        _subscription = AppObservable.bindSupportFragment(this, _getObservable())      // Observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getObserver());                             // Observer
    }

    @OnClick(R.id.btn_from_operation)
    public void fromOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("From Button Clicked");

        _getFromObservable();
    }

    @OnClick(R.id.btn_just_operation)
    public void justOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Just Button Clicked");

        _subscription = AppObservable.bindSupportFragment(this, _getJustObservable())      // Observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getJustObserver());                             // Observer
    }

    @OnClick(R.id.btn_timer_operation)
    public void timerOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Timer Button Clicked");

        _subscription = AppObservable.bindSupportFragment(this, Observable.timer(2, 2, TimeUnit.SECONDS))      // Observable, timer(2, 2, TimeUnit.SECONDS) same as interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getTimerObserver());                             // Observer
    }

    @OnClick(R.id.btn_range_operation)
    public void rangeOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Range Button Clicked");

        _subscription = AppObservable.bindSupportFragment(this, Observable.range(101, 20))      // Observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getObserver());                             // Observer
    }

    @OnClick(R.id.btn_repeat_operation)
    public void repeatOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Repeat Button Clicked");

        _subscription = AppObservable.bindSupportFragment(this, Observable.range(3, 3).repeat(2))      // Observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getObserver());                             // Observer
    }

    @OnClick(R.id.btn_repeat_when_operation)
    public void repeatWhenOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Repeat Button Clicked");

        _subscription = AppObservable.bindSupportFragment(this, Observable.just(1, 2, 3).repeatWhen(
                new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        //重复3次
                        return observable.zipWith(Observable.range(1, 3), new Func2<Void, Integer, Integer>() {
                            @Override
                            public Integer call(Void aVoid, Integer integer) {
                                return integer;
                            }
                        }).flatMap(new Func1<Integer, Observable<?>>() {
                            @Override
                            public Observable<?> call(Integer integer) {
                                System.out.println("delay repeat the " + integer + " count");
                                _log(String.format("call with return value \"%d\"", integer));
                                //1秒钟重复一次
                                return Observable.timer(1, TimeUnit.SECONDS);
                            }
                        });
                    }
                }))      // Observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getObserver());                             // Observer
    }

    private Observable<Integer> _getObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> observer) {
                try {
                    if (!observer.isUnsubscribed()) {
                        for (int i = 1; i < 5; i++) {
                            observer.onNext(i);
                        }
                        observer.onCompleted();
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        });
    }

    private Observable<Integer> _getJustObservable() {
        return Observable.just(1, 2, 3);
    }

    private void _getFromObservable() {
        Integer[] items = {0, 1, 2, 3, 4, 5};
        Observable myObservable = Observable.from(items);
        myObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Integer>() {
                            @Override
                            public void call(Integer item) {
                                _log(String.format("call with return value \"%d\"", item));
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable error) {
                                System.out.println("Error encountered: " + error.getMessage());
                                _log(String.format("Boo! Error %s", error.getMessage()));
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                System.out.println("Sequence complete");
                                _log("Sequence complete");
                            }
                        }
                );
    }

    /**
     * Observer that handles the result through the 3 important actions:
     * <p/>
     * 1. onCompleted
     * 2. onError
     * 3. onNext
     */
    private Subscriber<Integer> _getObserver() {
        return new Subscriber<Integer>() {

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

    private Subscriber<Integer> _getJustObserver() {
        return new Subscriber<Integer>() {

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

    private Subscriber<Long> _getTimerObserver() {
        return new Subscriber<Long>() {

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
            public void onNext(Long item) {
                _log(String.format("onNext with return value \"%d\"", item));
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