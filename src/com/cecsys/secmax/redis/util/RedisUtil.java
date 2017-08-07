package com.cecsys.secmax.redis.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 使用jedis操作redis数据库
 * @author lingzg
 *
 */
@Component
public class RedisUtil {
	
	@Autowired
	private JedisPool jedisPool; // jedis链接池

	/**
	 * 对外部提供jedis实例，方便使用jedis对象操作redis
	 * @return
	 */
	public Jedis openJedis(){
		return jedisPool.getResource();
	}
	
	/**
	 * 使用完毕后将jedis实例返回给jedis链接池
	 * @param jedis
	 */
	public void closeJedis(Jedis jedis){
		if(jedis != null){
			jedisPool.returnResource(jedis);
		}
	}
	
//----------------------常规操作------------------------------------
//	常规操作命令  	   
	
//	01  exits key              //测试指定key是否存在，返回1表示存在，0不存在  
//	02  del key1 key2 ....keyN //删除给定key,返回删除key的数目，0表示给定key都不存在  
//	03  type key               //返回给定key的value类型。返回 none 表示不存在key,string字符类型，list 链表类型 set 无序集合类型...  
//	04  keys pattern           //返回匹配指定模式的所有key,下面给个例子  
//	05  randomkey              //返回从当前数据库中随机选择的一个key,如果当前数据库是空的，返回空串  
//	06  rename oldkey newkey   //原子的重命名一个key,如果newkey存在，将会被覆盖，返回1表示成功，0失败。可能是oldkey不存在或者和newkey相同  
//	07  renamenx oldkey newkey //同上，但是如果newkey存在返回失败  
//	08  dbsize                 //返回当前数据库的key数量  
//	09  expire key seconds     //为key指定过期时间，单位是秒。返回1成功，0表示key已经设置过过期时间或者不存在  
//	10  ttl key                //返回设置过过期时间的key的剩余过期秒数 -1表示key不存在或者没有设置过过期时间  
//	11  select db-index        //通过索引选择数据库，默认连接的数据库所有是0,默认数据库数是16个。返回1表示成功，0失败  
//	12  move key db-index      //将key从当前数据库移动到指定数据库。返回1成功。0 如果key不存在，或者已经在指定数据库中  
//	13  flushdb                //删除当前数据库中所有key,此方法不会失败。慎用  
//	14  flushall               //删除所有数据库中的所有key，此方法不会失败。更加慎用 
	
