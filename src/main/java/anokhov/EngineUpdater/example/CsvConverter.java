package anokhov.EngineUpdater.example;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvConverter
{
    public static void main(String[] args)
    {

        System.out.println("CSV to XML Script - Processing Inputs... args count:" + args.length);

        if (args.length != 4)
        {
            System.out.println("The program accepts only 4 args, the first being the type of preset wanted (eg. \"nexi\"),\nthe second being the csv file path and the third being the output destination. The fourth being the regnumber prefix");
            System.out.println("Example: nexi ./src/main/resources/example.csv ./src/main/resources/example.xml LibraTony");
            System.exit(1);
        }

        //TODO: CHECK FOR CORRECT ARGS
        if (!validateArgs(args)) {
            System.exit(1);
        }


        try
        {
            generateXml(args[0], args[1], args[2], args[3]);                                     //CREATION OF THE XML FROM CUSTOM OBJECTS THAT WERE CREATED BY THE INPUT CSV AND INPUT PRESET
            System.out.println("Inputs accepted " + args[1] +" - Converting Data...");
        }
        catch (FileNotFoundException e)                                                         //ERROR WITH FINDING THE CSV OR THE DESTINATION PATH
        {
            throw new RuntimeException(e);
        }
        catch (Exception e)                                                                     //ERROR IN THE CONVERSION PROCESS
        {
            throw new RuntimeException(e);
        }



        System.exit(0);
    }

    private static CustomPreset generateCustomPreset(String presetName, String regNumberPrefix)                         //CUSTOM PRESETS, TODO: ADD CASES TO ADAPT
    {

        if (presetName.toLowerCase().contains("nexi")) {
            NexiPreset nexiPreset = new NexiPreset();
            nexiPreset.setRegNumberPrefix(regNumberPrefix);
            return nexiPreset;
        }

        else {

            System.out.println("incorrect preset");

            return null;
        }
       /* NexiPreset nexiPreset= new NexiPreset();
        nexiPreset.setRegNumberPrefix(regNumberPrefix);
        return nexiPreset;
*/
    }

    public static void generateXml(String preset, String csv, String destination, String regNumberPrefix) throws FileNotFoundException  //CREATE A LIST OF PRESET OBJECTS FROM THE CSV TO THEN CONVERT IN XML FORMAT
    {
        FileReader fileReader = new FileReader(csv);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        CustomPreset customPreset = generateCustomPreset(preset, regNumberPrefix);                             //GENERATE THE NEW PRESET
        customPreset.setDestination(destination);                                            //SETTING THE DESTINATION PATH

        try
        {
            boolean presetLine = true;
            String[] presetColumns = null;

            while (bufferedReader.ready())
            {
                if (presetLine) { //ONCE, SAVE AND REMOVE THE COLUMN HEADING FROM THE CSV FILE
                    presetColumns = bufferedReader.readLine().split(",");
                    presetLine = false;
                    continue;
                }

                String row = bufferedReader.readLine();
                if (row == null || row.trim().isEmpty()) {
                    continue; // skip empty lines
                }

                String[] columns = row.split(",");

                //TODO: ARE ALL THE ROWS COMPLETE? IF NOT --v
                //SPLIT DOESN'T CREATE BLANK COLUMNS AFTER THE LAST POPULATED
                if (columns.length<presetColumns.length){
                    System.out.println("incorrect line: " + row );
                    continue; //skip this row if it's not complete
                }

                //POPULATE CUSTOMPRESET
                CustomObject finalObj = customPreset.generateObject(presetColumns, columns);    //POPULATE IT WITH THE DATA

                // System.out.println("Created custom obj:" + finalObj);
                customPreset.addObj(finalObj);                                                          //ADD IT TO THE POOL
            }

            customPreset.writeXml();                                        //USE THE PRESET TO WRITE ITS XML VERSION
        }
        catch (IOException ioEx)                                            //TODO: EXPLAIN ERRORS AND EXPAND
        {
            System.out.println("error of input/output: " + ioEx.getMessage() );
            ioEx.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            System.err.println("illegal argument: " + e.getMessage());
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            System.out.println("error of configuration parser: "+e.getMessage());
            throw new RuntimeException(e);
        }
        catch (TransformerException e)
        {
            System.out.println("error of configuration transformer: " +e.getMessage());
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            System.err.println("uncatchable error: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                bufferedReader.close();                                 //CLOSE BUFFERS
                fileReader.close();
            }
            catch (IOException ioEx)
            {
                System.out.println("error when closing streams: "+ioEx.getMessage());
                ioEx.printStackTrace();
            }
        }
         System.out.println("Data Converted Successfully - Written  " );
    }

    private static boolean validateArgs (String[] args) {
        File csvFile = new File(args[1]);
        if (!csvFile.exists() || !csvFile.isFile() || !csvFile.canRead()) {
            System.out.println("CSV файл не существует или не доступен для чтения: " + args[1]);
            return false;
        }

        File outputFile = new File(args[2]);
        if (!outputFile.getParentFile().exists() || !outputFile.getParentFile().canWrite()) {
            System.out.println("Путь назначения недоступен для записи: " + args[2]);
            return false;
        }

        return true;
    }
}
