package file.upload.client;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.io.File;
import java.io.FileInputStream;

public class Test {
    public static void main(String[] args) throws Exception {


        //t_filelength();
        t_observables();

    }

    private static void t_filelength() throws Exception {
      String source = "testdata/2.5G.7z";
        long start = System.currentTimeMillis( );
        long length = new File(source).length();
        System.out.println("File       : " + length + " in " + (System.currentTimeMillis() - start));

      long start2 = System.currentTimeMillis( );
      long length2 = new FileInputStream( source ).getChannel( ).size( );
      System.out.println( "FileChannel: " + length2 + " in " + ( System.currentTimeMillis( ) - start2 ) );

    }

    private static void t_observables() {
        Observable<String> names = Observable.just("Toto", "Titi");


        final Observable<String> async = names.subscribeOn(Schedulers.io());
        async.subscribe(n -> System.out.println(Thread.currentThread().getName() + " : " + n));

        names.subscribe(n -> System.out.println(Thread.currentThread().getName() + " : " + n));
    }
}
