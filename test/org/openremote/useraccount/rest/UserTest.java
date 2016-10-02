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

public class UserTest
{
  private static int initialNumberOfUsers;

  private static Long addedUserOID;
  private static Long addedUserOID2;
  private static Long addedUserOID3;
  private static Long addedUserOID4;
  private static UserDTO addedUser;
  
  @BeforeClass
  public void setUp() throws IOException
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "users");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", UserDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to create user: " + res.getErrorMessage());
    } else {
      @SuppressWarnings("unchecked")
	  List<UserDTO> dtos = (List<UserDTO>)res.getResult(); 
	  initialNumberOfUsers = dtos.size();
    }
  }

  /**
   * Retrieve all users.
   * The test counts that the appropriate number of users has been added by executing "create" tests in this class.
   */
  @Test(dependsOnMethods = { "testCreateUser", "testCreateUserViaCheckout", "testInviteUser" })
  public void testQueryAllUsers() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "users");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", UserDTO.class).deserialize(str);
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to query all users: " + res.getErrorMessage());
    } else {
      @SuppressWarnings("unchecked")
	  List<UserDTO> dtos = (List<UserDTO>)res.getResult();
      Assert.assertEquals(dtos.size(), initialNumberOfUsers + 4, "4 users should have been created during the tests");
    }
  }
  
  /**
   * Create a user.
   */
  @Test
  public void testCreateUser() throws Exception
  {
    String username = "REST_TEST";
    UserDTO user = new UserDTO();
    user.setUsername(username);
    user.setPassword(new Md5PasswordEncoder().encodePassword(TestConfiguration.ACCOUNT_MANAGER_PASSWORD, username));
    user.setEmail("rest_test@openremote.de");
    user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
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
      Assert.assertNotNull(res.getResult(),"Create user should return a value");
      Assert.assertTrue(res.getResult() instanceof Long, "Create user return value shoudl be a Long");
    }
  }
  
  /**
   * Create user without password (this is the case when a user is created via Google checkout.
   */
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

    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(user));
    Representation r = cr.post(rep);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", Long.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to create user: " + res.getErrorMessage());
    } else {
      addedUserOID4 = (Long)res.getResult();
      Assert.assertNotNull(res.getResult(),"Create user should return a value");
      Assert.assertTrue(res.getResult() instanceof Long, "Create user return value shoudl be a Long");
    }
  }
  
  /**
   * Retrieve users by email address.
   */
  @Test(dependsOnMethods = { "testCreateUser" })
  public void testQueryUsersByEmail() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "users?email=rest_test@openremote.de");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", UserDTO.class).deserialize(str); 
    @SuppressWarnings("unchecked")
	List<UserDTO> dtos = (List<UserDTO>)res.getResult(); 
  
    Assert.assertEquals(1, dtos.size(), "One user should be returned for given e-mail");
  }
  
  /**
   * Retrieve user by userOid.
   */
  @Test(dependsOnMethods = { "testCreateUser" })
  public void testQueryUserByOid() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID);
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", UserDTO.class).deserialize(str); 
    addedUser = (UserDTO)res.getResult(); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to query user by id: " + res.getErrorMessage());
    } else {
      Assert.assertEquals(addedUserOID.longValue(), addedUser.getOid().longValue(), "Id of returned user object should be queried one");
    }
  }

  /**
   * Update user.
   */
  @Test(dependsOnMethods = { "testCreateUser" })
  public void testUpdateUser() throws Exception
  {
    addedUser.setValid(true);
    addedUser.addRole(new RoleDTO("ROLE_DESIGNER", Long.valueOf(1)));
    
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(addedUser));
    Representation r = cr.put(rep);

    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", UserDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to update user: " + res.getErrorMessage());
    } else {
      UserDTO savedUser = (UserDTO)res.getResult();
      Assert.assertEquals(addedUserOID.longValue(), savedUser.getOid().longValue(), "Update user return value should be id of updated user");
    }
  }
  
  /**
   * Retrieve users by email address and valid flag.
   */
  @Test(dependsOnMethods = { "testUpdateUser" })
  public void testQueryUsersByEmailAndValidFlag() throws Exception
  {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "users?email=rest_test@openremote.de&valid=true");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ArrayList.class).use("result.values", UserDTO.class).deserialize(str); 
    @SuppressWarnings("unchecked")
	List<UserDTO> dtos = (List<UserDTO>)res.getResult(); 
  
    Assert.assertEquals(1, dtos.size(), "One id should be returned for given query parameters");
  }
  

  /**
   * Check user name should be available if no user is already using this name.
   */
  @Test(dependsOnMethods = { "testCreateUser", "testCreateUserViaCheckout" })
  public void testCheckUsernameAvailability() throws Exception
  {
    String username = "REST_TEST2";
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/checkAvailabilty/" + username);
    Representation r = cr.get();
    String str = r.getText();
    Assert.assertEquals("{\"result\": \"true\"}", str, "User name should be available before user has been created");
  }
  
  /**
   * Add second user to existing account.
   */
  @Test(dependsOnMethods = { "testCreateUser", "testCreateUserViaCheckout", "testCheckUsernameAvailability" })
  public void testAddUserToAccount() throws Exception
  {
    String username = "REST_TEST2";
    UserDTO user = new UserDTO();
    user.setUsername(username);
    user.setPassword(new Md5PasswordEncoder().encodePassword(TestConfiguration.ACCOUNT_MANAGER_PASSWORD, username));
    user.setEmail("rest_test@openremote.de");
    user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
    user.addRole(new RoleDTO("ROLE_ADMIN", Long.valueOf(3)));
    user.setAccount(new AccountDTO(addedUser.getAccount().getOid()));

    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(user));
    Representation r = cr.post(rep);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", Long.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail("Impossible to create user: " + res.getErrorMessage());
    } else {
      addedUserOID2 = (Long)res.getResult();
      Assert.assertNotNull(res.getResult(),"Create user should return a value");
      Assert.assertTrue(res.getResult() instanceof Long, "Create user return value shoudl be a Long");
    }
  }
  
  /**
   * Check user name should not be available if a user with that name already exists.
   */
  @Test(dependsOnMethods = { "testAddUserToAccount" })
  public void testCheckUsernameAvailability2() throws Exception
  {
    String username = "REST_TEST2";
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/checkAvailabilty/" + username);
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation r = cr.get();
    String str = r.getText();
    Assert.assertEquals("{\"result\": \"false\"}", str, "User name should not be available for existing user");
  }
  
  /**
   * Validates availability check for user name is not case dependent.
   */
  @Test(dependsOnMethods = { "testAddUserToAccount" })
  public void testCheckUsernameAvailabilityIgnoreCase() throws Exception
  {
    String username = "rest_TEST2";
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/checkAvailabilty/" + username);
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation r = cr.get();
    String str = r.getText();
    Assert.assertEquals("{\"result\": \"false\"}", str, "User name, even with different case, should not be available for existing user");
  }
  
  /**
   * Invite user.
   */
  @Test(dependsOnMethods = { "testCreateUser" })
  public void testInviteUser() {
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID + "/inviteUser?inviteeEmail=mredeker@web.de&inviteeRoles=ROLE_ADMIN");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
    Representation r = cr.post(null);
    String str;
    try { 
      str = r.getText();
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", UserDTO.class).deserialize(str);
    UserDTO inviteeDTO = (UserDTO)res.getResult();
    addedUserOID3 = inviteeDTO.getOid();
    Assert.assertEquals("mredeker@web.de", inviteeDTO.getEmail(), "E-mail address of returned user object should be one used for invite");
    Assert.assertNull(res.getErrorMessage(), "Invite user should not return error message"); 
  }
  
  /**
   * Delete all user objects created by other tests in this class.
   */
  @Test(dependsOnMethods = { "testCreateUser", "testCreateUserViaCheckout", "testInviteUser", "testQueryAllUsers" })
  public void testDeleteUser() throws Exception
  {
      ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
      Representation result = cr.delete();
      String str = result.getText();
      GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertNull(res.getErrorMessage(), "Delete user should not return error message");
      
      cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID2);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
      result = cr.delete();
      str = result.getText();
      res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertNull(res.getErrorMessage(), "Delete user should not return error message");
      
      cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID3);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
      result = cr.delete();
      str = result.getText();
      res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertNull(res.getErrorMessage(), "Delete user should not return error message");
      
      cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/" + addedUserOID4);
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TestConfiguration.ACCOUNT_MANAGER_USER, TestConfiguration.ACCOUNT_MANAGER_PASSWORD);
      result = cr.delete();
      str = result.getText();
      res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertNull(res.getErrorMessage(), "Delete user should not return error message");
  }
  
  /**
   * Check user name should be available after a user with that name has been deleted.
   */
  @Test(dependsOnMethods = { "testDeleteUser" })
  public void testCheckUsernameAvailabilityAgain() throws Exception
  {
    String username = "rest_TEST2";
    ClientResource cr = new ClientResource(TestConfiguration.UAS_BASE_REST_URL + "user/checkAvailabilty/" + username);
    Representation r = cr.get();
    String str = r.getText();
    Assert.assertEquals("{\"result\": \"true\"}", str, "User name should be available once user has been deleted");
  }
  
}
