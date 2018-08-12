package kdk.program;

import java.awt.Point;
import java.util.Vector;

/** Die Aktions-ausf&uuml;hrende Klasse.<BR>
  Jeder Roboter bekommt mit seiner Zug-Routine eine Instanz dieser Klasse
  mitgeliefert, &uuml;ber die er Einflu&szlig; auf das Spiel nehmen kann.<BR>
  Nach Ende des Zuges wird diese Klasse unbrauchbar gemacht. */

public class ActionProcessor
{

	static final int AKTION_SCHUSS = 310;
	static final int AKTION_SENSOR = 311;
	static final int AKTION_EXPLOD = 312;
	static final int AKTION_PING   = 313;
	static final int AKTION_KOMM   = 314;

	private KdKFeld kf;
	private Robot1 r;
	private Werte w;
	private boolean inaktion = false;


/** Erstellt eine neue Instanz eines ActionProcessors */

	public ActionProcessor(KdKFeld kf, Robot1 r)
	{
		this.kf = kf;
		this.r = r;
		this.w = r.getWerte();
	}


/**  F&uuml;hrt einen Schuss auf ein Zielfeld aus. R&uuml;ckgabewert ist der Erfolg
 der Aktion. Der Schuss gilt als Erfolg, wenn bez&uuml;glich der Enfernung und
 Intensit&auml;t genug Schussenergie zur Verf&uuml;gung steht. */

	public boolean processSchuss(Point ziel, int ensitaet)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if ((w.getTyp() == Robot1.TYP_PARASIT) ||
		    (w.getTyp() == Robot1.TYP_MINE))
			return false;
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		int p = r.getAbstand(ziel);
		if (p>9)
			 return false;
		if ((!w.schuss(ensitaet,p)) || (r2 == null))
			return false;
		Werte w2 = r2.getWerte();
		w2.deltaEnergie(-ensitaet);
		if (w2.getEnergie() <= 0)
			kf.removeRobot(r2);
		else
			r2.addLog(AKTION_SCHUSS,w.getPosition(),ensitaet);
		return true;
	}


/** F&uuml;hrt einen Sensorscan aus. Der R&uuml;ckgabewert liefert einen Erfolg, wenn
  ein Roboter auf dem Zielfeld entdeckt wurde. Ein Misserfolg kann durch
  zu geringe Energie bez&uuml;glich der Entfernung und Intensit&auml;t, durch zu
  geringe Intensit&auml;t bez&uuml;glich des Tarnschildes des fremden Roboters oder
  einfach durch ein leeres Zielfeld entstehen. */

	public boolean processSensor(Point ziel, int ensitaet)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		int p = r.getAbstand(ziel);
		if (p>9)
			 return false;
		if (p==0)
			return true;
		int kosten = (ensitaet*w.sensorkosten[w.getTyp()][p-1]);
		if ((!w.sensor(ensitaet,p)) || (r2 == null))
			return false;
		if (p>1)
			r2.addLog(AKTION_SENSOR,w.getPosition(),ensitaet);
		if (r2.getWerte().getVisibilitaet()<ensitaet)
			return true;
		else
			return false;
	}


/** Vermehrt den Roboter. Der Erfolg h&auml;ngt von der Fertilit&auml;t und dem
  Inhalt des Zielfeldes ab. Ein Parasit kann sich auf ein besetztes Feld
  hin instantiieren. Dabei frisst das Junge den fremden Roboter sofort auf.
  Ist der Parasit nicht fertil, findet eine Infektion des gegnerischen
  Roboters ab. */

	public boolean processVermehren(Point ziel)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if (r.getAbstand(ziel)>1)
			return false;
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		if (w.getTyp() == Robot1.TYP_PARASIT)
		{
			if (w.isFertil())
			{
				int frass = 0;
				if (r2 != null)
				{
					kf.removeRobot(r2);
					frass = r2.getWerte().getEnergie();
				}
				else
					w.setUnFertil();
				inaktion = true;
				kf.addRobot(ziel.x, ziel.y, r.instantiiere());
				inaktion = false;
				kf.getFeld(ziel.x, ziel.y).getWerte().deltaEnergie(frass);
				return true;
			}
			else
				if (r2 == null)
					return false;
				else
				{
					inaktion = true;
					r2.getWerte().infekt(r.instantiiere());
					inaktion = false;
					return true;
				}
		}
		if (!w.isFertil())
			return false;
		if (r2 != null)
		{
			if (r2.getWerte().getTyp() == Robot1.TYP_PARASIT)
			{
				w.setUnFertil();
				r2.getWerte().deltaEnergie(2);
			}
			return false;
		}
		inaktion = true;
		kf.addRobot(ziel.x, ziel.y, r.instantiiere());
		inaktion = false;
		w.setUnFertil();
		return true;
	}


/** L&auml;sst die Mine explodieren. Dabei tr&auml;gt jeder nicht-verb&uuml;ndete Roboter
  in einem Feld Abstand einen Schaden von der doppelten &uuml;brigen Energie
  der Mine davon. Verb&uuml;ndete Roboter erleiden keinen Schaden.<BR>
  Die Mine wird durch diese Aktion zerst&ouml;rt, ist sie aber zu diesem
  Zeitpunkt fertil, so entsteht eine neue Mine in der Asche. */

	public void processExplosion()
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if (w.getTyp() != Robot1.TYP_MINE)
			return;
		int schaden = w.getEnergie()*2;
		Point[] umkreis = r.getUmgebung(1);
