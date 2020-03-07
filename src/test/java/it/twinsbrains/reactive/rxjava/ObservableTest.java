package it.twinsbrains.reactive.rxjava;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
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

  @Test
  void createFactory()
  {
    ObservableOnSubscribe<String> source = new ObservableOnSubscribe<>()
    {
      @Override public void subscribe(ObservableEmitter<String> emitter) throws Exception
      {
        emitter.onNext("ciccio");
        emitter.onComplete();
      }
    };
    List<String> objects = Observable.create(source).toList().blockingGet();

    assertEquals(objects, asList("ciccio"));
  }


}
