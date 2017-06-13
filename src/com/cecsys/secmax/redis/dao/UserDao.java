package com.cecsys.secmax.redis.dao;

import java.util.List;

import com.cecsys.secmax.redis.bean.User;

/**
 * 用户dao接口
 * @author liuyazhuang
 *
 */
public interface UserDao {
	 /**
     * 保存用户
     * @param user
     */
    void saveUser(User user);
	
	/**
	 * 获取所有用户列表
	 * @return
	 */
	List<User> getAllUser();
	
	/**
	 * 根据id查询用户信息
	 * @param id
	 * @return
	 */
	User getById(Integer id);
	
	/**
	 * 更新用户的名称
	 * @param user
	 */
	void rename(User user);
	
	/**
	 * 根据id删除指定的用户
	 * @param id
	 */
	void deleteById(Integer id);
}
