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
  
  public GenericDAO getDao()
  {
    return dao;
  }

  public void setDao(GenericDAO dao)
  {
    this.dao = dao;
  }
  
}
