package de.fh_dortmund.inf.cw.autofinanzierung.server.beans;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import de.fh_dortmund.inf.cw.autofinanzierung.server.beans.interfaces.FinanzierungsRechnerLocal;
import de.fh_dortmund.inf.cw.autofinanzierung.server.beans.interfaces.FinanzierungsRechnerRemote;

@Stateless
public class FinanzierungsRechnerBean implements FinanzierungsRechnerLocal, FinanzierungsRechnerRemote{

	@Resource(name="zinssatz")
	private double zinssatz;
	
	public FinanzierungsRechnerBean() {}
	
	public double getZinssatz()
	{
		return zinssatz;
	}
	
	public double getNettoDarlehenssumme(double preis, double anzahlung){
		return preis-anzahlung;
	}
	
	/**
	 * @param preis
	 *          Kaufpreis
	 * @param anzahlung
	 *          Anzahlung des Kaufpreises
	 * @param monate
	 *          Laufzeit der Finanzierung
	 * @return
	 * 			Errechnet den Darlehensbetrag aus, der sich aus Nettodarlehen + Zinseszinsen errechnet.
	 */
	@Override
	public double getDarlehenssumme(double preis, double anzahlung, int monate) {
		return getNettoDarlehenssumme(preis, anzahlung) + getZinseszinses(getNettoDarlehenssumme(preis, anzahlung), monate);
	}

	/**
	 * @param preis
	 *          Kaufpreis
	 * @param anzahlung
	 *          Anzahlung des Kaufpreises
	 * @param monate
	 *          Laufzeit der Finanzierung
	 * @return
	 * 			Monatliche Raten, die der Kunde bezahlen muss, um nach "monate" Monaten die Finanzierung abgeschlossen zu haben.
	 */
	@Override
	public double getMonatlicheRaten(double preis, double anzahlung, int monate) {		
		return getDarlehenssumme(preis, anzahlung, monate)/12.0;		
	}

	
	/**
	 * @param nettodarlehensbetrag
	 *          preis - anzahlung
	 * @param monate
	 *          Laufzeit in Monaten
	 * @return
	 * 			Berechnet die Zinseszinsen für die angegebenen Monate.
	 */
	private double getZinseszinses(double nettodarlehensbetrag, int monate) {
		double monatlicheTilgung = nettodarlehensbetrag/monate;
		double monatlicheZinsen = getZinssatz()/100.0/12.0;
		double tempNettodarlehensbetrag = nettodarlehensbetrag;
		double zinsBetrag = 0;
		
		for(int i = 0; i < monate; i++){
			zinsBetrag += tempNettodarlehensbetrag * monatlicheZinsen;
			tempNettodarlehensbetrag -= monatlicheTilgung;
		}
		
		return zinsBetrag;
	}
}
