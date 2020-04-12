package it.twinsbrains.reactive.rxjava;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.TestScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ObservableTest {

  private TestScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new TestScheduler();
    RxJavaPlugins.setComputationSchedulerHandler(s -> scheduler);
  }

  @Test
  void observableAsData() {
    Observable<String> strings = Observable
      .just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

    Disposable subscribe = strings.subscribe(System.out::println);
    subscribe.dispose();

    var firstTwo = strings.buffer(2).blockingFirst();
    assertEquals(List.of("Alpha", "Beta"), firstTwo);
  }

  @Test
  void createFactory() {
    ObservableOnSubscribe<String> source = emitter -> {
      emitter.onNext("Whatever");
      emitter.onComplete();
    };

    List<String> objects = Observable.create(source).toList().blockingGet();

    assertEquals(List.of("Whatever"), objects);
  }

  @Test
  void intervalAreColdObservable() {
    List<String> output = new LinkedList<>();

    Observable<Long> strings = Observable
      .interval(1, TimeUnit.SECONDS);

    var subscriber1 = strings.subscribe(e -> output.add("subscriber1 saw " + e));

    scheduler.advanceTimeBy(2, TimeUnit.SECONDS);

    var subscriber2 = strings.subscribe(e -> output.add("subscriber2 saw " + e));

    scheduler.advanceTimeBy(3, TimeUnit.SECONDS);

    // dispose both subscriber to stop collecting values
    subscriber1.dispose();
    subscriber2.dispose();

    assertEquals(
      List.of(
        "subscriber1 saw 0",
        "subscriber1 saw 1",
        "subscriber1 saw 2",
        "subscriber2 saw 0",
        "subscriber1 saw 3",
        "subscriber2 saw 1",
        "subscriber1 saw 4",
        "subscriber2 saw 2"
      ),
      output
    );
  }

  @Test
  void hotObservableHangingInfinitely() {
    assertTimeout(Duration.ofSeconds(5), () -> {
      Observable<String> strings = Observable
        .just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
        .publish();
      var subscribe = strings.subscribe(System.out::println);
      assertFalse(subscribe.isDisposed());
      //we should have called strings.connect() to start the observable flow
    });
  }

  @Test
  void hotObservableMultiCast() {
    List<String> output = new LinkedList<>();

    ConnectableObservable<Long> strings = Observable
      .interval(1, TimeUnit.SECONDS)
      .publish();

    var subscriber1 = strings.subscribe(e -> output.add("subscriber1 saw " + e));
    strings.connect();

    scheduler.advanceTimeBy(2, TimeUnit.SECONDS);

    var subscriber2 = strings.subscribe(e -> output.add("subscriber2 saw " + e));

    scheduler.advanceTimeBy(3, TimeUnit.SECONDS);

    // dispose both subscriber to stop collecting values
    subscriber1.dispose();
    subscriber2.dispose();

    assertEquals(
      List.of(
        "subscriber1 saw 0",
        "subscriber1 saw 1",
        "subscriber1 saw 2",
        "subscriber2 saw 2",
        "subscriber1 saw 3",
        "subscriber2 saw 3",
        "subscriber1 saw 4",
        "subscriber2 saw 4"
      ),
      output
    );
  }

  @Test
  void take() {
    List<String> output = new LinkedList<>();
    var subscribe = Observable.interval(300, TimeUnit.MILLISECONDS)
      .take(2, TimeUnit.SECONDS)
      .subscribe(i -> output.add("RECEIVED: " + i));

    scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
    subscribe.dispose();

    assertEquals(
      List.of(
        "RECEIVED: 0",
        "RECEIVED: 1",
        "RECEIVED: 2",
        "RECEIVED: 3",
        "RECEIVED: 4",
        "RECEIVED: 5"
      ),
      output
    );
  }

  @Test
  void takeLast() {
    List<String> output = new LinkedList<>();
    var subscribe = Observable.interval(500, TimeUnit.MILLISECONDS)
      .take(2, TimeUnit.SECONDS)
      .takeLast(2)
      .subscribe(i -> output.add("RECEIVED: " + i));

    scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
    subscribe.dispose();

    assertEquals(List.of("RECEIVED: 1", "RECEIVED: 2"), output);
  }

}
