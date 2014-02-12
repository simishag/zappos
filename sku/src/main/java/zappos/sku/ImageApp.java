package zappos.sku;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Hello world!
 * 
 */
public class ImageApp {
    private static final Logger logger = Logger.getLogger(ImageApp.class);

    private static final String PRODUCT_API_URL_BASE = "http://api.zappos.com/Product";
    private static final String PRODUCT_API_KEY = "52ddafbe3ee659bad97fcce7c53592916a6bfd73";

    public static void main(String[] args) {
        // Check arguments
        if (args.length != 1) {
            System.out.println("SKU filename is required");
            System.exit(1);
        }

        // Load & process sku file
        String skuFilename = args[0];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(skuFilename);
        } catch (FileNotFoundException e) {
            System.out.println("SKU file not found: " + skuFilename);
            System.exit(2);
        }

        // fis should be valid here but better safe than sorry
        if (fis == null) {
            System.out.println("SKU file not found: " + skuFilename);
            System.exit(2);
        }

        // Read SKUs from file & store in ArrayList
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
        ArrayList<String> skuList = new ArrayList<String>();
        String skuLine;
        try {
            while ((skuLine = br.readLine()) != null) {
                // Check for empty string
                if (StringUtils.isBlank(skuLine)) {
                    continue;
                }

                // Trim white space before adding
                skuList.add(StringUtils.strip(skuLine));
            }
        } catch (IOException e) {
            System.out.println("Error reading SKU file: " + skuFilename + ": " + e.getMessage());
            System.exit(3);
        }
        logger.info("Total SKUs loaded: " + skuList.size());

        // Clean up stream & reader
        try {
            br.close();
        } catch (IOException e) {
            // we can safely ignore this error
        }
        br = null;
        fis = null;

        // Build URL
        HttpClient client = new HttpClient();
        NameValuePair queryStringParams[] = new NameValuePair[1];
        NameValuePair apiKeyNVPair = new NameValuePair();
        apiKeyNVPair.setName("key");
        apiKeyNVPair.setValue(PRODUCT_API_KEY);
        queryStringParams[0] = apiKeyNVPair;

        // Iterate over SKU list
        for (String sku : skuList) {
            String url = PRODUCT_API_URL_BASE + "/" + sku;
            HttpMethod method = new GetMethod(url);
            method.setQueryString(queryStringParams);

            try {
                int status = client.executeMethod(method);
                if (status != HttpStatus.SC_OK) {
                    logger.error("Error retrieving SKU: " + sku + ": HTTP status: " + status);
                } else {
                    byte[] responseBody = method.getResponseBody();
                    processResponseBody(sku, responseBody);
                }
            } catch (IOException e) {
                logger.error("Error retrieving SKU: " + sku + ": " + e.getMessage());
            } finally {
                method.releaseConnection();
            }
        }
    }

    protected static void processResponseBody(String sku, byte[] responseBody) {
        // Define this once here, we will reuse it
        Object o;

        // Parse JSON response & create object
        o = JSONValue.parse(new String(responseBody));
        JSONObject json = (JSONObject) o;

        // Build list of images
        o = json.get("product");
        JSONArray product = (JSONArray) o;

        ArrayList<String> imageUrls = new ArrayList<String>();
        for (int i = 0; i < product.size(); i++) {
            o = product.get(i);
            JSONObject entry = (JSONObject) o;
            String defaultImageUrl = (String) entry.get("defaultImageUrl");

            // Check for empty string
            if (!StringUtils.isBlank(defaultImageUrl)) {
                imageUrls.add(defaultImageUrl);
            }
            
            logger.debug("sku/defaultImageUrl: " + sku + "/" + defaultImageUrl);
        }

        // Retrieve & Save images
        for (String url : imageUrls) {
            try {
                retrieveAndSaveImage(sku, url);
            } catch (Exception e) {
                logger.warn("Could not save image for SKU: " + sku + ": " + e.getMessage());
            }
        }
    }

    protected static void retrieveAndSaveImage(String sku, String url) {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        int status;
        byte[] imageData = null;
        try {
            status = client.executeMethod(method);
            if (status != HttpStatus.SC_OK) {
                logger.error("Error retrieving image for SKU at URL: " + sku + ": " + url + ": HTTP status: " + status);
                return;
            }
            imageData = method.getResponseBody();
            logger.debug("imageData: " + imageData.length);
        } catch (IOException e) {
            logger.error("Error retrieving image for SKU at URL: " + sku + ": " + url + ": " + e.getMessage());
            return;
        }

        // Create images directory if necessary
        File imagesDir = new File("images");
        if (!imagesDir.exists()) {
            logger.info("Creating images directory");
            boolean result = imagesDir.mkdir();
            if (!result) {
                logger.error("Failed to create images directory");
            }
        }

        // Save to disk
        String baseName = FilenameUtils.getBaseName(url);
        String extension = FilenameUtils.getExtension(url);
        String fileName = "images" + File.separator + baseName + "." + extension;
        FileOutputStream output;
        try {
            output = new FileOutputStream(new File(fileName));
            IOUtils.write(imageData, output);
        } catch (FileNotFoundException e) {
            logger.error("Failed to save image: " + fileName);
        } catch (IOException e) {
            logger.error("Failed to save image: " + fileName);
        }
    }
}
