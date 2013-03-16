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

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.openremote.useraccount.GenericDAO;
import org.openremote.useraccount.domain.User;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * This class is handling a REST call to check if a username is available or not 
 * @author marcus
 *
 */
public class CheckUsernameAvailabilityCommandResource extends ServerResource
{
  private static Logger log = Logger.getLogger(CheckUsernameAvailabilityCommandResource.class);
  private GenericDAO dao;
  
  /**
   * Check whether the given username is available or not. The username is not case sensitive<p>
   * <p>
   * REST Url: /rest/user/checkAvailabilty/{userName} -> return true or false<br>
   * 
   * @return true or false
   */
  @Get("json")
  public Representation forgotPassword()
  {
    String result = null;
    try
    {
      String userName = (String) getRequest().getAttributes().get("userName");
      DetachedCriteria search = DetachedCriteria.forClass(User.class);
      if (userName != null)
      {
        search.add(Restrictions.eq("username", userName).ignoreCase());
        User user = dao.findOneByDetachedCriteria(search);
        if (user != null) {
          result = "false";
        } else {
          result = "true";
        }
      } else {
        result = "false";
      }
    } catch (Exception e)
    {
      log.error("Error when checking username availability", e);
      result = "error";
    }
    Representation rep = new JsonRepresentation("{\"result\": \"" + result + "\"}");
    return rep;
  }

  public void setDao(GenericDAO dao)
  {
    this.dao = dao;
  }

  
}
