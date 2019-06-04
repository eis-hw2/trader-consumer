package com.example.taskconsumer.Util;

import java.util.HashMap;
import java.util.Map;

public class LRUCache<K, V> {
    private int maxSize;
    private int curSize;
    private Map<K, Node<K, V>> map;
    Node<K, V> head;
    Node<K, V> tail;

    public void print(){
        System.out.print("size: " + curSize + " ");
        Node<K, V> cur = head;
        while(cur.next != tail){
            cur = cur.next;
            System.out.print(cur.key + " " + cur.value + ", ");
        }
        System.out.println();
    }

    public LRUCache(int maxSize){
        this.maxSize = maxSize;
        this.map = new HashMap<>();
        this.head = new Node<>(null, null, null, null);
        this.tail = new Node<>(null, null, head, null);
        head.next = tail;
        curSize = 0;
    }

    class Node<P, Q>{
        Node<P, Q> previous;
        Node<P, Q> next;
        P key;
        Q value;
        Node(P k, Q v, Node<P, Q> p, Node<P, Q> n){
            previous = p;
            next = n;
            key = k;
            value = v;
        }
    }

    public V get(K key){
        //
        Node<K, V> node = this.map.get(key);
        if (node == null)
            return null;
        if (node == head.next)
            return node.value;

        synchronized(this){
            Node<K, V> next = node.next;
            Node<K, V> previous = node.previous;
            next.previous = previous;
            previous.next = next;

            Node<K, V> originalMostRecent = head.next;
            node.previous = head;
            node.next = originalMostRecent;
            head.next = node;
            originalMostRecent.previous = node;
            return node.value;
        }

    }

    public synchronized void put(K key, V value){

        Node<K, V> originalMostRecent = head.next;
        Node<K, V> node = new Node<>(key, value, head, originalMostRecent);
        head.next = node;
        originalMostRecent.previous = node;

        map.put(key, node);
        ++curSize;
        if (curSize > maxSize){
            removeEldest();
        }

    }

    private void removeEldest(){
        Node<K, V> originalLeast = tail.previous;
        map.remove(originalLeast.key);

        Node<K, V> leastRecent = originalLeast.previous;
        leastRecent.next = tail;
        tail.previous = leastRecent;
        --curSize;
    }

    public static void main(String[] args){
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.print();
        cache.put(4, 4);
        cache.get(2);
        cache.print();
        cache.put(5, 5);
        cache.print();
        cache.get(4);
        cache.print();
    }

}
