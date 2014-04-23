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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.GenericDAO;
import org.openremote.useraccount.domain.User;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.velocity.VelocityEngineUtils;

import flexjson.JSONSerializer;

/**
 * This class is handling all REST calls dealing with forgotten passwords 
 * @author marcus
 *
 */
public class ForgotPasswordCommandResource extends ServerResource
{
  private static Logger log = Logger.getLogger(ForgotPasswordCommandResource.class);
  
  public static final String FORGOT_PASSWORD_EMAIL_VM_NAME = "forgot-password-email.vm";

  private GenericDAO dao;
  private JavaMailSender mailSender;
  private VelocityEngine velocityEngine;
  private String designerWebappServerRoot;
  private TransactionTemplate transactionTemplate;
  private String emailFromAddress;

  /**
   * Generate a password token and send Email to user on how to reset his password <p>
   * <p>
   * REST Url: /rest/user/{userName}/forgotPassword -> return user<br>
   * 
   * @return one User
   */
  @Get("json")
  public Representation forgotPassword()
  {
    GenericResourceResultWithErrorMessage result = null;
    result = transactionTemplate.execute(new TransactionCallback<GenericResourceResultWithErrorMessage>() {
      @Override
      public GenericResourceResultWithErrorMessage doInTransaction(TransactionStatus transactionStatus)
      {
        try
        {
          String userName = (String) getRequest().getAttributes().get("userName");
          User user = dao.getByNonIdField(User.class, "username", userName);
          String passwordToken = UUID.randomUUID().toString();
          user.setToken(passwordToken);
          dao.merge(user);
          sendPasswordEmail(user, passwordToken, designerWebappServerRoot);
          return new GenericResourceResultWithErrorMessage(null, user);
        } catch (Exception e)
        {
          transactionStatus.setRollbackOnly();
          log.error("Can't send 'Reset password' email or update user", e);
          return new GenericResourceResultWithErrorMessage(e.getMessage(), null);
        }
      }
    });
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.users", "result.roles.users", "result.account.controllers").deepSerialize(result));
    return rep;
  }

  public void sendPasswordEmail(final User user, final String passwordToken, final String designerWebappServerRoot) throws Exception {
    
    MimeMessagePreparator preparator = new MimeMessagePreparator() {
       @SuppressWarnings("unchecked")
       public void prepare(MimeMessage mimeMessage) throws Exception {
          MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
          message.setSubject("OpenRemote Password Assistance");
          message.setTo(user.getEmail());
          message.setFrom(emailFromAddress);
          Map model = new HashMap();
          model.put("webapp", designerWebappServerRoot);
          model.put("username", user.getUsername());
          model.put("uid", user.getOid());
          model.put("aid", passwordToken);
          String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, FORGOT_PASSWORD_EMAIL_VM_NAME, "UTF-8", model);
          message.setText(text, true);
       }
    };
    this.mailSender.send(preparator);
 }

  public void setDao(GenericDAO dao)
  {
    this.dao = dao;
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

  public void setTransactionTemplate(TransactionTemplate transactionTemplate)
  {
    this.transactionTemplate = transactionTemplate;
  }

  public void setEmailFromAddress(String emailFromAddress) {
    this.emailFromAddress = emailFromAddress;
  }

}
