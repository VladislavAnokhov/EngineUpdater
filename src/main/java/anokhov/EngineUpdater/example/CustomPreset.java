package anokhov.EngineUpdater.example;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CustomPreset
{
     private List<CustomObject> objs = new ArrayList<>();
     protected String preset = "generic";
     protected String destination = "";

     public String getPreset()
     {
          return preset;
     }
     public void setDestination(String destination)
     {
          this.destination = destination;
     }
     public void clearObjs() {
          objs.clear();
     }

     public abstract CustomObject generateObject(String[] preset, String[] columns);

     protected String generateUUID()
     {
          UUID uuid = UUID.randomUUID();
          return uuid.toString();
     }

     public abstract void addObj(CustomObject finalObj);

     public abstract void writeXml() throws ParserConfigurationException, TransformerException;
}
