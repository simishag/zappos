package zappos.sku;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Hello world!
 * 
 */
public class ImageApp {
    private static final Logger logger = Logger.getLogger(ImageApp.class);

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

        // Get image URL from JSON response

        // Retrieve image

        // Save image
    }
}
