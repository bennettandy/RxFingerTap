package avsoftware.com.fingertap.recorder;

import java.io.File;

import avsoftware.com.fingertap.sensors.Recordable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface FileRecorder {
    Single<RecordedFile> writeToFile(Flowable<? extends Recordable> recordable, File outputDirectory, String filename);
}
