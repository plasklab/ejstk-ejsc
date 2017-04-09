package estree;

import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import estree.Node.*;

public class Literal extends Node implements ILiteral {

    public enum LiteralType {
        STRING, BOOLEAN, NULL, NUMBER, REG_EXP;
    }

    final Node.ILiteral.LiteralType literalType;

    String strVal;
    boolean boolVal;
    double numberVal;
    boolean isDouble;

    String regExpVal;
    String regexPattern;
    String regexFlags;

    private Literal(Node.ILiteral.LiteralType literalType) {
        type = LITERAL;
        this.literalType = literalType;
    }

    public Literal(String val) {
        type = LITERAL;
        if (val.equals("null")) {
            literalType = Node.ILiteral.LiteralType.NULL;
        } else if (val.equals("true")) {
            literalType = Node.ILiteral.LiteralType.BOOLEAN;
            this.boolVal = true;
        } else if (val.equals("false")) {
            literalType = Node.ILiteral.LiteralType.BOOLEAN;
            this.boolVal = false;
        } else if (val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"') {
            literalType = Node.ILiteral.LiteralType.STRING;
            //char????????????=='\''u'????????????????????????replace???????????????????????????????????????
            //???????????????????????????Character.toChars(???????????????);
            //???????????????????????????????????????????????????????????????????????????,
            //?????????????????????????????????????????????????????????????????????,
            //???????????????JsonDiff???ESPRIMA??????value????????????????????????????????????
            //???????????????????????????????????????????????????????????????????????????????????????
            //??????????????????????????????\\??????????????????????????????\)??????????????????????????????????????????
            val = val.replaceFirst("\"", "");
            StringBuffer sb = new StringBuffer(val);
            val = sb.reverse().toString();
            val = val.replaceFirst("\"", "");
            StringBuffer sb2 = new StringBuffer(val);
            val = sb2.reverse().toString();
            val = convertEscape(val);
            val = val.replaceAll("\"", ""+'"');
            val = val.replaceAll('\\'+"\"", "\"");
            val = val.replace("\\n", "\n");
            val = val.replace("\\r", "\r");
            val = val.replace("\\t", "\t");
            val = val.replace("\\b", "\b");
            val = val.replace("\\f", "\f");
            val = val.replace("\\\\", "\\");
            val = val.replace("\\'", "\'");
            val = val.replace("\\\"", "\"");
            val = val.replace("\\\n", "");
            this.strVal = val;
        } else if (val.charAt(0) == '\'' && val.charAt(val.length() - 1) == '\'') {
            literalType = Node.ILiteral.LiteralType.STRING;
            val = val.replaceFirst("\'", "");
            StringBuffer sb = new StringBuffer(val);
            val = sb.reverse().toString();
            val = val.replaceFirst("\'", "");
            StringBuffer sb2 = new StringBuffer(val);
            val = sb2.reverse().toString();
            val = convertEscape(val);
            val = val.replaceAll("\"", ""+'"');
            val = val.replaceAll('\\'+"\"", "\"");
            val = val.replace("\\n", "\n");
            val = val.replace("\\r", "\r");
            val = val.replace("\\t", "\t");
            val = val.replace("\\b", "\b");
            val = val.replace("\\f", "\f");
            val = val.replace("\\\\", "\\");
            val = val.replace("\\'", "\'");
            val = val.replace("\\\"", "\"");
            val = val.replace("\\\n", "");
            this.strVal = val;
        } else if (val.charAt(0) == '/' && val.charAt(val.length() - 1) == '/') {
            literalType = Node.ILiteral.LiteralType.REG_EXP;
            this.regExpVal = val.substring(1, val.length() - 1);
        }else {
            System.out.println("ERROR: failed to initialize Literal.");
            literalType = Node.ILiteral.LiteralType.NULL;
        }
    }

