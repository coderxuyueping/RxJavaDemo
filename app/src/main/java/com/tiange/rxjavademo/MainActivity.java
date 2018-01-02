package com.tiange.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by 徐大哈 on 2017/12/29.
 * 测试RxJava异步库
 * RxJava使用传统的观察订阅模式
 * observable被观察者，类似于广播
 * observer观察者，类似与广播接收器
 * 两者通过subscribe订阅关联，相当于注册广播接收器,observable发出的一系列事件会被observer接收到
 */
public class MainActivity extends AppCompatActivity {
    private final String  TAG = "xudaha";
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放连接
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
    }

    private void fun1(){
        //just会依次发送
        // Observable<Integer> observable = Observable.just(1,2,3);
        //from会从容器中依次发送
        //String[] strings = {"a", "b"};
        //Observable<String> observable = Observable.fromArray(strings);
        //创建一个被观察者
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                //ObservableEmitter是一个发射器，向下游观察者发送事件
                e.onNext("a");
                e.onNext("b");
                e.onComplete();
                //e.onError(new Throwable("error"));
                //onComplete和onError是互斥的，发送了某一个，下游就不会再接受事件了
            }
        });

        //观察者只有一个回调事件
        Consumer<String> consumer = new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {

            }
        };
        //创建一个观察者
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                //在接受事件之前调用，Disposable可以用来断开连接，之后不再接受任何事件
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        };

        //建立连接
        observable.subscribe(observer);
    }

    private void fun2(){
        //使用强大的基于事件流链式调用
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("a");
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())//发送事件在io线程，多次调用只有一次有效,上游事件
                .observeOn(AndroidSchedulers.mainThread())//接收事件在主线程,多次调用多次有效，下游事件
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    //Disposable的使用，可以通过这个来切断连接(onDestroy)，避免内存泄露,可以在观察者Observer里获取，也可以通过Consume观察者获取
    private void fun3(){
        //第一种获取
        Observable.just("a")
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        //第二种
        disposable = Observable.just(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {

                    }
                });
    }


    /**
     * 强大的操作符
     */

    //延时delay
    private void fun4(){
        /**
         1. 指定延迟时间
         参数1 = 时间；参数2 = 时间单位
         delay(long delay,TimeUnit unit)

         2. 指定延迟时间 & 调度器
         参数1 = 时间；参数2 = 时间单位；参数3 = 线程调度器
         delay(long delay,TimeUnit unit,mScheduler scheduler)

         3. 指定延迟时间  & 错误延迟
         错误延迟，即：若存在Error事件，则如常执行，执行后再抛出错误异常
         参数1 = 时间；参数2 = 时间单位；参数3 = 错误延迟参数
         delay(long delay,TimeUnit unit,boolean delayError)

         4. 指定延迟时间 & 调度器 & 错误延迟
         参数1 = 时间；参数2 = 时间单位；参数3 = 线程调度器；参数4 = 错误延迟参数
         delay(long delay,TimeUnit unit,mScheduler scheduler,boolean delayError):
         指定延迟多长时间并添加调度器，错误通知可以设置是否延迟
         */

        Observable.just(1,2,3)
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)//延时1s发送事件
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {

                    }
                });
    }

    //do()操作符
    private void fun5(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("a");
                e.onNext("b");
                e.onError(new Throwable("error"));
            }
        })
                // 1. 当Observable每发送1次数据事件就会调用1次
                .doOnEach(new Consumer<Notification<String>>() {
                    @Override
                    public void accept(Notification<String> stringNotification) throws Exception {

                    }
                })
                //在每次调用onNext前调用
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                    }
                })
                //在每次调用onNext后调用
                .doAfterNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                    }
                })
                // 4. Observable正常发送事件完毕后调用
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                })
                // 5. Observable发送错误事件时调用
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                })
                // 6. 观察者订阅时调用
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                })
                // 7. Observable发送事件完毕后调用，无论正常发送完毕 / 异常终止
                .doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                })
                // 8. 最后执行
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    //onErrorReturn遇到错误时，发送1个特殊事件 & 正常终止
    private void fun6(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("a");
                e.onNext("b");
                e.onError(new Throwable("error"));
            }
        })
                .onErrorReturn(new Function<Throwable, String>() {
                    @Override
                    public String apply(Throwable throwable) throws Exception {
                        return "发生了一个错误";
                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //onErrorResumeNext遇到错误时，发送1个新的Observable
    private void fun7(){
        /**
         onErrorResumeNext（）拦截的错误 = Throwable；若需拦截Exception请用onExceptionResumeNext（）
         若onErrorResumeNext（）拦截的错误 = Exception，则会将错误传递给观察者的onError方法
         */
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("a");
                e.onNext("b");
                e.onError(new Throwable("error"));
            }
        })
                .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends String>>() {
                    @Override
                    public ObservableSource<? extends String> apply(Throwable throwable) throws Exception {
                        return Observable.just("666");
                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void fun8(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("a");
                e.onNext("b");
                e.onError(new Throwable("error"));
            }
        })
                .onExceptionResumeNext(new Observable<String>() {
                    @Override
                    protected void subscribeActual(Observer<? super String> observer) {
                        observer.onNext("重新发送的事件");
                        observer.onComplete();
                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //retry 重试，即当出现错误时，让被观察者（Observable）重新发射数据
    private void fun9(){
        /**
         *
         接收到 onError（）时，重新订阅 & 发送事件
         Throwable 和 Exception都可拦截


         <-- 1. retry（） -->
         作用：出现错误时，让被观察者重新发送数据
         注：若一直错误，则一直重新发送

         <-- 2. retry（long time） -->
         作用：出现错误时，让被观察者重新发送数据（具备重试次数限制
         参数 = 重试次数

         <-- 3. retry（Predicate predicate） -->
         作用：出现错误后，判断是否需要重新发送数据（若需要重新发送& 持续遇到错误，则持续重试）
         参数 = 判断逻辑

         <--  4. retry（new BiPredicate<Integer, Throwable>） -->
         作用：出现错误后，判断是否需要重新发送数据（若需要重新发送 & 持续遇到错误，则持续重试
         参数 =  判断逻辑（传入当前重试次数 & 异常错误信息）

         <-- 5. retry（long time,Predicate predicate） -->
         作用：出现错误后，判断是否需要重新发送数据（具备重试次数限制
         参数 = 设置重试次数 & 判断逻辑
         */
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("a");
                e.onNext("b");
                e.onError(new Throwable("error"));
            }
        })
                .retry()
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //repeat无条件地、重复发送 被观察者事件
    private void fun10(){
        Observable.just(1)
                .repeat(2)//重复发送2次
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }



    /**
     * 基础操作符，被观察者的创建
     * 除了create，from，just等
     */
    Integer i = 1;
    //defer直到有观察者（Observer ）订阅时，才动态创建被观察者对象（Observable） & 发送事件
    private void fun11(){

        Observable<Integer> observable = Observable.defer(new Callable<ObservableSource<? extends Integer>>() {
            @Override
            public ObservableSource<? extends Integer> call() throws Exception {
                return Observable.just(i);
            }
        });

        i = 2;
        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d(TAG, ""+integer);//输出2
            }
        };

        observable.subscribe(consumer);
    }

    //timer本质 = 延迟指定时间后，调用一次 onNext(0) 一般用于检测

    private void fun12(){
        Observable.timer(2,TimeUnit.SECONDS)//2s后调用onNext(0)
        .subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {

            }
        });
    }

    //interval发送的事件序列 = 从0开始、无限递增1的的整数序列
    private void fun13(){
        // 该例子发送的事件序列特点：延迟3s后发送事件，每隔1秒产生1个数字（从0开始递增1，无限个）
        Observable.interval(3, 1 ,TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {

                    }
                });
    }

    //intervalRange
    private void fun14(){
        // 该例子发送的事件序列特点：
        // 1. 从事件3开始，一共发送10个事件；
        // 2. 第1次延迟2s发送，之后每隔1秒产生1个数字（从0开始递增1，无限个）
        Observable.intervalRange(3, 10, 2, 1 ,TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {

                    }
                });
    }

    //range
    private void fun15(){
        // 该例子发送的事件序列特点：从事件3开始发送，每次发送事件递增1，一共发送10个事件
        Observable.range(3, 10)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {

                    }
                });
    }


    /**
     * 变换操作符
     */

    //Map数据类型转换
    private void fun16(){
        //将Integer转为String
        Observable.just(1,2,3)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        return "转为字符串" + integer;
                    }
                })
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                });
    }

    //FlatMap将被观察者发送的事件序列进行 拆分 & 单独转换，再合并成一个新的事件序列，最后再进行发送
    private void fun17(){
        /**
         *
         1.为事件序列中每个事件都创建一个 Observable 对象；
         2.将对每个 原始事件 转换后的 新事件 都放入到对应 Observable对象；
         3.将新建的每个Observable 都合并到一个 新建的、总的Observable 对象；
         4.新建的、总的Observable 对象 将 新合并的事件序列 发送给观察者（Observer）
         */
        Observable.just(1,2)
                .flatMap(new Function<Integer, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Integer integer) throws Exception {
                        // 通过flatMap中将被观察者生产的事件序列先进行拆分，
                        // 再将每个事件转换为一个新的发送三个String事件
                        // 最终合并，再发送给被观察者
                        List<String> list = new ArrayList<>();
                        for(int i = 0; i< 3; i++){
                            list.add("事件"  + integer + "拆分事件" + i);
                        }
                        return Observable.fromIterable(list);
                    }
                })
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                });
    }

    //ConcatMap 与FlatMap（）的 区别在于：拆分 & 重新合并生成的事件序列 的顺序 = 被观察者旧序列生产的顺序
    private void fun18(){
        Observable.just(1,2)
                .concatMap(new Function<Integer, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Integer integer) throws Exception {
                        // 通过concatMap中将被观察者生产的事件序列先进行拆分，
                        // 再将每个事件转换为一个新的发送三个String事件
                        // 最终合并，再发送给被观察者
                        List<String> list = new ArrayList<>();
                        for(int i = 0; i< 3; i++){
                            list.add("事件"  + integer + "拆分事件" + i);
                        }
                        return Observable.fromIterable(list);
                    }
                })
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                });
    }

    //Buffer
    private void fun19(){
        Observable.just(1,2,3,4,5)
                // 设置缓存区大小 & 步长
                // 缓存区大小 = 每次从被观察者中获取的事件数量
                // 步长 = 每次获取新事件的数量
                .buffer(3,1)
                .subscribe(new Observer<List<Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Integer> integers) {
                        Log.d(TAG, "缓存事件数量：" +integers.size());
                        for(Integer i:integers){
                            Log.d(TAG, "事件"+i);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 组合，合并操作符
     */

    //concat（） / concatArray（）组合多个被观察者一起发送数据，合并后 按发送顺序串行执行
    //二者区别：组合被观察者的数量，即concat（）组合被观察者数量≤4个，而concatArray（）则可＞4个
    private void fun20(){
        Observable.concat(Observable.just(1,2),
                Observable.just(3,4),
                Observable.just(5,6),
                Observable.just(7,8))
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //merge（） / mergeArray（）组合多个被观察者一起发送数据，合并后 按时间线并行执行
    //二者区别：组合被观察者的数量，即merge（）组合被观察者数量≤4个，而mergeArray（）则可＞4个
    //区别上述concat（）操作符：同样是组合多个被观察者一起发送数据，但concat（）操作符合并后是按发送顺序串行执行
    private void fun21(){
        Observable.merge(
                //从事件0开发发送，共发送3个事件，第一个事件延时1s发送，之后间隔2s
                Observable.intervalRange(0,3,1,2,TimeUnit.SECONDS),
                Observable.intervalRange(2,3,3,1,TimeUnit.SECONDS))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //concatDelayError（） / mergeDelayError（）
    //使用concat后发生error，后面的事件无法继续发送
    private void fun22(){
       Observable.concatArrayDelayError(Observable.create(new ObservableOnSubscribe<Integer>() {
           @Override
           public void subscribe(ObservableEmitter<Integer> e) throws Exception {
               e.onNext(1);
               e.onNext(2);
               e.onError(new Exception());
           }
       }),
               Observable.just(1,2,3))
               .subscribe(new Observer<Integer>() {
                   @Override
                   public void onSubscribe(Disposable d) {

                   }

                   @Override
                   public void onNext(Integer integer) {

                   }

                   @Override
                   public void onError(Throwable e) {

                   }

                   @Override
                   public void onComplete() {

                   }
               });
    }


    //Zip合并多个被观察者（Observable）发送的事件，生成一个新的事件序列（即组合过后的事件序列），并最终发送
    //事件组合方式 = 严格按照原先事件序列 进行对位合并
    //最终合并的事件数量 = 多个被观察者（Observable）中数量最少的数量
    private void fun23(){
        //两个不同的线程可以保证事件合并不是同步的
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "被观察者1发送了事件1" );
                e.onNext(1);
                Thread.sleep(1000);
                Log.d(TAG, "被观察者1发送了事件2" );
                e.onNext(2);
                Thread.sleep(1000);
                Log.d(TAG, "被观察者1发送了事件3" );
                e.onNext(3);
                Thread.sleep(1000);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.d(TAG, "被观察者2发送了事件1" );
                e.onNext("A");
                Thread.sleep(1000);
                Log.d(TAG, "被观察者2发送了事件2" );
                e.onNext("B");
                Thread.sleep(1000);
                Log.d(TAG, "被观察者2发送了事件3" );
                e.onNext("C");
                Thread.sleep(1000);
                Log.d(TAG, "被观察者2发送了事件4" );
                e.onNext("D");
                Thread.sleep(1000);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, "合并后收到的事件"+s);//输出1A2B3CD
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    //combineLatest
    //当两个Observables中的任何一个发送了数据后，将先发送了数据的Observables 的最新（最后）一个数据
    // 与 另外一个Observable发送的每个数据结合，最终基于该函数的结果发送数据
    //与Zip（）的区别：Zip（） = 按个数合并，即1对1合并；CombineLatest（） = 按时间合并，即在同一个时间点上合并
    private void fun24(){
        Observable.combineLatest(Observable.just(1, 2, 3),
                Observable.intervalRange(0, 3, 1, 1, TimeUnit.SECONDS), new BiFunction<Integer, Long, String>() {
                    @Override
                    public String apply(Integer integer, Long aLong) throws Exception {
                        Log.d(TAG, "合并数据：" + integer + aLong);
                        //合并的逻辑 = 相加
                        // 即第1个Observable发送的最后1个数据 与 第2个Observable发送的每1个数据进行相加
                        return String.valueOf(integer + aLong);
                    }
                }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    //reduce聚合的逻辑根据需求撰写，但本质都是前2个数据聚合，然后与后1个数据继续进行聚合，依次类推
    private void fun25(){
        Observable.just(1,2,3,4)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        Log.d(TAG, "本次计算：" +integer+integer2);
                        return integer * integer2;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "结果："+integer);
                    }
                });
    }

    //collect 将被观察者Observable发送的数据事件收集到一个数据结构里
    private void fun26(){
        Observable.just(1,2,3,4)
                .collect(
                        // 1. 创建数据结构（容器），用于收集被观察者发送的数据
                        new Callable<List<Integer>>() {

                    @Override
                    public List<Integer> call() throws Exception {
                        return new ArrayList<>();
                    }
                },
                        // 2. 对发送的数据进行收集
                        new BiConsumer<List<Integer>, Integer>() {
                    @Override
                    public void accept(List<Integer> integers, Integer integer) throws Exception {
                        integers.add(integer);

                    }
                }).subscribe(new Consumer<List<Integer>>() {
            @Override
            public void accept(List<Integer> integers) throws Exception {
                Log.d(TAG, ""+integers);
            }
        });
    }

    //startWith（） / startWithArray（） 在一个被观察者发送事件前，追加发送一些数据 / 一个新的被观察者
    private void fun27(){
        //后调用先追加
        Observable.just(1,2,3)
                .startWith(0)
                .startWithArray(4,5)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, ""+integer);//输出450123
                    }
                });
    }

    private void fun28(){
        Observable.just(1,2)
                .startWith(Observable.just(0))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, ""+integer);//输出012
                    }
                });
    }

    //count统计被观察者发送事件的数量
    private void fun29(){
        Observable.just(1,2,3)
                .count()
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d(TAG, "发送事件的数量:"+aLong);//输出3
                    }
                });
    }
}
