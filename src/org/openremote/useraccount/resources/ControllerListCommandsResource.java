package org.openremote.useraccount.resources;

import org.openremote.useraccount.GenericDAO;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

public class ControllerListCommandsResource extends ServerResource
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
