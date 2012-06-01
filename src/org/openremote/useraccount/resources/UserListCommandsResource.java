/* OpenRemote, the Home of the Digital Home.
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
package org.openremote.useraccount.resources;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.GenericDAO;
import org.openremote.useraccount.domain.User;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import flexjson.JSONSerializer;

/**
 * This class is handling all REST calls dealing with user lists, URL: /rest/users 
 * @author marcus
 *
 */
public class UserListCommandsResource extends ServerResource
{

  private GenericDAO dao;

  /**
   * Return a list of Users.<p>
   * You can filter users when adding request parameter:<br>
   * username=aaa<br>
   * email=bbb<br>
   * valid=true<br>
   * <p>
   * REST Url: /rest/users -> return all users<br>
   * REST Url: /rest/users?valid=true -> return all activated users
   * 
   * @return a List of Users
   */
  @Get("json")
  public Representation getUser()
  {
    GenericResourceResultWithErrorMessage result = null;
    try
    {
      Form queryParams = getQuery();
      DetachedCriteria search = DetachedCriteria.forClass(User.class);
      search.setFetchMode("account", FetchMode.JOIN);
      search.setFetchMode("roles", FetchMode.JOIN);
      search.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
      if (queryParams.getFirstValue("username", true) != null)
      {
        String username = queryParams.getFirstValue("username", true);
        search.add(Restrictions.eq("username", username));
      }
      if (queryParams.getFirstValue("email", true) != null)
      {
        String email = queryParams.getFirstValue("email", true);
        search.add(Restrictions.eq("email", email));
      }
      if (queryParams.getFirstValue("valid", true) != null)
      {
        Boolean valid = Boolean.valueOf(queryParams.getFirstValue("valid", true));
        search.add(Restrictions.eq("valid", valid));
      }
      List<User> users = dao.findByDetachedCriteria(search);
      result = new GenericResourceResultWithErrorMessage(null, users);
    } catch (Exception e)
    {
      result = new GenericResourceResultWithErrorMessage(e.getMessage(), null);
    }
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.users", "result.roles.users").deepSerialize(result));
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
