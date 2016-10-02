/* OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
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

package org.openremote.useraccount.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.GenericDAO;
import org.openremote.useraccount.domain.Account;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.velocity.VelocityEngineUtils;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * This class is handling all REST calls dealing with one user, URL: /rest/user 
 * @author marcus
 *
 */
public class UserCommandsResource extends ServerResource
{
  public static final String REGISTRATION_ACTIVATION_EMAIL_VM_NAME= "registration-activation-email.vm";
  public static final String CHECKOUT_REGISTRATION_ACTIVATION_EMAIL_VM_NAME= "checkout_registration-activation-email.vm";
  
  
  private GenericDAO dao;
  private TransactionTemplate transactionTemplate;
  private JavaMailSender mailSender;
  private VelocityEngine velocityEngine;
  private String designerWebappServerRoot;
  private String emailFromAddress;

  /**
   * Return one user based on his oid<p>
   * <p>
   * REST Url: /rest/user/{userOid} -> return user<br>
   * 
   * @return one User
   */
  @Get("json")
  public Representation getUser()
  {
    GenericResourceResultWithErrorMessage result = null;
    try
    {
      String oidString = (String) getRequest().getAttributes().get("userOid");
      Long oid = Long.valueOf(oidString);
      User user = dao.getById(User.class, oid);
      result = new GenericResourceResultWithErrorMessage(null, user);
    } catch (Exception e)
    {
      result = new GenericResourceResultWithErrorMessage(e.getMessage(), null);
    }
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.users", "result.roles.users", "result.account.controllers").deepSerialize(result));
    return rep;
  }

  /**
   * Add the given user to the database
   * POST data has to contain a user as JSON string
   * REST POST Url:/rest/user
   * @param data
   * @return OID of the saved user
   */
  @Post("json:json")
  public Representation createUser(final Representation data)
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
              User newUser = new JSONDeserializer<User>().use(null, User.class).deserialize(jsonData);
              if( (newUser.getAccount() == null) || (newUser.getAccount().getOid() == 0)) {
                newUser.setAccount(new Account());
                dao.save(newUser.getAccount());
              } else {
                dao.merge(newUser.getAccount());
              }
              List<Role> dbRoles = new ArrayList<Role>();
              for (Role role : newUser.getRoles())
              {
                if (role.getOid() == 0) {
                  role = dao.getByNonIdField(Role.class, "name", role.getName()); 
                }
                dbRoles.add(role);
              }
              newUser.setRoles(dbRoles);
              String randomPassword = null;
              if (newUser.getPassword() == null) {
                randomPassword = generateRandomPassword();
                newUser.setPassword(new Md5PasswordEncoder().encodePassword(randomPassword, newUser.getUsername()));
              }
              dao.save(newUser);
              sendRegistrationEmail(newUser, designerWebappServerRoot, randomPassword);
              return new GenericResourceResultWithErrorMessage(null, newUser.getOid());
            } catch (Exception e) {
              transactionStatus.setRollbackOnly();
              return new GenericResourceResultWithErrorMessage(e.getMessage(), null);
            }
          }
        });

        rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(result));
      }
    }
    return rep;
  }


  /**
   * Update the given user
   * PUT data has to contain user as JSON string
   * REST PUT Url:/rest/user
   * @param data
   * @return the updated user
   */
  @Put("json:json")
  public Representation updateUser(final Representation data)
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
              User changedUser = new JSONDeserializer<User>().use(null, User.class).deserialize(jsonData);
              for (Role role : changedUser.getRoles())
              {
                dao.merge(role);
              }
              User savedUser = (User)dao.merge(changedUser);
              return new GenericResourceResultWithErrorMessage(null, savedUser);
            } catch (Exception e) {
              transactionStatus.setRollbackOnly();
              return new GenericResourceResultWithErrorMessage(e.getMessage(), null);
            }
          }
        });
        rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.users", "result.roles.users", "result.account.controllers").deepSerialize(result));
      }
    }
    return rep;
  }


  /**
   * Delete the user with the given id
   * REST Url: /rest/user/{userOid} 
   * @return
   */
  @Delete("json")
  public Representation deleteUser()
  {
    Representation rep = null;
    GenericResourceResultWithErrorMessage result = null;
    String oidString = (String) getRequest().getAttributes().get("userOid");
    if (oidString != null)
    {
      try
      {
        Long oid = Long.valueOf(oidString);
        User user = dao.getById(User.class, oid);
        dao.delete(user);
        result = new GenericResourceResultWithErrorMessage(null, null);
      } catch (Exception e)
      {
        result = new GenericResourceResultWithErrorMessage(e.getMessage(), null);
      }
    } else {
      result = new GenericResourceResultWithErrorMessage("No userOid found in URL", null);
    }
    rep = new JsonRepresentation(new JSONSerializer().exclude("*.class").deepSerialize(result));
    return rep;
  }
  
  
  
  private void sendRegistrationEmail(final User user, final String designerWebappServerRoot, final String randomPassword) throws Exception {
    
    MimeMessagePreparator preparator = new MimeMessagePreparator() {
       public void prepare(MimeMessage mimeMessage) throws Exception {
         String templateName = REGISTRATION_ACTIVATION_EMAIL_VM_NAME;
         MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
         message.setSubject("OpenRemote Designer Account Registration");
         message.setTo(user.getEmail());
         message.setFrom(emailFromAddress, "OpenRemote");
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("user", user);
         model.put("webapp", designerWebappServerRoot);
         if (randomPassword != null) {
           message.setSubject("OpenRemote Pro Account Activation");
           model.put("password", randomPassword);
           templateName = CHECKOUT_REGISTRATION_ACTIVATION_EMAIL_VM_NAME;
         }
         // TODO : this needs to be fixed (MR: comment was taken from original line in designer)
         model.put("aid", new Md5PasswordEncoder().encodePassword(user.getUsername(), user.getPassword()));
         String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateName, "UTF-8", model );
         message.setText(text, true);
       }
    };
    this.mailSender.send(preparator);
 }
  
  
  private String generateRandomPassword() {
    int passwordLength = 8;  
    StringBuffer sb = new StringBuffer();  
    for (int x = 0; x < passwordLength; x++)  
    {  
      sb.append((char)((int)(Math.random()*26)+97));  
    }  
    return sb.toString();
  }
  
  public void setDao(GenericDAO dao)
  {
    this.dao = dao;
  }

  public void setTransactionTemplate(TransactionTemplate transactionTemplate)
  {
    this.transactionTemplate = transactionTemplate;
  }

  public void setMailSender(JavaMailSender mailSender)
  {
    this.mailSender = mailSender;
  }

  public void setVelocityEngine(VelocityEngine velocityEngine)
  {
    this.velocityEngine = velocityEngine;
  }

  public void setDesignerWebappServerRoot(String designerWebappServerRoot)
  {
    this.designerWebappServerRoot = designerWebappServerRoot;
  }

  public void setEmailFromAddress(String emailFromAddress) {
    this.emailFromAddress = emailFromAddress;
  }
  
}
