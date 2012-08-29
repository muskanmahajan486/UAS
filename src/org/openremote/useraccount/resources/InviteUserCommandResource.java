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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.GenericDAO;
import org.openremote.useraccount.domain.Role;
import org.openremote.useraccount.domain.User;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.velocity.VelocityEngineUtils;

import flexjson.JSONSerializer;

/**
 * This class is handling all REST calls dealing with forgotten passwords
 * 
 * @author marcus
 * 
 */
public class InviteUserCommandResource extends ServerResource
{
  private static Logger log = Logger.getLogger(InviteUserCommandResource.class);

  public static final String REGISTRATION_INVITATION_EMAIL_VM_NAME = "registration-invitation-email.vm";

  private GenericDAO dao;
  private JavaMailSenderImpl mailSender;
  private VelocityEngine velocityEngine;
  private String designerWebappServerRoot;
  private TransactionTemplate transactionTemplate;

  /**
   * Create a temporary user based on invitee email address and send invitation email
   * <p>
   * REST Url: /rest/user/{userId}/inviteUser?inviteeEmail=xyz&inviteeRole=xyz -> return new user
   * <br>
   * 
   * @return one User
   */
  @Post
  public Representation inviteUser()
  {
    GenericResourceResultWithErrorMessage result = null;
    Form queryParams = getQuery();
    final String inviteeEmail = queryParams.getFirstValue("inviteeEmail", true);
    final String inviteeRoles = queryParams.getFirstValue("inviteeRoles", true);

    result = transactionTemplate.execute(new TransactionCallback<GenericResourceResultWithErrorMessage>() {
      @Override
      public GenericResourceResultWithErrorMessage doInTransaction(TransactionStatus transactionStatus)
      {
        try
        {
          if ((inviteeEmail == null) || (inviteeRoles == null))
          {
            throw new Exception("No invitee email or role was provided");
          }
          String oidString = (String) getRequest().getAttributes().get("userOid");
          Long oid = Long.valueOf(oidString);
          User user = dao.getById(User.class, oid);
          User invitee = null;
          if (!hasPendingInvitation(inviteeEmail))
          {
            invitee = new User(user.getAccount());
            invitee.setEmail(inviteeEmail);
            invitee.setUsername(inviteeEmail);
            invitee.setPassword("pending password");
            invitee.setRegisterTime(new Timestamp(System.currentTimeMillis()));
            StringTokenizer roles = new StringTokenizer(inviteeRoles, ",");
            while (roles.hasMoreElements())
            {
              String inviteeRole = (String) roles.nextElement();
              invitee.addRole(getRoleByName(inviteeRole));
            }
            dao.merge(invitee.getAccount());
            dao.save(invitee);
            sendInvitationEmail(invitee, user, designerWebappServerRoot);
          } else
          {
            throw new Exception("Pending invitation exists for email: " + inviteeEmail);
          }
          return new GenericResourceResultWithErrorMessage(null, invitee);
        } catch (Exception e)
        {
          transactionStatus.setRollbackOnly();
          log.error("Error creating invitation", e);
          return new GenericResourceResultWithErrorMessage(e.getMessage(), null);
        }
      }
    });
    Representation rep = new JsonRepresentation(new JSONSerializer().exclude("*.class", "result.account.users", "result.roles.users",
            "result.account.controllers").deepSerialize(result));
    return rep;
  }

  protected Role getRoleByName(String inviteeRole)
  {
    Role role = dao.getByNonIdField(Role.class, "name", inviteeRole);
    return role;
  }

  protected boolean hasPendingInvitation(String inviteeEmail)
  {
    User tmp = dao.getByNonIdField(User.class, "username", inviteeEmail);
    return (tmp != null);
  }

  public void sendInvitationEmail(final User invitee, final User currentUser, final String designerWebappServerRoot) throws Exception
  {

    MimeMessagePreparator preparator = new MimeMessagePreparator() {
      public void prepare(MimeMessage mimeMessage) throws Exception
      {
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        message.setSubject("Invitation to Share an OpenRemote Designer Account");
        message.setTo(invitee.getEmail());
        message.setFrom(mailSender.getJavaMailProperties().getProperty("mail.from"));
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("uid", invitee.getOid());
        model.put("role", invitee.getRole());
        model.put("cid", currentUser.getOid());
        model.put("host", currentUser.getEmail());
        model.put("webapp", designerWebappServerRoot);
        model.put("aid", new Md5PasswordEncoder().encodePassword(invitee.getEmail(), currentUser.getPassword()));
        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, REGISTRATION_INVITATION_EMAIL_VM_NAME, "UTF-8", model);
        message.setText(text, true);
      }
    };
    this.mailSender.send(preparator);
  }

  public void setDao(GenericDAO dao)
  {
    this.dao = dao;
  }

  public void setMailSender(JavaMailSenderImpl mailSender)
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

}
