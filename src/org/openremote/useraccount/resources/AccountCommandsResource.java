package org.openremote.useraccount.resources;

import org.openremote.useraccount.GenericDAO;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

public class AccountCommandsResource extends ServerResource
{

  private GenericDAO dao;

  /**
   */
  @Get("json")
  public Representation loadDevices()
  {
    Representation rep = null;
    //TODO
    return rep;
  }

  /**
   */
  @Post("json:json")
  public Representation saveDevices(Representation data)
  {
    Representation rep = null;
    //TODO
    return rep;
  }

  /**
   */
  @Put("json:json")
  public Representation updateDevice(Representation data)
  {
    Representation rep = null;
    //TODO
    return rep;
  }

  /**
   */
  @Delete("json")
  public Representation deleteDevice()
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
