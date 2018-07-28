package avsoftware.com.fingertap;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import avsoftware.com.fingertap.recorder.FileEnvelope;
import avsoftware.com.fingertap.recorder.FileRecorder;
import avsoftware.com.fingertap.recorder.FileRecorderImpl;
import avsoftware.com.fingertap.recorder.RecordWriter;
import avsoftware.com.fingertap.recorder.Recordable;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(MockitoJUnitRunner.class)
public class FileRecorderTest extends RxBaseTest {

    @Mock
    private RecordWriter mockRecordWriter;

    static class TestEvent implements Recordable {
        private final String mEventString;
        
        public TestEvent(String eventString){
            mEventString = eventString;
        }
        
        @NotNull
        @Override
        public String getRecordableString() {
            return mEventString;
        }
    }
    
    @Test
    public void testMockEventsFlowable() {

        // flowable stream of test events
        Flowable<? extends Recordable> eventsFlowable = createEventsFlowable(10, "-Tested");

        TestSubscriber<? extends Recordable> test = eventsFlowable.subscribeOn(mTestScheduler)
                .test();

        test.assertValueCount(0);

        mTestScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        test.assertComplete();

        test.assertValueAt(0, o -> o.getRecordableString().equals("0-Tested"));
        test.assertValueAt(5, o -> o.getRecordableString().equals("5-Tested"));
        test.assertValueAt(9, o -> o.getRecordableString().equals("9-Tested"));

        test.assertValueCount(10);

        test.dispose();
    }

    public void testFileRecorder(){

        FileRecorder fileRecorder = new FileRecorderImpl();

        Flowable<? extends Recordable> eventsFlowable = createEventsFlowable(15, "-Test");

        FileEnvelope fileEnvelope = new FileEnvelope("header123", "footer321");

      //  fileRecorder.writeToFile(eventsFlowable, mockRecordWriter, fileEnvelope);

        // Consume and record Tap Events
//                recorderTaps.writeToFile(tapEventPipeline,
//                        externalCacheDir, "taps.txt", tapDataEnvelope )
//                        .doOnSuccess { Timber.d("DONE TAPS") }
//                        .doOnError { Timber.e(it, "Failed to write file") }
//                        .doOnSuccess { Timber.d("DONE $it") }

    }



    private Flowable<TestEvent> createEventsFlowable( int length, String suffix){
        return Flowable.range(0, length)
                .map(integer -> integer + suffix )
                .map(TestEvent::new);
    }
}
