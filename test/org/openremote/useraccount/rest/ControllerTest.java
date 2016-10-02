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
package org.openremote.useraccount.rest;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.TestConfiguration;
import org.openremote.useraccount.domain.AccountDTO;
import org.openremote.useraccount.domain.ControllerDTO;
import org.openremote.useraccount.domain.RoleDTO;
import org.openremote.useraccount.domain.UserDTO;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class ControllerTest
{

  private static ControllerDTO addedController;
  private static ControllerDTO addedController2;
  private static Long addedUserOID;
  private static UserDTO addedUser;
  
  /**
   * Sets up a specific user to run controller tests.
   */
  @BeforeClass
  public void setUpUser() throws Exception
  {
    String username = "CONTROLLER_TEST";
    UserDTO user = new UserDTO();
    user.setUsername(username);
    user.setPassword(new Md5PasswordEncoder().encodePassword(TestConfiguration.ACCOUNT_MANAGER_PASSWORD, username));
    user.setEmail("controller_test@openremote.de");
    user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
    user.setValid(true);
    user.addRole(new RoleDTO("ROLE_ADMIN", Long.valueOf(3)));
    user.setAccount(new AccountDTO());

    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(user));
    Representation r = cr.post(rep);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", Long.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to create user: " + res.getErrorMessage());
    } else {
      addedUserOID = (Long)res.getResult();
      Assert.assertNotNull(res.getResult(), "Create user should return a value");
      Assert.assertTrue(res.getResult() instanceof Long, "Craete user return value should be a Long");
    }

    cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID);
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    r = cr.get();
    str = r.getText();
    res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", UserDTO.class).deserialize(str); 
    addedUser = (UserDTO)res.getResult(); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to get user details by id: " + res.getErrorMessage());
    } else {
      Assert.assertEquals(addedUserOID.longValue(), addedUser.getOid().longValue(), "Id of returned user object should be the requested one");
    }
  }

  /**
   * Announce a new controller with a MAC address that is not yet known.
   * Controller should be added to the list.
   */
  @Test
  public void testCreateAnnounceController() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/announce/"+ getMACAddresses1());
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to announce a controller: " + res.getErrorMessage());
    } else {
      addedController = (ControllerDTO)res.getResult();
      Assert.assertNotNull(addedController, "Announce controller should return a controller object");
      Assert.assertEquals(getMACAddresses1(), addedController.getMacAddress(), "Returned controller object should have MAC address used for announce");
      Assert.assertFalse(addedController.isLinked(), "Announced controller should initially not be linked to an account");
    }
  }

  /**
   * Announce a controller with multiple MAC addresses, one of them being already known.
   * Controller should not be added again, existing controller should be returned but its MAC address should be updated.
   */
  @Test(dependsOnMethods = { "testCreateAnnounceController" })
  public void testCreateAnnounceController2() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/announce/"+ getMACAddresses2());
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to announce a controller: " + res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertNotNull(tempController, "Announce controller should return a controller object");
      Assert.assertEquals(getMACAddresses2(), tempController.getMacAddress(), "Returned controller object should have MAC address used for announce");
      Assert.assertEquals(addedController.getOid(), tempController.getOid(), "Returned controller's id should be one of already existing controller when controller is already known");
    }
  }

  /**
   * Announce a new controller with a MAC address that is not yet known.
   * Controller should be added to the list.
   */
  @Test(dependsOnMethods = { "testCreateAnnounceController", "testCreateAnnounceController2" })
  public void testCreateAnnounceController3() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/announce/"+ getMACAddresses3());
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to announce a controller: " + res.getErrorMessage());
    } else {
      addedController2 = (ControllerDTO)res.getResult();
      Assert.assertNotNull(addedController2, "Announce controller should return a controller object");
      Assert.assertEquals(getMACAddresses3(), addedController2.getMacAddress(), "Returned controller object should have MAC address used for announce");
      Assert.assertFalse(addedController2.isLinked(), "Announced controller should initially not be linked to an account");
    }
  }
  
  /**
   * Link a known controller to an existing account.
   * This should succeed and return the updated controller.
   */
  @Test(dependsOnMethods = { "testCreateAnnounceController", "testCreateAnnounceController2" })
  public void testUpdateController() throws Exception
  {
    addedController.setLinked(true);
    addedController.setAccount(addedUser.getAccount());
    
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(addedController));
    Representation r = cr.put(rep);

    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to update controller: " + res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertTrue(tempController.isLinked(), "Updated controller should be linked");
      Assert.assertEquals(addedUser.getAccount().getOid(), tempController.getAccount().getOid(), "Returned controller account id should be the one set by update");
    }
  }
  
  /**
   * Link a known controller to an existing account.
   * This should succeed and return the updated controller.
   */
  @Test(dependsOnMethods = { "testCreateAnnounceController2" })
  public void testUpdateController2() throws Exception
  {
    addedController2.setLinked(true);
    addedController2.setAccount(addedUser.getAccount());
    
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(addedController2));
    Representation r = cr.put(rep);

    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to update controller: " + res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertTrue(tempController.isLinked(), "Updated controller should be linked");
      Assert.assertEquals(addedUser.getAccount().getOid(), tempController.getAccount().getOid(), "Returned controller account id should be the one set by update");
    }
  }
  
  /**
   * Announce a controller with MAC address that is known.
   * Controller is already linked to an account.
   * Call should return controller, including information on linked account.
   */
  @Test(dependsOnMethods = { "testUpdateController2" })
  public void testCreateAnnounceControllerAfterLink() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/announce/"+ getMACAddresses2());
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to announce a controller: " + res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertNotNull(tempController, "Announce controller should return a controller object");
      Assert.assertEquals(getMACAddresses2(), tempController.getMacAddress(), "Returned controller object should have MAC address used for announce");
      Assert.assertEquals(addedController.getOid(), tempController.getOid(), "Returned controller's id should be one of already existing controller when controller is already known");
      Assert.assertEquals(addedUser.getAccount().getOid(), tempController.getAccount().getOid(), "Returned controller account id should be the one of account it was previously linked to");
    }
  }
  
  /**
   * Get all controllers that are linked to a given account.
   */
  @SuppressWarnings("unchecked")
  @Test(dependsOnMethods = { "testUpdateController", "testUpdateController2", "testCreateAnnounceControllerAfterLink" })
  public void testFindAllControllerFromAccount() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/find");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to get all controllers: " + res.getErrorMessage());
    } else {
      List<ControllerDTO> tempList = (List<ControllerDTO>)res.getResult();
      
      Assert.assertEquals(2, tempList.size(), "There should be 2 controllers registered");
      if (addedController.getOid() != tempList.get(0).getOid() && addedController.getOid() != tempList.get(1).getOid()) {
        Assert.fail("Invalid 1st controller id");
      }
      Assert.assertEquals(addedUser.getAccount().getOid(), tempList.get(0).getAccount().getOid(), "Invalid user id for 1st controller");
      if (addedController2.getOid() != tempList.get(0).getOid() && addedController2.getOid() != tempList.get(1).getOid()) {
        Assert.fail("Invalid 2nd controller id");
      }
      Assert.assertEquals(addedUser.getAccount().getOid(), tempList.get(1).getAccount().getOid(), "Invalid user id for 2nd controller");
    }
  }
  
  /**
   * Delete everything that has been created during these tests.
   * Validates that all deletes are successfully performed.
   */
  @Test(dependsOnMethods = { "testFindAllControllerFromAccount" })
  public void testDeleteControllerAndUser() throws Exception
  {
      ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/" + addedController.getOid());
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
      Representation result = cr.delete();
      String str = result.getText();
      GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertNull(res.getErrorMessage(), "Delete controller should not return error message");
      Assert.assertNull(res.getResult(), "Delete controller should not return any information");
      
      cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/" + addedController2.getOid());
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
      result = cr.delete();
      str = result.getText();
      res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertNull(res.getErrorMessage(), "Delete controller should not return error message");
      Assert.assertNull(res.getResult(), "Delete controller should not return any information");
      
      cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
      result = cr.delete();
      str = result.getText();
      res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertNull(res.getErrorMessage(), "Delete user should not return error message");
  }
  
  /**
   * Announces 2 controllers with different MAC addresses but each including
   * the Microsoft Windows Vista/7 6to4 Adaptor fake MAC address.
   * Test validates that this address is not taken into account and that 2 distinct controllers
   * are correctly registered in the system.
   * @see MODELER-531 in JIRA
   */
  @Test
  public void testMicrosoft6To4MacIsNotTakenIntoAccount() throws IOException
  {
	  Long controller1Id = 0l;

	    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/announce/"+ getMACAddresses1WithMS6To4());
	    Representation r = cr.post(null);
	    String str = r.getText();
	    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
	    if (res.getErrorMessage() != null) {
	      Assert.fail("Impossible to announce a controller: " + res.getErrorMessage());
	    } else {
	      ControllerDTO controller = (ControllerDTO)res.getResult();
	      Assert.assertNotNull(controller, "Announce controller should return a controller object");
	      controller1Id = controller.getOid();
	    }
	    cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/announce/"+ getMACAddresses2WithMS6To4());
	    r = cr.post(null);
	    str = r.getText();
	    res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
	    if (res.getErrorMessage() != null) {
	      Assert.fail("Impossible to announce a controller: " + res.getErrorMessage());
	    } else {
	      ControllerDTO controller = (ControllerDTO)res.getResult();
	      Assert.assertNotNull(controller, "Announce controller should return a controller object");
	      Assert.assertFalse((controller.getOid() == controller1Id), "A second distinct controller should have been created");
	    }
  }
  
  private String getMACAddresses1()
  {
    return "11-22-33-44-55-66";
  }
  
  private String getMACAddresses2()
  {
    return "aa-bb-cc-dd-ee-ff,11-22-33-44-55-66";
  }
  
  private String getMACAddresses3()
  {
    return "12-34-56-78-90-aa";
  }
  
  private String getMACAddresses1WithMS6To4()
  {
	  return "a0-12-34-56-78-90,00-00-00-00-00-00-00-E0"; 
  }

  private String getMACAddresses2WithMS6To4()
  {
	  return "a1-12-34-56-78-90,00-00-00-00-00-00-00-E0"; 
  }

}
