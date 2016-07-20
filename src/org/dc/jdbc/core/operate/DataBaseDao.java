package org.dc.jdbc.core.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataBaseDao extends BaseDao implements IDataBaseDao{
	@SuppressWarnings("unchecked")
	@Override
	public  <T> T selectOne(Connection conn,String sql,Class<? extends T> cls,Object[] params) throws Exception{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = super.preparedSQLReturnRS(ps, sql, params);

			int row = 0;
			if(rs.last() && (row = rs.getRow())>1){
				throw new Exception("Query results too much!");
			}
			if(row==1){//判断是否有返回结果，有的话执行下面转化操作
				if(cls==null || Map.class.isAssignableFrom(cls)){
					return (T) super.parseSqlResultToMap(rs);
				}else{
					if(cls.getClassLoader()==null){//java基本类型
						return (T) super.parseSqlResultToBaseType(rs);
					}else{//java对象
						return (T) super.parseSqlResultToObject(rs, cls);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			super.close(ps,rs);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> selectList(Connection conn, String sql, Class<? extends T> cls, Object[] params) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = super.preparedSQLReturnRS(ps, sql, params);
			rs.last();
			int rowNum = rs.getRow();
			if(rowNum>0){
				rs.beforeFirst();
				List<Object> list = new ArrayList<Object>(rowNum);

				if(cls==null || Map.class.isAssignableFrom(cls)){//封装成Map
					super.parseSqlResultToListMap(rs,list);
				}else{
					if(cls.getClassLoader()==null){//封装成基本类型
						super.parseSqlResultToListBaseType(rs,list);
					}else{//对象
						super.parseSqlResultToListObject(rs,cls,list);
					}
				}
				return (List<T>) list;
			}
		} catch (Exception e) {
			throw e;
		}finally{
			super.close(ps,rs);
		}
		return null;
	}

	@Override
	public int update(Connection conn, String sql, Object[] params) throws Exception {
		return 0;
	}

	@Override
	public int insert(Connection conn, String sql, Object[] params) throws Exception {
		return 0;
	}

	@Override
	public int[] insertBatch(Connection conn, String sql, Object[][] params) throws Exception {
		return null;
	}

	@Override
	public <T> T insertRtnPKKey(Connection conn, String sql, Object[] params) throws Exception {
		return null;
	}

	@Override
	public int delete(Connection conn, String sql, Object[] params) throws Exception {
		return 0;
	}
	
}