UMKREISFOR:	for (int i=0; i<umkreis.length; i++)
		{
			Robot1 r2 = kf.getFeld(umkreis[i].x, umkreis[i].y);
			if (r2 == null)
				continue UMKREISFOR;
			if (!kf.isVerbuendet(r,r2))
			{
				Werte w2 = r2.getWerte();
				w2.deltaEnergie(-schaden);
				if (w2.getEnergie() < 1)
					kf.removeRobot(r2);
				else
					r2.addLog(AKTION_EXPLOD,w.getPosition(),schaden);
			}
		}
		Point pos = w.getPosition();
		inaktion = true;
		if (w.isFertil())
			kf.addRobot(pos.x, pos.y, r.instantiiere());
		w.deltaEnergie(w.getEnergie());
		dispose();
		throw new KdKException("Für die Rasse !");
	}


/** F&uuml;hrt eine Bewegung des Roboters aus. Der Erfolg h&auml;ngt von den noch
  verf&uuml;gbaren Bewegungspunkten ab und dem Inhalt des Zielfeldes ab. Ist
  das Zielfeld von einem Parasiten besetzt, so wird der Roboter gefressen,
  jeder andere Roboter verhindert die Bewegung vollst&auml;ndig. */

	public boolean processBewegung(Point ziel)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		int p = r.getAbstand(ziel);
		if (p > w.getMobilitaet())
			return false;
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		if (r2 != null)
		{
			Werte w2 = r2.getWerte();
			if (w.getTyp() == Robot1.TYP_PARASIT)
			{
				w.deltaEnergie(w2.getEnergie());
				kf.removeRobot(r2);
			}
			else if (w2.getTyp() == Robot1.TYP_PARASIT)
			{
				w2.deltaEnergie(w.getEnergie());
				inaktion = true;
				w.deltaEnergie(-w.getEnergie());
				dispose();
				throw new KdKException("In den Schlund");
			}
			else
				return false;
		}
		w.deltaMobilitaet(p);
		kf.move(w.getPosition(),ziel);
		w.setPosition(ziel);
		return true;
	}


/** Sendet einen Ping an alle Roboter im Abstand der Intensit&auml;t. Die maximale
  Intensit&auml;t betr&auml;gt hierbei 9. Alle Roboter in diesem Umkreis erhalten einen
  Eintrag in ihr Logbuch, dabei wird auch die Position und die Sendesignatur
  des Roboters gesendet. */

	public void processPing(int ensitaet)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		Robot1 r2;
		if (ensitaet > 9)
			ensitaet = 9;
		Vector robs = kf.getNachbarn(r,ensitaet);
		for (int i=0; i<robs.size(); i++)
		{
			r2 = (Robot1)robs.elementAt(i);
			r2.addLog(AKTION_PING,w.getPosition(),w.getSendSignatur());
		}
	}


/** Dieser kostenlose Scan f&uuml;hrt zu einem Erfolg, wenn ein Verb&uuml;ndeter
  Roboter auf dem Zielfeld steht. */

	public boolean processSigScan(Point ziel)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if (w.getTyp() == Robot1.TYP_PARASIT)
			return false;
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		if (r2 == null)
			return false;
		return (kf.isVerbuendet(r,r2));
	}


/** F&uuml;gt einen Integer-Eintrag in das Logbuch eines benachbarten Roboters
  ein. Diese Aktion ist sehr unhygienisch und gibt parasit&auml;re Infektionen
  weiter. */

	public boolean processKommunikation(Point ziel, int data)
	{
                if (inaktion)
                        throw new KdKException("Nicht Schummeln !");
                Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
                int p = r.getAbstand(ziel);
		if ((r2 != null) && (p==1)) {
			r2.addLog(AKTION_KOMM,w.getPosition(),data);
			Werte w2 = r2.getWerte();
			boolean rueck = w2.isInfiziert();
			if (w.isInfiziert())
			{
				inaktion = true;
				w2.infekt(w.getParasit().instantiiere());
				inaktion = false;
			}
			else if (rueck)
			{
				inaktion = true;
				w.infekt(w2.getParasit().instantiiere());
				inaktion = false;
			}
		}
		return true;
	}


/** Sonderaktion der J&auml;ger.  Der Position des Roboter auf dem Zielfeld ist
  bis zu seinem Tod vom J&auml;ger per <I>getMarked()</I> zu erhalten. */

	public void processMark(Point ziel)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if (w.getTyp() != Robot1.TYP_JAEGER)
			return;
		Robot1 a = kf.getFeld(w.getMarked().x,w.getMarked().y);
		if (a != null)
			a.getWerte().removeMarker(r);
		Robot1 z = kf.getFeld(ziel.x, ziel.y);
		if (z != null)
			z.getWerte().setMarker(r);
	}


/** Sonderaktion f&uuml;r J&auml;ger. Gibt die aktuelle Position eines markierten
  Roboter zur&uuml;ck. */

	public Point getMarked()
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		return w.getMarked();
	}

/** L&ouml;scht alle Verbindungen zum Spielfeld. Die Methode processAction()
  wird dadurch unbrauchbar und wird NullPointerExceptions werfen. */

	public void dispose()
	{
		inaktion = true;
		kf = null;
		r = null;
		w = null;
	}

}
