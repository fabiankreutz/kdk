package kdk.program;

import java.awt.Point;
import java.util.Vector;


/** Klasse zur Speicherung der Eigenschaften eines Roboters.
  Eine Instanz dieser Klasse wird im Konsturktor permanent mit
  diesem verbunden, somit liegen einige Werte durch den Konstrukt fest,
  andere sind w&auml;rend das Spiels &auml;nderbar. */

public final class Werte
{


/** Kosten der Eigenschaften verschiedener Robotertypen */
	static final int[][] kosten = {
/* kein Typ */	{2, 3, 2, 2, 2},
/* Parasit */	{3, 9, 1, 3, 2},
/* Mine */	{2, 6, 5, 1, 2},
/* Jäger */	{4, 5, 1, 4, 1},
/* Brüter */	{1, 2, 5, 4, 3}};
/** Maximalkosten die Robotertypen w&auml;hlen k&ouml;nnen */
	static final int[] maxkosten = {50, 50, 50, 50, 50};
	static final int[] regeneration = {2,-1,1,1,3};
	static final int[][] schusskosten = {
		{1, 1, 2, 2, 2, 3, 3, 3, 4},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{4, 2, 1, 1, 2, 2, 2, 2, 3},
		{2, 3, 4, 5, 5, 6, 6, 7, 8}};
	static final int[][] sensorkosten = {
		{0, 1, 1, 1, 2, 2, 2, 3, 3},
		{1, 1, 2, 2, 3, 3, 3, 4, 4},
		{0, 1, 1, 3, 5, 5, 5, 3, 3},
		{0, 1, 2, 3, 4, 5, 4, 3, 2},
		{1, 2, 3, 4, 5, 6, 7, 8, 9}};

	private int akt_signatur, signatur;
	private int akt_energie, maxenergie;
	private int akt_fertilitaet, fertilitaet;
	private int typ;
	private int[] verbuendete;
	private Point pos = null;
	private int akt_mobilitaet, mobilitaet;
	private int akt_ssenergie, ssenergie;
	private int visibilitaet;
	private Robot1 parasit = null;
	private Point marked = null;
	private Vector marker = new Vector();
	private int infektionsdauer = 0;

/** Erstellt eine neue Instanz der KdkWerte. Die Parameter liegen in
  dieser Instanz dann fest und k&ouml;nnen nicht mehr vom Roboter
  ver&auml;ndert werden.
 * @param energie Energiewert des Roboters.
 * @param signatur Identifikationsmerkmal
 * @param fertilitaet Anzahl der Runden bis zur Vermehrung
 * @param typ Typ des Roboters
 * @param verbuendete Feld von Signaturen anderer Roboter */

	public Werte(int energie, int signatur, int fertilitaet,
				int typ, int[] verbuendete)
	{
		if ((energie > 8) || (energie < 1))
			energie = 5;
		akt_energie = energie;
		maxenergie = 2*energie;
		if ((fertilitaet < 2) || (fertilitaet > 10))
			fertilitaet = 6;
		akt_fertilitaet = fertilitaet;
		this.fertilitaet = fertilitaet;
		this.signatur = signatur;
		akt_signatur = signatur;
		this.typ = typ;
		this.verbuendete = verbuendete;
	}


/** Errechnet, ob die Energiewerte des Roboters innerhalb der
  Toleranzen liegen. */

	public final int validate()
	{
		int fert;
		if (fertilitaet==0)
			fert=10;
		else
			fert=10-fertilitaet;
		int[] werte = {maxenergie/2, fert,
			mobilitaet, visibilitaet,
			ssenergie};
		int result = 0;
		for (int i=0; i<werte.length; i++)
			result += werte[i]*kosten[typ][i];
		return (result - maxkosten[typ]);
	}


/** Pr&uml;ft entg&uuml;ltig, ob der Roboter Schrott ist, regeneriert andernfalls die verbliebenen Bewegungspunkte in Energie, bzw zieht einen Punkt vom Parasiten ab. */

	void regeneriere()
	{
		if (akt_energie <= 0)
			throw new KdKException("Roboter ist Schrott");
		if (akt_fertilitaet > 0)
			akt_fertilitaet--;
		if (isInfiziert())
		{
			if (getInfektionsDauer()<5)
				infektionsdauer++;
			else
				throw new KdKException("Ausbruch des Parasiten");
		}
		if (typ == Robot1.TYP_PARASIT)
			if (akt_energie - 1 <= 0)
				throw new KdKException("Parasit verhungert");
		if (!isInfiziert())
			akt_energie = Math.min(
				maxenergie,
				akt_energie + regeneration[typ]);
		akt_ssenergie = ssenergie;
		akt_mobilitaet = mobilitaet;
	}


/** Infiziert diese Instanz. */

