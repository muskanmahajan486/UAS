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

package org.openremote.useraccount.resources;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.hibernate.Hibernate;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.GenericDAO;
import org.openremote.useraccount.domain.Account;
import org.openremote.useraccount.domain.Role;
import org.openremote.useraccount.domain.User;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * This class is handling all REST calls dealing with one user, URL: /rest/user 
 * @author marcus
 *
 */
public class UserCommandsResource extends ServerResource
{

  private GenericDAO dao;

  /**
   * Return one user based on his oid<p>
   * <p>
   * REST Url: /rest/user/{userOid} -> return user<br>
   * 
   * @return one User
   */
  @Get("json")
  public Representation getUser()
  {
    GenericResourceResultWithErrorMessage result = null;
    try
    {
      String oidString = (String) getRequest().getAttributes().get("userOid");
      Long oid = Long.valueOf(oidString);
      User user = dao.getById(User.class, oid);
      result = new GenericResourceResultWithErrorMessage(null, user);
    } catch (Exception e)
    {
      result = new GenericResourceResultWithErrorMessage(e.getMessage(), null);
    }
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.users", "result.roles.users").deepSerialize(result));
    return rep;
  }

  /**
   * Add the given user to the database
   * POST data has to contain a user as JSON string
   * REST POST Url:/rest/user
   * @param data
   * @return OID of the saved user
   */
  @Post("json:json")
  public Representation createUser(Representation data)
  {
    Representation rep = null;
    GenericResourceResultWithErrorMessage result = null;
    if (data != null) {
      if (MediaType.APPLICATION_JSON.equals(data.getMediaType(), true)) {
        try {
          String jsonData = data.getText();
          User newUser = new JSONDeserializer<User>().use(null, User.class).deserialize(jsonData);
          String roleName = newUser.getRoles().get(0).getName();
          newUser.setAccount(new Account());
          newUser.setRoles(new ArrayList<Role>());
          newUser.addRole(dao.getByNonIdField(Role.class, "name", roleName));
          dao.save(newUser.getAccount());
          dao.save(newUser);
          result = new GenericResourceResultWithErrorMessage(null, newUser.getOid());
        } catch (Exception e) {
          result = new GenericResourceResultWithErrorMessage(e.getMessage(), null);
        }
        rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(result));
      }
    }
    return rep;
  }


  /**
   * Update the user with the given id
   * PUT data has to contain user as JSON string
   * REST PUT Url:/rest/user
   * @param data
   * @return the OID of the updated user
   */
  @Put("json:json")
  public Representation updateUser(Representation data)
  {
    Representation rep = null;
    GenericResourceResultWithErrorMessage result = null;
    if (data != null) {
      if (MediaType.APPLICATION_JSON.equals(data.getMediaType(), true)) {
        try {
          String jsonData = data.getText();
          User changedUser = new JSONDeserializer<User>().use(null, User.class).deserialize(jsonData);
          for (Role role : changedUser.getRoles())
          {
            dao.merge(role);
          }
          User savedUser = (User)dao.merge(changedUser);
          result = new GenericResourceResultWithErrorMessage(null, savedUser.getOid());
        } catch (Exception e) {
          result = new GenericResourceResultWithErrorMessage(e.getMessage(), null);
        }
        rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(result));
      }
    }
    return rep;
  }


  /**
   * Delete the user with the given id
   * REST Url: /rest/user/{userOid} 
   * @return
   */
  @Delete("json")
  public Representation deleteUser()
  {
    Representation rep = null;
    GenericResourceResultWithErrorMessage result = null;
    String oidString = (String) getRequest().getAttributes().get("userOid");
    if (oidString != null)
    {
      try
      {
        Long oid = Long.valueOf(oidString);
        User user = dao.getById(User.class, oid);
        dao.delete(user);
        result = new GenericResourceResultWithErrorMessage(null, null);
      } catch (Exception e)
      {
        result = new GenericResourceResultWithErrorMessage(e.getMessage(), null);
      }
    } else {
      result = new GenericResourceResultWithErrorMessage("No userOid found in URL", null);
    }
    rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(result));
    return rep;
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
