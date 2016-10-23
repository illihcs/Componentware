package de.fh_dortmund.inf.cw.chat.server.beans.interfaces;

import de.fh_dortmund.inf.cw.chat.server.entities.User;
//import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;

public interface Chat {

	/**
	 * Loggt einen Benutzer ein. Benutzername und Passwiórt werden überprüft.
	 * 
	 * @param user
	 *            Benutzer, der eingeloggt werden soll.
	 * @return Den eingeloggten Benutzer
	 * @throws ChatException
	 *             Wenn der Login fehlschlägt
	 */
	public User login(User user) throws Exception;

	/**
	 * Loggt den aktuellen Benutzer aus.
	 * 
	 * @throws ChatException
	 *             Wenn der Logout fehlschlägt
	 */
	public void logout() throws Exception;

	/**
	 * Gibt den aktuell eingeloggten Benutzer.
	 * 
	 * @return Den eingeloggten Benutzer oder null
	 */
	public User getUser();

	/**
	 * Ändert das Passwort des aktuellen Benutzers. Eine Überprüfung des
	 * aktuellen Passwortes wird zu Sicherheit durchgeführt.
	 * 
	 * @param oldPasswordHash
	 *            Aktuelles Passwort
	 * @param newPasswordHash
	 *            Neues Passwort
	 * @return Der neue Benutzer
	 * @throws ChatException
	 *             Wenn bei der Änderung ein Fehler auftritt
	 */
	public User changePassword(String oldPasswordHash, String newPasswordHash) throws Exception;

	/**
	 * Gibt die aktuelle Benutzerstatistik des Benutzers
	 * 
	 * @return Die Benutzerstatistik
	 * @throws ChatException
	 *             Wenn die Statistik nicht gebildet werden konnte
	 */
	//public UserStatistic getStatistic();

	/**
	 * Löscht den aktuellen Benutzer. Eine Überprüfung des Passwortes wird zu
	 * Sicherheit durchgeführt.
	 * 
	 * @param passwordHash
	 *            Passwort des Benutzers
	 * @throws ChatException
	 *             Wenn beim Löschen ein Felher auftritt
	 */
	public void deleteUser(String passwordHash) throws Exception;

	/**
	 * Schließt den aktuellen chat und gibt alle Ressourcen frei.
	 */
	public void close();

}