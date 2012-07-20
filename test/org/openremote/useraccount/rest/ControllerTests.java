package org.openremote.useraccount.rest;

import org.junit.Assert;
import org.junit.Test;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.domain.AccountDTO;
import org.openremote.useraccount.domain.ControllerDTO;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class ControllerTests
{

  private static ControllerDTO addedController;
  

  /**
   * Test: Announce controller with no macAddresses
   */
  @Test
  public void testCreateAnnounceControllerWithNoMACAddresses() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.post(null);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() == null) {
      Assert.fail("we expected an error message");
    } else {
      Assert.assertEquals(res.getErrorMessage(), "No macAddresses were provided");
    }
  }
  
  /**
   * Test: Announce controller
   */
  @Test
  public void testCreateAnnounceController() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/announce/"+ getMACAddresses1());
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
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
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
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
   * Test: Update controller with account
   */
  @Test
  public void testUpdateController() throws Exception
  {
    AccountDTO account = new AccountDTO(1);
    addedController.setLinked(true);
    addedController.setAccount(account);
    
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(addedController));
    Representation r = cr.put(rep);

    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertTrue(tempController.isLinked());
      Assert.assertEquals(account.getOid(), tempController.getAccount().getOid());
    }
  }
  
  
  /**
   * Test: Announce controller with second macAddress after linked to an account
   */
  @Test
  public void testCreateAnnounceControllerAfterLink() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/announce/"+ getMACAddresses2());
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
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
      Assert.assertEquals(1, tempController.getAccount().getOid().longValue());
    }
  }
  
  /**
   * Test: Find controller via macAddress
   */
  @Test
  public void testFindControllerViaMacAddress() throws Exception
  {
    ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/find/"+ getMACAddresses1());
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
    Representation r = cr.get();
    String str = r.getText();
    GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", ControllerDTO.class).deserialize(str); 
    if (res.getErrorMessage() != null) {
      Assert.fail(res.getErrorMessage());
    } else {
      ControllerDTO tempController = (ControllerDTO)res.getResult();
      Assert.assertNotNull(tempController);
      Assert.assertEquals(getMACAddresses2(), tempController.getMacAddress());
      Assert.assertEquals(addedController.getOid(), tempController.getOid());
      Assert.assertEquals(1, tempController.getAccount().getOid().longValue());
    }
  }
  
  /**
   * Test: delete controller
   */
  @Test
  public void testDeleteController() throws Exception
  {
      ClientResource cr = new ClientResource("http://localhost:8080/uas/rest/controller/" + addedController.getOid());
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "designer_appl", "password");
      Representation result = cr.delete();
      String str = result.getText();
      GenericResourceResultWithErrorMessage res =new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", String.class).deserialize(str); 
      Assert.assertEquals(null, res.getErrorMessage());
      Assert.assertEquals(null, res.getResult());
  }
  
  
  private String getMACAddresses1()
  {
    return "11-22-33-44-55-66";
  }
  
  private String getMACAddresses2()
  {
    return "aa-bb-cc-dd-ee-ff,11-22-33-44-55-66";
  }
}
