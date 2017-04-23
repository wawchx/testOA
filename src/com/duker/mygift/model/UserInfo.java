package com.duker.mygift.model;

/**
 * UserInfo generated by MyEclipse Persistence Tools
 */

public class UserInfo implements java.io.Serializable {

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 用户昵称
	 */
	private String nickname;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 供货商公司名称
	 */
	private String workname;

	/**
	 * 状态
	 */
	private Integer state;

	/**
	 * 地址
	 */
	private String address;

	/**
	 * 联系方式
	 */
	private String phone;

	/**
	 * 用户所属角色
	 */
	private Role role;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getWorkname() {
		return workname;
	}

	public void setWorkname(String workname) {
		this.workname = workname;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}