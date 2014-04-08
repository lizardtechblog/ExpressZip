package org.vaadin.vol.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LogicalFilter extends Filter {

    private ArrayList<Filter> filters; // {Array(OpenLayers.Filter)} Child
                                       // filters for this filter.

    public LogicalFilter() {
        super();
        filters = new ArrayList<Filter>();
        filter.setProperty("filters", filters);
    }

    /**
     * 
     * @see java.util.ArrayList#trimToSize()
     */
    public void trimToSize() {
        filters.trimToSize();
    }

    /**
     * @param minCapacity
     * @see java.util.ArrayList#ensureCapacity(int)
     */
    public void ensureCapacity(int minCapacity) {
        filters.ensureCapacity(minCapacity);
    }

    /**
     * @return
     * @see java.util.ArrayList#size()
     */
    public int size() {
        return filters.size();
    }

    /**
     * @return
     * @see java.util.ArrayList#isEmpty()
     */
    public boolean isEmpty() {
        return filters.isEmpty();
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return filters.contains(o);
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
        return filters.indexOf(o);
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
        return filters.lastIndexOf(o);
    }

    /**
     * @return
     * @see java.util.AbstractList#iterator()
     */
    public Iterator<Filter> iterator() {
        return filters.iterator();
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        return filters.containsAll(c);
    }

    /**
     * @return
     * @see java.util.AbstractList#listIterator()
     */
    public ListIterator<Filter> listIterator() {
        return filters.listIterator();
    }

    /**
     * @return
     * @see java.util.ArrayList#toArray()
     */
    public Object[] toArray() {
        return filters.toArray();
    }

    /**
     * @param index
     * @return
     * @see java.util.AbstractList#listIterator(int)
     */
    public ListIterator<Filter> listIterator(int index) {
        return filters.listIterator(index);
    }

    /**
     * @param <T>
     * @param a
     * @return
     * @see java.util.ArrayList#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
        return filters.toArray(a);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        return filters.removeAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        return filters.retainAll(c);
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#get(int)
     */
    public Filter get(int index) {
        return filters.get(index);
    }

    /**
     * @param index
     * @param element
     * @return
     * @see java.util.ArrayList#set(int, java.lang.Object)
     */
    public Filter set(int index, Filter element) {
        return filters.set(index, element);
    }

    /**
     * @param e
     * @return
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    public boolean add(Filter e) {
        return filters.add(e);
    }

    /**
     * @param index
     * @param element
     * @see java.util.ArrayList#add(int, java.lang.Object)
     */
    public void add(int index, Filter element) {
        filters.add(index, element);
    }

    /**
     * @param fromIndex
     * @param toIndex
     * @return
     * @see java.util.AbstractList#subList(int, int)
     */
    public List<Filter> subList(int fromIndex, int toIndex) {
        return filters.subList(fromIndex, toIndex);
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#remove(int)
     */
    public Filter remove(int index) {
        return filters.remove(index);
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return filters.remove(o);
    }

    /**
     * @param o
     * @return
     * @see java.util.AbstractList#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        return filters.equals(o);
    }

    /**
     * 
     * @see java.util.ArrayList#clear()
     */
    public void clear() {
        filters.clear();
    }

    /**
     * @param c
     * @return
     * @see java.util.ArrayList#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends Filter> c) {
        return filters.addAll(c);
    }

    /**
     * @param index
     * @param c
     * @return
     * @see java.util.ArrayList#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends Filter> c) {
        return filters.addAll(index, c);
    }

}
