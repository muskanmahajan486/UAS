/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.useraccount.domain;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import flexjson.JSON;

/**
 * TODO
 * 
 * @author Marcus
 */
@Entity
@Table(name = "user")
public class User extends BusinessEntity
{

  private static final long serialVersionUID = 6064996041309363949L;

  private String username;

  private String password;

  private String email;
  
  private String token;

  private boolean valid;

  private transient Timestamp registerTime;

  /** The account containing all business entities. */
  private Account account;
  
  
  private List<Role> roles;

  /**
   * Instantiates a new user.
   */
  public User()
  {
    account = new Account();
  }

  public User(Account account)
  {
    this.account = account;
  }

  /**
   * Gets the username.
   * 
   * @return the username
   */
  @Column(unique = true, nullable = false)
  public String getUsername()
  {
    return username;
  }

  /**
   * Sets the username.
   * 
   * @param username
   *          the new username
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  /**
   * Gets the password.
   * 
   * @return the password
   */
  @Column(nullable = false)
  public String getPassword()
  {
    return password;
  }

  /**
   * Sets the password.
   * 
   * @param password
   *          the new password
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /**
   * Gets the account.
   * 
   * @return the account
   */
  @ManyToOne
  public Account getAccount()
  {
    return account;
  }

  /**
   * Sets the account.
   * 
   * @param account
   *          the new account
   */
  public void setAccount(Account account)
  {
    this.account = account;
  }

  /**
   * Gets the roles.
   * 
   * @return the roles
   */
  @ManyToMany(fetch=FetchType.EAGER)
  @JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "user_oid") }, inverseJoinColumns = { @JoinColumn(name = "role_oid") })
  public List<Role> getRoles() {
     return roles;
  }
  
  /**
   * Sets the roles.
   * 
   * @param roles the new roles
   */
  public void setRoles(List<Role> roles) {
     this.roles = roles;
  }
  
  /**
   * Adds the role.
   * 
   * @param role the role
   */
  public void addRole(Role role) {
     roles.add(role);
  }
  
  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }
  
  public String getToken()
  {
    return token;
  }

  public void setToken(String token)
  {
    this.token = token;
  }

  public boolean isValid()
  {
    return valid;
  }

  public void setValid(boolean valid)
  {
    this.valid = valid;
  }

  @Column(name = "register_time")
  public Timestamp getRegisterTime()
  {
    return registerTime;
  }

  public void setRegisterTime(Timestamp registerTime)
  {
    this.registerTime = registerTime;
  }

  @Override
  public int hashCode()
  {
    return (int) getOid();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    User other = (User) obj;
    return other.getOid() == getOid();
  }

}
