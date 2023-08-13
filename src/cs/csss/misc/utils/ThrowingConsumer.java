package cs.csss.misc.utils;

public interface ThrowingConsumer<ExceptionType extends Throwable, T> {

	public void acceptOrThrow(T object) throws ExceptionType;
	
}
