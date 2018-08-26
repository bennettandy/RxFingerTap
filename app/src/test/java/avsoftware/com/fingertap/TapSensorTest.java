package avsoftware.com.fingertap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

@RunWith(MockitoJUnitRunner.class)
public class TapSensorTest {

    @Test
    public void testTaps() throws Exception{

        Observable.just("one", "two", "three").repeat(3)

                .reduce("", ((String s, String s2) -> s+s2))

                .subscribeWith(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("Subscribe");
            }

                    @Override
                    public void onSuccess(String s) {
                        System.out.println("Success " + s );

                    }

                    @Override
            public void onError(Throwable e) {

            }
//
//            @Override
//            public void onComplete() {
//                System.out.println("Completed");
//            }
        });

        Thread.sleep(1000);
    }
}
