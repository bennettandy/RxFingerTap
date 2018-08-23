package avsoftware.com.fingertap;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.concurrent.TimeUnit;

import avsoftware.com.fingertap.recorder.FileEnvelope;
import avsoftware.com.fingertap.recorder.FileRecorder;
import avsoftware.com.fingertap.recorder.FileRecorderImpl;
import avsoftware.com.fingertap.recorder.RecordWriter;
import avsoftware.com.fingertap.recorder.Recordable;
import avsoftware.com.fingertap.recorder.RecordedFile;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subscribers.TestSubscriber;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class FileRecorderTest extends RxBaseTest {

    @Mock
    private RecordWriter mockRecordWriter;

    @Mock
    private File mockFileResult;

    @Test
    public void testFileRecorder_short() {

        // set up Mock Writer
        when(mockRecordWriter.getFileObject()).thenReturn(mockFileResult);

        FileRecorder fileRecorder = new FileRecorderImpl();

        Flowable<? extends Recordable> eventsFlowable = createEventsFlowable(15, "-Test");

        FileEnvelope fileEnvelope = new FileEnvelope("header123", "footer321");

        Observable<Boolean> stopFlag = Observable.just(true).delay(800, TimeUnit.MILLISECONDS).startWith(false);

        Single<RecordedFile> fileRecorderStream = fileRecorder.writeToFile(eventsFlowable, mockRecordWriter, fileEnvelope, ", ", stopFlag);
        TestObserver<RecordedFile> testSubscriber = fileRecorderStream.subscribeOn(mTestScheduler).test();

        mTestScheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        // Test Result Object, should contain File pointing to the written file
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(recordedFile -> recordedFile.getFile().equals(mockFileResult));

        testSubscriber.dispose();

        // Check Write Events on Mocks
        //verify(mockRecordWriter, times(14)).writeSeparator(); // one less than the number of items
        verify(mockRecordWriter, times(1)).write("header123"); // one header
        verify(mockRecordWriter, times(1)).write("footer321"); // one footer
        verify(mockRecordWriter, times(6)).write(any()); // total write events
        verify(mockRecordWriter, times(1)).close(); // close called once
        verify(mockRecordWriter, times(1)).getFileObject(); // get File Object called once

        // some random writes from data Events
//        verify(mockRecordWriter, times(1)).write("13-Test");
//        verify(mockRecordWriter, times(1)).write("0-Test");
//        verify(mockRecordWriter, times(1)).write("4-Test");
//        verify(mockRecordWriter, times(1)).write("1-Test");
    }

    @Test
    public void testFileRecorder_long() {

        // set up Mock Writer
        when(mockRecordWriter.getFileObject()).thenReturn(mockFileResult);

        FileRecorder fileRecorder = new FileRecorderImpl();

        Flowable<? extends Recordable> eventsFlowable = createEventsFlowable(15000, "-Test");

        FileEnvelope fileEnvelope = new FileEnvelope("header123", "footer321");

        Observable<Boolean> stopFlag = Observable.just(true).delay(4, TimeUnit.SECONDS).startWith(false);

        Single<RecordedFile> fileRecorderStream = fileRecorder.writeToFile(eventsFlowable, mockRecordWriter, fileEnvelope, ", ", stopFlag);
        TestObserver<RecordedFile> testSubscriber = fileRecorderStream.subscribeOn(mTestScheduler).test();

        mTestScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        // Test Result Object, should contain File pointing to the written file
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(recordedFile -> recordedFile.getFile().equals(mockFileResult));

        testSubscriber.dispose();

        // Check Write Events on Mocks
        //verify(mockRecordWriter, times(14999)).writeSeparator(); // one less than the number of items
        verify(mockRecordWriter, times(1)).write("header123"); // one header
        verify(mockRecordWriter, times(1)).write("footer321"); // one footer
        verify(mockRecordWriter, times(6)).write(any()); // total write events
        verify(mockRecordWriter, times(1)).close(); // close called once
        verify(mockRecordWriter, times(1)).getFileObject(); // get File Object called once

        // some random writes from data Events
//        verify(mockRecordWriter, times(1)).write("14-Test");
//        verify(mockRecordWriter, times(1)).write("0-Test");
//        verify(mockRecordWriter, times(1)).write("4-Test");
//        verify(mockRecordWriter, times(1)).write("14999-Test");
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

    private Flowable<TestEvent> createEventsFlowable(int length, String suffix) {
        return Flowable.range(0, length)
                .map(integer -> integer + suffix)
                .map(TestEvent::new);
    }

    static class TestEvent implements Recordable {
        private final String mEventString;

        public TestEvent(String eventString) {
            mEventString = eventString;
        }

        @NotNull
        @Override
        public String getRecordableString() {
            return mEventString;
        }
    }

}
