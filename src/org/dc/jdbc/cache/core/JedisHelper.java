package org.dc.jdbc.cache.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.entity.SqlEntity;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
/**
 * reids缓存操作
 * @author DC
 * @time 2016-05-16
 */
public class JedisHelper {
	private static final int EXPIRE_TIME = 365*24*60*60;
	public static String DATA_KEY = "data";
	public static String DATASOURCE_KEY = "dataSource";

	private static JedisHelper jedisHelper = new JedisHelper(JedisConfig.defaultJedisPool);

	public static JedisHelper getInstance(){
		return jedisHelper;
	}

	private static final Log log = LogFactory.getLog(JedisHelper.class);
	private volatile JedisPool jedisPool;

	public JedisHelper(JedisPool jedisPool){
		this.jedisPool = jedisPool;
	}
	public <T> T getSQLCache(String sqlKey) throws Exception{
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			byte[] obj_bytes = jedis.hget(sqlKey.getBytes(),DATA_KEY.getBytes());
			if(obj_bytes!=null){
				return SerializationUtils.deserialize(obj_bytes);
			}
		} catch (Exception e) {  
			log.error("",e);
		} finally {
			//返还到连接池  
			jedis.close();
		}
		return null;  
	}
	public void setSQLCache(String sqlKey,Object value){
		Jedis jedis = null;
		Transaction t = null;
		try {  
			jedis = jedisPool.getResource();
			t = jedis.multi();

			boolean isPersistent = false;//是否持久化

			String conf_tableNameStr = JedisConfig.properties.getProperty("tableCache");
			
			Set<String>  sql_table = ConnectionManager.entityLocal.get().getTables();
			if(conf_tableNameStr!=null){
				int i = 0;
				String[] conf_tables = conf_tableNameStr.split(",");
				for (String conf_tableName : conf_tables) {
					if(sql_table.contains(conf_tableName)){
						i++;
					}
				}
				if(i==sql_table.size()){
					isPersistent = true;
				}
			}
			for (String tableName : ConnectionManager.entityLocal.get().getTables()) {
				t.sadd(tableName, sqlKey);
			}
			if(isPersistent){
				t.set(sqlKey.getBytes(), SerializationUtils.serialize((Serializable) value));
			}else{
				t.setex(sqlKey.getBytes(),EXPIRE_TIME,SerializationUtils.serialize((Serializable) value));
			}
			t.exec();
		} catch (Exception e) {
			log.error("",e);
		} finally {
			try {
				t.close();
			} catch (IOException e) {
				log.error("",e);
			}
			//返还到连接池  
			jedis.close();
		}
	}
	public void delSQLCache(String sqlKey){
		Jedis jedis = null;
		Transaction t = null;
		try {  
			jedis = jedisPool.getResource();
			List<String> sqlKeyList = new ArrayList<String>();
			for (String tableName : ConnectionManager.entityLocal.get().getTables()) {
				Set<String> sqlkeySet = jedis.smembers(tableName);
				if(sqlkeySet!=null){
					for (String sql : sqlkeySet) {
						if(jedis.exists(sql)){
							sqlKeyList.add(sql);
						}
					}
				}
			}
			t = jedis.multi();
			for (String key : sqlKeyList) {
				//Storage ss = Storage.getInstance();
				//ss.push(key);
				t.del(key);
			}
			for (String tableName : ConnectionManager.entityLocal.get().getTables()) {
				t.del(tableName);
			}
			t.exec();
		} catch (Exception e) {
			log.error("",e);
		} finally {
			try {
				if(t!=null){
					t.close();
				}
			} catch (Exception e) {
				log.error("",e);
			}
			//返还到连接池  
			jedis.close();
		}
	}

	public String getSQLKey(SqlEntity sqlEntity,DataSource dataSource){
		Object[] params_obj = sqlEntity.getParams();
		StringBuilder params = new StringBuilder();
		for (int i = 0; i < params_obj.length; i++) {
			params.append(String.valueOf(params_obj[i]));
		}
		return sqlEntity.getSql()+params.toString()+dataSource.hashCode();
	}
	public Set<String> getKeys(String key){
		Jedis jedis = null;  
		try {
			jedis = jedisPool.getResource();
			return jedis.keys(key);
		} catch (Exception e) {  
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public String get(String key){  

		Jedis jedis = null;  
		try {  
			jedis = jedisPool.getResource();
			return jedis.get(key);  
		} catch (Exception e) {  
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public <T>  T getObject(String key){
		Jedis jedis = null;
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {  
			jedis = jedisPool.getResource();
			byte[] obj_bytes = jedis.get(key.getBytes());
			if(obj_bytes!=null){
				bais = new ByteArrayInputStream(obj_bytes);
				ois = new ObjectInputStream(bais);
				return (T) ois.readObject();
			}
		} catch (Exception e) {  
			log.error("",e);
		} finally {
			if(ois!=null){
				try {
					ois.close();
					ois = null;
				} catch (IOException e) {
					log.error("",e);
				}
			}
			if(bais!=null){
				try {
					bais.close();
					bais = null;
				} catch (IOException e) {
					log.error("",e);
				}
			}
			//返还到连接池  
			jedis.close();
		}
		return null;  
	}
	public String set(String key,String value){  
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.set(key, value);
		} catch (Exception e) {  
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	/**
	 * 存储字节数组
	 * @param key
	 * @param value
	 * @return
	 */
	public String setObject(String key,Object value){  
		Jedis jedis = null;
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {  
			jedis = jedisPool.getResource();


			//序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.flush();
			baos.flush();

			return jedis.set(key.getBytes(), baos.toByteArray());
		} catch (Exception e) {
			log.error("",e);
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				log.error("",e);
			}
			try {
				baos.close();
			} catch (IOException e) {
				log.error("",e);
			}
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public Long delObject(byte[]...keys){
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.del(keys);
		} catch (Exception e) {  
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public Long del(String...keys){  
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.del(keys);
		} catch (Exception e) {
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public String hmset(String key,Map<String,String> valueMap){  
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.hmset(key,valueMap);
		} catch (Exception e) {
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public List<String> hmget(String key,String...fields){  
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.hmget(key, fields);
		} catch (Exception e) {
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public Long sadd(String key,String...value){
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.sadd(key, value);
		} catch (Exception e) {
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public Set<String> smembers(String key){
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.smembers(key);
		} catch (Exception e) {
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
}
