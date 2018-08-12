package kdk.program;

/** Default exception which ends the turn of a robot on its death.
  It also gets thrown on attempts to cheat. */

public class KdKException extends RuntimeException
{

/** Creates a new instance */

	public KdKException(String s)
	{
		super(s);
	}

}
