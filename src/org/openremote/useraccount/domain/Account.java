/* OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
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

import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The Class Account.
 * 
 * @author Marcus
 */
@Entity
@Table(name = "account")
public class Account extends BusinessEntity
{

  private static final long serialVersionUID = -5029444774173816237L;

  /** The users. */
  private Set<User> users;

  /** The controllers this account is linked to */
  private List<Controller> controllers;
  
  /**
   * Instantiates a new account.
   */
  public Account()
  {
  }

  @OneToMany(mappedBy = "account")
  public Set<User> getUsers()
  {
    return users;
  }

  public void setUsers(Set<User> users)
  {
    this.users = users;
  }

  @OneToMany(mappedBy = "account")  
  public List<Controller> getControllers()
  {
    return controllers;
  }

  public void setControllers(List<Controller> controllers)
  {
    this.controllers = controllers;
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
    Account other = (Account) obj;
    return other.getOid() == getOid();
  }
}
