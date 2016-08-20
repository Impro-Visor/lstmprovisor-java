/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author cssummer16
 */
public class ConnectomeLoader {
    
    public String[] load (String meatFolderOrZipPath, Loadable network){
        return refresh(meatFolderOrZipPath, network, "");
    }
    
    public String[] refresh (String meatFolderOrZipPath, Loadable network, String filter)
    {
        
        try {
            String[] unrecognizedPaths = null;
            File meatFileOrFolder = new File(meatFolderOrZipPath);
            if(meatFileOrFolder.isDirectory())
            {
                File[] meatFiles = meatFileOrFolder.listFiles(new FilenameFilter() {
                                        @Override
                                        public boolean accept(File dir, String name) { return !name.equals(".DS_Store") && name.contains(filter);}
                                    });
                boolean[] found = new boolean[meatFiles.length];
                int numFound = 0;
                for(int i = 0; i < meatFiles.length; i++)
                {
                    //System.out.println(meatFiles[i].getPath());
                    found[i] = network.load(nickd4j.ReadWriteUtilities.readNumpyCSVFile(meatFiles[i].getPath()), network.pathCdr(meatFiles[i].getName()).replaceFirst(".csv", ""));
                    if(found[i])
                        numFound++;
                }
                unrecognizedPaths = new String[meatFiles.length - numFound];
                int j = 0;
                for(int i = 0; i < found.length; i++)
                {
                    if(!found[i])
                        unrecognizedPaths[j++] = meatFiles[i].getPath();
                }

            }
            else if(meatFileOrFolder.isFile())
            {
                // Try to read it as a zip file of params
                ArrayList<String> unrecognizedPathsList = new ArrayList<String>();
                FileInputStream fis = new FileInputStream(meatFileOrFolder);
                ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fis));
                ZipEntry entry;
                while((entry = zin.getNextEntry()) != null) {
                   
                    if(entry.getName().contains(filter) && !(entry.getName().contains(".DS_Store")))
                    {
                        String loadPath = network.pathCdr(entry.getName()).replaceFirst(".csv", "");
                        Reader zreader = new InputStreamReader(zin);
                        boolean found = network.load(nickd4j.ReadWriteUtilities.readNumpyCSVReader(zreader), loadPath);
                        if(!found)
                            unrecognizedPathsList.add(entry.getName());
                    }
                }
                unrecognizedPaths = unrecognizedPathsList.toArray(new String[unrecognizedPathsList.size()]);
            }
            else
            {
                throw new RuntimeException("Not a directory or a zip file!!!");
            }
            
            
            return unrecognizedPaths;
            
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
