package it.twinsbrains.reactive.rxjava;

import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

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

  @Test
  void hotObservableHangingInfinitely()
  {
    assertTimeout(Duration.ofSeconds(5), () -> {
      Observable<String> strings = Observable
          .just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
          .publish();

      strings.subscribe(System.out::println);
    });
  }

  @Test
  void hotObservableMultiCast()
  {
    List<String> subscriber1 = new LinkedList<>();
    List<String> subscriber2 = new LinkedList<>();

    ConnectableObservable<String> strings = Observable
        .just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
        .publish();

    strings.subscribe(subscriber1::add);
    strings.subscribe(subscriber2::add);
    strings.connect();

    assertEquals(subscriber1, asList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));
    assertEquals(subscriber2, asList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));

  }

  @Test
  void hotObservableLateSubscriberGetsNothing()
  {
    List<String> subscriber3 = new LinkedList<>();

    ConnectableObservable<String> strings = Observable
        .just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
        .publish();

    strings.connect();

    Observable<List<String>> firstTwo = strings.buffer(2);
    firstTwo.subscribe(subscriber3::addAll);

    assertEquals(subscriber3, Collections.emptyList());
  }

}
