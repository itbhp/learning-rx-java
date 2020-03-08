package it.twinsbrains.reactive.rxjava;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
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
   assertEquals(asList("Alpha", "Beta"), firstTwo);
  }

  @Test
  void createFactory()
  {
    ObservableOnSubscribe<String> source = emitter -> {
      emitter.onNext("Whatever");
      emitter.onComplete();
    };

    List<String> objects = Observable.create(source).toList().blockingGet();

    assertEquals(List.of("Whatever"), objects);
  }

  @Test
  void intervalAreColdObservable()
  {
    List<String> output = new LinkedList<>();

    Observable<Long> strings = Observable
        .interval(1, TimeUnit.SECONDS);

    var subscriber1 = strings.subscribe(e -> output.add("subscriber1 saw " + e));

    safeSleep(2_000);

    var subscriber2 = strings.subscribe(e -> output.add("subscriber2 saw " + e));

    safeSleep(3_000);

    // dispose both subscriber to stop collecting values
    subscriber1.dispose();
    subscriber2.dispose();

    assertEquals(asList(
        "subscriber1 saw 0",
        "subscriber1 saw 1",
        "subscriber1 saw 2",
        "subscriber2 saw 0",
        "subscriber1 saw 3",
        "subscriber2 saw 1",
        "subscriber1 saw 4",
        "subscriber2 saw 2"
    ), output);
  }

  @Test
  void hotObservableHangingInfinitely()
  {
    assertTimeout(Duration.ofSeconds(5), () -> {
      Observable<String> strings = Observable
          .just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
          .publish();
      strings.subscribe(System.out::println);
      //we should have called strings.connect() to start the observable flow
    });
  }

  @Test
  void hotObservableMultiCast()
  {
    List<String> output = new LinkedList<>();

    ConnectableObservable<Long> strings = Observable
        .interval(1, TimeUnit.SECONDS)
        .publish();

    var subscriber1 = strings.subscribe(e -> output.add("subscriber1 saw " + e));
    strings.connect();

    safeSleep(2_000);

    var subscriber2 = strings.subscribe(e -> output.add("subscriber2 saw " + e));

    safeSleep(3_000);

    // dispose both subscriber to stop collecting values
    subscriber1.dispose();
    subscriber2.dispose();

    assertEquals(output, asList(
        "subscriber1 saw 0",
        "subscriber1 saw 1",
        "subscriber1 saw 2",
        "subscriber2 saw 2",
        "subscriber1 saw 3",
        "subscriber2 saw 3",
        "subscriber1 saw 4",
        "subscriber2 saw 4"
    ));
  }

  @Test
  void take()
  {
    List<String> output = new LinkedList<>();
    var subscribe = Observable.interval(300, TimeUnit.MILLISECONDS)
        .take(2, TimeUnit.SECONDS)
        .subscribe(i -> output.add("RECEIVED: " + i));

    safeSleep(2000);
    subscribe.dispose();

    assertEquals(asList(
        "RECEIVED: 0",
        "RECEIVED: 1",
        "RECEIVED: 2",
        "RECEIVED: 3",
        "RECEIVED: 4",
        "RECEIVED: 5"
    ), output);
  }

  @Test
  void takeLast()
  {
    List<String> output = new LinkedList<>();
    var subscribe = Observable.interval(300, TimeUnit.MILLISECONDS)
        .take(2, TimeUnit.SECONDS)
        .takeLast(2)
        .subscribe(i -> output.add("RECEIVED: " + i));

    safeSleep(2000);
    subscribe.dispose();

    assertEquals(asList(
        "RECEIVED: 4",
        "RECEIVED: 5"
    ), output);
  }

  private void safeSleep(int millis)
  {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e){
      //
    }
  }

}
