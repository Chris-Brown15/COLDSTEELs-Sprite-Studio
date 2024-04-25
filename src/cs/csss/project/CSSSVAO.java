/**
 * 
 */
package cs.csss.project;

import cs.core.graphics.CSVAO;

/**
 * Decorator over the COLDSTEEL Core VAO class in which a single user is set in the number of users. This is done for shutdown purposes.
 */
public class CSSSVAO extends CSVAO {
	
	{
		//make sure one user is set at the beginning
		users.getAndIncrement();
	}
	
}
