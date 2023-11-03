//package com.github.houbb.cache.core;
//
//import com.github.houbb.heaven.support.tuple.impl.Pair;
//import com.github.houbb.heaven.util.lang.ObjectUtil;
//import com.github.houbb.log.integration.core.Log;
//import com.github.houbb.log.integration.core.LogFactory;
//
//import java.util.*;
//
//public class MyProgressiveReHashMap<K,V> extends AbstractMap<K,V> implements Map<K,V> {
//
//    private static final Log log = LogFactory.getLog(MyProgressiveReHashMap.class);
//
//    /**
//     * rehash 的下标
//     *
//     * 如果 rehashIndex != -1，说明正在进行 rehash
//     * @since 0.0.3
//     * @author binbin.hou
//     */
//    private int rehashIndex = -1;
//
//    /**
//     * 容量
//     * 默认为 8
//     */
//    private int capacity;
//
//    /**
//     * 处于 rehash 状态的容量
//     * @since 0.0.3
//     */
//    private int rehashCapacity;
//
//    /**
//     * 统计大小的信息
//     */
//    private int size = 0;
//
//    /**
//     * 阈值
//     * 阈值=容量*factor
//     * 暂时不考虑最大值的问题
//     *
//     * 当达到这个阈值的时候，直接进行两倍的容量扩充+rehash。
//     */
//    private final double factor = 1.0;
//
//    /**
//     * 用来存放信息的 table 数组。
//     * 数组：数组的下标是一个桶，桶对应的元素 hash 值相同。
//     * 桶里放置的是一个链表。
//     *
//     * 可以理解为 table 是一个 ArrayList
//     * arrayList 中每一个元素，都是一个 DoubleLinkedList
//     */
//    private List<List<Entry<K, V>>> table;
//
//    /**
//     * 渐进式 rehash 时，用来存储元素信息使用。
//     *
//     * @since 0.0.3
//     */
//    private List<List<Entry<K, V>>> rehashTable;
//
//    /**
//     * 是否开启 debug 模式
//     * @since 0.0.3
//     */
//    private boolean debugMode = false;
//
//    public MyProgressiveReHashMap() {
//        this(8);
//    }
//
//    /**
//     * 初始化 hash map
//     * @param capacity 初始化容量
//     */
//    public MyProgressiveReHashMap(int capacity) {
//        this(capacity, false);
//    }
//
//    /**
//     * 初始化 hash map
//     * @param capacity 初始化容量
//     * @param debugMode 是否开启 debug 模式
//     * @since 0.0.3
//     * @author binbin.hou
//     */
//    public MyProgressiveReHashMap(int capacity, boolean debugMode) {
//        this.capacity = capacity;
//        // 初始化最大为容量的个数，如果 hash 的非常完美的话。
//        this.table = new ArrayList<>(capacity);
//        // 初始化为空列表
//        for(int i = 0; i < capacity; i++) {
//            this.table.add(i, new ArrayList<Entry<K, V>>());
//        }
//        this.debugMode = debugMode;
//        this.rehashIndex = -1;
//        this.rehashCapacity = -1;
//        this.rehashTable = null;
//    }
//
//    /**
//     * put 一个值
//     *
//     * （1）如果不处于 rehash 阶段
//     *
//     * 1.1 判断是否为 table 更新，如果是，则进行更新
//     * 1.2 如果不是更新，则进行插入
//     *
//     *  插入的时候可能触发 rehash
//     *
//     * （2）如果处于 rehash 阶段
//     *
//     * 2.0 执行一次渐进式 rehash 的动作
//     *
//     * 2.1 判断是否为更新，需要遍历 table 和 rehashTable
//     * 如果是，执行更新
//     *
//     * 2.2 如果不是，则执行插入
//     * 插入到 rehashTable 中
//     *
//     * @param key 键
//     * @param value 值
//     * @return 值
//     * @author binbin.hou
//     */
//    @Override
//    public V put(K key, V value) {
//        boolean isInRehash = isInReHash();
//        if(!isInRehash) {
//            //1. 是否为更新
//            Pair<Boolean, V> pair = updateTableInfo(key, value, this.table, this.capacity);
//            if(pair.getValueOne()) {
//                V oldVal = pair.getValueTwo();
//                if(debugMode) {
//                    log.debug("不处于渐进式 rehash，此次为更新操作。key: {}, value: {}", key, value);
//                    printTable(this.table);
//                }
//                return oldVal;
//            } else {
//                // 插入
//                return this.createNewEntry(key, value);
//            }
//        } else {
//            //2.0 执行一个附加操作，进行渐进式 rehash 处理
//            if(debugMode) {
//                log.debug("当前处于渐进式 rehash 阶段，额外执行一次渐进式 rehash 的动作");
//            }
//            rehashToNew();
//            //2.1 是否为 table 更新
//            Pair<Boolean, V> pair = updateTableInfo(key, value, this.table, this.capacity);
//            if(pair.getValueOne()) {
//                V oldVal = pair.getValueTwo();
//                if(debugMode) {
//                    log.debug("此次为更新 table 操作。key: {}, value: {}", key, value);
//                    printTable(this.table);
//                }
//                return oldVal;
//            }
//            //2.2 是否为 rehashTable 更新
//            Pair<Boolean, V> pair2 = updateTableInfo(key, value, this.rehashTable, this.rehashCapacity);
//            if(pair2.getValueOne()) {
//                V oldVal = pair2.getValueTwo();
//                if(debugMode) {
//                    log.debug("此次为更新 rehashTable 操作。key: {}, value: {}", key, value);
//                    printTable(this.table);
//                }
//                return oldVal;
//            }
//            //2.3 插入
//            return this.createNewEntry(key, value);
//        }
//    }
//
//    @Override
//    public Set<Entry<K, V>> entrySet() {
//        return null;
//    }
//
//    private Pair<Boolean, V> updateTableInfo(K key, V value, final List<List<Entry<K,V>>> table,
//                                             final int tableCapacity) {
//        // 计算 index 值
//        int hash = HashUtil.hash(key);
//        int index = HashUtil.indexFor(hash, tableCapacity);
//        // 判断是否为替换
//        List<Entry<K,V>> entryList = new ArrayList<>();
//        if(index < table.size()) {
//            entryList = table.get(index);
//        }
//        // 遍历
//        for(Entry<K,V> entry : entryList) {
//            // 二者的 key 都为 null，或者二者的 key equals()
//            final K entryKey = entry.getKey();
//            if(ObjectUtil.isNull(key, entryKey)
//                    || key.equals(entryKey)) {
//                final V oldValue = entry.getValue();
//                // 更新新的 value
//                entry.setValue(value);
//                return Pair.of(true, oldValue);
//            }
//        }
//        return Pair.of(false, null);
//    }
//
//    private boolean isInReHash() {
//        return rehashIndex != -1;
//    }
//    /**
//     * 创建一个新的明细
//     *
//     * （1）如果处于渐进式 rehash 中，则设置到 rehashTable 中
//     * （2）如果不是，则判断是否需要扩容
//     *
//     * 2.1 如果扩容，则直接放到 rehashTable 中。
//     * 因为我们每次扩容内存翻倍，一次只处理一个 index 的信息，所以不会直接 rehash 结束，直接放到新的 rehashTable 中即可
//     * 2.2 如果不扩容，则放入 table 中
//     */
//    private V createNewEntry(final K key,
//                             final V value) {
//        Entry<K,V> entry = new DefaultMapEntry<>(key, value);
//        // 重新计算 tableIndex
//        int hash = HashUtil.hash(key);
//        //是否处于 rehash 中？
//        if(isInReHash()) {
//            int index = HashUtil.indexFor(hash, this.rehashCapacity);
//            List<Entry<K,V>> list = this.rehashTable.get(index);
//            list.add(entry);
//            if(debugMode) {
//                log.debug("目前处于 rehash 中，元素直接插入到 rehashTable 中。");
//                printTable(this.rehashTable);
//            }
//        }
//        // 是否需要扩容 && 不处于渐进式 rehash
//        // rehash 一定是扩容 rehashTable
//        // 如果发生了 rehash，元素是直接放到 rehashTable 中的
//        if(isNeedExpand()) {
//            rehash();
//            // 放入到 rehashTable 中
//            int index = HashUtil.indexFor(hash, this.rehashCapacity);
//            List<Entry<K,V>> list = this.rehashTable.get(index);
//            list.add(entry);
//            if(debugMode) {
//                log.debug("目前处于 rehash 中，元素直接插入到 rehashTable 中。");
//                printTable(this.rehashTable);
//            }
//        } else {
//            int index = HashUtil.indexFor(hash, this.capacity);
//            List<Entry<K,V>> list = this.table.get(index);
//            list.add(entry);
//            if(debugMode) {
//                log.debug("目前不处于 rehash 中，元素直接插入到 table 中。");
//                printTable(this.table);
//            }
//        }
//        this.size++;
//        return value;
//    }
//    /**
//     * 是否需要扩容
//     *
//     * 比例满足，且不处于渐进式 rehash 中
//     * @return 是否
//     * @since 0.0.3
//     * @author binbin.hou
//     */
//    private boolean isNeedExpand() {
//        // 验证比例
//        double rate = size*1.0 / capacity*1.0;
//        return rate >= factor && !isInReHash();
//    }
//    /**
//     * 直接 rehash 的流程
//     *
//     * （1）如果处于 rehash 中，直接返回
//     * （2）初始化 rehashTable，并且更新 rehashIndex=0;
//     * （3）获取 table[0]，rehash 到 rehashTable 中
//     * （4）设置 table[0] = new ArrayList();
//     */
//    private void rehash() {
//        if(isInReHash()) {
//            if(debugMode) {
//                log.debug("当前处于渐进式 rehash 阶段，不重复进行 rehash!");
//            }
//            return;
//        }
//        // 初始化 rehashTable
//        this.rehashIndex = -1;
//        this.rehashCapacity = 2*capacity;
//        this.rehashTable = new ArrayList<>(this.rehashCapacity);
//        for(int i = 0; i < rehashCapacity; i++) {
//            rehashTable.add(i, new ArrayList<Entry<K, V>>());
//        }
//        // 遍历元素第一个元素，其他的进行渐进式更新。
//        rehashToNew();
//    }
//
//    /**
//     * 将信息从旧的 table 迁移到新的 table 中
//     *
//     * （1）table[rehashIndex] 重新 rehash 到 rehashTable 中
//     * （2）设置 table[rehashIndex] = new ArrayList();
//     * （3）判断是否完成渐进式 rehash
//     */
//    private void rehashToNew() {
//        rehashIndex++;
//
//        List<Entry<K, V>> list = table.get(rehashIndex);
//        for(Entry<K, V> entry : list) {
//            int hash = HashUtil.hash(entry);
//            int index = HashUtil.indexFor(hash, rehashCapacity);
//            //  添加元素
//            // 获取列表，避免数组越界
//            List<Entry<K,V>> newList = rehashTable.get(index);
//            // 添加元素到列表
//            // 元素不存在重复，所以不需要考虑更新
//            newList.add(entry);
//            rehashTable.set(index, newList);
//        }
//
//        // 清空 index 处的信息
//        table.set(rehashIndex, new ArrayList<Entry<K, V>>());
//
//        // 判断大小是否完成 rehash
//        // 验证是否已经完成
//        if(rehashIndex == (table.size()-1)) {
//            this.capacity = this.rehashCapacity;
//            this.table = this.rehashTable;
//            this.rehashIndex = -1;
//            this.rehashCapacity = -1;
//            this.rehashTable = null;
//            if(debugMode) {
//                log.debug("渐进式 rehash 已经完成。");
//                printTable(this.table);
//            }
//        } else {
//            if(debugMode) {
//                log.debug("渐进式 rehash 处理中, 目前 index：{} 已完成", rehashIndex);
//                printAllTable();
//            }
//        }
//    }
//
//    /**
//     * 查询方法
//     * （1）如果处于渐进式 rehash 状态，额外执行一次 rehashToNew()
//     * （2）判断 table 中是否存在元素
//     * （3）判断 rehashTable 中是否存在元素
//     * @param key key
//     * @return 结果
//     */
//    @Override
//    public V get(Object key) {
//        if(isInReHash()) {
//            if(debugMode) {
//                log.debug("当前处于渐进式 rehash 状态，额外执行一次操作");
//                rehashToNew();
//            }
//        }
//
//        //1. 判断 table 中是否存在
//        V result = getValue(key, this.table);
//        if(result != null) {
//            return result;
//        }
//
//        //2. 是否处于渐进式 rehash
//        if(isInReHash()) {
//            return getValue(key, this.rehashTable);
//        }
//        return null;
//    }
//}
