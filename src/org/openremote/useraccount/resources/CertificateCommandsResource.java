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
package org.openremote.useraccount.resources;

import org.openremote.useraccount.GenericDAO;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

public class CertificateCommandsResource extends ServerResource
{

  private GenericDAO dao;

  /**
   */
  @Get("json")
  public Representation loadUSer()
  {
    //TODO
    return null;
  }

  /**
   */
  @Post("json:json")
  public Representation saveUser(Representation data)
  {
    //TODO
    return null;
  }

  /**
   */
  @Put("json:json")
  public Representation updateUser(Representation data)
  {
    Representation rep = null;
    //TODO
    return rep;
  }

  /**
   */
  @Delete("json")
  public Representation deleteUser()
  {
    Representation rep = null;
    //TODO
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
