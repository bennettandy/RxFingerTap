package avsoftware.com.fingertap;

import org.junit.After;
import org.junit.Before;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;

public class RxBaseTest {

    protected CompositeDisposable mDisposable;
    protected TestScheduler mTestScheduler;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        setUpRxSchedulersForTest(mTestScheduler);
        mDisposable = new CompositeDisposable();
    }

    @After
    public void tearDown() {
        tearDownRxSechedulersForTest();
        mDisposable.dispose();
    }

    /**
     * Setting up RxJavaHooks enables Observable chains to run in single threaded JUnit environment
     */

    public static void setUpRxSchedulersForTest() {
        // Override RxJava schedulers
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    public static void setUpRxSchedulersForTest(Scheduler testScheduler) {
        // Override RxJava schedulers
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> testScheduler);
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> testScheduler);
        RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> testScheduler);
        RxAndroidPlugins.setMainThreadSchedulerHandler(scheduler -> testScheduler);
    }

    public static void tearDownRxSechedulersForTest() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
    }
}
