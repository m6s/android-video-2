package info.mschmitt.androidsupport;

/**
 * @author Matthias Schmitt
 */
public class ObjectsBackport {
    public static boolean nonNull(Object obj) {
        return obj != null;
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
