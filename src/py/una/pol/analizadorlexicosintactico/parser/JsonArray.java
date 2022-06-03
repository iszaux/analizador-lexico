package py.una.pol.analizadorlexicosintactico.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import py.una.pol.analizadorlexicosintactico.exception.JSONTypeException;

public class JsonArray implements Iterable {  
  
    private List list = new ArrayList();  
  
    public void add(Object obj) {  
        list.add(obj);  
    }  
  
    public Object get(int index) {  
        return list.get(index);  
    }  
  
    public int size() {  
        return list.size();  
    }  
  
    public JsonObject getJsonObject(int index) {  
        Object obj = list.get(index);  
        if (!(obj instanceof JsonObject)) {  
            throw new JSONTypeException("Type of value is not JsonObject");  
        }  
  
        return (JsonObject) obj;  
    }  
  
    public JsonArray getJsonArray(int index) {  
        Object obj = list.get(index);  
        if (!(obj instanceof JsonArray)) {  
            throw new JSONTypeException("Type of value is not JsonArray");  
        }  
  
        return (JsonArray) obj;  
    }  
  
    public Iterator iterator() {  
        return list.iterator();  
    }  
}