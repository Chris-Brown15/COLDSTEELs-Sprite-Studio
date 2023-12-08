package cs.csss.misc.textio;

import java.util.Collection;

public record CSNamedCollection<T>(String name , Collection<T> collection) {}
