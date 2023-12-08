/**
 * 
 */
package cs.csss.misc.textio;

import java.util.Map;

/**
 * Contains a {@link Map} and a {@code String} naming it.
 */
public record CSNamedMap<K , V>(String name , Map<K , V> map) {

}
