package hub.gateway.mgr

import java.util.concurrent.*

/**
 * 热对象缓冲
 */
class HotCache<K, V>(val max:Int = 1024*4) {
    private val _cache: ConcurrentMap<K, CacheRecord<V>> = ConcurrentHashMap<K,CacheRecord<V>>()

    /**
     * 从缓冲中尝试检索对象
     * 有可能返回空值
     */
    fun get(key: K):V?{
        return _cache.get(key)?.value
    }

    /**
     * 加载对象
     * 先从缓冲区中加载对象
     * 如果失败,从另外的源加载
     * 如果从另外的源加载成功,把对象缓冲
     */
    fun get(key: K, fnSrc:(key:K)->V?):V?{
        var v = this.get(key)
        if(v == null){
            v = fnSrc(key)
            if(v != null) this.put(key, v)
        }
        return v
    }

    /**
     * 把对象存入缓冲区中
     * 如果缓冲区已满,会把最久没使用的对象移出
     */
    fun put(key:K, value:V){
        if(_cache.size >= max){
            var minTS = System.currentTimeMillis()
            var minKey:K? = null
            _cache.keys.forEach(){ k->
                val c = _cache[k]!!
                if(c.lastVisit < minTS){
                    minTS = c.lastVisit
                    minKey = k
                }
            }

            if(key != minKey)
                _cache.remove(minKey)
        }

        _cache.putIfAbsent(key, CacheRecord(value))
    }

    /**
     * 清除一条记录
     */
    fun remove(key:K){
        _cache.remove(key)
    }
}

/**
 * 缓冲记录
 */
class CacheRecord<V>(v: V){
    val value = v
        get(){
            lastVisit = System.currentTimeMillis()
            hitted++
            return field
        }

    // 最后访问时间戳
    var lastVisit = System.currentTimeMillis()
        private set

    // 命中计数器
    var hitted = 0
        private set
}