	/**
	 * 手动调用save命令把缓存内容保存到硬盘上
	 * 
	 * @return
	 */
	public boolean save() {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.save();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 是否存在key的记录
	 * @param key
	 * @return
	 */
	public boolean exists(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.exists(key);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 根据key删除缓存中的对象
	 * 
	 * @param key
	 * @return
	 */
	public boolean del(String... key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.del(key);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 根据通配符批量删除缓存中的对象
	 * 參考https://redis.io/commands/keys
	 * 
	 * @param key
	 * @return
	 */
	public boolean delPattern(String pattern) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			Set<String> keys = jedis.keys(pattern);
			String[] s = new String[1];
			jedis.del(keys.toArray(s));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * redis支持的数据类型 ：   
	 * 字符串类型    string  
	 * 散列类型         Hash  
	 * 链表表类型     lists  
	 * 集合类型          sets  
	 * 有序集合类型  zsets  
	 */
//----------------------jedis操作String(字符串类型)------------------------------------
//	string 类型数据操作命令  
	   
//	01  set key value         //设置key对应的值为string类型的value,返回1表示成功，0失败  
//	02  setnx key value       //同上，如果key已经存在，返回0 。nx 是not exist的意思  
//	03  get key               //获取key对应的string值,如果key不存在返回nil  
//	04  getset key value      //原子的设置key的值，并返回key的旧值。如果key不存在返回nil  
//	05  mget key1 key2 ... keyN            //一次获取多个key的值，如果对应key不存在，则对应返回nil。下面是个实验,首先清空当前数据库，然后设置k1,k2.获取时k3对应返回nil  
//	06  mset key1 value1 ... keyN valueN   //一次设置多个key的值，成功返回1表示所有的值都设置了，失败返回0表示没有任何值被设置  
//	07  msetnx key1 value1 ... keyN valueN //同上，但是不会覆盖已经存在的key  
//	08  incr key              //对key的值做加加操作,并返回新的值。注意incr一个不是int的value会返回错误，incr一个不存在的key，则设置key为1  
//	09  decr key              //同上，但是做的是减减操作，decr一个不存在key，则设置key为-1  
//	10  incrby key integer    //同incr，加指定值 ，key不存在时候会设置key，并认为原来的value是 0  
//	11  decrby key integer    //同decr，减指定值。decrby完全是为了可读性，我们完全可以通过incrby一个负值来实现同样效果，反之一样。  
//	12  append key value      //给指定key的字符串值追加value,返回新字符串值的长度。下面给个例子  
//	13  substr key start end  //返回截取过的key的字符串值,注意并不修改key的值。下标是从0开始的，接着上面例子  
	
	/**
	 * 向缓存中设置字符串内容
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @return
	 * @throws Exception
	 */
	public boolean set(String key, String value) throws Exception {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String s = jedis.set(key, value);
			return "1".equals(s);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 向缓存中设置对象
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key, Object value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String objectJson = JSON.toJSONString(value);
			String s = jedis.set(key, objectJson);
			return "1".equals(s);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 根据key 获取内容
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String value = jedis.get(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 根据key 获取对象
	 * 
	 * @param key
	 * @return
	 */
	public <T> T get(String key, Class<T> clazz) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String value = jedis.get(key);
			return JSON.parseObject(value, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 缓存多个值
	 * 一个key一个value成对的
	 * @param keysvalues
	 * @return
	 */
	public boolean mset(String... keysvalues) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String s = jedis.mset(keysvalues);
			return "1".equals(s);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 获取多个缓存的值
	 * @param keys
	 * @return
	 */
	public List<String> mget(String... keys) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.mget(keys);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
//-----------------------jedis操作hash(散列类型)------------------------------------
//	hash 类型数据操作命令  
//	   
//	01  hset key field value       //设置hash field为指定值，如果key不存在，则先创建  
//	02  hget key field             //获取指定的hash field  
//	03  hmget key filed1....fieldN //获取全部指定的hash filed  
//	04  hmset key filed1 value1 ... filedN valueN //同时设置hash的多个field  
//	05  hincrby key field integer  //将指定的hash filed 加上给定值  
//	06  hexists key field          //测试指定field是否存在  
//	07  hdel key field             //删除指定的hash field  
//	08  hlen key                   //返回指定hash的field数量  
//	09  hkeys key                  //返回hash的所有field  
//	10  hvals key                  //返回hash的所有value  
//	11  hgetall                    //返回hash的所有filed和value  
	
	/**
	 * 缓存Map
	 * @param key
	 * @param hash 保存的Map
	 * @return
	 */
	public boolean hmset(String key,Map<String, String> hash) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.hmset(key, hash);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 获取缓存的Map中多个键值
	 * @param key
	 * @param fields Map的key
	 * @return
	 */
	public List<String> hmget(String key,String... fields) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.hmget(key, fields);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 返回缓存的Map中存放的键值的个数
	 * @param key
	 * @return
	 */
	public Long hlen(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.hlen(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 返回缓存的Map的所有key
	 * @param key
	 * @return
	 */
	public Set<String> hkeys(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.hkeys(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 返回缓存的Map的所有value
	 * @param key
	 * @return
	 */
	public List<String> hvals(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.hvals(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
//----------------------jedis操作List(链表类型)------------------------------------
//	list 类型数据操作命令  
//	   
//	01  lpush key string          //在key对应list的头部添加字符串元素，返回1表示成功，0表示key存在且不是list类型  
//	02  rpush key string          //同上，在尾部添加  
//	03  llen key                  //返回key对应list的长度，key不存在返回0,如果key对应类型不是list返回错误  
//	04  lrange key start end      //返回指定区间内的元素，下标从0开始，负值表示从后面计算，-1表示倒数第一个元素 ，key不存在返回空列表  
//	05  ltrim key start end       //截取list，保留指定区间内元素，成功返回1，key不存在返回错误  
//	06  lset key index value      //设置list中指定下标的元素值，成功返回1，key或者下标不存在返回错误  
//	07  lrem key count value      //从key对应list中删除count个和value相同的元素。count为0时候删除全部  
//	08  lpop key                  //从list的头部删除元素，并返回删除元素。如果key对应list不存在或者是空返回nil，如果key对应值不是list返回错误  
//	09  rpop                      //同上，但是从尾部删除  
//	10  blpop key1...keyN timeout //从左到右扫描返回对第一个非空list进行lpop操作并返回，比如blpop list1 list2 list3 0 ,如果list不存在list2,list3都是非空则对list2做lpop并返回从list2中删除的元素。如果所有的list都是空或不存在，则会阻塞timeout秒，timeout为0表示一直阻塞。当阻塞时，如果有client对key1...keyN中的任意key进行push操作，则第一在这个key上被阻塞的client会立即返回。如果超时发生，则返回nil。有点像unix的select或者poll  
//	11  brpop                     //同blpop，一个是从头部删除一个是从尾部删除  
//	12  rpoplpush srckey destkey  //从srckey对应list的尾部移除元素并添加到destkey对应list的头部,最后返回被移除的元素值，整个操作是原子的.如果srckey是空或者不存在返回nil  
	
	/**
	 * 在key对应list的头部添加字符串元素
	 * @param key
	 * @param strings
	 * @return
	 */
	public boolean lpush(String key,String... strings) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			Long l = jedis.lpush(key, strings);
			return l==1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 在key对应list的尾部添加字符串元素
	 * @param key
	 * @param strings
	 * @return
	 */
	public boolean rpush(String key,String... strings) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			Long l = jedis.rpush(key, strings);
			return l==1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 返回指定区间内的元素，下标从0开始，负值表示从后面计算，-1表示倒数第一个元素 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public List<String> lrange(String key,long start, long end) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.lrange(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 返回所有元素
	 * @param key
	 * @return
	 */
	public List<String> lrange(String key) {
		return lrange(key, 0, -1);
	}
	
	
//----------------------jedis操作Set(集合类型)------------------------------------
//	set 类型数据操作命令  
//	   
//	01  sadd key member                //添加一个string元素到,key对应的set集合中，成功返回1,如果元素以及在集合中返回0,key对应的set不存在返回错误  
//	02  srem key member                //从key对应set中移除给定元素，成功返回1，如果member在集合中不存在或者key不存在返回0，如果key对应的不是set类型的值返回错误  
//	03  spop key                       //删除并返回key对应set中随机的一个元素,如果set是空或者key不存在返回nil  
//	04  srandmember key                //同spop，随机取set中的一个元素，但是不删除元素  
//	05  smove srckey dstkey member     //从srckey对应set中移除member并添加到dstkey对应set中，整个操作是原子的。成功返回1,如果member在srckey中不存在返回0，如果key不是set类型返回错误  
//	06  scard key                      //返回set的元素个数，如果set是空或者key不存在返回0  
//	07  sismember key member           //判断member是否在set中，存在返回1，0表示不存在或者key不存在  
//	08  sinter key1 key2...keyN        //返回所有给定key的交集  
//	09  sinterstore dstkey key1...keyN //同sinter，但是会同时将交集存到dstkey下  
//	10  sunion key1 key2...keyN        //返回所有给定key的并集  
//	11  sunionstore dstkey key1...keyN //同sunion，并同时保存并集到dstkey下  
//	12  sdiff key1 key2...keyN         //返回所有给定key的差集  
//	13  sdiffstore dstkey key1...keyN  //同sdiff，并同时保存差集到dstkey下  
//	14  smembers key                   //返回key对应set的所有元素，结果是无序的  
	
	/**
	 * 添加string元素到key对应的set集合中
	 * @param key
	 * @param strings
	 * @return
	 */
	public boolean sadd(String key,String... strings) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			Long l = jedis.sadd(key, strings);
			return l==1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 从key对应set中移除给定元素
	 * @param key
	 * @param strings
	 * @return
	 */
	public boolean srem(String key,String... strings) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			Long l = jedis.srem(key, strings);
			return l==1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 判断member是否在set中
	 * @param key
	 * @param member
	 * @return
	 */
	public boolean sismember(String key,String member) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.sismember(key, member);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 返回key对应set的所有元素，结果是无序的  
	 * @param key
	 * @return
	 */
	public Set<String> smembers(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.smembers(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
//----------------------jedis操作ZSets(有序集合类型 )------------------------------------
//	sorted set 类型数据操作命令  
//	   
//	01  zadd key score member        //添加元素到集合，元素在集合中存在则更新对应score  
//	02  zrem key member              //删除指定元素，1表示成功，如果元素不存在返回0  
//	03  zincrby key incr member      //增加对应member的score值，然后移动元素并保持skip list保持有序。返回更新后的score值  
//	04  zrank key member             //返回指定元素在集合中的排名（下标）,集合中元素是按score从小到大排序的  
//	05  zrevrank key member          //同上,但是集合中元素是按score从大到小排序  
//	06  zrange key start end         //类似lrange操作从集合中去指定区间的元素。返回的是有序结果  
//	07  zrevrange key start end      //同上，返回结果是按score逆序的  
//	08  zrangebyscore key min max    //返回集合中score在给定区间的元素  
//	09  zcount key min max           //返回集合中score在给定区间的数量  
//	10  zcard key                    //返回集合中元素个数  
//	11  zscore key element           //返回给定元素对应的score  
//	12  zremrangebyrank key min max  //删除集合中排名在给定区间的元素  
//	13  zremrangebyscore key min max //删除集合中score在给定区间的元素  
	
	/**
	 * 添加元素到集合，元素在集合中存在则更新对应score  
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 */
	public boolean zadd(String key,double score,String member) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			long l = jedis.zadd(key, score, member);
			return l==1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 删除指定元素，1表示成功，如果元素不存在返回0  
	 * @param key
	 * @param member
	 * @return
	 */
	public boolean zrem(String key,String member) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			long l = jedis.zrem(key, member);
			return l==1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 增加对应member的score值，然后移动元素并保持skip list保持有序。返回更新后的score值  
	 * @param key
	 * @param incr
	 * @param member
	 * @return
	 */
	public Double zincrby(String key,double incr,String member) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			Double score =  jedis.zincrby(key, incr, member);
			return score;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 返回指定元素在集合中的排名（下标）,集合中元素是按score从小到大排序的  
	 * @param key
	 * @param member
	 * @return
	 */
	public Long zrank(String key,String member) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			Long index = jedis.zrank(key, member);
			return index;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 类似lrange操作从集合中去指定区间的元素。返回的是有序结果  
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<String> zrange(String key, long start, long end) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.zrange(key, end, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
}
