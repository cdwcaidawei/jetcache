/**
 * Created on  13-09-12 19:02
 */
package com.alicp.jetcache.embedded;

import java.util.*;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCache<K, V> extends AbstractEmbeddedCache<K, V> {


    public LinkedHashMapCache(EmbeddedCacheConfig config) {
        super(config);
    }

    @Override
    protected InnerMap createAreaCache() {
        return new LRUMap(config.getLimit(), this);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(LinkedHashMap.class)) {
            return (T) innerMap;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    private static final class LRUMap extends LinkedHashMap implements InnerMap {

        private final int max;
        private Object lock;

        public LRUMap(int max, Object lock) {
            super((int) (max * 1.4f), 0.75f, true);
            this.max = max;
            this.lock = lock;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > max;
        }

        public Object getValue(Object key) {
            synchronized (lock) {
                return get(key);
            }
        }

        @Override
        public Map getAllValues(Collection keys) {
            Map values = new HashMap();
            synchronized (lock) {
                for (Object key : keys) {
                    Object v = get(key);
                    if (v != null) {
                        values.put(key, v);
                    }
                }
            }
            return values;
        }

        public void putValue(Object key, Object value) {
            synchronized (lock) {
                put(key, value);
            }
        }

        @Override
        public void putAllValues(Map map) {
            synchronized (lock){
                Set<Map.Entry> set = map.entrySet();
                for (Map.Entry en : set){
                    put(en.getKey(), en.getValue());
                }
            }
        }

        @Override
        public boolean removeValue(Object key) {
            synchronized (lock) {
                return remove(key) != null;
            }
        }

        @Override
        public void removeAllValues(Collection keys) {
            synchronized (lock) {
                for(Object k: keys){
                    remove(k);
                }
            }
        }

        @Override
        public boolean putIfAbsentValue(Object key, Object value) {
            synchronized (lock) {
                if (get(key) == null) {
                    put(key, value);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }


}

