package it.twinsbrains.reactive.rxjava;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class ObservableTest
{
  @Test
  void observableJust()
  {
    Observable<String> strings = Observable
        .just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

    Disposable subscribe = strings.subscribe(System.out::println);
    subscribe.dispose();

    var firstTwo = strings.buffer(2).blockingFirst();
    assertThat(firstTwo, Matchers.contains("Alpha", "Beta"));
  }
}
