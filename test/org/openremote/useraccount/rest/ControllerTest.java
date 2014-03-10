package org.openremote.useraccount.rest;

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
      Assert.fail(res.getErrorMessage());
    } else {
      addedUserOID = (Long)res.getResult();
      Assert.assertNotNull(res.getResult());
      Assert.assertTrue(res.getResult() instanceof Long);
    }

    cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID);
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    r = cr.get();
    str = r.getText();
    res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", UserDTO.class).deserialize(str); 
    addedUser = (UserDTO)res.getResult(); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      Assert.assertEquals(addedUserOID.longValue(), addedUser.getOid().longValue());
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
      Assert.fail(res.getErrorMessage());
    } else {
      addedController = (ControllerDTO)res.getResult();
      Assert.assertNotNull(addedController);
      Assert.assertEquals(getMACAddresses1(), addedController.getMacAddress());
      Assert.assertFalse(addedController.isLinked());
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
      Assert.fail(res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertNotNull(tempController);
      Assert.assertEquals(getMACAddresses2(), tempController.getMacAddress());
      Assert.assertEquals(addedController.getOid(), tempController.getOid());
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
      Assert.fail(res.getErrorMessage());
    } else {
      addedController2 = (ControllerDTO)res.getResult();
      Assert.assertNotNull(addedController2);
      Assert.assertEquals(getMACAddresses3(), addedController2.getMacAddress());
      Assert.assertFalse(addedController2.isLinked());
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
      Assert.fail(res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertTrue(tempController.isLinked());
      Assert.assertEquals(addedUser.getAccount().getOid(), tempController.getAccount().getOid());
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
      Assert.fail(res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertTrue(tempController.isLinked());
      Assert.assertEquals(addedUser.getAccount().getOid(), tempController.getAccount().getOid());
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
      Assert.fail(res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertNotNull(tempController);
      Assert.assertEquals(getMACAddresses2(), tempController.getMacAddress());
      Assert.assertEquals(addedController.getOid(), tempController.getOid());
      Assert.assertEquals(addedUser.getAccount().getOid(), tempController.getAccount().getOid());
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
      Assert.fail(res.getErrorMessage());
    } else {
      List<ControllerDTO> tempList = (List<ControllerDTO>)res.getResult();
      
      Assert.assertEquals(2, tempList.size());
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
      Assert.assertEquals(null, res.getErrorMessage());
      Assert.assertEquals(null, res.getResult());
      
      cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "controller/" + addedController2.getOid());
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
      result = cr.delete();
      str = result.getText();
      res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
      Assert.assertEquals(null, res.getResult());
      
      cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
      result = cr.delete();
      str = result.getText();
      res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
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
}
