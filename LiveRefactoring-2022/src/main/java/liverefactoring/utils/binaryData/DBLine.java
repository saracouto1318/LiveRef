package liverefactoring.utils.binaryData;

import java.util.ArrayList;

public class DBLine {
    public String rownName;
    public ArrayList<Integer> rowBinaryData;

    public DBLine(String rownName, ArrayList<Integer> rowBinaryData){
        this.rownName = rownName;
        this.rowBinaryData = rowBinaryData;
    }
}
