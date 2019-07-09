package com.github.chenhao96.utils.mapper;

import java.util.*;

public class UpdateList<D> implements Iterable<D> {

    private int currentIndex;
    private TreeMap<Integer, D> items;

    public void put(int index, D value) {
        if (items == null) {
            items = new TreeMap<>();
        }
        if (index > currentIndex) {
            currentIndex = index;
        }
        items.put(index, value);
    }

    public D get(int index) {
        return items == null ? null : items.get(index);
    }

    @Override
    public Iterator<D> iterator() {
        return new UpdateListIterator(0);
    }

    private class UpdateListIterator implements Iterator<D> {
        private int nextIndex;
        private NavigableMap<Integer, D> navigableMap;

        public UpdateListIterator(int nextIndex) {
            this.nextIndex = nextIndex;
        }

        public boolean hasNext() {
            return items != null && this.nextIndex <= UpdateList.this.currentIndex;
        }

        public D next() {
            if (hasNext()) {
                navigableMap = items.tailMap(nextIndex, true);
                Map.Entry<Integer, D> entry = navigableMap.firstEntry();
                if (entry != null) {
                    nextIndex = entry.getKey() + 1;
                    return entry.getValue();
                }
            }
            throw new NoSuchElementException();
        }
    }

    public String toString() {
        StringBuilder var2 = new StringBuilder();
        var2.append('[');
        Iterator<D> var1 = this.iterator();
        while (var1.hasNext()) {
            D var3 = var1.next();
            var2.append(var3 == this ? "(this UpdateList)" : var3);
            if (!var1.hasNext()) break;
            var2.append(',').append(' ');
        }
        var2.append(']');
        return var2.toString();
    }
}
