package org.openremote.useraccount.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.GenericDAO;
import org.openremote.useraccount.domain.Account;
import org.openremote.useraccount.domain.Controller;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class ControllerCommandsResource extends ServerResource
{

  private GenericDAO dao;
  private TransactionTemplate transactionTemplate;

  /**
   * Is called by the controller when the controller starts.<br>
   * If for the given macAddresses no controller is found, a controller is created in the database.<br>
   * For the given macAddresses the controllerDTO is returned.
   * 
   * REST POST Url: /rest/controller/announce/{macAddresses} -> return controllerDTO<br>
   * 
   * @param data
   * @return ControllerDTO
   */
  @Post("json")
  public Representation announceController()
  {
    Representation rep = null;
    GenericResourceResultWithErrorMessage result = null;
    final String macAddresses = (String) getRequest().getAttributes().get("macAddresses");
      
    result = transactionTemplate.execute(new TransactionCallback<GenericResourceResultWithErrorMessage>() {
      @Override
      public GenericResourceResultWithErrorMessage doInTransaction(TransactionStatus transactionStatus)
      {
        try {
          if (macAddresses == null) {
            return new GenericResourceResultWithErrorMessage("No macAddresses were provided", null);
          }
          StringTokenizer st = new StringTokenizer(macAddresses, ",");
          Controller controller = null;
          while (st.hasMoreElements())
          {
            String macAddress = (String) st.nextElement();
            DetachedCriteria search = DetachedCriteria.forClass(Controller.class);
            search.add(Restrictions.ilike("macAddress", macAddress, MatchMode.ANYWHERE));
            controller = dao.findOneByDetachedCriteria(search);
            if (controller != null) {
              break; //If we found a controller for one address this is the same controller even if we have multiple addresses
            }
          }
          
          if (controller != null) {
            //We found a controller and update the macAddress string in case the controller has an additional macAddress
            if (!controller.getMacAddress().equals(macAddresses)) {
              controller.setMacAddress(macAddresses);
              dao.merge(controller);
            }
          } else {
            controller = new Controller();
            controller.setMacAddress(macAddresses);
            controller.setLinked(false);
            dao.save(controller);
          }
          if (controller.getAccount() != null) {
            Hibernate.initialize(controller.getAccount().getUsers());
          }          
          return new GenericResourceResultWithErrorMessage(null, controller);
        } catch (Exception e) {
          transactionStatus.setRollbackOnly();
          return new GenericResourceResultWithErrorMessage(e.getMessage(), null);
        }
      }
    });
    rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.controllers", "result.account.users.roles").deepSerialize(result));
    return rep;
  }

  
  /**
   * Update the given controller 
   * PUT data has to contain controller as JSON string
   * REST PUT Url:/rest/controller
   * @param data
   * @return the updated controller
   */
  @Put("json:json")
  public Representation updateController(final Representation data)
  {
    Representation rep = null;
    GenericResourceResultWithErrorMessage result = null;
    if (data != null) {
      if (MediaType.APPLICATION_JSON.equals(data.getMediaType(), true)) {
        result = transactionTemplate.execute(new TransactionCallback<GenericResourceResultWithErrorMessage>() {
          @Override
          public GenericResourceResultWithErrorMessage doInTransaction(TransactionStatus transactionStatus)
          {
            try {
              String jsonData = data.getText();
              Controller changedController = new JSONDeserializer<Controller>().use(null, Controller.class).deserialize(jsonData);
              if (changedController.getAccount() != null) {
                dao.merge(changedController.getAccount());
              }
              Controller savedController = (Controller)dao.merge(changedController);
              if (savedController.getAccount() != null) {
                Hibernate.initialize(savedController.getAccount().getUsers());
              }
              return new GenericResourceResultWithErrorMessage(null, savedController);
            } catch (Exception e) {
              transactionStatus.setRollbackOnly();
              return new GenericResourceResultWithErrorMessage(e.getMessage(), null);
            }
          }
        });
        rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.controllers", "result.account.users.roles").deepSerialize(result));
      }
    }
    return rep;
  }
  
  
  /**
   * Delete the controller with the given id
   * REST Url: /rest/controller/{controllerOid} 
   * @return
   */
  @Delete("json")
  public Representation deleteController()
  {
    Representation rep = null;
    GenericResourceResultWithErrorMessage result = null;
    String oidString = (String) getRequest().getAttributes().get("controllerOid");
    if (oidString != null)
    {
      try
      {
        Long oid = Long.valueOf(oidString);
        Controller controller = dao.getById(Controller.class, oid);
        dao.delete(controller);
        result = new GenericResourceResultWithErrorMessage(null, null);
      } catch (Exception e)
      {
        result = new GenericResourceResultWithErrorMessage(e.getMessage(), null);
      }
    } else {
      result = new GenericResourceResultWithErrorMessage("No controllerOid found in URL", null);
    }
    rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(result));
    return rep;
  }
  
  
  /**
   * Is called by the designer to find a controller via its macAddress<br>
   * 
   * REST POST Url: /rest/controller/find/{macAddresses} -> return controllerDTO<br>
   * 
   * @param data
   * @return ControllerDTO
   */
  @Get("json")
  public Representation findController()
  {
    Representation rep = null;
    GenericResourceResultWithErrorMessage result = null;
    final String macAddresses = (String) getRequest().getAttributes().get("macAddresses");
      
    result = transactionTemplate.execute(new TransactionCallback<GenericResourceResultWithErrorMessage>() {
      @Override
      public GenericResourceResultWithErrorMessage doInTransaction(TransactionStatus transactionStatus)
      {
        try {
          if (macAddresses == null) {
            return new GenericResourceResultWithErrorMessage("No macAddresses were provided", null);
          }
          StringTokenizer st = new StringTokenizer(macAddresses, ",");
          Controller controller = null;
          while (st.hasMoreElements())
          {
            String macAddress = (String) st.nextElement();
            DetachedCriteria search = DetachedCriteria.forClass(Controller.class);
            search.add(Restrictions.ilike("macAddress", macAddress, MatchMode.ANYWHERE));
            controller = dao.findOneByDetachedCriteria(search);
            if (controller != null) {
              break; //If we found a controller for one address this is the same controller even if we have multiple addresses
            }
          }
          return new GenericResourceResultWithErrorMessage(null, controller);
        } catch (Exception e) {
          transactionStatus.setRollbackOnly();
          return new GenericResourceResultWithErrorMessage(e.getMessage(), null);
        }
      }
    });
    rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.users", "result.account.controllers").deepSerialize(result));
    return rep;
  }
  
  
  public void setDao(GenericDAO dao)
  {
    this.dao = dao;
  }

  public void setTransactionTemplate(TransactionTemplate transactionTemplate)
  {
    this.transactionTemplate = transactionTemplate;
  }
}
