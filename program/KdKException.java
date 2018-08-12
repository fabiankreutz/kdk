package kdk.program;

/** Standard-Exception, die beim Tod des Roboters die Zug-Rountine beendet.
  Ebenso wird sie bei einem Schummelversuch geworfen. */

public class KdKException extends RuntimeException
{

/** Erstellt eine neue Instanz der Exception */

	public KdKException(String s)
	{
		super(s);
	}

}
