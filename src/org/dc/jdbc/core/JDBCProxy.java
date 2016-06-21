package org.dc.jdbc.core;

import java.lang.reflect.Method;

import org.dc.jdbc.anno.Transactional;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
/**
 * 动态反向代理,主要作用：拦截参数、管理数据库事务
 * @author dc
 * @time 2015-8-17
 */
public final class JDBCProxy implements MethodInterceptor {
    private final static  JDBCProxy jdbcProxy = new JDBCProxy();
    public static JDBCProxy getInstance(){
        return jdbcProxy;
    }

    private JDBCProxy(){}
	public Object intercept(Object obj, Method method, Object[] objects, MethodProxy proxy) throws Throwable {
		Transactional transactional  = method.getAnnotation(Transactional.class);
        if (transactional == null) {
            //方法无注解，查找类上注解，并判断当前调用方法是否为当前类定义的（防止父类方法触发事务边界）
            transactional = method.getDeclaringClass().getAnnotation(Transactional.class);
        }

        if(transactional!=null){//如果不为空，则开启事务
			if(transactional.readonly()==false){
				ConnectionManager.startTransaction();
			}else{
				ConnectionManager.setReadOnly();
			}
		}

		Object invokeObj = null;
		try{
		    //执行目标方法
			invokeObj = proxy.invokeSuper(obj, objects);
			ConnectionManager.commit();
		}catch(Throwable e){
            ConnectionManager.rollback();
            throw e;
		}finally{
			ConnectionManager.closeConnection();
		}
		return invokeObj;
	}

	@SuppressWarnings("unchecked")
	public <T> T getTarget(Class<T> target) {
        Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(target);
		// 回调方法
		enhancer.setCallback(this);

        // 创建代理对象
		return (T) enhancer.create();
	}
}
