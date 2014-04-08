package org.vaadin.vol.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FeatureIdFilter extends Filter {

    private ArrayList<String> fids; // {Array(String)} Feature Ids to evaluate
                                    // this rule against.

    public FeatureIdFilter() {
        super();
        fids = new ArrayList<String>();
        filter.setProperty("fids", fids);
    }

    /**
     * 
     * @see java.util.ArrayList#trimToSize()
     */
    public void trimToSize() {
        fids.trimToSize();
    }

    /**
     * @param minCapacity
     * @see java.util.ArrayList#ensureCapacity(int)
     */
    public void ensureCapacity(int minCapacity) {
        fids.ensureCapacity(minCapacity);
    }

    /**
     * @return
     * @see java.util.ArrayList#size()
     */
    public int size() {
        return fids.size();
    }

    /**
     * @return
     * @see java.util.ArrayList#isEmpty()
     */
    public boolean isEmpty() {
        return fids.isEmpty();
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return fids.contains(o);
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
        return fids.indexOf(o);
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
        return fids.lastIndexOf(o);
    }

    /**
     * @return
     * @see java.util.AbstractList#iterator()
     */
    public Iterator<String> iterator() {
        return fids.iterator();
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        return fids.containsAll(c);
    }

    /**
     * @return
     * @see java.util.AbstractList#listIterator()
     */
    public ListIterator<String> listIterator() {
        return fids.listIterator();
    }

    /**
     * @return
     * @see java.util.ArrayList#toArray()
     */
    public Object[] toArray() {
        return fids.toArray();
    }

    /**
     * @param index
     * @return
     * @see java.util.AbstractList#listIterator(int)
     */
    public ListIterator<String> listIterator(int index) {
        return fids.listIterator(index);
    }

    /**
     * @param <T>
     * @param a
     * @return
     * @see java.util.ArrayList#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
        return fids.toArray(a);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        return fids.removeAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        return fids.retainAll(c);
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#get(int)
     */
    public String get(int index) {
        return fids.get(index);
    }

    /**
     * @param index
     * @param element
     * @return
     * @see java.util.ArrayList#set(int, java.lang.Object)
     */
    public String set(int index, String element) {
        return fids.set(index, element);
    }

    /**
     * @param e
     * @return
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    public boolean add(String e) {
        return fids.add(e);
    }

    /**
     * @param index
     * @param element
     * @see java.util.ArrayList#add(int, java.lang.Object)
     */
    public void add(int index, String element) {
        fids.add(index, element);
    }

    /**
     * @param fromIndex
     * @param toIndex
     * @return
     * @see java.util.AbstractList#subList(int, int)
     */
    public List<String> subList(int fromIndex, int toIndex) {
        return fids.subList(fromIndex, toIndex);
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#remove(int)
     */
    public String remove(int index) {
        return fids.remove(index);
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return fids.remove(o);
    }

    /**
     * @param o
     * @return
     * @see java.util.AbstractList#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        return fids.equals(o);
    }

    /**
     * 
     * @see java.util.ArrayList#clear()
     */
    public void clear() {
        fids.clear();
    }

    /**
     * @param c
     * @return
     * @see java.util.ArrayList#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends String> c) {
        return fids.addAll(c);
    }

    /**
     * @param index
     * @param c
     * @return
     * @see java.util.ArrayList#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends String> c) {
        return fids.addAll(index, c);
    }

}
