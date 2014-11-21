package uk.co.real_logic;

import org.junit.Test;

import java.util.Queue;

public class QueueTest
{
    private static final int CAPACITY = 10;

    private final Queue<Integer> queue = new OneToOneConcurrentArrayQueue<Integer>(CAPACITY);

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnNullOffer()
    {
        queue.offer(null);
    }
}