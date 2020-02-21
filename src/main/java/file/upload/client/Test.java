package file.upload.client;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Test {
    public static void main(String[] args) throws Exception {


        t_filelength();
        t_observables();

    }

    private static void t_filelength() throws Exception {
        String source = "/Users/julienrevaultdallonnes/DEV/TESTS/GITHUB/FileSplitAndUpload/testdata/001.fastq.gz";
        long start = System.currentTimeMillis( );
        long length = new File(source).length();
        System.out.println("File       : " + length + " in " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis( );
        length = new FileInputStream(source).getChannel().size();
        System.out.println("FileChannel: " + length + " in " + (System.currentTimeMillis() - start));

    }

    private static void t_observables() {
        Observable<String> names = Observable.just("Toto", "Titi");


        final Observable<String> async = names.subscribeOn(Schedulers.io());
        async.subscribe(n -> System.out.println(Thread.currentThread().getName() + " : " + n));

        names.subscribe(n -> System.out.println(Thread.currentThread().getName() + " : " + n));
    }
}
