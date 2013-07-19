package org.openremote.useraccount.rest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.domain.AccountDTO;
import org.openremote.useraccount.domain.ControllerDTO;
import org.openremote.useraccount.domain.RoleDTO;
import org.openremote.useraccount.domain.UserDTO;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class ControllerTest
{

  private static ControllerDTO addedController;
  private static ControllerDTO addedController2;
  private static Long addedUserOID;
  private static UserDTO addedUser;
  
  /**
   * Test: Create user
   */
  @Test
  public void testCreateUser() throws Exception
  {
    String username = "CONTROLLER_TEST";
    UserDTO user = new UserDTO();
    user.setUsername(username);
    user.setPassword(new Md5PasswordEncoder().encodePassword("password", username));
    user.setEmail("controller_test@openremote.de");
    user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
    user.setValid(true);
    user.addRole(new RoleDTO("ROLE_ADMIN", Long.valueOf(3)));
    user.setAccount(new AccountDTO());

    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(user));
    Representation r = cr.post(rep);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", Long.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      addedUserOID = (Long)res.getResult();
      Assert.assertNotNull(res.getResult());
      Assert.assertTrue(res.getResult() instanceof Long);
    }
  }
  
  /**
   * Test: Retrieve user by userOid
   */
  @Test
  public void testQueryUserByOid() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/user/" + addedUserOID);
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", UserDTO.class).deserialize(str); 
    addedUser = (UserDTO)res.getResult(); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      Assert.assertEquals(addedUserOID.longValue(), addedUser.getOid().longValue());
    }
  }

  /**
   * Test: Announce controller
   */
  @Test
  public void testCreateAnnounceController() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/announce/"+ getMACAddresses1());
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
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
   * Test: Announce controller with second macAddress availble after controller was already announced
   */
  @Test
  public void testCreateAnnounceController2() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/announce/"+ getMACAddresses2());
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
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
   * Test: Announce a second controller for account 
   */
  @Test
  public void testCreateAnnounceController3() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/announce/"+ getMACAddresses3());
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
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
   * Test: Update controller with account
   */
  @Test
  public void testUpdateController() throws Exception
  {
    addedController.setLinked(true);
    addedController.setAccount(addedUser.getAccount());
    
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(addedController));
    Representation r = cr.put(rep);

    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertTrue(tempController.isLinked());
      Assert.assertEquals(addedUser.getAccount().getOid(), tempController.getAccount().getOid());
    }
  }
  
  /**
   * Test: Update second controller with account
   */
  @Test
  public void testUpdateController2() throws Exception
  {
    addedController2.setLinked(true);
    addedController2.setAccount(addedUser.getAccount());
    
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(addedController2));
    Representation r = cr.put(rep);

    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertTrue(tempController.isLinked());
      Assert.assertEquals(addedUser.getAccount().getOid(), tempController.getAccount().getOid());
    }
  }
  
  /**
   * Test: Announce controller with second macAddress after linked to an account
   */
  @Test
  public void testCreateAnnounceControllerAfterLink() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/announce/"+ getMACAddresses2());
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
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
   * Test: Find all linked controller for the account
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testFindAllControllerFromAccount() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/find");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      List<ControllerDTO> tempList = (List<ControllerDTO>)res.getResult();
      Assert.assertEquals(2, tempList.size());
      Assert.assertEquals(addedController.getOid(), tempList.get(0).getOid());
      Assert.assertEquals(addedUser.getAccount().getOid(), tempList.get(0).getAccount().getOid());
      Assert.assertEquals(addedController2.getOid(), tempList.get(1).getOid());
      Assert.assertEquals(addedUser.getAccount().getOid(), tempList.get(1).getAccount().getOid());
    }
  }
  
  /**
   * Test: delete controller and user
   */
  @Test
  public void testDeleteControllerAndUser() throws Exception
  {
      ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/" + addedController.getOid());
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
      Representation result = cr.delete();
      String str = result.getText();
      GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
      Assert.assertEquals(null, res.getResult());
      
      cr = new ClientResource("http://localhost:8080/uas/rest/controller/" + addedController2.getOid());
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, addedUser.getUsername(), addedUser.getPassword());
      result = cr.delete();
      str = result.getText();
      res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
      Assert.assertEquals(null, res.getResult());
      
      cr = new ClientResource("http://localhost:8080/uas/rest/user/" + addedUserOID);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
      result = cr.delete();
      str = result.getText();
      res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
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
