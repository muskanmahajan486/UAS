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
package org.openremote;

import java.io.InputStream;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class JavaMailSenderMock implements JavaMailSender {

  @Override
  public void send(SimpleMailMessage arg0) throws MailException {
    System.out.println("Sending mail");
  }

  @Override
  public void send(SimpleMailMessage[] arg0) throws MailException {
    System.out.println("Sending mail");
  }

  @Override
  public MimeMessage createMimeMessage() {
    return null;
  }

  @Override
  public MimeMessage createMimeMessage(InputStream arg0) throws MailException {
    return null;
  }

  @Override
  public void send(MimeMessage arg0) throws MailException {
    System.out.println("Sending mail");
  }

  @Override
  public void send(MimeMessage[] arg0) throws MailException {
    System.out.println("Sending mail");
  }

  @Override
  public void send(MimeMessagePreparator arg0) throws MailException {
    System.out.println("Sending mail");
  }

  @Override
  public void send(MimeMessagePreparator[] arg0) throws MailException {
    System.out.println("Sending mail");
  }

}
