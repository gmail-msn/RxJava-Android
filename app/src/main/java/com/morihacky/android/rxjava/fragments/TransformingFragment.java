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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import rx.functions.Action2;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class TransformingFragment
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
        View layout = inflater.inflate(R.layout.fragment_transforming, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_buffer_operation)
    public void bufferOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Buffer Clicked");

        _subscription = AppObservable.bindSupportFragment(this, _getObservable())      // Observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .buffer(3, TimeUnit.SECONDS).subscribe(new Action1<List<String>>() {//把上面产生的邮件内容缓存到列表中，并每隔3秒通知订阅者
                    @Override
                    public void call(List<String> list) {

                        System.out.println(String.format("You've got %d new messages!  Here they are!", list.size()));
                        _log(String.format("You've got %d new messages!  Here they are!", list.size()));
                        for (int i = 0; i < list.size(); i++) {
                            System.out.println("**" + list.get(i).toString());
                            _log(String.format("call with return value \"%s\"", list.get(i).toString()));
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Error in RxJava Demo concurrency");
                        _log(String.format("Boo! Error %s", throwable.getMessage()));
                        _progress.setVisibility(View.INVISIBLE);
                    }
                });                             // Observer
    }

    @OnClick(R.id.btn_flat_map_operation)
    public void flatMapOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Flat Map Clicked");

        Observable.just(getActivity().getApplicationContext().getExternalCacheDir())
                .flatMap(new Func1<File, Observable<File>>() {
                    @Override
                    public Observable<File> call(File file) {
                        //参数file是just操作符产生的结果，这里判断file是不是目录文件，如果是目录文件，则递归查找其子文件flatMap操作符神奇的地方在于，返回的结果还是一个Observable，而这个Observable其实是包含多个文件的Observable的，输出应该是ExternalCacheDir下的所有文件
                        return listFiles(file);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        System.out.println(file.getAbsolutePath());
                        _log(String.format("onNext with return value \"%s\"", file.getAbsolutePath()));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Error in RxJava Demo concurrency");
                        _log(String.format("Boo! Error %s", throwable.getMessage()));
                        _progress.setVisibility(View.INVISIBLE);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        _progress.setVisibility(View.INVISIBLE);
                        _log("Action0 call with return value null");
                    }
                });                             // Observer
    }

    @OnClick(R.id.btn_group_by_operation)
    public void groupByOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Group By Clicked");

        Observable.interval(1, TimeUnit.SECONDS).take(10).groupBy(new Func1<Long, Long>() {
            @Override
            public Long call(Long value) {
                //按照key为0,1,2分为3组
                return value % 3;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<GroupedObservable<Long, Long>>() {
            @Override
            public void call(final GroupedObservable<Long, Long> result) {
                result.subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long value) {
                        System.out.println("key:" + result.getKey() + ", value:" + value);
                        _log("key:" + result.getKey() + ", value:" + value);
                    }
                });
            }
        });
    }

    @OnClick(R.id.btn_map_operation)
    public void mapOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Map Clicked");

        Observable.just(1, 2, 3, 4, 5, 6).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                //对源Observable产生的结果，都统一乘以3处理
                return integer * 3;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("next:" + integer);
                _log("next:" + integer);
            }
        });
    }

    @OnClick(R.id.btn_cast_operation)
    public void castOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Cast Clicked");

        Observable.just(1, 2, 3, 4, 5, 6).cast(Integer.class).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer value) {
                System.out.println("next:" + value);
                _log("next:" + value);
            }
        });

        //Scan operator
        Observable.just(1, 2, 3, 4, 5)
                .scan(new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer sum, Integer item) {
                        //参数sum就是上一次的计算结果
                        return sum + item;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getObserver());

        //Window
        Observable.interval(1, TimeUnit.SECONDS).take(12)
                .window(3, TimeUnit.SECONDS)
                .subscribe(new Action1<Observable<Long>>() {
                    @Override
                    public void call(Observable<Long> observable) {
                        System.out.println("subdivide begin......");
                        _log("subdivide begin......");
                        observable.subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                _progress.setVisibility(View.INVISIBLE);
                                System.out.println("Next:" + aLong);
                                _log("Next:" + aLong);
                            }
                        });
                    }
                });
    }

    @OnClick(R.id.btn_concat_map_operation)
    public void concatMapOperation() {
        _progress.setVisibility(View.VISIBLE);
        _log("Concat Map Clicked");

        //flatMap操作符的运行结果
        Observable.just(10, 20, 30).flatMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
                int delay = 200;
                if (integer > 10) {
                    delay = 180;
                }

                return Observable.from(new Integer[]{integer, integer / 2}).delay(delay, TimeUnit.MILLISECONDS);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("flatMap Next:" + integer);
                _log("flatMap Next:" + integer);
            }
        });

        //concatMap操作符的运行结果
        Observable.just(10, 20, 30).concatMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
                int delay = 200;
                if (integer > 10)
                    delay = 180;

                return Observable.from(new Integer[]{integer, integer / 2}).delay(delay, TimeUnit.MILLISECONDS);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("concatMap Next:" + integer);
                _log("concatMap Next:" + integer);
            }
        });

        //switchMap操作符的运行结果
        Observable.just(10, 20, 30).switchMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
                int delay = 200;
                if (integer > 10)
                    delay = 180;

                return Observable.from(new Integer[]{integer, integer / 2}).delay(delay, TimeUnit.MILLISECONDS);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("switchMap Next:" + integer);
                _log("switchMap Next:" + integer);
            }
        });
    }

    @RxLogObservable
    private Observable<String> _getObservable() {
        //定义邮件内容
        final String[] mails = new String[]{"Here is an email!", "Another email!", "Yet another email!"};
        //每隔1秒就随机发布一封邮件
        Observable<String> endlessMail = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    if (subscriber.isUnsubscribed()) return;
                    Random random = new Random();
                    while (true) {
                        String mail = mails[random.nextInt(mails.length)];
                        subscriber.onNext(mail);
                        Thread.sleep(1000);
                    }
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
        return endlessMail;
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

    @RxLogObservable
    private Observable<File> listFiles(final File f) {
        if (null != f && f.isDirectory()) {
            return Observable.from(f.listFiles()).flatMap(new Func1<File, Observable<File>>() {
                @Override
                public Observable<File> call(File file) {
                    return listFiles(f);
                }
            });
        } else {
            return Observable.just(f);
        }
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