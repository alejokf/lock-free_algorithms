package uk.co.real_logic;

import java.util.Queue;
import java.util.concurrent.CyclicBarrier;

public class OneToOneQueueValidatingPerfTest
{
    public static final int REPETITIONS = 100 * 1000 * 1000;
    public static final int QUEUE_SIZE = 64 * 1024;

    public static void main(final String[] args)
        throws Exception
    {
        final OneToOneQueueValidatingPerfTest test = new OneToOneQueueValidatingPerfTest();
        test.shouldPerformanceTestOfferAndPoll();
    }

    private void shouldPerformanceTestOfferAndPoll()
        throws Exception
    {
        //final Queue<Integer> queue = new java.util.concurrent.ConcurrentLinkedQueue<Integer>();
        //final Queue<Integer> queue = new java.util.concurrent.LinkedBlockingQueue<Integer>(QUEUE_SIZE);
        //final Queue<Integer> queue = new java.util.concurrent.ArrayBlockingQueue<Integer>(QUEUE_SIZE);
        final Queue<Integer> queue = new OneToOneConcurrentArrayQueue<Integer>(QUEUE_SIZE);

        for (int i = 0; i < 5; i++)
        {
            System.gc();
            Thread.sleep(1000);
            testRun(i, queue);
        }
    }

    private void testRun(final int runNumber, final Queue<Integer> queue)
        throws Exception
    {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final Runnable runner = new Producer(barrier, queue);
        final Thread t = new Thread(runner);
        t.start();

        barrier.await();
        final long start = System.nanoTime();

        int i = REPETITIONS + 1;
        while (0 != --i)
        {
            Integer item;
            while (null == (item = queue.poll()))
            {
                Thread.yield();
            }

            if (i != item.intValue())
            {
                final String msg = String.format("Invalid item: got %d, expected %d", item, Integer.valueOf(i));
                throw new IllegalStateException(msg);
            }
        }

        final long finish = System.nanoTime();
        final long duration = finish - start;
        final long ops = (REPETITIONS * 1000L * 1000L * 1000L) / duration;

        System.out.format("%d - ops/sec=%,d - %s\n",
                          Integer.valueOf(runNumber),
                          Long.valueOf(ops),
                          this.getClass().getSimpleName());
    }

    private static class Producer implements Runnable
    {
        private final CyclicBarrier barrier;
        private final Queue<Integer> queue;

        public Producer(final CyclicBarrier barrier, final Queue<Integer> queue)
        {
            this.barrier = barrier;
            this.queue = queue;
        }

        public void run()
        {
            try
            {
                barrier.await();
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();
            }

            try
            {
                int i = REPETITIONS + 1;
                while (0 != --i)
                {
                    while (!queue.offer(Integer.valueOf(i)))
                    {
                        Thread.yield();
                    }
                }
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
