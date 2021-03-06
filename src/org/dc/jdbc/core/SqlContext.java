package org.dc.jdbc.core;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
/**
 * sql上下文
 * @author DC
 * @time 2015-8-17
 */
public class SqlContext{
	private static final ThreadLocal<SqlContext> sqlContext = new ThreadLocal<SqlContext>();
	
	private String sql;
    private List<Object> paramList;
    private boolean transaction = false;
    private boolean readOnly = false;
    private Map<DataSource,Connection> dataSourceMap = new HashMap<DataSource,Connection>();
    
    private DataSource currentDataSource;
    
	public DataSource getCurrentDataSource() {
		return currentDataSource;
	}
	public void setCurrentDataSource(DataSource currentDataSource) {
		this.currentDataSource = currentDataSource;
	}
	public Map<DataSource, Connection> getDataSourceMap() {
		return dataSourceMap;
	}
	public void setDataSourceMap(Map<DataSource, Connection> dataSourceMap) {
		this.dataSourceMap = dataSourceMap;
	}
	public boolean getTransaction() {
		return transaction;
	}
	public void setTransaction(boolean transaction) {
		this.transaction = transaction;
	}
	public String getSql() {
        return sql;
    }
    public void setSql(String sql) {
        this.sql = sql;
    }
	public List<Object> getParamList() {
		return paramList;
	}
	public void setParamList(List<Object> paramList) {
		this.paramList = paramList;
	}
	public boolean getReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public static SqlContext getContext(){
		SqlContext context = sqlContext.get();
		if(context==null){
			context = new SqlContext();
			sqlContext.set(context);
		}
		return context;
	}
	public static void closeSqlContext(){
		sqlContext.remove();
	}
}
