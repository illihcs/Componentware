package de.fh_dortmund.inf.cw.chat.server.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException
public class LoginException extends Exception {

	public LoginException(String string) {
		super(string);
	}

}
