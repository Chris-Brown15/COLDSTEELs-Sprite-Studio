package cs.csss.misc.ranges;

import java.util.function.Function;

public final class Ranges {

	public static byte[] bytes(byte...bytes) {
		
		return bytes;
		
	}
	
	public static byte[] bytes(int...ints) {
		
		byte[] bytes = new byte[ints.length];
		for(int i = 0 ; i < ints.length ; i++) bytes[i] = (byte)ints[i];
		return bytes;
		
	}
	
	public static byte[] fromRange(ByteRange range) {
		
		return (byte[]) range.toArray();
		
	}
	
	public static short[] shorts(short...shorts) {
		
		return shorts;
		
	}
	
	public static short[] shorts(int...ints) {
		
		short[] shorts = new short[ints.length];
		for(int i = 0 ; i < ints.length ; i++) shorts[i] = (short)ints[i];
		return shorts;
		
	}
	
	public static int[] ints(int...ints ) {
		
		return ints;
		
	}
	
	public static long[] longs(long...longs) {
		
		return longs;
		
	}
	
	public static float[] floats(float...floats) {
		
		return floats;
		
	}

	public static float[] floats(int...ints) {
		
		float[] floats = new float[ints.length];
		for(int i = 0 ; i < ints.length ; i++) floats[i] = (float)ints[i];
		return floats;
		
	}
	
	public static double[] doubles(double...doubles) {
		
		return doubles;
		
	}

	public static ByteRange bytes(int bound , Function<Integer , Byte> calculator) {
	
		return calculator == null ? new ByteRange(bound) : new ByteRange(bound , calculator);
		
	}
		
	public static ShortRange shorts(int bound , Function<Integer , Short> calculator) {
		
		return calculator == null ? new ShortRange(bound) : new ShortRange(bound , calculator);
		
	}
	
	public static IntRange ints(int bound , Function<Integer , Integer> calculator) {
		
		return calculator == null ? new IntRange(bound) : new IntRange(bound , calculator);
		
	}

	public static LongRange longs(long bound , Function<Long , Long> calculator) {
		
		return calculator == null ? new LongRange(bound) : new LongRange(bound , calculator);
		
	}
	
	public static String asString(int[] ints) {
		
		StringBuilder builder = new StringBuilder(ints.length * 2);
		
		builder.append("Ints: ");
		for(int i = 0 ; i < ints.length - 1 ; i++) builder.append(ints[i] + ", ");
		builder.append(ints[ints.length - 1]);
		
		return builder.toString();
		
	}
	
	private Ranges() {}

}
