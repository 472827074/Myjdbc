package org.dc.jdbc.core;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.core.operate.DataBaseDaoImp;
import org.dc.jdbc.core.operate.IDataBaseDao;
import org.dc.jdbc.core.proxy.DataBaseOperateProxy;
import org.dc.jdbc.exceptions.TooManyResultsException;

/**
 * 数据持久化操作类
 * sql执行步骤：1，sql解析，2，获得数据库连接，3，执行核心jdbc操作。
 * @author dc
 * @time 2015-8-17
 */
public class DBHelper {
	private volatile DataSource dataSource;

	private static final Log LOG = LogFactory.getLog(DBHelper.class);
	private static final IDataBaseDao dataBaseDaoProxy = (IDataBaseDao) new DataBaseOperateProxy(DataBaseDaoImp.getInstance()).getProxy();
	public DBHelper(DataSource dataSource){
		this.dataSource = dataSource;
	}
	public <T> T selectOneEntity(Object entity) throws Exception{
		return this.selectOneEntity(entity,null,new Object[]{});
	}
	public <T> T selectOneEntity(Object entity,String whereSql) throws Exception{
		return this.selectOneEntity(entity,whereSql,new Object[]{});
	}
	public <T> T selectOneEntity(Object entity,String whereSql,Object...params) throws Exception{
		List<T> list = this.selectEntity(entity, whereSql,params);
		if(list==null){
			return null;
		}else if(list.size()>1){
			throw new TooManyResultsException(list.size());
		}else{
			return list.get(0);
		}
	}
	public <T> List<T> selectEntity(Object entity) throws Exception{
		return this.selectEntity(entity, null,new Object[]{});
	}
	public <T> List<T> selectEntity(Object entity,String whereSql) throws Exception{
		return this.selectEntity(entity, whereSql,new Object[]{});
	}
	public <T> List<T> selectEntity(Object entity,String whereSql,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.selectList(entity, whereSql, params);
	}
	public long selectCount(String sqlOrID) throws Exception{
		return this.selectCount(sqlOrID,new Object[]{});
	}
	public long selectCount(String sqlOrID,Object...params) throws Exception{
		String dosql = sqlOrID.startsWith("$")?CacheCenter.SQL_SOURCE_MAP.get(sqlOrID):sqlOrID;
		return this.selectOne("SELECT COUNT(*) FROM ("+dosql+") t", Long.class, params);
	}
	public <T> T selectOne(String sqlOrID,Class<? extends T> returnClass,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.selectOne(sqlOrID, returnClass, params);
	}
	public Map<String,Object> selectOne(String sqlOrID,Object...params) throws Exception{
		return this.selectOne(sqlOrID, null , params);
	}
	/**
	 * 查询一行数据，超过一行数据会报错，
	 * @param sqlOrID 直接传入sql脚本或者是以$符号开头的引用key，如果使用key，则具体源sql集合保存位置固定为为CacheCenter.sqlSourceMap中
	 * @param returnClass 返回值类型
	 * @param params 传入参数，如果sql以?匹配参数，则可以传入数组或者list集合，如果是以#{key}匹配，则传入map或者一个实体对象（传入对象，程序会自动根据字段名匹配#{key}）
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> selectList(String sqlOrID,Class<? extends T> returnClass,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.selectList(sqlOrID, returnClass, params);
	}
	public List<Map<String,Object>> selectList(String sqlOrID,Object...params) throws Exception{
		return this.selectList(sqlOrID, null, params);
	}
	/**
	 * 返回受影响的行数
	 * @param sqlOrID
	 * @param params
	 * @return 插入行数
	 * @throws Exception
	 */
	public int insert(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.insert(sqlOrID,null, params);
	}
	/**
	 * 插入一个实体对象
	 * @param entity
	 * @return 插入行数
	 * @throws Exception
	 */
	public int insertEntity(Object entity) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.insertEntity(entity);
	}
	/**
	 * 插入数据并返回自增后的主键
	 * @param sqlOrID
	 * @param params
	 * @return 主键
	 * @throws Exception
	 */
	@Deprecated
	public <T> T insertReturnKey(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.insertReturnPK(sqlOrID,null, params);
	}
	/**
	 * 插入数据并返回自增后的主键
	 * @param sqlOrID
	 * @param params
	 * @return 主键
	 * @throws Exception
	 */
	public <T> T insertReturnPK(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.insertReturnPK(sqlOrID,null, params);
	}
	public <T> T insertEntityRtnPK(Object entity) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.insertEntityRtnPK(entity);
	}
	/**
	 * 批量插入
	 * @param sqlOrID
	 * @param params
	 * @return 返回每条插入语句受影响的行数
	 * @throws Exception
	 */
	public List<Integer> insertBatch(String sqlOrID,Object[] params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.insertBatch(sqlOrID,null,params);
	}
	public int update(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.update(sqlOrID,null, params);
	}
	public int updateEntity(Object entity) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.updateEntity(entity);
	}

	public int delete(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.delete(sqlOrID,null, params);
	}
	public int deleteEntity(Object entity) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.deleteEntity(entity);
	}
	public int excuteSQL(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.excuteSQL(sqlOrID,null, params);
	}
	/**
	 * 仅仅只回滚当前连接
	 * @throws Exception
	 */
	public void rollback() {
		try{
			Map<DataSource,Connection> connMap = SqlContext.getContext().getDataSourceMap();
			Connection conn = connMap.get(dataSource);
			conn.rollback();
		}catch (Exception e) {
			LOG.error("",e);
		}
	}
	/**
	 * 仅仅只提交当前连接
	 * @throws Exception
	 */
	public void commit() throws Exception{
		Map<DataSource,Connection> connMap = SqlContext.getContext().getDataSourceMap();
		Connection conn = connMap.get(dataSource);
		conn.commit();
	}
	/**
	 * 仅仅只关闭当前连接
	 * @throws Exception
	 */
	public void close(){
		try{
			Map<DataSource,Connection> connMap = SqlContext.getContext().getDataSourceMap();
			Connection conn = connMap.get(dataSource);
			conn.close();
		}catch (Exception e) {
			LOG.error("",e);
		}
	}
}