package mp;

public class Utility {
    //static method to return the nth occurrence of a string
    public static int nthIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }
}
