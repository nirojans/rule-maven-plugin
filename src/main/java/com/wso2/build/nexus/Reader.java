package com.wso2.build.nexus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Reader {

    public String getLatestMetaData(String nexusMetaURL) throws URISyntaxException, IOException, HttpException {

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(nexusMetaURL);
        HttpResponse response = client.execute(request);
        InputStream stream=response.getEntity().getContent();

        return convertStreamToString(stream);

    }

    public String getCurrentMetaData(String metaDataFile) throws FileNotFoundException {

        InputStream stream = new FileInputStream(metaDataFile);
        return convertStreamToString(stream);

    }

    public Boolean getZipFile(String ruleLocation,String nexusZipURL)  {

        HttpClient client = new DefaultHttpClient();
        ZipInputStream stream=null;
        FileOutputStream outStream = null;
        Boolean status=false;

        try {
            HttpGet request =  new HttpGet(nexusZipURL);
            HttpResponse response = client.execute(request);
            stream = new ZipInputStream(response.getEntity().getContent());

            byte[] buffer = new byte[4096];

            ZipEntry entry;
            File dir = new File(ruleLocation);

            if (!dir.exists()) {
                FileUtils.forceMkdir(dir);
            }

            FileUtils.cleanDirectory(dir);
            while ((entry = stream.getNextEntry()) != null) {

                String s = String.format("Downloading : %s len %d added %TD",entry.getName(), entry.getSize(), new Date(entry.getTime()));
                System.out.println(s);

                String outPath = dir + File.separator + entry.getName();

                outStream = new FileOutputStream(outPath);
                    int len = 0;
                    while ((len = stream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, len);
                    }
            }
            status =true;

        } catch (HttpException e) {
            e.printStackTrace();
        }  catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(stream != null ) IOUtils.closeQuietly(stream);
            if(outStream!= null) IOUtils.closeQuietly(outStream);
        }
        return status;
    }

    public List<String> getStringRules(String ruleLocation) throws FileNotFoundException {

        List<String> ruleList=new ArrayList<String>();
        File folder = new File(ruleLocation);
        List<String> ruleFiles=listFilesForFolder(folder);

        File initialFile =null;
        InputStream targetStream =null;

        for (String files:ruleFiles) {
            initialFile = new File(folder + File.separator + files);
            targetStream = new FileInputStream(initialFile);
            String inputConverted = convertStreamToString(targetStream);
            ruleList.add( inputConverted );
        }

        return ruleList;
    }

    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public List<String> listFilesForFolder(File folder) {
        List<String> ruleFiles=new ArrayList<String>();

        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                ruleFiles.add(fileEntry.getName());
            }
        }

        return ruleFiles;
    }
}
