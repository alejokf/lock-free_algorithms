package uk.co.real_logic;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.out;

public class ConditionVariablesPingPong
{
    private static final int REPETITIONS = 1 * 1000 * 1000;

    private static final Lock pingLock = new ReentrantLock();
    private static final Condition pingCondition = pingLock.newCondition();

    private static final Lock pongLock = new ReentrantLock();
    private static final Condition pongCondition = pongLock.newCondition();

    private static long pingValue = -1;
    private static long pongValue = -1;

    public static void main(final String[] args)
        throws Exception
    {
        final Thread sendThread = new Thread(new PingRunner());
        final Thread echoThread = new Thread(new PongRunner());
        echoThread.start();
        sendThread.start();

        final long start = System.nanoTime();

        echoThread.join();

        final long duration = System.nanoTime() - start;

        out.printf("duration %,d (ns)\n", duration);
        out.printf("%,d ns/op\n", duration / (REPETITIONS * 2L));
        out.printf("%,d ops/s\n", (REPETITIONS * 2L * 1000L * 1000L * 1000L) / duration);
        out.println("pingValue = " + pingValue + ", pongValue = " + pongValue);
    }

    public static class PingRunner implements Runnable
    {
        public void run()
        {
            for (long i = 0; i < REPETITIONS; i++)
            {
                pingLock.lock();
                try
                {
                    pingValue = i;
                    pingCondition.signal();
                }
                finally
                {
                    pingLock.unlock();
                }

                pongLock.lock();
                try
                {
                    while (pongValue != i)
                    {
                        pongCondition.await();
                    }
                }
                catch (final InterruptedException ex)
                {
                    break;
                }
                finally
                {
                    pongLock.unlock();
                }

            }
        }
    }

    public static class PongRunner implements Runnable
    {
        public void run()
        {
            for (long i = 0; i < REPETITIONS; i++)
            {
                pingLock.lock();
                try
                {
                    while (pingValue != i)
                    {
                        pingCondition.await();
                    }
                }
                catch (final InterruptedException ex)
                {
                    break;
                }
                finally
                {
                    pingLock.unlock();
                }

                pongLock.lock();
                try
                {
                    pongValue = i;
                    pongCondition.signal();
                }
                finally
                {
                    pongLock.unlock();
                }
            }
        }
    }
}
