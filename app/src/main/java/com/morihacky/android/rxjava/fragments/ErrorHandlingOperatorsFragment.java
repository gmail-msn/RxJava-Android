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
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ErrorHandlingOperatorsFragment
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
        View layout = inflater.inflate(R.layout.fragment_error_handling_operators, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_on_error_return_operation)
    public void onErrorReturnOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Button Clicked");

        Observable<Integer> observable = Observable.create(_getIntegerObservable());      // Observable
        observable.onErrorReturn(new Func1<Throwable, Integer>() {
            @Override
            public Integer call(Throwable throwable) {
                return 1004;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(_getIntegerSubscriber());
    }

    @OnClick(R.id.btn_on_error_resume_next_operation)
    public void onErrorResumeNextOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Error Resume Clicked");

        Observable<Integer> observable = Observable.create(_getIntegerObservable());      // Observable
        observable.onErrorResumeNext(new Func1<Throwable, Observable<? extends Integer>>() {
            @Override
            public Observable<? extends Integer> call(Throwable throwable) {
                return Observable.just(100, 101, 102);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(_getIntegerSubscriber());
    }

    @OnClick(R.id.btn_exception_resume_operation)
    public void onExceptionResumeNextOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Exception Resume Clicked");

        Observable<Integer> observable = Observable.create(_getIntegerObservable());      // Observable
        observable.onExceptionResumeNext(Observable.just(100, 101, 102))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(_getIntegerSubscriber());
    }

    @OnClick(R.id.btn_retry_operation)
    public void retryOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Retry Clicked");

        Observable<Integer> observable = Observable.create(_getIntegerObservable());      // Observable
        observable.retry(2).subscribe(_getIntegerSubscriber());
    }

    @OnClick(R.id.btn_retry_when_operation)
    public void retryWhenOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Retry When Clicked");

        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("subscribing");
                _log("subscribing");
                subscriber.onError(new RuntimeException("always fails"));
            }
        });

        observable.retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> observable) {
                return observable.zipWith(Observable.range(1, 3), new Func2<Throwable, Integer, Integer>() {
                    @Override
                    public Integer call(Throwable throwable, Integer integer) {
                        return integer;
                    }
                }).flatMap(new Func1<Integer, Observable<?>>() {
                    @Override
                    public Observable<?> call(Integer integer) {
                        System.out.println("delay retry by " + integer + " second(s)");
                        _log("delay retry by " + integer + " second(s)");
                        //每一秒执行一次
                        return Observable.timer(integer, TimeUnit.SECONDS);
                    }
                });
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(_getIntegerSubscriber());
    }

    @RxLogObservable
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
                Timber.e(e, "Error in RxJava Demo Handler");
                _log(String.format("Boo! Error %s", e.getMessage()));
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNext(Boolean bool) {
                _log(String.format("onNext with return value \"%b\"", bool));
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