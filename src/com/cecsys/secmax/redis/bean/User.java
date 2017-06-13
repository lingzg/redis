package com.cecsys.secmax.redis.bean;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 用户实体类
 * @author liuyazhuang
 *
 */
@JsonSerialize
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class) 
public class User implements Serializable{
	private static final long serialVersionUID = 1332643889208978231L;
	
	/**
	 * id
	 */
	private Integer id;
	
	/**
	 * 姓名
	 */
	private String name;
	
	/**
	 * 性别
	 */
	private String sex;
	
	/**
	 * 年龄
	 */
	private Integer age;

	public User() {
		super();
	}
	
	public User(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public User(String name, String sex, Integer age) {
		super();
		this.name = name;
		this.sex = sex;
		this.age = age;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", sex=" + sex + ", age=" + age + "]";
	}
	
}
