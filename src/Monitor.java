import java.util.ArrayDeque;
/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	private final int count;
	private final boolean[] chopsticks;
	private final ArrayDeque<Integer> talkingQueue;
	private boolean philTalking = false;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		// TODO: set appropriate number of chopsticks based on the # of philosophers
		this.count = piNumberOfPhilosophers; //number of philosophers
		this.chopsticks = new boolean[piNumberOfPhilosophers]; //number of chopsticks
		this.talkingQueue = new ArrayDeque<>(count); //queue based on the number of philosophers
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID)
	{
		/**
		 * In the pickup method, left and right are interchangeable, depending on the way we number the philosophers and chopsticks.
		 * The last philosopher picks up their chopsticks in a different order (left first, then right) 
		 * than the other philosophers (right first, then left).
		 * This method prevents a scenario where all philosophers have 1 chopstick in their hand and are waiting for the other.
		 * This is done to avoid circular waiting, causing deadlock.
		 * This also helps in solving starvation.
		 */
		int first; //first chopstick to be picked up
		int second;  //second chopstick to be picked up
		
		if (piTID == this.count) { //if last philosopher
			first = 0; //first = left chopstick
			second = piTID - 1; //second = right chopstick
		}
		else { //if any other philosopher
			first = piTID - 1; //first = right chopstick
			second = piTID; //second = left chopstick
		}
		try {
			while(chopsticks[first]) { //wait until the first required chopstick is free
				wait();
			}
			chopsticks[first] = true; //chopstick has been picked up: set it to true
			while(chopsticks[second]) { //wait until the second required chopstick is free
				wait();
			}
			chopsticks[second] = true; //chopstick has been picked up: set it to true
		}
		catch (InterruptedException e) { //exception handling
			System.err.println("Monitor.pickup():");
			DiningPhilosophers.reportException(e);
			System.exit(1);
		}
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		//put down chopsticks: set them to false
		chopsticks[piTID - 1] = false;
		chopsticks[piTID % this.count] = false;
		notifyAll(); //notify waiting philosophers so they can attempt to eat now
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk(final int piTID)
	{
		if (this.philTalking) { //if there is a philosopher talking
			this.talkingQueue.push(piTID); //add given philosopher to queue
			try {
				do {
					wait();
				} while (this.talkingQueue.peek() == piTID); //wait until it is given philosopher's turn to speak
				this.talkingQueue.pop(); //given philosopher is removed from the queue, they are able to talk
			}
			catch (InterruptedException e) { //exception handling
				System.err.println("Monitor.requestTalk():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		} else { //if no one is currently talking, given philosopher can talk
			this.philTalking = true; //set to true, since someone is talking now
		}
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		if (this.talkingQueue.isEmpty()) { //if no one is waiting to talk, set our boolean to false
			this.philTalking = false;
		} else { 
			notifyAll(); //notify philosophers so that the next in queue can start talking
		}
	}
}

// EOF
