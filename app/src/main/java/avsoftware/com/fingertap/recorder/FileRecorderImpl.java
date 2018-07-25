package avsoftware.com.fingertap.recorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import avsoftware.com.fingertap.sensors.Recordable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class FileRecorderImpl implements FileRecorder {

    public FileRecorderImpl( ){
    }

    @Override
    public Single<RecordedFile> writeToFile( Flowable<? extends Recordable> recordable, File outputDirectory, String filename){

        return Single.just(new FileStream(new File(outputDirectory, filename)))
                .observeOn(Schedulers.io()) // requires IO scheduler to write to filesystem
                .doOnSuccess(FileStream::open)
                .flatMap(filestream -> recordable
                        .doOnNext(event -> filestream.writeData(event.getBytes()))
                        .ignoreElements()
                        .toSingleDefault(filestream))
                .doOnSuccess(FileStream::close)
                .map(fileStream -> fileStream.file)
                .map(RecordedFile::new);
    }

    class FileStream{

        public final File file;

        FileOutputStream fOut;

        FileStream(File f){
            file = f;
        }

        void open() throws FileNotFoundException {
            Timber.d("Open File: %s", file.toString());
            fOut = new FileOutputStream(file);
        }
        void writeData( byte[] data){
            Timber.d("Write %d bytes", data.length);
            try {
                fOut.write(data);
            }
            catch (Exception e){
                Timber.e(e, "Write failed");
                try {
                    fOut.close();
                }
                catch (Exception ex){
                    Timber.e(ex, "Failed to close");
                }
            }
        }

        void close() throws IOException {
            fOut.flush();
            fOut.close();
        }
    }
}
