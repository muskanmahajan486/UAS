package org.openremote.useraccount.rest;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.domain.AccountDTO;
import org.openremote.useraccount.domain.RoleDTO;
import org.openremote.useraccount.domain.UserDTO;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class UserTest
{

  private static Long addedUserOID;
  private static Long addedUserOID2;
  private static Long addedUserOID3;
  private static Long addedUserOID4;
  private static UserDTO addedUser;
  
  /**
   * Test: Retrieve all users
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testQueryAllUsers() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/users");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", UserDTO.class).deserialize(str); 
    List<UserDTO> dtos = (List<UserDTO>)res.getResult(); 
  
    Assert.assertTrue("Did not get all users", dtos.size() > 5);
  }
  
  /**
   * Test: Create user
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCreateUser() throws Exception
  {
    String username = "REST_TEST";
    UserDTO user = new UserDTO();
    user.setUsername(username);
    user.setPassword(new Md5PasswordEncoder().encodePassword("password", username));
    user.setEmail("rest_test@openremote.de");
    user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
    user.addRole(new RoleDTO("ROLE_ADMIN", Long.valueOf(3)));
    user.setAccount(new AccountDTO());

    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user");
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
   * Test: Create user without password (this is the case when a user is created via Google checkout
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCreateUserViaCheckout() throws Exception
  {
    String username = "REST_TEST_CHECKOUT";
    UserDTO user = new UserDTO();
    user.setUsername(username);
    user.setEmail("marcus@openremote.org");
    user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
    user.addRole(new RoleDTO("ROLE_ADMIN", Long.valueOf(3)));
    user.setAccount(new AccountDTO());

    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(user));
    Representation r = cr.post(rep);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", Long.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      addedUserOID4 = (Long)res.getResult();
      Assert.assertNotNull(res.getResult());
      Assert.assertTrue(res.getResult() instanceof Long);
    }
  }
  
  /**
   * Test: Retrieve users by email address
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testQueryUsersByEmail() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/users?email=rest_test@openremote.de");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", UserDTO.class).deserialize(str); 
    List<UserDTO> dtos = (List<UserDTO>)res.getResult(); 
  
    Assert.assertEquals(1, dtos.size());
  }
  
  /**
   * Test: Retrieve user by userOid
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testQueryUserByOid() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user/" + addedUserOID);
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
   * Test: Update user
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testUpdateUser() throws Exception
  {
    addedUser.setValid(true);
    addedUser.addRole(new RoleDTO("ROLE_DESIGNER", Long.valueOf(1)));
    
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(addedUser));
    Representation r = cr.put(rep);

    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", UserDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      UserDTO savedUser = (UserDTO)res.getResult();
      Assert.assertEquals(addedUserOID.longValue(), savedUser.getOid().longValue());
    }
  }
  
  /**
   * Test: Retrieve users by email address and valid flag
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testQueryUsersByEmailAndValidFlag() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/users?email=rest_test@openremote.de&valid=true");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", UserDTO.class).deserialize(str); 
    List<UserDTO> dtos = (List<UserDTO>)res.getResult(); 
  
    Assert.assertEquals(1, dtos.size());
  }
  

  /**
   * Test: Check username availability
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCheckUsernameAvailability() throws Exception
  {
    String username = "REST_TEST2";
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user/checkAvailabilty/" + username);
   // cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    Assert.assertEquals("{\"result\": \"true\"}", str);
  }
  
  /**
   * Test: Add second user to existing account
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testAddUserToAccount() throws Exception
  {
    String username = "REST_TEST2";
    UserDTO user = new UserDTO();
    user.setUsername(username);
    user.setPassword(new Md5PasswordEncoder().encodePassword("password", username));
    user.setEmail("rest_test@openremote.de");
    user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
    user.addRole(new RoleDTO("ROLE_ADMIN", Long.valueOf(3)));
    user.setAccount(new AccountDTO(addedUser.getAccount().getOid()));

    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(user));
    Representation r = cr.post(rep);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", Long.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      addedUserOID2 = (Long)res.getResult();
      Assert.assertNotNull(res.getResult());
      Assert.assertTrue(res.getResult() instanceof Long);
    }
  }
  
  /**
   * Test: Check username availability2
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCheckUsernameAvailability2() throws Exception
  {
    String username = "REST_TEST2";
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user/checkAvailabilty/" + username);
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    Assert.assertEquals("{\"result\": \"false\"}", str);
  }
  
  /**
   * Test: Check username availability ignore case
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCheckUsernameAvailabilityIgnoreCase() throws Exception
  {
    String username = "rest_TEST2";
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user/checkAvailabilty/" + username);
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    Assert.assertEquals("{\"result\": \"false\"}", str);
  }
  
  /**
   * Test invite user REST call
   */
  @Test
  public void testInviteUser() {
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user/" + addedUserOID + "/inviteUser?inviteeEmail=mredeker@web.de&inviteeRoles=ROLE_ADMIN");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.post(null);
    String str;
    try { 
      str = r.getText();
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", UserDTO.class).deserialize(str);
    UserDTO inviteeDTO = (UserDTO)res.getResult();
    addedUserOID3 = inviteeDTO.getOid();
    Assert.assertEquals("mredeker@web.de", inviteeDTO.getEmail());
    Assert.assertEquals(null, res.getErrorMessage()); 
  }
  
  /**
   * Test: delete user
   */
  @Test
  public void testDeleteUser() throws Exception
  {
      ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user/" + addedUserOID);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
      Representation result = cr.delete();
      String str = result.getText();
      GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
      
      cr = new ClientResource("http://localhost:8090/uas/rest/user/" + addedUserOID2);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
      result = cr.delete();
      str = result.getText();
      res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
      
      cr = new ClientResource("http://localhost:8090/uas/rest/user/" + addedUserOID3);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
      result = cr.delete();
      str = result.getText();
      res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
      
      cr = new ClientResource("http://localhost:8090/uas/rest/user/" + addedUserOID4);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
      result = cr.delete();
      str = result.getText();
      res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
  }
  
  /**
   * Test: Check username availability after delete
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCheckUsernameAvailabilityAgain() throws Exception
  {
    String username = "rest_TEST2";
    ClientResource cr = new ClientResource("http://localhost:8090/uas/rest/user/checkAvailabilty/" + username);
    //cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    Assert.assertEquals("{\"result\": \"true\"}", str);
  }
  
}