    public String convertEscape(String val) {
        for (int i = 0; i < val.length() - 1; i++) {
            char[] charArray = val.toCharArray();
            String temp = null;
            int[] temp2 = new int[1];
            String str = null;
            String[] con = {"t", "n", "r", "f", "b", "\\", "\"", "\'"};
        if (i + 3 <= val.length() - 1 && ("" + charArray[i]).equals("\\") && ("" + charArray[i+1]).equals("x")) {
                str = "" + charArray[i] + charArray[i+1];
                temp = ""+ charArray[i+2] + charArray[i+3];
                str = str + temp;
                temp2[0] = Integer.parseInt(temp, 16);
                temp = String.valueOf(Character.toChars(temp2[0]));
                val = val.replace(str, temp);
            }
       else if (("" + charArray[i]).equals("\\") && ("" + charArray[i+1]).equals("v")) {
           val = val.replace("\\v", "\u000b");
       }
            else if ((""+charArray[i]).equals("\\") && Arrays.asList(con).contains("" + charArray[i+1])) {
                continue;
            }
            else if ((str = "" + charArray[i]).equals("\\")) {
                temp = "" + charArray[i+1];
                if (i + 2 <= val.length() - 1 && checkInteger(charArray[i+2])) {
                    temp += charArray[i+2];
                    if (i + 3 <= val.length() - 1 && checkInteger(charArray[i+3])) {
                        temp += charArray[i+3];
                    }
                    str = str + temp;
                    try {
                        temp2[0] = Integer.parseInt(temp, 8);
                        temp = String.valueOf(Character.toChars(temp2[0]));
                        val = val.replace(str, temp);
                    } catch (NumberFormatException x) {
                        continue;
                    }
                } else {
                    str = str + temp;
                    // "\\v"????????????\\???v??????????????????????????????????????????????????????//?????????v??????????????????????????????????????????????????????
                    //????????????????????????????????????replace????????????????????????
                    if (temp.equals("0")) {
                        val = val.replace("\\0", "\u0000");
                    } else {
                        temp = String.format("\\u%04X", Character.codePointAt(temp, 0));
                        val = val.replace(str, temp);
                    }
                }
            }
        }
        return val;
    }

    public boolean checkInteger(char ch) {
        if (Character.digit(ch, 10) >= 8) return false;
        return Character.isDigit(ch);
    }

    public Literal(boolean val) {
        this(Node.ILiteral.LiteralType.BOOLEAN);
        this.boolVal = val;
    }

    public Literal() {
        this(Node.ILiteral.LiteralType.NULL);
    }

    
	//public Literal(int val) {
	//	this(LiteralType.NUMBER);
	//	this.numberVal = val;
	//}
    public Literal(double val, boolean isDouble) {
        this(Node.ILiteral.LiteralType.NUMBER);
        this.numberVal = val;
        this.isDouble = isDouble;
    }

    public Literal(String val, String pattern, String flags) {
        this(Node.ILiteral.LiteralType.REG_EXP);
        this.regExpVal = val;
        this.regexPattern = pattern;
        this.regexFlags = flags;
    }

    public String toString() {
        String str = "Literal(";
        switch(this.literalType) {
        case STRING:
            str += this.strVal;
            break;
        case BOOLEAN:
            if (this.boolVal) {
                str += "true";
            } else {
                str += "false";
            }
            break;
        case NUMBER:
            str += Double.toString(this.numberVal);
            break;
        case NULL:
            str += "null";
            break;
        case REG_EXP:
            str += this.regExpVal + "," + this.regexPattern + "," + this.regexFlags;
            break;
        }
        str += ")";
        return str;
    }

    @Override
    public JsonObject getEsTree() {
        // TODO Auto-generated method stub
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add(KEY_TYPE, "Literal");
        switch (literalType) {
        case STRING:
            jsonBuilder.add(KEY_VALUE, strVal);
            break;
        case BOOLEAN:
            jsonBuilder.add(KEY_VALUE, boolVal);
            break;
        case NUMBER:
            jsonBuilder.add(KEY_VALUE, numberVal);
            break;
        case REG_EXP:
            jsonBuilder.add(KEY_VALUE, regExpVal);
            break;
        case NULL:
            jsonBuilder.addNull(KEY_VALUE);
            break;
        }
        // jsonBuilder.add(KEY_LOC, loc.getAstWithJson());
        return jsonBuilder.build();
    }
/*
    public LiteralType getLiteralType() {
        return literalType;
    }
*/
    @Override
    public String getStringValue() {
        // TODO Auto-generated method stub
        return strVal;
    }

    @Override
    public boolean getBooleanValue() {
        // TODO Auto-generated method stub
        return boolVal;
    }

    @Override
    public double getNumValue() {
        // TODO Auto-generated method stub
        return numberVal;
    }

    @Override
    public String getRegExpValue() {
        // TODO Auto-generated method stub
        return regExpVal;
    }

    public boolean isDouble() {
        return isDouble;
    }

    @Override
    public estree.Node.ILiteral.LiteralType getLiteralType() {
        // TODO Auto-generated method stub
        return this.literalType;
    }

	@Override
	public Object accept(ESTreeBaseVisitor visitor) {
		// TODO Auto-generated method stub
		return visitor.visitLiteral(this);
	}
}
