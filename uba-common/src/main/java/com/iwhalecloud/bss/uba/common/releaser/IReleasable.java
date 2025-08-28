package com.iwhalecloud.bss.uba.common.releaser;

import java.util.concurrent.TimeUnit;

/**定义用来自动回收的接口，可以将接口实例放入到自动回收器，可以根据自动超时时间进行释放*/
public interface IReleasable {
    /**自动回收调用的方法，只有再AutoReleaser中才会强制释放，这种释放是将对象移除放在foreach外面，所以这里不能移除，否则会有问题*/
    public void release(boolean isForce);

    /**
     * 超时时间
     */
    public default long timeout() {
        //有效连接最大持有时间：2分钟(轮询是1分钟一次，这里超时时间1分钟，最多持有2分钟)
        return TimeUnit.MINUTES.toMillis(1);
    }
    /**
     * 是否需要销毁自己，销毁的执行方式是提前将对象加入到队列中，如果是临时对象，销毁资源的时候不自我销毁，会导致这种临时对象越来越多，最终oom <br />
     * 对于长期贮存内存的对象，这个方法返回false；<br />
     * 临时new出来的对象，这个方法返回true，在超时自动销毁的时候，对象自身也从销毁队列中移除（是否真实销毁，由对象是否被其他对象引用来决定）；<br />
     * */
    public default boolean removeSelf() {
        return false;
    }

}
