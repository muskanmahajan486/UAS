package org.openremote.useraccount;

import java.io.IOException;
import java.sql.Timestamp;

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

public class CreateAccountTool {

  private static final String ADMIN_USER = "designer_appl";
  private static final String ADMIN_PASSWORD = "";

  private static final String USER_NAME = "matthew.shane";
  private static final String USER_EMAIL = "mshane@linearcorp.com";
  private static final String USER_PASSWORD = "gjhsqhghf";

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    UserDTO user = new UserDTO();
    user.setUsername(USER_NAME);
    user.setPassword(new Md5PasswordEncoder().encodePassword(USER_PASSWORD, USER_NAME));
    user.setEmail(USER_EMAIL);
    user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
    user.addRole(new RoleDTO("ROLE_ADMIN", Long.valueOf(3)));
    user.setAccount(new AccountDTO());

    ClientResource cr = new ClientResource("http://designer.openremote.com/uas/rest/user");
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, ADMIN_USER, ADMIN_PASSWORD);
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(user));
    Representation r = cr.post(rep);
    String str = r.getText();
    GenericResourceResultWithErrorMessage res = new JSONDeserializer<GenericResourceResultWithErrorMessage>().use(null, GenericResourceResultWithErrorMessage.class).use("result", Long.class).deserialize(str);
    if (res.getErrorMessage() != null) {
      System.out.println("Account creation failed : " + res.getErrorMessage());
    } else {
      System.out.println("Created user account, oid : " + res.getResult());
    }
  }

}
