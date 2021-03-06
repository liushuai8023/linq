package com.bestvike.linq.enumerable;

import com.bestvike.function.Func1;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.exception.ExceptionArgument;
import com.bestvike.linq.exception.ThrowHelper;
import com.bestvike.linq.util.ArrayUtils;
import com.bestvike.out;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2018-05-03.
 */
public final class Range {
    private Range() {
    }

    public static IEnumerable<Integer> range(int start, int count) {
        long max = (long) start + count - 1;
        if (count < 0 || max > Integer.MAX_VALUE)
            ThrowHelper.throwArgumentOutOfRangeException(ExceptionArgument.count);

        if (count == 0)
            return EmptyPartition.instance();

        return new RangeIterator(start, count);
    }
}


final class RangeIterator extends Iterator<Integer> implements IPartition<Integer> {
    private final int start;
    private final int end;

    RangeIterator(int start, int count) {
        assert count > 0;
        this.start = start;
        this.end = start + count;
    }

    @Override
    public AbstractIterator<Integer> clone() {
        return new RangeIterator(this.start, this.end - this.start);
    }

    @Override
    public boolean moveNext() {
        switch (this.state) {
            case 1:
                assert this.start != this.end;
                this.current = this.start;
                this.state = 2;
                return true;
            case 2:
                if (++this.current == this.end) {
                    this.close();
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public <TResult> IEnumerable<TResult> _select(Func1<Integer, TResult> selector) {
        return new SelectIPartitionIterator<>(this, selector);
    }

    @Override
    public Integer[] _toArray(Class<Integer> clazz) {
        Integer[] array = ArrayUtils.newInstance(clazz, this.end - this.start);
        int cur = this.start;
        for (int i = 0; i != array.length; ++i) {
            array[i] = cur;
            ++cur;
        }
        return array;
    }

    @Override
    public Object[] _toArray() {
        Object[] array = new Object[this.end - this.start];
        int cur = this.start;
        for (int i = 0; i != array.length; ++i) {
            array[i] = cur;
            ++cur;
        }
        return array;
    }

    @Override
    public List<Integer> _toList() {
        List<Integer> list = new ArrayList<>(this.end - this.start);
        for (int cur = this.start; cur != this.end; cur++)
            list.add(cur);
        return list;
    }

    @Override
    public int _getCount(boolean onlyIfCheap) {
        return this.end - this.start;
    }

    @Override
    public IPartition<Integer> _skip(int count) {
        return count >= this.end - this.start
                ? EmptyPartition.instance()
                : new RangeIterator(this.start + count, this.end - this.start - count);
    }

    @Override
    public IPartition<Integer> _take(int count) {
        return count >= this.end - this.start
                ? this
                : new RangeIterator(this.start, count);
    }

    @Override
    public Integer _tryGetElementAt(int index, out<Boolean> found) {
        if (index < this.end - this.start) {
            found.value = true;
            return this.start + index;
        }
        found.value = false;
        return null;
    }

    @Override
    public Integer _tryGetFirst(out<Boolean> found) {
        found.value = true;
        return this.start;
    }

    @Override
    public Integer _tryGetLast(out<Boolean> found) {
        found.value = true;
        return this.end - 1;
    }
}
