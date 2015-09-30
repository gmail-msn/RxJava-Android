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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class CombiningObservablesFragment
        extends BaseFragment {

    @InjectView(R.id.progress_operation_running)
    ProgressBar _progress;
    @InjectView(R.id.list_threading_log)
    ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;
    private Subscription _subscription;

    //产生0,5,10,15,20数列
    Observable<Long> observable1 = Observable.timer(0, 1000, TimeUnit.MILLISECONDS)
            .map(new Func1<Long, Long>() {
                @Override
                public Long call(Long aLong) {
                    return aLong * 5;
                }
            }).take(5);

    //产生0,10,20,30,40数列
    Observable<Long> observable2 = Observable.timer(500, 1000, TimeUnit.MILLISECONDS)
            .map(new Func1<Long, Long>() {
                @Override
                public Long call(Long aLong) {
                    return aLong * 10;
                }
            }).take(5);

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
        View layout = inflater.inflate(R.layout.fragment_combining_observables, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_combine_last_operation)
    public void combineLastOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Combine Clicked");

        Observable.combineLatest(observable1, observable2, new Func2<Long, Long, Long>() {
            @Override
            public Long call(Long aLong, Long aLong2) {
                return aLong + aLong2;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(_getLongObserver());
    }

    @OnClick(R.id.btn_join_operation)
    public void joinOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Join Clicked");

        observable1.join(observable2, new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                //使Observable延迟600毫秒执行
                return Observable.just(aLong).delay(600, TimeUnit.MILLISECONDS);
            }
        }, new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                //使Observable延迟600毫秒执行
                return Observable.just(aLong).delay(600, TimeUnit.MILLISECONDS);
            }
        }, new Func2<Long, Long, Long>() {
            @Override
            public Long call(Long aLong, Long aLong2) {
                return aLong + aLong2;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(_getLongObserver());
    }

    @OnClick(R.id.btn_merge_operation)
    public void mergeOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Merge Clicked");

        Observable.merge(observable1, observable2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getLongSubscriber());
    }

    @OnClick(R.id.btn_merge_delay_operation)
    public void mergeDelayOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Merge Delay Clicked");

        //产生0,5,10数列,最后会产生一个错误
        Observable<Long> errorObservable = Observable.error(new Exception("this is end!"));
        observable1.take(3).mergeWith(errorObservable.delay(3500, TimeUnit.MILLISECONDS));
        Observable.mergeDelayError(observable1, observable2)
                .subscribe(_getLongSubscriber());
    }

    @OnClick(R.id.btn_start_with_operation)
    public void startWithOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Start With Clicked");

        Observable.just(10, 20, 30).startWith(2, 3, 4).subscribe(_getIntegerObserver());
    }

    @OnClick(R.id.btn_switch_on_next_operation)
    public void switchOnNextOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Switch On Next Clicked");

        //每隔500毫秒产生一个observable
        Observable<Observable<Long>> observable = Observable.timer(0, 500, TimeUnit.MILLISECONDS).map(new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                //每隔200毫秒产生一组数据（0,10,20,30,40)
                return Observable.timer(0, 200, TimeUnit.MILLISECONDS).map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        return aLong * 10;
                    }
                }).take(5);
            }
        }).take(2);

        Observable.switchOnNext(observable).subscribe(_getLongSubscriber());
    }

    @OnClick(R.id.btn_zip_operation)
    public void zipOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Zip Clicked");

        Observable<Integer> observable1 = Observable.just(10, 20, 30);
        Observable<Integer> observable2 = Observable.just(4, 8, 12, 16);
        Observable.zip(observable1, observable2, new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) {
                return integer + integer2;
            }
        }).subscribe(_getIntegerSubscriber());
    }

    @OnClick(R.id.btn_group_join_operation)
    public void groupJoinOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Group Join Clicked");

        observable1.groupJoin(observable2, new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                return Observable.just(aLong).delay(1600, TimeUnit.MILLISECONDS);
            }
        }, new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                return Observable.just(aLong).delay(600, TimeUnit.MILLISECONDS);
            }
        }, new Func2<Long, Observable<Long>, Observable<Long>>() {
            @Override
            public Observable<Long> call(final Long aLong, Observable<Long> observable) {
                return observable.map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong2) {
                        return aLong + aLong2;
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Observable<Long>>() {
            @Override
            public void onCompleted() {
                System.out.println("Sequence complete.");
                _log("Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                System.err.println("Error: " + e.getMessage());
                _log("Error: " + e.getMessage());
            }

            @Override
            public void onNext(Observable<Long> observable) {
                observable.subscribe(_getLongSubscriber());
            }
        });
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

    private Subscriber<Long> _getLongSubscriber() {
        return new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                System.out.println("Sequence complete.");
                _log("Merge Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                System.err.println("Error: " + e.getMessage());
                _log("Merge Error: " + e.getMessage());
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("Next:" + aLong);
                _log("Merge Next:" + aLong);
            }
        };
    }

    private Subscriber<Integer> _getIntegerSubscriber() {
        return new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("Sequence complete.");
                _log("Merge Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                System.err.println("Error: " + e.getMessage());
                _log("Merge Error: " + e.getMessage());
            }

            @Override
            public void onNext(Integer aLong) {
                System.out.println("Next:" + aLong);
                _log("Merge Next:" + aLong);
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
            public void onNext(Long item) {
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