package com.cecsys.secmax.redis.service;

import java.util.List;

import com.cecsys.secmax.redis.bean.User;

/**
 * 用户service接口
 * @author liuyazhuang
 *
 */
public interface UserService {
	 /**
     * 保存用户
     * @param user
     */
    void saveUser(String name, String sex, Integer age);
	
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
	User getUserById(Integer id);
	
	/**
	 * 更新用户的名称
	 * @param user
	 */
	void renameUser(String name, Integer id);
	
	/**
	 * 根据id删除指定的用户
	 * @param id
	 */
	void deleteUserById(Integer id);
}
