package repls;

public class BooleanMethods {
    public boolean isLetter(String s) {
        char ch = s.charAt(0);
        return ch >= 'A' && ch <= 'z';
    }

    public boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public boolean checkRange(String a) {
        if(isLetter(a)) {
            char c = a.charAt(0);
            if (c >= 'A' && c <= 'H') {
                return true;
            } else {
                return c >= 'a' && c <= 'h';
            }
        } else {
            return Integer.parseInt(a) >= 1 && Integer.parseInt(a) <= 8;
        }
    }
}
