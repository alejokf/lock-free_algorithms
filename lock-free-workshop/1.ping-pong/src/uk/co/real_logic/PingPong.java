package uk.co.real_logic;

import static java.lang.System.out;

public final class PingPong
{
    private static final long REPETITIONS = 100L * 1000L * 1000L;

    private static volatile long pingValue = -1;
    private static volatile long pongValue = -1;

    public static void main(final String[] args)
        throws Exception
    {
        final Thread pongThread = new Thread(new PongRunner());
        final Thread pingThread = new Thread(new PingRunner());
        pongThread.start();
        pingThread.start();

        final long start = System.nanoTime();

        pongThread.join();

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
        	for (int i = 0; i < REPETITIONS; i++) {
				pingValue = i;
				while (pongValue != i) {}
			}
        }
    }

    public static class PongRunner implements Runnable
    {
        public void run()
        {
        	for (int i = 0; i < REPETITIONS; i++) {
				while (pingValue != i) {}
				pongValue = i;
			}
        }
    }
}
