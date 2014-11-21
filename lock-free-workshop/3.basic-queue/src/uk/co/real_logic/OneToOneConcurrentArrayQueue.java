package uk.co.real_logic;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class OneToOneConcurrentArrayQueue<E> implements Queue<E>
{
    private final E[] buffer;

    private volatile long tail = 0;
    private volatile long head = 0;

    @SuppressWarnings("unchecked")
    public OneToOneConcurrentArrayQueue(final int size)
    {
        buffer = (E[])new Object[size];
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
    	
    	if((tail - head) >= buffer.length) {
    		return false;
    	} 
    	
		buffer[(int)(tail % buffer.length)] = e;
		tail++;
		return true;
    }

    public E poll()
    {
        // TODO
    	if(tail == head) {
    		return null;
    	} else {
    		int i = (int)(head % buffer.length);
    		E e = buffer[i];
    		buffer[i] = null;
    		head++;
    		return e;
    	}
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
        return buffer[(int)(head % buffer.length)];
    }

    public int size()
    {
        long currentHeadBefore;
        long currentTail;
        long currentHeadAfter = head;

        do
        {
            currentHeadBefore = currentHeadAfter;
            currentTail = tail;
            currentHeadAfter = head;

        }
        while (currentHeadAfter != currentHeadBefore);

        return (int)(currentTail - currentHeadAfter);
    }

    public boolean isEmpty()
    {
        return tail == head;
    }

    public boolean contains(final Object o)
    {
        if (null == o)
        {
            return false;
        }

        for (long i = head, limit = tail; i < limit; i++)
        {
            final E e = buffer[(int)(i % buffer.length)];
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
