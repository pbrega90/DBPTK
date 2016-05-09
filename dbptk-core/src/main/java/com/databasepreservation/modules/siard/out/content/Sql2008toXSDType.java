package com.databasepreservation.modules.siard.out.content;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnknownTypeException;
import com.databasepreservation.model.structure.type.ComposedTypeArray;
import com.databasepreservation.model.structure.type.ComposedTypeStructure;
import com.databasepreservation.model.structure.type.SimpleTypeBinary;
import com.databasepreservation.model.structure.type.SimpleTypeBoolean;
import com.databasepreservation.model.structure.type.SimpleTypeDateTime;
import com.databasepreservation.model.structure.type.SimpleTypeNumericApproximate;
import com.databasepreservation.model.structure.type.SimpleTypeNumericExact;
import com.databasepreservation.model.structure.type.SimpleTypeString;
import com.databasepreservation.model.structure.type.Type;
import com.databasepreservation.model.structure.type.UnsupportedDataType;

/**
 * Convert sql2008 types into XML or XSD types
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Sql2008toXSDType {
  private static final Map<String, String> sql2008toXSDconstant = new HashMap<String, String>();
  private static final Map<String, String> sql2008toXSDregex = new HashMap<String, String>();

  private static final Logger LOGGER = LoggerFactory.getLogger(Sql2008toXSDType.class);

  static {
    // initialize sql2008 conversion tables

    // direct mapping
    sql2008toXSDconstant.put("BINARY LARGE OBJECT", "blobType");
    sql2008toXSDconstant.put("BIT VARYING", "blobType");
    sql2008toXSDconstant.put("BIT", "blobType");
    sql2008toXSDconstant.put("BLOB", "blobType");
    sql2008toXSDconstant.put("BOOLEAN", "xs:boolean");
    sql2008toXSDconstant.put("CHARACTER LARGE OBJECT", "clobType");
    sql2008toXSDconstant.put("CHARACTER VARYING", "xs:string");
    sql2008toXSDconstant.put("CHARACTER", "xs:string");
    sql2008toXSDconstant.put("CLOB", "clobType");
    sql2008toXSDconstant.put("DATE", "xs:date");
    sql2008toXSDconstant.put("DECIMAL", "xs:decimal");
    sql2008toXSDconstant.put("DOUBLE PRECISION", "xs:float");
    sql2008toXSDconstant.put("DOUBLE", "xs:float");
    sql2008toXSDconstant.put("FLOAT", "xs:float");
    sql2008toXSDconstant.put("INTEGER", "xs:integer");
    sql2008toXSDconstant.put("NATIONAL CHARACTER LARGE OBJECT", "clobType");
    sql2008toXSDconstant.put("NATIONAL CHARACTER VARYING", "xs:string");
    sql2008toXSDconstant.put("NATIONAL CHARACTER", "xs:string");
    sql2008toXSDconstant.put("NUMERIC", "xs:decimal");
    sql2008toXSDconstant.put("REAL", "xs:float");
    sql2008toXSDconstant.put("SMALLINT", "xs:integer");
    sql2008toXSDconstant.put("TIME WITH TIME ZONE", "xs:time");
    sql2008toXSDconstant.put("TIME", "xs:time");
    sql2008toXSDconstant.put("TIMESTAMP WITH TIME ZONE", "xs:dateTime");
    sql2008toXSDconstant.put("TIMESTAMP", "xs:dateTime");

    // mapping using regex
    sql2008toXSDregex.put("^BIT VARYING\\(\\d+\\)$", "blobType");
    sql2008toXSDregex.put("^BIT\\(\\d+\\)$", "blobType");
    sql2008toXSDregex.put("^CHARACTER VARYING\\(\\d+\\)$", "xs:string");
    sql2008toXSDregex.put("^CHARACTER\\(\\d+\\)$", "xs:string");
    sql2008toXSDregex.put("^DECIMAL\\(\\d+(,\\d+)?\\)$", "xs:decimal");
    sql2008toXSDregex.put("^FLOAT\\(\\d+\\)$", "xs:float");
    sql2008toXSDregex.put("^NUMERIC\\(\\d+(,\\d+)?\\)$", "xs:decimal");
  }

  /**
   * Gets the XML type that corresponds to the provided Type
   *
   * @param type
   *          the type
   * @return the XML type string
   * @throws ModuleException
   *           if the conversion is not supported
   * @throws UnknownTypeException
   *           if the type is not known
   */
  public static String convert(Type type) throws ModuleException, UnknownTypeException {
    String ret = null;
    if (type instanceof SimpleTypeString || type instanceof SimpleTypeNumericExact
      || type instanceof SimpleTypeNumericApproximate || type instanceof SimpleTypeBoolean
      || type instanceof SimpleTypeDateTime || type instanceof SimpleTypeBinary) {

      ret = convert(type.getSql2008TypeName());

    } else if (type instanceof UnsupportedDataType) {
      LOGGER.warn("Unsupported datatype: " + type.toString() + ". Using xs:string as xml type.");
      ret = "xs:string";
    } else if (type instanceof ComposedTypeArray) {
      throw new ModuleException("Not yet supported type: ARRAY");
    } else if (type instanceof ComposedTypeStructure) {
      ret = null;
    } else {
      throw new UnknownTypeException(type.toString());
    }
    return ret;
  }

  /**
   * Gets the XSD type that corresponds to the provided SQL99 type
   *
   * @param sql2008Type
   *          the SQL2008 type
   * @return the XSD type string
   */
  public static String convert(String sql2008Type) {
    // try to find xsd corresponding to the sql2008 type in the constants
    // conversion table
    String ret = sql2008toXSDconstant.get(sql2008Type);

    // if that failed, try to find xsd corresponding to the sql2008 type by
    // using
    // the regex in the regex conversion table
    if (ret == null) {
      for (Map.Entry<String, String> entry : sql2008toXSDregex.entrySet()) {
        if (sql2008Type.matches(entry.getKey())) {
          ret = entry.getValue();
          break;
        }
      }
    }

    return ret;
  }
}
