package com.github.houbb.cache.core.mytest;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCache<K, V> implements Serializable {
    private final int capacity;
    private final Map<K, Node<K, V>> hotMap;
    private final Map<K, Node<K, V>> coldMap;
    private Node<K, V> head;
    private Node<K, V> tail;
    private Node<K, V> point;
    final float LOAD_FACTOR=0.375F;
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.hotMap = new ConcurrentHashMap<>((int) (1-LOAD_FACTOR)*capacity);
        this.coldMap = new ConcurrentHashMap<>((int) LOAD_FACTOR*capacity); // 3/8 * capacity
        head = new Node(0, 0);
        tail = new Node(0, 0);
        point = new Node(0, 0);
        head.next=point;
        point.next=tail;
        tail.prev=point;
        point.prev=head;
    }
    public void remove(K key) {
        Node<K, V> coldNode = coldMap.get(key);
        if (coldNode != null) {
            removeNode(coldNode);
            coldMap.remove(key);
        }else {
            Node<K, V> hotNode = hotMap.get(key);
            if(hotNode != null){
                removeNode(hotNode);
                hotMap.remove(key);
            }
        }
    }
    public void clear() {
        head.next=point;
        point.next=tail;
        tail.prev=point;
        point.prev=head;
        hotMap.clear();
        coldMap.clear();
    }
    public void PrintVaule(){
        Node cur=head;
        while(cur!=null){
            if(cur.next==null)
                System.out.print(cur.value);
            else
                System.out.print(cur.value+"->");
            cur=cur.next;
        }
        System.out.println();
    }
    public V get(K key) {
        Node<K, V> hotNode = hotMap.get(key);
        if (hotNode == null) {
            Node<K, V> coldNode = coldMap.get(key);
            if(coldNode == null)
                return null;
            //访问了第二次
            moveToHotHead(coldNode);
            hotMap.put(coldNode.key,coldNode);
            coldMap.remove(coldNode.key);
            return coldNode.value;
        }
        moveToHotHead(hotNode);
        return hotNode.value;
    }
    //put操作只发生在cold链表，当get时，访问了第二次 判断
    public void put(K key, V value) {
        Node<K, V> node = coldMap.get(key);
        if (node == null) {
            node = new Node<>(key, value);
            coldMap.put(key, node);
            addToColdHead(node); //始终保持有节点插入时，未满
            if (coldMap.size() >= LOAD_FACTOR * capacity) {
                removeColdTail();
            }
        } else {
            node.value = value;
            moveToColdHead(node);
        }
    }
    private void addToHotHead(Node<K, V> node) {
        node.prev=head;
        node.next=head.next;
        head.next.prev=node;
        head.next=node;
    }
    //point
    private void addToColdHead(Node<K, V> node) {
        node.prev=point;
        node.next=point.next;
        point.next.prev=node;
        point.next=node;
    }

    private void removeNode(Node<K, V> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    private void moveToHotHead(Node<K, V> node) {
        removeNode(node);
        addToHotHead(node);
        //热链表满
        if (hotMap.size() >= capacity - LOAD_FACTOR * capacity)  //10 - 3.75 =6.25
            removeHotTail();
    }
    private void moveToColdHead(Node<K, V> node) {
        removeNode(node);
        addToColdHead(node);
    }

    private void removeHotTail() {
        hotMap.remove(point.prev.key);
        removeNode(point.prev);
    }
    private void removeColdTail() {
        coldMap.remove(tail.prev.key);
        removeNode(tail.prev);
    }
    private static class Node<K, V> implements Serializable{
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}

