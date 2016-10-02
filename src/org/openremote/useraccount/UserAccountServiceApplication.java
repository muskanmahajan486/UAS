/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2016, OpenRemote Inc.
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
package org.openremote.useraccount;

import java.util.List;

import org.openremote.useraccount.domain.Role;
import org.restlet.Application;

public class UserAccountServiceApplication extends Application {

  private GenericDAO dao;
  
  @Override
  public synchronized void start() throws Exception
  {
    super.start();
    initRoles();
  }
  
  public void initRoles() {
    boolean hasDesignerRole = false;
    boolean hasModelerRole = false;
    boolean hasAdminRole = false;
    boolean hasAccountManagerRole = false;
    List<Role> allRoles = dao.loadAll(Role.class);
    for (Role r : allRoles) {
       if (r.getName().equals(Role.ROLE_DESIGNER)) {
          hasDesignerRole = true;
       } else if (r.getName().equals(Role.ROLE_MODELER)) {
          hasModelerRole = true;
       } else if (r.getName().equals(Role.ROLE_ADMIN)) {
          hasAdminRole = true;
       } else if (r.getName().equals(Role.ROLE_ACCOUNT_MANAGER)) {
         hasAccountManagerRole = true;
      }
    }
    if (!hasDesignerRole) {
       Role r = new Role();
       r.setName(Role.ROLE_DESIGNER);
       dao.save(r);
    }
    if (!hasModelerRole) {
       Role r = new Role();
       r.setName(Role.ROLE_MODELER);
       dao.save(r);
    }
    if (!hasAdminRole) {
       Role r = new Role();
       r.setName(Role.ROLE_ADMIN);
       dao.save(r);
    }
    if (!hasAccountManagerRole) {
      Role r = new Role();
      r.setName(Role.ROLE_ACCOUNT_MANAGER);
      dao.save(r);
   }
    
 }
  

  public void setDao(GenericDAO dao)
  {
    this.dao = dao;
  }
  
  
  
}

