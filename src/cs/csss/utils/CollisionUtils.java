package cs.csss.utils;

import cs.csss.engine.Position;

/**
 * Container for methods for checking collisions between instances of {@link Position}.
 */
public final class CollisionUtils {

	/**
	 * Returns whether {@code isBetween} is strictly greater than {@code lower} and strictly less than {@code higher}.
	 * 
	 * @param isBetween — a number whose status as being between {@code lower} and {@code higher} is being queried
	 * @param lower — a value {@code isBetween} must be greater than for {@code true} to be returned 
	 * @param higher — a value {@code isBetween} must be less than for {@code true} to be returned
	 * @return {@code true} if {@code isBetween} is strictly greater than {@code lower} and strictly less than {@code higher}.
	 */
	public static boolean between(int isBetween, int lower , int higher) {
		
		return isBetween > lower && isBetween < higher;
		
	}
	
	/**
	 * Returns whether {@code artboard1} and {@code artboard2} are colliding <b>horizontally</b>.
	 * 
	 * @param artboard1 — an artboard
	 * @param artboard2 — a second artboard
	 * @return {@code true} if the artboards are colliding horizontally.
	 */
	public static boolean collidingX(Position artboard1 , Position artboard2) {
		
		//two quads are colliding horizontally if one's horizontal positions are between the other's horizontal positions 
		int
			a1LX = (int) artboard1.leftX() ,
			a1RX = (int) artboard1.rightX() ,
			a2LX = (int) artboard2.leftX() ,
			a2RX = (int) artboard2.rightX();
		
		return between(a1LX , a2LX , a2RX) || between(a2LX , a1LX , a1RX);
		
	}

	/**
	 * Returns whether {@code artboard1} and {@code artboard2} are colliding <b>vertically</b>.
	 * 
	 * @param artboard1 — an artboard
	 * @param artboard2 — a second artboard
	 * @return {@code true} if the artboards are colliding vertically.
	 */
	public static boolean collidingY(Position artboard1 , Position artboard2) {
		
		int
			a1BY = (int) artboard1.bottomY() ,
			a1TY = (int) artboard1.topY() ,
			a2BY = (int) artboard2.bottomY() ,
			a2TY = (int) artboard2.topY();
		
		return between(a1BY , a2BY , a2TY) || between(a2BY , a1BY , a1TY);
		
	}
	
	/**
	 * Returns whether {@code artboard1} and {@code artboard2} are colliding. By colliding, we mean they overlap one another.
	 * 
	 * @param artboard1 — an artboard
	 * @param artboard2 — a second artboard
	 * @return {@code true} if the artboards are colliding both horizontally and vertically.
	 */
	public static boolean colliding(Position artboard1 , Position artboard2) {
	
		return collidingX(artboard1 , artboard2) && collidingY(artboard1 , artboard2);
		
	}
	
	/**
	 * Returns the distance to translate {@code artboard1} to stop it from colliding with {@code artboard2}. The resulting deltas are 
	 * invalid if the two artboards are not colliding.
	 * 
	 * @param artboard1 — an artboard
	 * @param artboard2 — a second artboard
	 * @return Array containing two integers representing the amount to translate {@code artboard1} to stop it from colliding with 
	 * 		   {@code artboard2}.
	 */
	public static int[] collisionDeltas(Position artboard1 , Position artboard2) {
		
		int[] deltas = new int[2];
		
		int
			a1LX = (int) artboard1.leftX() ,
			a1RX = (int) artboard1.rightX() ,
			a2LX = (int) artboard2.leftX() ,
			a2RX = (int) artboard2.rightX() ,
			a1BY = (int) artboard1.bottomY() ,
			a1TY = (int) artboard1.topY() ,
			a2BY = (int) artboard2.bottomY() ,
			a2TY = (int) artboard2.topY();
		
		//x delta
		if(between(a1LX , a2LX , a2RX)) deltas[0] = a2RX - a1LX;
		else deltas[0] = a2LX - a1RX;
		
		//y delta
		if(between(a1BY , a2BY , a2TY)) deltas[1] = a2TY - a1BY;
		//negate this to move the result down
		else deltas[1] = -(a1TY - a2BY);
				
		return deltas;
		
	}
	
	private CollisionUtils() {}

}
