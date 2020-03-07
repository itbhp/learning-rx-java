package it.twinsbrains.reactive.rxjava;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObservableTest
{
  @Test
  void observableAsData()
  {
    Observable<String> strings = Observable
        .just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

    Disposable subscribe = strings.subscribe(System.out::println);
    subscribe.dispose();

    var firstTwo = strings.buffer(2).blockingFirst();
   assertEquals(firstTwo, asList("Alpha", "Beta"));
  }

  @Disabled
  void observableAsEvents() throws InterruptedException
  {
    var interval = Observable.interval(1, TimeUnit.SECONDS);

    var subscribe = interval.subscribe(System.out::println);
//    subscribe.dispose(); disposing subscriber will result in no lines printed

    Thread.sleep(3000);
  }
}