	void infekt(Robot1 p)
	{
		parasit = p;
		infektionsdauer = 0;
	}


/** Gibt den Parasiten zur&uuml;ck */

	Robot1 getParasit()
	{
		return parasit;
	}


/** Gibt an, ob diese Instanz von einem Parasiten infiziert ist. */

	boolean isInfiziert()
	{
		return (parasit != null);
	}


/** Gibt an, wie lange die Infektion her ist. */

	int getInfektionsDauer()
	{
		return infektionsdauer;
	}


/** Gibt die aktuelle Position des Roboters zur&uuml;ck. */

	public Point getPosition()
	{
		return new Point(pos.x,pos.y);
	}

	public int getEnergie()
	{
		return akt_energie;
	}


/** Gibt ein Feld mit den Signaturen verb&uuml;ndeter Roboterklassen zur&uuml;ck. Ein Roboter wird sofort abgewiesen, wenn es bei seiner Initiierung Konflikte mit anderen oder nicht erwiederten B&uuml;ndnissen gibt. */

	int[] getVerbuendete()
	{
		return verbuendete;
	}

	public int getVisibilitaet()
	{
		return visibilitaet;
	}

	public int getFertilitaet()
	{
		return akt_fertilitaet;
	}


/** Die Signatur, die der Roboter im Spiel sendet, die also ein anderer empf&auml;ngt, der sie abfragt. */

	public int getSendSignatur()
	{
		return akt_signatur;
	}


/** Die Roboterspezifische echte Signatur, die zur &Uuml;berpr&uuml;fung von B&uuml;ndnissen abgefragt wird. */

	public int getRealSignatur()
	{
		return signatur;
	}
		
	public boolean isFertil()
	{
		return (akt_fertilitaet == 0);
	}

	public int getTyp()
	{
		return typ;
	}

	public int getMobilitaet()
	{
		return akt_mobilitaet;
	}

	public int getRundenEnergie()
	{
		return ssenergie;
	}

	void setPosition(Point p)
	{
		pos = new Point(p.x,p.y);
		for (int i=0; i<marker.size(); i++)	
		{
			Robot1 m = (Robot1)marker.elementAt(i);
			m.getWerte().setMarked(p);
		}
	}


/** Addiert den &uuml;bergebenen Wert zur aktuellen Energie. */

	void deltaEnergie(int wert)
	{
		akt_energie += wert;
	}


/** Zieht den &uuml;bergebenen Wert von der Mobilit&auml;t ab. */
	void deltaMobilitaet(int wert)
	{
		akt_mobilitaet -= wert;
	}


/** Zieht eins von der Anzahl der Sch&uuml;sse in dieser Runde ab. */

	boolean schuss(int wert, int abstand)
	{
		int kosten = wert*(schusskosten[getTyp()][abstand-1]);
		if (wert>4)
			kosten += (wert-5);
		akt_ssenergie-=kosten;
		return (akt_ssenergie>=0);
	}


/** Zieht eins von der Anzahl der Sensorscans in dieser Runde ab. */

	boolean sensor(int wert, int abstand)
	{
		int kosten = wert*(sensorkosten[getTyp()][abstand-1]);
		if (wert>4)
			kosten += (wert-5);
		akt_ssenergie-=kosten;
		return (akt_ssenergie>=0);
	}

	void setUnFertil()
	{
		akt_fertilitaet = fertilitaet;
	}

	public void setMobilitaet(int wert)
	{
		if ((wert <= 9) && (wert >= 0))
			mobilitaet = wert;
	}

	public void setRundenEnergie(int wert)
	{
		if (wert >= 0)
			ssenergie = wert;
	}

	public void setVisibilitaet(int wert)
	{
		if ((wert <= 5) && (wert >= 0))
			visibilitaet = wert;
	}

	void setMarker(Robot1 r)
	{
		marker.add(r);
		r.getWerte().setMarked(getPosition());
	}

	void removeMarker(Robot1 r)
	{
		marker.remove(r);
	}

	void setMarked(Point p)
	{
		marked = p;
	}

	Point getMarked()
	{
		return marked;
	}

}
