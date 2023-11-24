package com.example.journalapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for testing LiveData objects
 */
public class LiveDataTestUtil {

    /**
     * Waits for a LiveData to emit a value and returns that value.
     * Useful for tests when observing LiveData and retrieving the latest value
     *
     * @param liveData LiveData to observe
     * @return
     * @param <T>
     * @throws InterruptedException
     */
    public static <T> T getValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T t) {
                data[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(observer);

        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return (T) data[0];
    }
}
