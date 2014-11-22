package uk.co.real_logic;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class OneToOneConcurrentArrayQueue<E> implements Queue<E>
{
    private final int capacity;
    private final int mask;
    private final E[] buffer;

    private final AtomicLong head = new AtomicLong(0);
    private final AtomicLong tail = new AtomicLong(0);

    @SuppressWarnings("unchecked")
    public OneToOneConcurrentArrayQueue(final int capacity)
    {
        this.capacity = findNextPositivePowerOfTwo(capacity);
        mask = this.capacity - 1;
        buffer = (E[])new Object[this.capacity];
    }

    public static int findNextPositivePowerOfTwo(final int size)
    {
        return 1 << (32 - Integer.numberOfLeadingZeros(size - 1));
    }

    public boolean add(final E e)
    {
        if (offer(e))
        {
            return true;
        }

        throw new IllegalStateException("Queue is full");
    }

    public boolean offer(final E e)
    {
    	if(e == null) {
    		throw new NullPointerException("item cannot be null");
    	}
    	
    	final long currentTail = tail.get();
    	if((currentTail - head.get()) >= capacity) {
    		return false;
    	} 
    	
		buffer[(int)currentTail & mask] = e;
		tail.lazySet(currentTail + 1);
		return true;
    }

    public E poll()
    {
    	final long currentHead = head.get();
    	if(tail.get() == currentHead) {
    		return null;
    	}
    	
		int i = (int)currentHead & mask;
		E e = buffer[i];
		buffer[i] = null;
		head.lazySet(currentHead + 1);
		return e;
    }

    public E remove()
    {
        final E e = poll();
        if (null == e)
        {
            throw new IllegalStateException("Queue is empty");
        }

        return e;
    }

    public E element()
    {
        final E e = peek();
        if (null == e)
        {
            throw new NoSuchElementException("Queue is empty");
        }

        return e;
    }

    public E peek()
    {
        return buffer[(int)head.get() & mask];
    }

    public int size()
    {
        long currentHeadBefore;
        long currentTail;
        long currentHeadAfter = head.get();

        do
        {
            currentHeadBefore = currentHeadAfter;
            currentTail = tail.get();
            currentHeadAfter = head.get();

        }
        while (currentHeadAfter != currentHeadBefore);

        return (int)(currentTail - currentHeadAfter);
    }

    public boolean isEmpty()
    {
        return tail.get() == head.get();
    }

    public boolean contains(final Object o)
    {
        if (null == o)
        {
            return false;
        }

        for (long i = head.get(), limit = tail.get(); i < limit; i++)
        {
            final E e = buffer[(int)i & mask];
            if (o.equals(e))
            {
                return true;
            }
        }

        return false;
    }

    public Iterator<E> iterator()
    {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray()
    {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray(final T[] a)
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove(final Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(final Collection<?> c)
    {
        for (final Object o : c)
        {
            if (!contains(o))
            {
                return false;
            }
        }

        return true;
    }

    public boolean addAll(final Collection<? extends E> c)
    {
        for (final E o : c)
        {
            add(o);
        }

        return true;
    }

    public boolean removeAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }
}
