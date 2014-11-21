package uk.co.real_logic;

import org.junit.Assert;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.CyclicBarrier;

public class ManyToOneQueueValidatingPerfTest
{
    private static final int REPETITIONS = 20 * 1000 * 1000;
    private static final int NUM_PRODUCERS = 3;
    private static final int QUEUE_CAPACITY = 64 * 1024;

    public static void main(final String[] args)
        throws Exception
    {
        final ManyToOneQueueValidatingPerfTest test = new ManyToOneQueueValidatingPerfTest();
        test.shouldPerformanceTestOfferAndPoll();
    }

    @Test
    public void shouldPerformanceTestOfferAndPoll()
        throws Exception
    {
        final Queue<Integer> queue = new ManyToOneConcurrentArrayQueue<Integer>(QUEUE_CAPACITY);
        //final Queue<Integer> queue = new java.util.concurrent.ArrayBlockingQueue<Integer>(QUEUE_CAPACITY);

        for (int i = 0; i < 5; i++)
        {
            System.gc();
            Thread.sleep(1000L);
            perfRun(i, queue);
        }
    }

    private void perfRun(final int runNum, final Queue<Integer> queue)
        throws Exception
    {
        final CyclicBarrier barrier = new CyclicBarrier(NUM_PRODUCERS + 1);
        for (int i = 0; i < NUM_PRODUCERS; i++)
        {
            new Thread(new Producer(queue, i, barrier)).start();
        }

        final int[] producerCounts = new int[NUM_PRODUCERS];

        barrier.await();

        final long start = System.nanoTime();

        int i = (REPETITIONS * NUM_PRODUCERS) + 1;
        while (0 != --i)
        {
            Integer id;
            while (null == (id = queue.poll()))
            {
                Thread.yield();
            }

            int producerNum = id.intValue();
            ++producerCounts[producerNum];
        }

        final long duration = System.nanoTime() - start;
        final long opsPerSec = (REPETITIONS * NUM_PRODUCERS * 1000L * 1000L * 1000L) / duration;
        System.out.printf("%d - %d producers: %,d ops/sec - %s\n",
                          Integer.valueOf(runNum),
                          Integer.valueOf(NUM_PRODUCERS),
                          Long.valueOf(opsPerSec),
                          this.getClass().getSimpleName());

        for (final int producerCount : producerCounts)
        {
            Assert.assertEquals(REPETITIONS, producerCount);
        }
    }

    private static class Producer implements Runnable
    {
        private final Queue<Integer> queue;
        private final Integer id;
        private final CyclicBarrier barrier;

        private Producer(final Queue<Integer> queue, final int id, final CyclicBarrier barrier)
        {
            this.queue = queue;
            this.id = Integer.valueOf(id);
            this.barrier = barrier;
        }

        public void run()
        {
            try
            {
                barrier.await();
            }
            catch (final Exception ignore)
            {
            }

            int i = REPETITIONS + 1;
            while (0 != --i)
            {
                while (!queue.offer(id))
                {
                    Thread.yield();
                }
            }
        }
    }
}

