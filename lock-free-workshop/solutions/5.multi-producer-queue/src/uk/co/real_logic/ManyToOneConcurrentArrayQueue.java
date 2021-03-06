package uk.co.real_logic;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ManyToOneConcurrentArrayQueue<E> implements Queue<E>
{
    private final AtomicReferenceArray<E> buffer;
    private final int mask;
    private final int capacity;

    private final AtomicLong head = new AtomicLong(0);
    private final AtomicLong tail = new AtomicLong(0);

    @SuppressWarnings("unchecked")
    public ManyToOneConcurrentArrayQueue(final int capacity)
    {
        this.capacity = Util.findNextPositivePowerOfTwo(capacity);
        mask = this.capacity - 1;
        buffer = new AtomicReferenceArray<E>(this.capacity);
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
        if (null == e)
        {
            throw new NullPointerException("element cannot be null");
        }

        long currentTail;
        final long bufferLimit = head.get() + capacity;
        do
        {
            currentTail = tail.get();

            if (currentTail >= bufferLimit)
            {
                return false;
            }
        }
        while (!tail.compareAndSet(currentTail, currentTail + 1));

        final int index = (int)currentTail & mask;
        buffer.lazySet(index, e);

        return true;
    }

    @SuppressWarnings("unchecked")
    public E poll()
    {
        final long currentHead = head.get();
        if (currentHead == tail.get())
        {
            return null;
        }

        final int index = (int)currentHead & mask;
        E item;
        do
        {
            item = buffer.get(index);
        }
        while (null == item);

        buffer.lazySet(index, null);
        head.lazySet(currentHead + 1);

        return item;
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
        final int index = (int)head.get() & mask;
        return buffer.get(index);
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
            final E e = buffer.get((int)i & mask);
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
